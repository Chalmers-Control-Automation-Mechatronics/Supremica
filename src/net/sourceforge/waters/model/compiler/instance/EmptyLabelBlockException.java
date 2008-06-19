//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.instance
//# CLASS:   EmptyLabelBlockException
//###########################################################################
//# $Id: EmptyLabelBlockException.java,v 1.1 2008-06-19 21:26:59 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.instance;

import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.NodeProxy;


public class EmptyLabelBlockException extends EvalException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception indicating the the specified edge has no
   * labels.
   */
  public EmptyLabelBlockException(final LabelBlockProxy block,
                                  final EdgeProxy edge,
                                  final ComponentProxy comp)
  {
    super(edge == null ? block : edge);
    mEdge = edge;
    mComponent = comp;
  }


  //#########################################################################
  //# Message
  public String getMessage()
  {
    if (mComponent == null) {
      return null;
    } else if (mEdge == null) {
      return
        "No event labels in blocked events list of " +
        mComponent.getName() + "!";
    } else {
      final NodeProxy source = mEdge.getSource();
      final NodeProxy target = mEdge.getTarget();
      return
        "No event labels on transition from " + source.getName() + " to " +
        target.getName() + " in " + mComponent.getName() + "!";
    }
  }


  //#########################################################################
  //# Static Class Variables
  private final EdgeProxy mEdge;
  private final ComponentProxy mComponent;
  
  
  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
