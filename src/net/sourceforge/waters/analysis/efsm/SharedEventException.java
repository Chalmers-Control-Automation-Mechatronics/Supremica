//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SharedEventException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * An exception indicating that an event is used in more than one
 * automaton, although this is not supported.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class SharedEventException extends EvalException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public SharedEventException()
  {
  }

  /**
   * Constructs a new exception indicating that the given identifier is already
   * defined.
   */
  public SharedEventException(final SimpleExpressionProxy ident,
                              final ComponentProxy comp1,
                              final ComponentProxy comp2)
  {
    super("Event '" + ident.toString() + "' is used in components '" +
          comp1.getName() + "' and '" + comp2.getName() +
          "', but shared events are not supported.", ident);
  }


  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}