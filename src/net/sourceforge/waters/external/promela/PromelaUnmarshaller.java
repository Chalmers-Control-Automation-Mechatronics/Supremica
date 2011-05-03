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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.filechooser.FileFilter;

import net.sourceforge.waters.external.promela.ast.ModuleTreeNode;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.marshaller.CopyingProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;

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
    final URL url = uri.toURL();
    final InputStream stream = url.openStream();
    ModuleTreeNode ast = null;
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

    mVisitor.visitModule(ast);
    mVisitor.output();


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

//Creating Default events

    //adding channel events
    for (final Map.Entry<String,ChanInfo> entry : mVisitor.getChan().entrySet()) {
      final String key = entry.getKey();
      final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy(key);

      final int size = entry.getValue().getType().size();
      final Collection<SimpleExpressionProxy> ranges = new ArrayList<SimpleExpressionProxy>(size);
      for(final String value: entry.getValue().getType()){
        if(value.equals("byte")){
          //Consider event range if it is byte
          final IntConstantProxy zero = mFactory.createIntConstantProxy(0);
          final IntConstantProxy c255 = mFactory.createIntConstantProxy(255);
          final BinaryOperator op = optable.getRangeOperator();
          final BinaryExpressionProxy range = mFactory.createBinaryExpressionProxy(op, zero, c255);
          ranges.add(range);
        }
      }
      final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE, true, ScopeKind.LOCAL, ranges, null, null);

      events.add(event);
    }

    //adding atomic event if it is atomic
    if(mVisitor.getAtomic()){
      final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("init");
      final EventDeclProxy event = mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE);
      events.add(event);
    }
//End of Creating default events


//Creating Components

    //list to store component
    final List<Proxy> components = new ArrayList<Proxy>();

    //Main loop to create Node and Edges, for GraphProxy, then create components
    for(final Map.Entry<String, ArrayList<List<String>>> entry: mVisitor.getComponent().entrySet()){
        final Collection<NodeProxy> nodes = new ArrayList<NodeProxy>();
        final Collection<EdgeProxy> edges = new ArrayList<EdgeProxy>();
        final Collection<Proxy> eventList = new ArrayList<Proxy>();


        final ArrayList<List<String>> value = entry.getValue();
        final String componentName = entry.getKey();

        //create events for each component
        for(int i=0;i<value.size();i++){
          //check if its "init", create simple event init
          if(value.get(i).size()==1&&value.get(i).get(0).equals("init")){
            final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("init");
            eventList.add(ident);
            System.out.println("initial");
          }
          //check if its receiving
          else if(value.get(i).size()==1 && value.get(i).get(0).equals("receive")){
              //!!!to do ... when its receiving
            final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("testing");
            eventList.add(ident);
            System.out.println("testing");
          }
          //if it is normal event, create it
          else{
              //create specific index event, etc name[33][124]
              System.out.println("normal");
              final String ename = value.get(i).get(0);
              final Collection<SimpleExpressionProxy> indexes = new ArrayList<SimpleExpressionProxy>(value.get(i).size()-1);
              for(int y=1;y<value.get(i).size();y++){
                final IntConstantProxy c = mFactory.createIntConstantProxy(Integer.parseInt(value.get(i).get(y)));
                indexes.add(c);
              }
              //I suspect this line is the problem, adding indexedIdentifier into event list
              final IndexedIdentifierProxy indexEvent = mFactory.createIndexedIdentifierProxy(ename,indexes);
              eventList.add(indexEvent);
          }
        }

        //create simple nodes, with only names, maybe problem is here?
        for(int i=0;i<=value.size();i++){
          final String nodeName = componentName+i;
          final NodeProxy node = mFactory.createSimpleNodeProxy(nodeName);
          nodes.add(node);
        }

        //there is not get method in Collection, so i use toArray
        final NodeProxy[] nodeinfo = nodes.toArray(new NodeProxy[0]);
        //!!!to do... how to handle multiple events for 1 edge
        final Proxy[] eventLabel = eventList.toArray(new Proxy[0]);

        //Create Edges
        for(int i=0;i<nodes.size()-1;i++){
          final NodeProxy nodeSource = nodeinfo[i];
          final NodeProxy nodeTarget = nodeinfo[i+1];
          final Collection<Proxy> labelBlock = new ArrayList<Proxy>();
          labelBlock.add( eventLabel[i]);
          final LabelBlockProxy label = mFactory.createLabelBlockProxy(labelBlock, null); //label blocks
          final EdgeProxy edge = mFactory.createEdgeProxy(nodeSource, nodeTarget, label, null, null, null, null);
          edges.add(edge);
        }

        //after created edges and nodes, we create graphProxy now
        final GraphProxy graph = mFactory.createGraphProxy(true, null, nodes, edges);

        //after created GraphProxy, we create SimpleComponent now
        final IdentifierProxy identifier =mFactory.createSimpleIdentifierProxy(componentName);
        final SimpleComponentProxy simpleComponent = mFactory.createSimpleComponentProxy(identifier, ComponentKind.PLANT, graph);

        //Creating one SimpleComponent each loop, store them
        components.add(simpleComponent);
    }

//End of Creating Components


    // Create automata ...

    final ModuleProxy module =
      mFactory.createModuleProxy(name, comment, null,
                                 null, events, null, components);
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
  private final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
  private File mOutputDir;

  private DocumentManager mDocumentManager;

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
  private final EventCollectingVisitor mVisitor = new EventCollectingVisitor();
}
