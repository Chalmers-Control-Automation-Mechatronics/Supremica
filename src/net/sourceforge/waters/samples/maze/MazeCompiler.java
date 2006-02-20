//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.samples.maze
//# CLASS:   MazeCompiler
//###########################################################################
//# $Id: MazeCompiler.java,v 1.5 2006-02-20 22:20:22 robi Exp $
//###########################################################################

package net.sourceforge.waters.samples.maze;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.IndexedSet;
import net.sourceforge.waters.model.base.IndexedTreeSet;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.AliasProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.ParameterProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class MazeCompiler implements ProxyUnmarshaller<ModuleProxy>
{

  //#########################################################################
  //# Constructors
  public MazeCompiler(final File inputdir,
                      final File outputdir,
                      final ModuleProxyFactory factory)
    throws JAXBException
  {
    this(inputdir, outputdir, factory,
         new JAXBModuleMarshaller(factory, null));
  }

  public MazeCompiler(final File inputdir,
                      final File outputdir,
                      final ModuleProxyFactory factory,
                      final JAXBModuleMarshaller marshaller)
    throws JAXBException
  {
    mInputDir = inputdir;
    mOutputDir = outputdir;
    mFactory = factory;
    mJAXBMarshaller = marshaller;
    mReader = new MazeReader();
    mCopiedModules = new HashSet<String>(16);
  }


  //#########################################################################
  //# Parameters
  public void setInputDir(final File inputdir)
  {
    mInputDir = inputdir;
    mCopiedModules.clear();
  }

  public void setOutputDir(final File outputdir)
  {
    mOutputDir = outputdir;
    mCopiedModules.clear();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.ProxyUnmarshaller
  public ModuleProxy unmarshal(final URI uri)
    throws WatersUnmarshalException, IOException
  {
    final String pathname = uri.getPath();
    final int start1 = pathname.lastIndexOf(File.separatorChar);
    final int start2 = start1 < 0 ? 0 : start1 + 1;
    final int stop1 = pathname.lastIndexOf('.');
    final int stop2 = stop1 < 0 ? pathname.length() : stop1;
    final String name = pathname.substring(start2, stop2);
    final Maze maze = mReader.load(uri, name);
    maze.createActions();
    return createModule(maze);
  }

  public Class<ModuleProxy> getDocumentClass()
  {
    return ModuleProxy.class;
  }

  public String getDefaultExtension()
  {
    return MAZEEXT;
  }

  public Collection<String> getSupportedExtensions()
  {
    return EXTENSIONS;
  }


  //#########################################################################
  //# Compiling Maze Files
  private ModuleProxy createModule(final Maze maze)
    throws WatersUnmarshalException, IOException
  {
    final String name = maze.getName();
    final String extname = name + mJAXBMarshaller.getDefaultExtension();
    final File outputfile = new File(mOutputDir, extname);
    final URI uri = outputfile.toURI();
    final Collection<Action> escapes = new LinkedList<Action>();
    final List<Proxy> components = new LinkedList<Proxy>();
    mEvents = new IndexedTreeSet<EventDeclProxy>();
    final Collection<Square> squares = maze.getSquares();
    for (final Square square : squares) {
      final InstanceProxy inst = createInstance(square, escapes);
      components.add(inst);
    }
    if (!escapes.isEmpty()) {
      final SimpleComponentProxy prop = createProperty(escapes);
      components.add(prop);
    }
    final List<ParameterProxy> parameters = Collections.emptyList();
    final List<AliasProxy> constants = Collections.emptyList();
    final List<Proxy> aliases = Collections.emptyList();
    return mFactory.createModuleProxy
      (name, uri, parameters, constants, mEvents, aliases, components);
  }

  private InstanceProxy createInstance(final Square square,
                                       final Collection<Action> escapes)
    throws WatersUnmarshalException, IOException
  {
    final String templname = square.getTemplateName();
    copyModule(templname);
    final String name = square.getName();
    final SimpleIdentifierProxy ident =
      mFactory.createSimpleIdentifierProxy(name);
    final List<ParameterBindingProxy> bindings =
      new LinkedList<ParameterBindingProxy>();
    for (final int kind : square.getActionKinds()) {
      final String param = Action.getTemplateName(kind);
      final Collection<Action> actions = square.getActions(kind);
      final ExpressionProxy value = createEventList(actions, escapes);
      final ParameterBindingProxy binding =
        mFactory.createParameterBindingProxy(param, value);
      bindings.add(binding);
    }
    return mFactory.createInstanceProxy(ident, templname, bindings);
  }

  private SimpleComponentProxy createProperty(final Collection<Action> escapes)
  {
    final ComponentKind kind = ComponentKind.PROPERTY;
    final SimpleIdentifierProxy ident =
      mFactory.createSimpleIdentifierProxy("property");
    final List<Proxy> empty = Collections.emptyList();
    final EventListExpressionProxy props =
      mFactory.createPlainEventListProxy(empty);  
    final SimpleNodeProxy node =
      mFactory.createSimpleNodeProxy("q0", props, true, null, null, null);
    final List<SimpleNodeProxy> nodes = Collections.singletonList(node);
    final List<Proxy> blocked = new LinkedList<Proxy>();
    for (final Action action : escapes) {
      final ExpressionProxy event = createEvent(action, escapes);
      blocked.add(event);
    }
    final LabelBlockProxy labelBlock =
      mFactory.createLabelBlockProxy(blocked, null);
    final List<EdgeProxy> edges = Collections.emptyList();
    final GraphProxy graph =
      mFactory.createGraphProxy(true, labelBlock, nodes, edges);
    return mFactory.createSimpleComponentProxy(ident, kind, graph);
  }

  private ExpressionProxy createEventList(final Collection<Action> actions,
                                          final Collection<Action> escapes)
  {
    if (actions.size() == 1) {
      final Iterator<Action> iter = actions.iterator();
      final Action action = iter.next();
      return createEvent(action, escapes);
    } else {
      final List<Proxy> eventlist = new LinkedList<Proxy>();
      for (final Action action : actions) {
        final ExpressionProxy event = createEvent(action, escapes);
        eventlist.add(event);
      }
      return mFactory.createPlainEventListProxy(eventlist);
    }
  }

  private SimpleIdentifierProxy createEvent(final Action action,
                                            final Collection<Action> escapes)
  {
    final String name = action.getName();
    if (!mEvents.containsName(name)) {
      final EventKind kind = EventKind.UNCONTROLLABLE;
      final List<SimpleExpressionProxy> empty = Collections.emptyList();
      final EventDeclProxy decl =
        mFactory.createEventDeclProxy(name, kind, true, empty, null);
      mEvents.add(decl);
      if (action.isEscapeAction()) {
        escapes.add(action);
      }
    }
    return mFactory.createSimpleIdentifierProxy(name);
  }

  private void copyModule(final String name)
    throws WatersUnmarshalException, IOException
  {
    if (!mCopiedModules.contains(name)) {
      final String extname = name + mJAXBMarshaller.getDefaultExtension();
      final File source = new File(mInputDir, extname);
      final File target = new File(mOutputDir, extname);
      try {
        mJAXBMarshaller.copyXMLFile(source, target);
      } catch (final JAXBException exception) {
        throw new WatersUnmarshalException(exception);
      }
      mCopiedModules.add(name);
    }
  }


  //#########################################################################
  //# Data Members
  private File mInputDir;
  private File mOutputDir;

  private final ModuleProxyFactory mFactory;
  private final JAXBModuleMarshaller mJAXBMarshaller;
  private final MazeReader mReader;
  private final Set<String> mCopiedModules;

  private IndexedSet<EventDeclProxy> mEvents;

  private static final String MAZEEXT = ".txt";
  private static final Collection<String> EXTENSIONS =
    Collections.singletonList(MAZEEXT);

}
