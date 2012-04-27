//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.external.promela
//# CLASS:   PromelaUnmarshaller
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.external.promela;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.waters.external.promela.ast.PromelaTree;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.marshaller.CopyingProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import org.anarres.cpp.LexerException;
import org.antlr.runtime.RecognitionException;


/**
 * A tool to import Promela files into the IDE.
 *
 * @author Zufeng Yu
 */

public class PromelaUnmarshaller
  implements CopyingProxyUnmarshaller<ModuleProxy>
{

  //#########################################################################
  //# Constructors
  public PromelaUnmarshaller(final ModuleProxyFactory factory)
  {
    this(factory, null);
  }

  public PromelaUnmarshaller(final ModuleProxyFactory factory,
                         final DocumentManager docman)
  {
    this(null, factory, docman);
  }

  public PromelaUnmarshaller(final File outputdir,
                         final ModuleProxyFactory factory,
                         final DocumentManager docman)
  {
    mOutputDir = outputdir;
    mFactory = factory;
    mDocumentManager = docman;
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.model.marshaller.CopyingProxyUnmarshaller
  public File getOutputDirectory()
  {
    return mOutputDir;
  }

  public void setOutputDirectory(final File outputdir)
  {
    mOutputDir = outputdir;
  }

  public ModuleProxy unmarshalCopying(final URI uri)
    throws IOException, WatersMarshalException, WatersUnmarshalException
  {
    try {
      return importPromelaFile(uri);
    } catch (final URISyntaxException exception) {
      throw new WatersUnmarshalException(exception);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.marshaller.ProxyUnmarshaller
  public ModuleProxy unmarshal(final URI uri)
    throws IOException, WatersUnmarshalException
  {
    try {
      return unmarshalCopying(uri);
    } catch (final WatersMarshalException exception) {
      throw new WatersUnmarshalException(exception);
    }
  }

  public Class<ModuleProxy> getDocumentClass()
  {
    return ModuleProxy.class;
  }

  public String getDefaultExtension()
  {
    return PROMELA_EXTENSION;
  }

  public Collection<String> getSupportedExtensions()
  {
    return EXTENSIONS;
  }

  public Collection<FileFilter> getSupportedFileFilters()
  {
    return FILTERS;
  }

  public DocumentManager getDocumentManager()
  {
    return mDocumentManager;
  }

  public void setDocumentManager(final DocumentManager manager)
  {
    mDocumentManager = manager;
  }


  //#########################################################################
  //# Invocation
  private ModuleProxy importPromelaFile(final URI uri)
    throws IOException, URISyntaxException,
           WatersMarshalException, WatersUnmarshalException
  {
    //Create the symbol table for use by the event visitor and graph visitor
    mSymbolTable = new SymbolTable();
    mEventVisitor = new EventCollectingVisitor(mFactory, mSymbolTable);
    mGraphVisitor= new GraphCollectingVisitor(mEventVisitor, mSymbolTable);

    final URL url = uri.toURL();
    final InputStream stream = url.openStream();
    //ModuleTreeNode ast = null;
    PromelaTree ast = null;
    try {

      final PromelaTools tool = new PromelaTools();
      ast = tool.parseStream(stream);
    } catch (final LexerException exception) {
      throw new WatersUnmarshalException(exception);
    } catch (final RecognitionException exception) {
      throw new WatersUnmarshalException(exception);
    } finally {
      stream.close();
    }

    //mVisitor.visitModule(ast);
    mEventVisitor.collectEvents(ast);
    mEventVisitor.makeMsg();
    mGraphVisitor.collectGraphs(ast);
   // mGraphVisitor.createChannelGraph();
   // mVisitor.output();


    final ModuleProxy module = constructModule(uri);
    return module;
  }


  //#########################################################################
  //# Module Construction
  private ModuleProxy constructModule(final URI uri)
  {
    String source = null;
    try {
      final File file = new File(uri);
      source = file.getName();
    } catch (final IllegalArgumentException exception) {
      source = uri.toString();
    }
    final String name = getModuleName(source);
    final String comment = "Imported from Promela file " + source + ".";

    // Create automata ...
    final Collection<EventDeclProxy> events = mGraphVisitor.getEvent();
    final Collection<Proxy> components = mGraphVisitor.getComponents();
    final ModuleProxy module =mFactory.createModuleProxy(name, comment, null, null, events, null, components);
    //final ModuleProxy module =mFactory.createModuleProxy(name, comment, null, null, events, null, null);
    return module;
  }

  private String getModuleName(final String filename)
  {
    int end = filename.lastIndexOf('.');
    if (end < 0) {
      end = filename.length();
    }
    int start = filename.lastIndexOf('/', end);
    if (start < 0) {
      start = 0;
    }
    return filename.substring(start, end);
  }


  //#########################################################################
  //# Data Members
  /**
   * The factory used to build up the modules for the <CODE>.wmod</CODE> files
   * we are converting into.
   */
  private final ModuleProxyFactory mFactory;

  private File mOutputDir;

  private DocumentManager mDocumentManager;
  private EventCollectingVisitor mEventVisitor;
  private GraphCollectingVisitor mGraphVisitor;
  private SymbolTable mSymbolTable;

  //#########################################################################
  //# Class Constants
  private static final String PROMELA_EXTENSION = ".pml";
  private static final Collection<String> EXTENSIONS =
      Collections.singletonList(PROMELA_EXTENSION);
  private static final FileFilter PROMELA_FILTER =
      new StandardExtensionFileFilter("Promela files [*" +
      PROMELA_EXTENSION + "]",
                                      PROMELA_EXTENSION);
  private static final Collection<FileFilter> FILTERS =
      Collections.singletonList(PROMELA_FILTER);

}
