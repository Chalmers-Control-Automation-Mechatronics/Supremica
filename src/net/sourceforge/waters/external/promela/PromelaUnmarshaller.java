//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.external.promela
//# CLASS:   TTCTImporter
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.filechooser.FileFilter;


import net.sourceforge.waters.external.promela.ast.*;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.marshaller.CopyingProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.xsd.base.EventKind;

import org.anarres.cpp.LexerException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;


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
    final URL url = uri.toURL();
    final InputStream stream = url.openStream();
    CommonTree ast = null;
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
    collectMsg(ast);

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
    // Create event declarations ...
    final List<EventDeclProxy> events = new ArrayList<EventDeclProxy>();
    for (final Map.Entry<String,ChanInfo> entry : chan.entrySet()) {
      final String key = entry.getKey();
      //final ChanInfo info = entry.getValue();
      final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy(key);
      final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE);
      events.add(event);
    }
    // Create automata ...
    final List<Proxy> components = new ArrayList<Proxy>();


    final ModuleProxy module =
      mFactory.createModuleProxy(name, comment, null,
                                 null, events, null, null);
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

  private void collectMsg(final CommonTree t){
    if(t!=null){

        if(t.getText().equals("chan")){
            final CommonTree tr1 = (CommonTree)t.getChild(1);
            final String name = t.getChild(0).getText();
            final int length = Integer.parseInt(tr1.getChild(0).getText());
            final int datalength = tr1.getChildCount()-2;
            chan.put(name,new ChanInfo(name, length, datalength));


        }

        if(t instanceof ProctypeTreeNode){
            for(int b =0;b<t.getChildCount();b++){

                if(t.getChild(b).toString().equals("STATEMENT")){
                final CommonTree tr = (CommonTree)t.getChild(b);

                for(int a=0;a<tr.getChildCount();a++){
                  final CommonTree childA = (CommonTree) tr.getChild(a);
                 // if(childA.toString().equals("Exchange")){
                  if(childA instanceof ExchangeTreeNode){
                      if(childA.getText().equals("!")|| tr.getChild(a).getText().equals("!!")){


                      final ArrayList<String> data =new ArrayList<String>();
                      final StringBuilder sb = new StringBuilder();
                      sb.append(childA.getChild(0).getText());
                     // sb.append(childA.getText()+"[");
                      chan.get(childA.getChild(0).getText()).incSendnumber();
                      final CommonTree msgargs = (CommonTree) childA.getChild(1);

                      for(int y = 0; y <msgargs.getChildCount();y++){
                        final CommonTree childY = (CommonTree) msgargs.getChild(y);
                        if(childY instanceof ConstantTreeNode){
                          sb.append("[");
                          sb.append(childY.getText());
                          sb.append("]");
                          data.add(childY.getText());
                        }
                      }
                      //testing event output
             //         System.out.println(sb.toString());

                      //store proctype name and relevant data into hashtable
                      chan.get(childA.getChild(0).getText()).storeMsg(data);

                      }
                      if(childA.getText().equals("?")|| tr.getChild(a).getText().equals("??")){
                        chan.get(childA.getChild(0).getText()).incRecnumber();
                      }
                  }
                }
                }
            }

        }
       // if(t instanceof InitTreeNode){

        //}
        for(int i = 0; i < t.getChildCount();i++){

            collectMsg((CommonTree)t.getChild(i));

        }
    }
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
  private final Hashtable<String, ChanInfo> chan = new Hashtable<String,ChanInfo>();

  //#########################################################################
  //# Class Constants
  private static final String PROMELA_EXTENSION = ".pml";
  private static final Collection<String> EXTENSIONS =
      Collections.singletonList(PROMELA_EXTENSION);
  private static final FileFilter PROMELA_FILTER =
      new StandardExtensionFileFilter(PROMELA_EXTENSION,
                                      "Promela files [*" +
                                      PROMELA_EXTENSION + "]");
  private static final Collection<FileFilter> FILTERS =
      Collections.singletonList(PROMELA_FILTER);

}
