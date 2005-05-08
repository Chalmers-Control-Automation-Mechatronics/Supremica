//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   ControllabilityChecker
//###########################################################################
//# $Id: ControllabilityChecker.java,v 1.1 2005-05-08 00:24:31 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis;

import java.util.List;

import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * <P>A dummy implementation of a controllability checker.</P>
 *
 * <P>The {@link #run()} method of this model checker does nothing,
 * and simply claims that every model is controllable.</P>
 *
 * @see ModelChecker
 *
 * @author Robi Malik
 */

public class ControllabilityChecker extends ModelChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new controllability checker to check a particular model.
   * @param  model   The model to be checked by this controllability checker.
   */
  public ControllabilityChecker(final ProductDESProxy model)
  {
    super(model);
  }


  //#########################################################################
  //# Invocation
  /**
   * A dummy method. Does nothing but always returns <CODE>true</CODE>.
   * @return <CODE>true</CODE>
   */
  public boolean run()
  {
    return true;
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the result of controllability checking.
   * @return <CODE>true</CODE> if the model was found to be controllable,
   *         <CODE>false</CODE> otherwise.
   * @throws IllegalStateException in all cases, because this method is
   *         not yet implemented.
   */
  public boolean getResult()
  {
    throw new IllegalStateException
      ("Controllability checking not yet implemented!");
  }

  /**
   * Gets a counterexample if the model was found to be not controllable.
   * @return A list of events of type
   *         {@link net.sourceforge.waters.model.des.EventProxy EventProxy}
   *         representing a controllability error trace.
   *         A controllability error trace is a nonempty sequence of events
   *         such that all except the last event in the list can be
   *         executed by the model. The last event in list is an
   *         uncontrollable event that is possible in all plant
   *         automata, but not in all specification automata present
   *         in the model. Thus, the last step demonstrates why the
   *         model is not controllable.
   * @throws IllegalStateException in all cases, because this method is
   *         not yet implemented.
   */
  public List getCounterExample()
  {
    throw new IllegalStateException
      ("Controllability checking not yet implemented!");
  }

}
