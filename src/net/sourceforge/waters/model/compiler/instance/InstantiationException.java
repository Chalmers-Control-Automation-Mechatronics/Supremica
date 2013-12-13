//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.instance
//# CLASS:   InstantiationException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.instance;

import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.InstanceProxy;


public class InstantiationException extends EvalException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception indicating that the given instance
   * failed to be instantiated because of the indicated exception
   * being thrown when loading the module.
   */
  public InstantiationException(final Exception cause,
				final InstanceProxy inst)
  {
    super(createMessage(cause, inst), cause, inst);
  }


  //#########################################################################
  //# Message Preparation
  private static String createMessage(final Exception cause,
				      final InstanceProxy inst)
  {
    final StringBuilder buffer = new StringBuilder();
    buffer.append("Can't load module '");
    buffer.append(inst.getName());
    buffer.append("' for instantiation");
    final String msg = cause.getMessage();
    if (msg == null) {
      buffer.append('!');
    } else {
      buffer.append(msg);
    }
    return buffer.toString();
  }

  
  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
