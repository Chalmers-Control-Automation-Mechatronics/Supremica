//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.gui.renderer;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


/**
 * A utility class to check whether a {@link GraphProxy} has geometry
 * information.
 *
 * @author Robi Malik
 */

public class GeometryChecker extends DefaultModuleProxyVisitor
{

  //#######################################################################
  //# Singleton Pattern
  private static class SingletonHolder
  {
    private static GeometryChecker INSTANCE = new GeometryChecker();
  }


  //#######################################################################
  //# Invocation
  public static boolean hasGeometry(final GraphProxy graph)
  {
    try {
      return SingletonHolder.INSTANCE.visitGraphProxy(graph);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  @Override
  public Boolean visitGraphProxy(final GraphProxy graph)
    throws VisitorException
  {
    final LabelBlockProxy block = graph.getBlockedEvents();
    if (block != null && !visitLabelBlockProxy(block)) {
      return false;
    }
    for (final NodeProxy node : graph.getNodes()) {
      final boolean geo = (Boolean) node.acceptVisitor(this);
      if (!geo) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Boolean visitGroupNodeProxy(final GroupNodeProxy node)
  {
    return node.getGeometry() != null;
  }

  @Override
  public Boolean visitLabelBlockProxy(final LabelBlockProxy block)
  {
    return block.getGeometry() != null;
  }

  @Override
  public Boolean visitSimpleNodeProxy(final SimpleNodeProxy node)
  {
    return node.getPointGeometry() != null;
  }

}
