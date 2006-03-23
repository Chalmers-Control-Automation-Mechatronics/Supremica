//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   EmptyLabelBlockException
//###########################################################################
//# $Id: EmptyLabelBlockException.java,v 1.3 2006-03-23 16:06:03 flordal Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.NodeProxy;


public class EmptyLabelBlockException extends EvalException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public EmptyLabelBlockException()
  {
  }

  /**
   * Constructs a new exception indicating the the specified edge has no
   * labels.
   */
  public EmptyLabelBlockException(final EdgeProxy edge, String componentName)
  {
    super(edge);
    this.componentName = componentName;
  }


  //#########################################################################
  //# Message
  public String getMessage()
  {
    final EdgeProxy edge = (EdgeProxy) getLocation();
    if (edge == null) {
      return null;
    } else {
      final NodeProxy source = edge.getSource();
      final NodeProxy target = edge.getTarget();
      return
        "No labels on transition from " + source.getName() + " to " +
        target.getName() + " in " + componentName + ".";
    }
  }

  private String componentName = null;
}
