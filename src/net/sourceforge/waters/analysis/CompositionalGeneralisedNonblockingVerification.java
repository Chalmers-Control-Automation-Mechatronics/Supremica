package net.sourceforge.waters.analysis;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class CompositionalGeneralisedNonblockingVerification
{

  public CompositionalGeneralisedNonblockingVerification(
                                                         final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public CompositionalGeneralisedNonblockingVerification(
                                                         final ProductDESProxy model,
                                                         final ProductDESProxyFactory factory)
  {
    this(model, null, factory);
  }

  public CompositionalGeneralisedNonblockingVerification(
                                                         final ProductDESProxy model,
                                                         final EventProxy marking,
                                                         final ProductDESProxyFactory factory)
  {
    this(model, marking, null, factory);
  }

  public CompositionalGeneralisedNonblockingVerification(
                                                         final ProductDESProxy model,
                                                         final EventProxy marking,
                                                         final EventProxy preMarking,
                                                         final ProductDESProxyFactory factory)
  {
    mMarking = marking;
    mPreconditionMarking = preMarking;
    mModel = model;
    // need to decide whether this class extends another so I know what
    // variables I want to store
  }

  // #########################################################################
  // # Invocation

  /**
   * This method is used as the main invocation for this class. It will use the
   * methods within this class to perform the composition of the model and then
   * run a conflict checker with the composed model to determine whether the
   * model satisfies generalised nonblocking.
   */
  public boolean run()
  {
    return false;
  }

  public void setConflictChecker(final ConflictChecker conflictChecker)
  {
  }

  public ConflictChecker getConflictChecker()
  {
    return mConflictChecker;
  }

  /**
   * Returns the counter example which can be executed by the original
   * un-composed model.
   *
   * @return
   */
  public ConflictTraceProxy getCounterexample()
  {
    final ConflictTraceProxy counterexample =
        mConflictChecker.getCounterExample();

    return convertCounterexample(counterexample);
  }

  /**
   * This method determines the order in which to compose the automata for the
   * model using the two step approach. Models should be composed in order of
   * the start of the collection through to the end.
   */
  @SuppressWarnings("unused")
  private Collection<AutomatonProxy> composeModel(final ProductDESProxy model)
  {
    return null;
  }

  /**
   * Determines the automata with the fewest transitions for the first step of
   * the approach to determining the ordering of composition. This heuristic has
   * first priority for determining the order.
   *
   * @return
   */
  @SuppressWarnings("unused")
  private Collection<AutomatonProxy> orderCompositionStep1MinT()
  {
    return null;
  }

  /**
   * Determines the automata with the most states for the first step of the
   * approach to determining the ordering of composition. This heuristic has
   * second priority for determining the order.
   *
   * @return
   */
  @SuppressWarnings("unused")
  private Collection<AutomatonProxy> orderCompositionStep1MaxS()
  {
    return null;
  }

  /**
   * Determines the set of automaton which use the specified event. This is a
   * heuristic for the first step of the approach to determining the ordering of
   * composition. This heuristic has third/last priority for determining the
   * order.
   *
   * @return
   */
  @SuppressWarnings("unused")
  private Collection<AutomatonProxy> orderCompositionStep1MustL(
                                                                final EventProxy event)
  {
    return null;
  }

  /**
   * Determines the automaton with the highest proportion of local events for
   * the second step of the approach to determining the ordering of composition.
   * This heuristic has first priority for determining the order.
   *
   * @return
   */
  @SuppressWarnings("unused")
  private AutomatonProxy orderCompositionStep2MaxL()
  {
    return null;
  }

  /**
   * Determines the automaton with the highest proportion of common/shared
   * events for the second step of the approach to determining the ordering of
   * composition. This heuristic has second priority for determining the order.
   *
   * @return
   */
  @SuppressWarnings("unused")
  private AutomatonProxy orderCompositionStep2MaxC()
  {
    return null;
  }

  /**
   * Determines the automaton for which the product of the number of states in
   * the automaton is smallest. Used for the second step of the approach to
   * determining the ordering of composition. This heuristic has third/last
   * priority for determining the order.
   *
   * @return
   */
  @SuppressWarnings("unused")
  private AutomatonProxy orderCompositionStep2MinS()
  {
    return null;
  }

  /**
   * This method takes a collection of ordered automaton and uses a set of
   * simplification rules to compose the model, so it's ready to be passed to a
   * conflict checker.
   *
   * @return
   */
  @SuppressWarnings("unused")
  private ProductDESProxy abstractModel(
                                        final Collection<AutomatonProxy> orderedAut)
  {
    return null;
  }

  /**
   * Determines whether two automaton satisfy observation equivalence.
   *
   * @param aut1
   * @param aut2
   * @return
   */
  @SuppressWarnings("unused")
  private boolean testObservationEquivalence(final AutomatonProxy aut1,
                                             final AutomatonProxy aut2)
  {
    return false;
  }

  /**
   * If an automaton contains a transition between two different states which
   * are both marked with the generalised precondition marking, then the marking
   * is removed from the source state.
   *
   * @param aut
   * @return
   */
  @SuppressWarnings("unused")
  private AutomatonProxy removeGeneralisedMarking(final AutomatonProxy aut)
  {
    return null;
  }

  /**
   * If a state 'x' is not reachable from any state with the generalised
   * precondition marking then the default marking proposition is removed from
   * 'x'.
   *
   * @param aut
   * @return
   */
  @SuppressWarnings("unused")
  private AutomatonProxy removeDefaultMarking(final AutomatonProxy aut)
  {
    return null;
  }

  /**
   * States which cannot be reached by a state marked with either the
   * generalised precondition marking or default marking proposition are removed
   * from the given automaton.
   *
   * @param aut
   * @return
   */
  @SuppressWarnings("unused")
  private AutomatonProxy removeNoncoreachableStates(final AutomatonProxy aut)
  {
    return null;
  }

  /**
   * If two states are not marked with the generalised precondition marking and
   * are reachable by exactly the same strings from the initial state and from
   * each state marked with the generalised precondition, then the two states
   * can be merged into a single state.
   *
   * @param aut
   * @return
   */
  @SuppressWarnings("unused")
  private AutomatonProxy determiniseNonPreconditionMarkedStates(
                                                                final AutomatonProxy aut)
  {
    return null;
  }

  /**
   * If a transition uses the silent event and both the source and target state
   * do not have the generalised precondition marking then the transition is
   * removed from the automaton and all transitions originating from the target
   * state are copied to the source state of the transition.
   *
   * @param aut
   * @return
   */
  @SuppressWarnings("unused")
  private AutomatonProxy removeLeadingSilentTransitions(final AutomatonProxy aut)
  {
    return null;
  }

  /**
   *If a state does not contain the generalised precondition marking or the
   * default marking proposition, and has an outgoing silent transition then the
   * state is removed from the automaton. Incoming transitions to the removed
   * state are added to its successor states.
   *
   * @param aut
   * @return
   */
  @SuppressWarnings("unused")
  private AutomatonProxy removeOriginatingSilentTransitions(
                                                            final AutomatonProxy aut)
  {
    return null;
  }

  /**
   * Converts a counterexample into a conflict trace proxy which can be executed
   * within the original un-composed model.
   *
   * @return
   */
  private ConflictTraceProxy convertCounterexample(
                                                   final ConflictTraceProxy counterexample)
  {
    return null;
  }

  // #########################################################################
  // # Data Members
  private final EventProxy mMarking;
  private final EventProxy mPreconditionMarking;
  private final ProductDESProxy mModel;
  ConflictChecker mConflictChecker;

}
