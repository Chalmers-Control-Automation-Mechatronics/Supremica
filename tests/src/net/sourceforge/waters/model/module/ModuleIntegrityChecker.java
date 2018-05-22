//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.model.module;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.marshaller.DocumentIntegrityChecker;


public class ModuleIntegrityChecker
  extends DocumentIntegrityChecker<ModuleProxy>
{

  //#########################################################################
  //# Singleton Pattern
  public static ModuleIntegrityChecker getModuleIntegrityCheckerInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final ModuleIntegrityChecker INSTANCE =
      new ModuleIntegrityChecker();
  }


  //#########################################################################
  //# Constructor
  protected ModuleIntegrityChecker()
  {
  }


  //#########################################################################
  //# Invocation
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
      } else if (proxy instanceof ForeachProxy) {
        final ForeachProxy foreach = (ForeachProxy) proxy;
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
      Assert.assertNotNull("Null source node found in graph '" +
                           comp.getName() + "'!", source);
      Assert.assertTrue("Bad source node '" + source.getName() +
                        "' in graph '" + comp.getName() + "'!",
                        collected.contains(source));
      final NodeProxy target = edge.getTarget();
      Assert.assertNotNull("Null target node found in graph '" +
                           comp.getName() + "'!", target);
      Assert.assertTrue("Bad target node '" + target.getName() +
                        "' in graph '" + comp.getName() + "'!",
                        collected.contains(target));
    }
  }

}
