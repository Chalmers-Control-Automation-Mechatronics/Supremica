//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   EmptyLabelBlockException
//###########################################################################
//# $Id: EmptyLabelBlockException.java,v 1.2 2005-11-03 01:24:15 robi Exp $
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
  public EmptyLabelBlockException(final EdgeProxy edge)
  {
    super(edge);
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
	target.getName() + "!";
    }
  }

}