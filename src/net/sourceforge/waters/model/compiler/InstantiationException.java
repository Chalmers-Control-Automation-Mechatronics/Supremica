//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.base
//# CLASS:   InstantiationException
//###########################################################################
//# $Id: InstantiationException.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

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
    super("Can't load module '" + inst.getName() +
	  "' for instantiation: " + cause.getMessage(), cause, inst);
  }

}