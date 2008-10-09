//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   ConflictChecker
//###########################################################################
//# $Id: ConflictChecker.java,v 1.1 2005/05/08 00:24:31 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis.comp552;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.des.ConflictKind;


/**
 * <P>A dummy implementation of a conflict checker.</P>
 *
 * <P>The {@link #run()} method of this model checker does nothing,
 * and simply claims that every model is nonconflicting.</P>
 *
 * @see ModelChecker
 *
 * @author Robi Malik
 */

public class ConflictChecker extends ModelChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new conflict checker to check whether the given model
   * nonconflicting with respect to the default marking proposition.
   * @param  model      The model to be checked by this conflict checker.
   * @param  desFactory Factory used for trace construction.
   */
  public ConflictChecker(final ProductDESProxy model,
                         final ProductDESProxyFactory desFactory)
  {
    this(model, getDefaultMarkingProposition(model), desFactory);
  }


  /**
   * Creates a new conflict checker to check a particular model.
   * @param  model      The model to be checked by this conflict checker.
   * @param  marking    The proposition event that defines which states
   *                    are marked. Every state has a list of propositions
   *                    attached to it; the conflict checker considers only
   *                    those states as marked that are labelled by
   *                    <CODE>marking</CODE>, i.e., their list of
   *                    propositions must contain this event (exactly the
   *                    same object).
   * @param  desFactory Factory used for trace construction.
   */
  public ConflictChecker(final ProductDESProxy model,
                         final EventProxy marking,
                         final ProductDESProxyFactory desFactory)
  {
    super(model, desFactory);
    mMarking = marking;
  }


  //#########################################################################
  //# Invocation
  /**
   * Runs this conflict checker.
   * This method starts the model checking process on the model given
   * as parameter to the constructor of this object. On termination,
   * the result of checking the property is known and can be queried
   * using the {@link #getResult()} and {@link #getCounterExample()}
   * methods.
   * Presently, this is a dummy implementation that does nothing but always
   * returns <CODE>true</CODE>.
   * @return <CODE>true</CODE> if the model is nonconflicting, or
   *         <CODE>false</CODE> if it is not.
   *         The same value can be queried using the {@link #getResult()}
   *         method.
   */
  public boolean run()
  {
    // The following code determines and prints the number of marked
    // states in the model (before synchronous composition).
    // This is not very helpful for a conflict check, but it demonstrates
    // the use of the interfaces.

    int count = 0;

    // First get the model
    final ProductDESProxy model = getModel();

    // For each automaton ...
    for (final AutomatonProxy aut : model.getAutomata()) {
      // For each state ...
      for (final StateProxy state : aut.getStates()) {
        // Does the list of propositions contain our marking?
        final Collection<EventProxy> props = state.getPropositions();
        if (props.contains(mMarking)) {
          // Yes --- this is a marked state.
          count++;
        }
      }
    }
    // Print the number of marked states that we have counted.
    System.out.print(count + " ");

    // This all was no good as far as conflict checking was concerned.
    // Let us just leave.
    return true;
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the result of conflict checking.
   * @return <CODE>true</CODE> if the model was found to be nonconflicting,
   *         <CODE>false</CODE> otherwise.
   * @throws IllegalStateException in all cases, because this method is
   *         not yet implemented.
   */
  public boolean getResult()
  {
    throw new IllegalStateException
      ("Conflict checking not yet implemented!");
  }

  /**
   * Gets a counterexample if the model was found to be conflicting,
   * representing a conflict error trace. A conflict error trace is a
   * sequence of events that takes the model to a state that is not
   * coreachable. That is, after executing the counterexample, the automata
   * are in a state from where it is no longer possible to reach a state
   * where all automata are marked at the same time.
   * @return A trace object representing the counterexample.
   *         The returned trace is constructed for the input product DES
   *         of this conflict checker and shares its automata and
   *         event objects.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before {@link #run()}
   *         has been called, or model checking has found that the
   *         property is satisfied and there is no counterexample.
   */
  public ConflictTraceProxy getCounterExample()
  {
    // The following creates a trace that consists of all the events in
    // the input model.
    // This code is only here to demonstrate the use of the interfaces.
    // IT DOES NOT GIVE A CORRECT TRACE!

    final ProductDESProxyFactory desFactory = getFactory();
    final ProductDESProxy model = getModel();
    final String modelname = model.getName();
    final String tracename = modelname + ":conflicting";
    final Collection<EventProxy> events = model.getEvents();
    final List<EventProxy> tracelist = new LinkedList<EventProxy>();
    for (final EventProxy event : events) {
      tracelist.add(event);
    }
    // Note. To complete conflict kind field of the trace is optional for
    // this assignment---it will not be tested.
    final ConflictTraceProxy trace =
      desFactory.createConflictTraceProxy(tracename, model, tracelist,
                                          ConflictKind.CONFLICT);
    return trace;
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Searches the given model for a proposition event with the default
   * marking name and returns this event.
   * @throws IllegalArgumentException to indicate that the given model
   *         does not contain any proposition with the default marking
   *         name.
   */
  private static EventProxy getDefaultMarkingProposition
    (final ProductDESProxy model)
  {
    for (final EventProxy event : model.getEvents()) {
      if (event.getKind() == EventKind.PROPOSITION &&
          event.getName().equals(EventDeclProxy.DEFAULT_MARKING_NAME)) {
        return event;
      }
    }
    throw new IllegalArgumentException
      ("ProductDESProxy '" + model.getName() +
       "' does not contain any proposition called '" +
       EventDeclProxy.DEFAULT_MARKING_NAME + "'!");
  }


  //#########################################################################
  //# Data Members
  private EventProxy mMarking;

}
