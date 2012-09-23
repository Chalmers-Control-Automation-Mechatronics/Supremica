//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   AbstractCommandTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.command;

import gnu.trove.THashSet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;

import net.sourceforge.waters.gui.GraphEditorPanel;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.DescendingModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.printer.ModuleProxyPrinter;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.SubjectTools;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.EventAliasSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;

import org.supremica.gui.SupremicaLoggerFactory;
import org.supremica.gui.ide.ComponentEditorPanel;
import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.DocumentContainerManager;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.log.Logger;


public abstract class AbstractCommandTest extends AbstractWatersTest
{

  //#########################################################################
  //# Constructors
  public AbstractCommandTest()
  {
  }

  public AbstractCommandTest(final String name)
  {
    super(name);
  }


  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final Logger logger = SupremicaLoggerFactory.createLogger(IDE.class);
    IDE.setLogger(logger);
    mIDE = new IDE();
    mSubjectFactory = ModuleSubjectFactory.getInstance();
    mPlainCloner = ModuleElementFactory.getCloningInstance();
    mSubjectCloner = ModuleSubjectFactory.getCloningInstance();
    mEqualityChecker = new ModuleEqualityVisitor(true, true);
    mCollector = new ProxyCollector();
  }

  @Override
  protected void tearDown() throws Exception
  {
    mIDE = null;
    mSubjectFactory = null;
    mPlainCloner = null;
    mSubjectCloner = null;
    mEqualityChecker = null;
    mCollector = null;
    super.tearDown();
  }


  //#########################################################################
  //# Simple Access
  protected IDE getIDE()
  {
    return mIDE;
  }

  protected ModuleSubjectFactory getSubjectFactory()
  {
    return mSubjectFactory;
  }

  protected ModuleProxyCloner getSubjectCloner()
  {
    return mSubjectCloner;
  }


  //#########################################################################
  //# Test Methods
  protected void executeCommand(final Command cmd,
                                final ModuleContainer container,
                                final ProxySubject subject,
                                final Proxy proxyAfter)
  {
    final Proxy proxyBefore = mPlainCloner.getClone(subject);
    final Set<Proxy> nodesBefore = mCollector.collect(subject);
    container.executeCommand(cmd);
    assertProxyEquals(mEqualityChecker,
                      "Unexpected result after command execution!",
                      subject, proxyAfter);
    final Set<Proxy> nodesAfter = mCollector.collect(subject);
    container.undo();
    assertProxyEquals(mEqualityChecker,
                      "Unexpected result after command undo!",
                      subject, proxyBefore);
    assertNodesEqual(subject, nodesBefore, "Unexpected nodes after undo!");
    container.redo();
    assertProxyEquals(mEqualityChecker,
                      "Unexpected result after command redo!",
                      subject, proxyAfter);
    assertNodesEqual(subject, nodesAfter, "Unexpected nodes after redo!");
  }


  //#########################################################################
  //# Support Methods
  protected ModuleContainer loadModule(final String dir, final String name)
    throws WatersUnmarshalException, IOException
  {
    final File root = getWatersInputRoot();
    final File dir1 = new File(root, dir);
    return loadModule(dir1, name);
  }

  protected ModuleContainer loadModule(final String dir,
                                       final String subdir,
                                       final String name)
    throws WatersUnmarshalException, IOException
  {
    final File root = getWatersInputRoot();
    final File dir1 = new File(root, dir);
    final File dir2 = new File(dir1, subdir);
    return loadModule(dir2, name);
  }

  protected ModuleContainer loadModule(final File dir, final String name)
    throws WatersUnmarshalException, IOException
  {
    final DocumentManager manager = mIDE.getDocumentManager();
    final ProxyMarshaller<? extends ModuleProxy> marshaller =
      manager.findProxyMarshaller(ModuleProxy.class);
    final String ext = marshaller.getDefaultExtension();
    final File file = new File(dir, name + ext);
    return loadModule(file);
  }

  protected ModuleContainer loadModule(final File file)
    throws WatersUnmarshalException, IOException
  {
    final DocumentContainerManager cManager =
      mIDE.getDocumentContainerManager();
    final DocumentManager dManager = cManager.getDocumentManager();
    final DocumentProxy doc = dManager.load(file);
    final DocumentContainer container = cManager.openContainer(doc);
    return (ModuleContainer) container;
  }

  protected GraphEditorPanel openGraph(final ModuleContainer container,
                                       final String name)
    throws GeometryAbsentException
  {
    final SimpleComponentSubject comp = findSimpleComponent(container, name);
    final ComponentEditorPanel panel = container.showEditor(comp);
    return panel.getGraphEditorPanel();
  }


  protected EdgeSubject findEdge(final GraphEditorPanel panel,
                                 final String sourceName,
                                 final String targetName)
  {
    final GraphSubject graph = panel.getGraph();
    for (final EdgeProxy edge : graph.getEdges()) {
      final NodeProxy source = edge.getSource();
      final NodeProxy target = edge.getTarget();
      if (source.getName().equals(sourceName) &&
          target.getName().equals(targetName)) {
        return (EdgeSubject) edge;
      }
    }
    final SimpleComponentSubject comp =
      (SimpleComponentSubject) graph.getParent();
    fail("Graph '" + comp.getName() +
         "' does not contain an edge from '" + sourceName +
         "' to '" + targetName + "'!");
    return null;
  }

  protected EventAliasSubject findEventAlias
    (final ModuleContainer container, final String name)
  {
    final ModuleSubject module = container.getModule();
    for (final Proxy proxy : module.getEventAliasList()) {
      if (proxy instanceof EventAliasSubject) {
        final EventAliasSubject alias = (EventAliasSubject) proxy;
        if (alias.getName().equals(name)) {
          return alias;
        }
      }
    }
    fail("Module '" + module.getName() +
         "' does not contain an event alias called '" + name + "'!");
    return null;
  }

  protected SimpleComponentSubject findSimpleComponent
    (final ModuleContainer container, final String name)
  {
    final ModuleSubject module = container.getModule();
    for (final Proxy proxy : module.getComponentList()) {
      if (proxy instanceof SimpleComponentSubject) {
        final SimpleComponentSubject comp = (SimpleComponentSubject) proxy;
        if (comp.getName().equals(name)) {
          return comp;
        }
      }
    }
    fail("Module '" + module.getName() +
         "' does not contain a component called '" + name + "'!");
    return null;
  }

  protected NodeSubject findNode(final GraphEditorPanel panel,
                                 final String name)
  {
    final GraphSubject graph = panel.getGraph();
    for (final NodeSubject node : graph.getNodesModifiable()) {
      if (node.getName().equals(name)) {
        return node;
      }
    }
    final SimpleComponentSubject comp =
      (SimpleComponentSubject) graph.getParent();
    fail("Graph '" + comp.getName() +
         "' does not contain a node called '" + name + "'!");
    return null;
  }

  protected SimpleNodeSubject findSimpleNode(final GraphEditorPanel panel,
                                             final String name)
  {
    final NodeSubject node = findNode(panel, name);
    if (node instanceof SimpleNodeSubject) {
      return (SimpleNodeSubject) node;
    } else {
      final GraphSubject graph = panel.getGraph();
      final SimpleComponentSubject comp =
        (SimpleComponentSubject) graph.getParent();
      fail("Node '" + name + "' in graph '" + comp.getName() +
         "' is not a simple node!");
      return null;
    }
  }

  protected GroupNodeSubject findGroupNode(final GraphEditorPanel panel,
                                           final String name)
  {
    final NodeSubject node = findNode(panel, name);
    if (node instanceof GroupNodeSubject) {
      return (GroupNodeSubject) node;
    } else {
      final GraphSubject graph = panel.getGraph();
      final SimpleComponentSubject comp =
        (SimpleComponentSubject) graph.getParent();
      fail("Node '" + name + "' in graph '" + comp.getName() +
         "' is not a group node!");
      return null;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void assertNodesEqual(final ProxySubject root,
                                final Set<Proxy> expected,
                                final String msg)
  {
    final Set<Proxy> nodes = mCollector.collect(root);
    if (!nodes.equals(expected)) {
      try {
        final StringWriter sWriter = new StringWriter();
        final PrintWriter pWriter = new PrintWriter(sWriter);
        final ModuleProxyPrinter printer = new ModuleProxyPrinter(pWriter);
        for (final Proxy node : nodes) {
          if (!expected.contains(node)) {
            final ProxySubject subject = (ProxySubject) node;
            final ProxySubject parent = SubjectTools.getProxyParent(subject);
            if (parent ==  null || expected.contains(parent)) {
              printer.pprint("\nUnexpected: ");
              printer.pprint(node);
            }
          }
        }
        for (final Proxy node : expected) {
          if (!nodes.contains(node)) {
            final ProxySubject subject = (ProxySubject) node;
            final ProxySubject parent = SubjectTools.getProxyParent(subject);
            if (parent ==  null || nodes.contains(parent)) {
              printer.pprint("\nMissing: ");
              printer.pprint(node);
            }
          }
        }
        final String diagnostics = sWriter.toString();
        getLogger().error(msg);
        getLogger().info(diagnostics);
        final String fullmsg;
        if (System.getProperty("waters.test.ant") == null) {
          fullmsg = msg + diagnostics;
        } else {
          // Funny thing, when run from ANT, the text after the first newline
          // in the argument passed to fail() gets printed on the console.
          // So we suppress the output and refer the programmer to the log file.
          fullmsg = msg + " (See " + getLogFile() + " for details.)";
        }
        fail(fullmsg);
      } catch (final IOException exception) {
        throw new WatersRuntimeException(exception);
      }
    }
  }


  //#########################################################################
  //# Inner Class ProxyCollector
  private static class ProxyCollector
    extends DescendingModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private Set<Proxy> collect(final Proxy root)
    {
      try {
        mCurrentSet = new THashSet<Proxy>();
        root.acceptVisitor(this);
        return mCurrentSet;
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    @Override
    public Object visitProxy(final Proxy proxy)
      throws VisitorException
    {
      mCurrentSet.add(proxy);
      return null;
    }

    //#######################################################################
    //# Data Members
    private Set<Proxy> mCurrentSet;

  }


  //#########################################################################
  //# Data Members
  private IDE mIDE;
  private ModuleSubjectFactory mSubjectFactory;
  private ModuleProxyCloner mPlainCloner;
  private ModuleProxyCloner mSubjectCloner;
  private ModuleEqualityVisitor mEqualityChecker;
  private ProxyCollector mCollector;

}
