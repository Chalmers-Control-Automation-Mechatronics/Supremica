//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.samples.maze
//# CLASS:   MazeCompiler
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.samples.maze;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.filechooser.FileFilter;

import net.sourceforge.waters.model.base.IndexedSet;
import net.sourceforge.waters.model.base.IndexedTreeSet;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.marshaller.CopyingProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class MazeCompiler implements CopyingProxyUnmarshaller<ModuleProxy>
{

  //#########################################################################
  //# Constructors
  public MazeCompiler(final ModuleProxyFactory factory,
                      final DocumentManager docman)
  {
    this(null, factory, docman);
  }

  public MazeCompiler(final File outputdir,
                      final ModuleProxyFactory factory,
                      final DocumentManager docman)
  {
    mOutputDir = outputdir;
    mFactory = factory;
    mUseLanguageInclusion = true;
    mDocumentManager = docman;
    mReader = new MazeReader();
    mCopiedModules = new HashSet<String>(16);
  }


  //#########################################################################
  //# Parameters
  public void setUseLanguageInclusion(final boolean inclusion)
  {
    mUseLanguageInclusion = inclusion;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.marshaller.CopyingProxyUnmarshaller
  public File getOutputDirectory()
  {
    return mOutputDir;
  }

  public void setOutputDirectory(final File outputdir)
  {
    mOutputDir = outputdir;
    mCopiedModules.clear();
  }

  public ModuleProxy unmarshalCopying(final URI uri)
    throws IOException, WatersMarshalException, WatersUnmarshalException
  {
    final String pathname = uri.getPath();
    final int start1 = pathname.lastIndexOf(File.separatorChar);
    final int start2 = start1 < 0 ? 0 : start1 + 1;
    final int stop1 = pathname.lastIndexOf('.');
    final int stop2 = stop1 < 0 ? pathname.length() : stop1;
    final String name = pathname.substring(start2, stop2);
    final Maze maze = mReader.load(uri, name);
    maze.createActions();
    final ModuleProxy module = createModule(maze);
    final File outfile = module.getFileLocation();
    mDocumentManager.saveAs(module, outfile);
    return module;
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
    return MAZEEXT;
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
  //# Compiling Maze Files
  private ModuleProxy createModule(final Maze maze)
  throws IOException, WatersMarshalException, WatersUnmarshalException
  {
    final String name = maze.getName();
    final String comment =
      "Automatically generated from maze description '" + name + "'.";
    final ProxyMarshaller<ModuleProxy> marshaller =
      mDocumentManager.findProxyMarshaller(ModuleProxy.class);
    final String extname = name + marshaller.getDefaultExtension();
    final File outputfile = new File(mOutputDir, extname);
    final URI uri = outputfile.toURI();
    final Collection<Action> escapes = new LinkedList<Action>();
    final List<Proxy> components = new LinkedList<Proxy>();
    mEvents = new IndexedTreeSet<EventDeclProxy>();
    final Collection<Square> squares = maze.getSquares();
    for (final Square square : squares) {
      final InstanceProxy inst = createInstance(square, escapes);
      if (inst != null) {
        components.add(inst);
      }
    }
    if (!escapes.isEmpty()) {
      final SimpleComponentProxy prop = createProperty(escapes);
      components.add(prop);
    }
    return mFactory.createModuleProxy
      (name, comment, uri, null, mEvents, null, components);
  }

  private InstanceProxy createInstance(final Square square,
                                       final Collection<Action> escapes)
    throws IOException, WatersMarshalException, WatersUnmarshalException
  {
    final String templname = square.getTemplateName();
    if (templname == null) {
      return null;
    } else {
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
  }

  private SimpleComponentProxy createProperty(final Collection<Action> escapes)
  {
    final ComponentKind kind =
      mUseLanguageInclusion ? ComponentKind.PROPERTY : ComponentKind.SPEC;
    final SimpleIdentifierProxy ident =
      mFactory.createSimpleIdentifierProxy("property");
    final PlainEventListProxy props =
      mFactory.createPlainEventListProxy();
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
    final GraphProxy graph =
      mFactory.createGraphProxy(true, labelBlock, nodes, null);
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
      final SimpleIdentifierProxy ident =
        mFactory.createSimpleIdentifierProxy(name);
      final EventKind kind = EventKind.UNCONTROLLABLE;
      final EventDeclProxy decl = mFactory.createEventDeclProxy(ident, kind);
      mEvents.add(decl);
      if (action.isEscapeAction()) {
        escapes.add(action);
      }
    }
    return mFactory.createSimpleIdentifierProxy(name);
  }

  private void copyModule(final String name)
    throws WatersMarshalException, WatersUnmarshalException, IOException
  {
    if (!mCopiedModules.contains(name)) {
      final ProxyMarshaller<ModuleProxy> marshaller =
        mDocumentManager.findProxyMarshaller(ModuleProxy.class);
      final String extname = name + marshaller.getDefaultExtension();
      final URL source = getClass().getResource(extname);
      final File target = new File(mOutputDir, extname);
      final ModuleProxy module = (ModuleProxy) mDocumentManager.load(source);
      mDocumentManager.saveAs(module, target);
      mCopiedModules.add(name);
    }
  }


  //#########################################################################
  //# Data Members
  private File mOutputDir;
  private boolean mUseLanguageInclusion;

  private final ModuleProxyFactory mFactory;
  private DocumentManager mDocumentManager;
  private final MazeReader mReader;
  private final Set<String> mCopiedModules;

  private IndexedSet<EventDeclProxy> mEvents;

  private static final String MAZEEXT = ".maze";
  private static final Collection<String> EXTENSIONS =
    Collections.singletonList(MAZEEXT);
  private static final FileFilter MAZEFILTER =
    new StandardExtensionFileFilter
      (MAZEEXT, "Maze description files [*" + MAZEEXT + "]");
  private static final Collection<FileFilter> FILTERS =
    Collections.singletonList(MAZEFILTER);

}
