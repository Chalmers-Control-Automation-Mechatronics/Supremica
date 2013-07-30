//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   SilentIncomingTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.BitSet;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>A list buffer transition relation implementation of the
 * <I>Silent Incoming Rule</I> or the <I>Only Silent Incoming Rule</I>.</P>
 *
 * <P>The <I>Silent Incoming Rule</I> removes a transition
 * when a tau event links two states <I>x</I> and&nbsp;<I>y</I> where at most
 * the source state&nbsp;<I>x</I> contains the precondition
 * marking&nbsp;<I>alpha</I>. If the target state&nbsp;<I>y</I> becomes
 * unreachable, it is removed, too. All transitions originating from the target
 * state&nbsp;<I>y</I> are copied to the source state&nbsp;<I>x</I>.</P>
 *
 * <P>The implementation can be configured to remove only transitions leading
 * to states that become unreachable, giving the <I>Only Silent Incoming
 * Rule</I>.</P>
 *
 * <P>The implementation supports both standard and generalised nonblocking
 * variants of the abstraction. If a precondition marking is configured,
 * only transitions leading to states not marked by the precondition marking
 * can be abstracted (as described above). Without a precondition marking, only
 * transitions leading to states with an outgoing silent transition can be
 * abstracted.</P>
 *
 * <P><I>References:</I><BR>
 * Hugo Flordal, Robi Malik. Compositional Verification
 * in Supervisory Control. SIAM Journal of Control and Optimization,
 * 48(3), 1914-1938, 2009.<BR>
 * Robi Malik, Ryan Leduc. A Compositional Approach for Verifying
 * Generalised Nonblocking, Proc. 7th International Conference on Control and
 * Automation, ICCA'09, 448-453, Christchurch, New Zealand, 2009.</P>
 *
 * @author Rachel Francis, Robi Malik
 */

