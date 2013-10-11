//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   GeometryChecker
//###########################################################################
//# $Id$
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
