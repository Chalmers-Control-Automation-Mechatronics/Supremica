//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   EmptyLabelBlockException
//###########################################################################
//# $Id$
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
    mComponentName = null;
  }

  /**
   * Constructs a new exception indicating the the specified edge has no
   * labels.
   */
  public EmptyLabelBlockException(final EdgeProxy edge, final String compname)
  {
    super(edge);
    mComponentName = compname;
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
        "No event labels on transition from " + source.getName() + " to " +
        target.getName() + " in " + mComponentName + ".";
    }
  }


  //#########################################################################
  //# Static Class Variables
  private final String mComponentName;
  
  
  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
