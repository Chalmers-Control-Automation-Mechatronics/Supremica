//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   ModuleIntegrityChecker
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.model.module;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.marshaller.DocumentIntegrityChecker;


public class ModuleIntegrityChecker
  extends DocumentIntegrityChecker
{

  //#########################################################################
  //# Constructors
  public static ModuleIntegrityChecker getInstance()
  {
    if (theInstance == null) {
      theInstance = new ModuleIntegrityChecker();
    }
    return theInstance;
  }

  protected ModuleIntegrityChecker()
  {
  }


  //#########################################################################
  //# Invocation
  public void check(final DocumentProxy doc)
    throws Exception
  {
    final ModuleProxy module = (ModuleProxy) doc;
    check(module);
  }

  public void check(final ModuleProxy module)
    throws Exception
  {
    super.check(module);
    checkModuleIntegrity(module);
  }


  //#########################################################################
  //# Integrity Checking
  private void checkModuleIntegrity(final ModuleProxy module)
  {
    final List<Proxy> components = module.getComponentList();
    checkComponentListIntegrity(components);
  }

  private void checkComponentListIntegrity(final List<Proxy> list)
  {
    for (final Proxy proxy : list) {
      if (proxy instanceof SimpleComponentProxy) {
        final SimpleComponentProxy comp = (SimpleComponentProxy) proxy;
        checkGraphIntegrity(comp);
      } else if (proxy instanceof VariableComponentProxy) {
        // O.K.
      } else if (proxy instanceof InstanceProxy) {
        // O.K.
      } else if (proxy instanceof ForeachComponentProxy) {
        final ForeachComponentProxy foreach = (ForeachComponentProxy) proxy;
        final List<Proxy> body = foreach.getBody();
        checkComponentListIntegrity(body);
      } else {
        Assert.fail("Bad component type " + proxy.getClass().getName());
      } 
    }
  }

  private void checkGraphIntegrity(final SimpleComponentProxy comp)
  {
    final GraphProxy graph = comp.getGraph();
    final Set<NodeProxy> nodes = graph.getNodes();
    final int numnodes = nodes.size();
    final Set<NodeProxy> collected = new HashSet<NodeProxy>(numnodes);
    for (final NodeProxy node : nodes) {
      if (node instanceof SimpleNodeProxy) {
        collected.add(node);
      } else if (node instanceof GroupNodeProxy) {
        final Set<NodeProxy> children = node.getImmediateChildNodes();
        for (final NodeProxy child : children) {
          Assert.assertTrue("Bad child node '" + child.getName() +
                            "' found in group node '" + node.getName() +
                            "' of graph '" + comp.getName() + "'!",
                            collected.contains(child));
        }
        collected.add(node);
      } else {
        Assert.fail("Bad node type " + node.getClass().getName() +
                    " in graph '" + comp.getName() + "'!");
      }
    }
    final Collection<EdgeProxy> edges = graph.getEdges();
    for (final EdgeProxy edge : edges) {
      final NodeProxy source = edge.getSource();
      Assert.assertTrue("Bad source node '" + source.getName() +
                        "' in graph '" + comp.getName() + "'!",
                        collected.contains(source));
      final NodeProxy target = edge.getTarget();
      Assert.assertTrue("Bad target node '" + target.getName() +
                        "' in graph '" + comp.getName() + "'!",
                        collected.contains(target));
    }
  }


  //#########################################################################
  //# Class Variables
  private static ModuleIntegrityChecker theInstance = null;

}