public class EnabledEventsSilentIncomingTRSimplifier
  extends AbstractMarkingTRSimplifier
{

  //#########################################################################
  //# Constructors
  public EnabledEventsSilentIncomingTRSimplifier()
  {
  }

  public EnabledEventsSilentIncomingTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets whether this simplifier should consider deadlock states when
   * removing selfloops.
   * @see AbstractMarkingTRSimplifier#isDumpStateAware()
   */
  public void setDumpStateAware(final boolean aware)
  {
    mDumpStateAware = aware;
  }

  /**
   * Gets whether this simplifier considers deadlock states when
   * removing selfloops.
   */
  @Override
  public boolean isDumpStateAware()
  {
    return mDumpStateAware;
  }

  /**
   * Sets whether abstraction is applied to all states or only to states
   * that become unreachable. When this option is set to <CODE>true</CODE>
   * (the default), then the <I>Silent Incoming Rule</I> is only applied
   * to tau-transitions that lead to a state that becomes unreachable
   * by application of the rule. When set to <CODE>false</CODE>, the rule
   * is applied to all tau transitions leading to a state not marked by
   * the precondition, regardless of whether these states become unreachable
   * or not.
   */
  public void setRestrictsToUnreachableStates(final boolean restrict)
  {
    mRestrictsToUnreachableStates = restrict;
  }

  /**
   * Gets whether abstraction is applied to all states or only to states
   * that become unreachable.
   * @see #setRestrictsToUnreachableStates(boolean) setRestrictsToUnreachableStates()
   */
  public boolean getRestrictsToUnreachableStates()
  {
    return mRestrictsToUnreachableStates;
  }

  /**
   * Sets the number of always enabled events. Always enabled events are events
   * that are not disabled by any other current automaton.
   */
  public void setNumberOfEnabledEvents(final int numEnabledEvents)
  {
    mNumberOfEnabledEvents = numEnabledEvents;
  }

  public int getNumberOfEnabledEvents()
  {
    return mNumberOfEnabledEvents;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return ListBufferTransitionRelation.CONFIG_SUCCESSORS;
  }

  @Override
  public boolean isObservationEquivalentAbstraction()   //unknown
  {
    return true;
  }

  //where are the statistics?


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifier
  @Override
  public boolean runSimplifier()
  throws AnalysisException
  {
    final int tauID = EventEncoding.TAU;
    final ListBufferTransitionRelation rel = getTransitionRelation();
    if ((rel.getProperEventStatus(tauID) & EventEncoding.STATUS_UNUSED) != 0) {
      return false;
    } else if (getPreconditionMarkingID() < 0) {        //what is the precondition marking alpha
      mTauTestIterator = rel.createSuccessorsReadOnlyIterator();
      mTauTestIterator.resetEvents(0, mNumberOfEnabledEvents);               //Definitely change this to include enabled events

    }
    final int numStates = rel.getNumberOfStates();
    final BitSet keep = new BitSet(numStates);  //Creates a BitSet which remembers which states will be kept
    if (mRestrictsToUnreachableStates) {
      for (int state = 0; state < numStates; state++) { //loop through all the states
        if (rel.isInitial(state) ||                     //If it is an initial state
            !isReducible(state) ||                      //If it has NO outgoing tau/AE or alpha marked
            !rel.isReachable(state)) {                  //and is not reachable
          keep.set(state);                              //Then we want to keep it
        }
      }
      final TransitionIterator iter =                       //loop over all transitions
        rel.createAllTransitionsReadOnlyIterator();
      while (iter.advance()) {
        if (iter.getCurrentEvent() != tauID) {              //If the transition is not tau (this stays tau)
          final int target = iter.getCurrentTargetState();
          keep.set(target);                                 //Keep the target state
        }       //So keep everything with no outgoing tau or incoming not tau
                //Only silent incoming needs this to be true to work
      }
    } else {
      for (int state = 0; state < numStates; state++) { //otherwise if it is just not reachable or reducible keep it
        if (!isReducible(state) || !rel.isReachable(state)) {
          keep.set(state);      //This doesn't care if ALL the incoming trans are tau. Can also simplify initial for some reason.
        }
      }
    }
    checkAbort();
    if (keep.cardinality() == numStates) {      //If we kept all the states stop
      return false;
    }
    final TransitionIterator reader = rel.createSuccessorsReadOnlyIterator();
    final TransitionIterator writer = rel.createSuccessorsModifyingIterator();
    final TIntArrayList targets = new TIntArrayList();
    final TIntStack stack = new TIntArrayStack();
    boolean modified = false;
    for (int source = 0; source < numStates; source++) {        //Loop through all states
      if (rel.isReachable(source)) {                            //If we can actually reach the state
        checkAbort();
        final TIntHashSet visited = new TIntHashSet();          //Then keep track of states we've visited through this state
        stack.push(source);
        visited.add(source);                                    //We've obviously visited the starting state already
        while (stack.size() > 0) {
          final int current = stack.pop();                      //Get the current state to look at
          reader.reset(current, tauID); //This stays tau
          while (reader.advance()) {                            //If the current state has an outgoing tau
            final int target = reader.getCurrentTargetState();  //Get the state that tau is targeted at
            if (!keep.get(target) && visited.add(target)) {     //If we don't want to keep target state and we haven't already visited it
              stack.push(target);
              targets.add(target);                              //Then we can apply the rule to this state
            }                                  //then loop through any other possible outgoing taus on the visited states and target them too
          }
        }
        if (!targets.isEmpty()) {                           //If we can still apply the rule
          rel.copyOutgoingTransitions(targets, source);     //Copy all the outgoing transitions from
          //the targets to all the states that have tau transitions pointing at it.

          //If there is a tau chain outgoing from target, all outgoing transitions from those are also copied.
          //But the targets could not have incoming not tau transitions so this is not likely to occur when restricting.

          writer.reset(source, tauID);  //This stays tau
          while (writer.advance()) {
            final int target = writer.getCurrentTargetState();  //Then remove any taus from source to something we visited in tau chain
            if (visited.contains(target)) {
              writer.remove();
            }
          }
          targets.clear();          //empty the targets before starting next on next state
          modified = true;          //say we have changed the aut
        }
      }
    }                               //After looping through all the states
    if (modified) {                 //If we changed anything
      applyResultPartitionAutomatically();      //Magic happens
    }
    return modified;                //return if we changed it or not
  }

  @Override
  protected void tearDown()
  {
    mTauTestIterator = null;
    super.tearDown();
  }

  @Override
  protected void applyResultPartition()
  throws AnalysisException
  {
    super.applyResultPartition();
    final ListBufferTransitionRelation rel = getTransitionRelation();
    rel.checkReachability();
    rel.removeTauSelfLoops();
    removeProperSelfLoopEvents();
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Returns whether the given state may be simplified by the silent
   * continuation rule. A state is reducible if it has an outgoing
   * tau transition or, in the case of generalised nonblocking, if it
   * is not marked by the precondition (alpha) marking.
   */
  private boolean isReducible(final int state)
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int alphaID = getPreconditionMarkingID();
    if (alphaID < 0) {
      mTauTestIterator.resetState(state);           //If it has outgoing tau (or always enabled) event
      return mTauTestIterator.advance();            //then it is reducible
    } else {
      return !rel.isMarked(state, alphaID);     //if it is marked alpha then it is not reducible otherwise it is
    }
  }


  //#########################################################################
  //# Data Members
  private boolean mRestrictsToUnreachableStates = true;
  private boolean mDumpStateAware = false;
  private int mNumberOfEnabledEvents;

  private TransitionIterator mTauTestIterator;

}

