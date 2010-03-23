//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   SynchrononousProductStateMap
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.Collection;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * Additional information to be returned by the observation equivalence and
 * similar abstraction rules to identify sets of original states after merging.
 *
 * @author Rachel Francis
 */

public interface MergingStateMap
{
  /**
   * Gets the automaton that was simplified.
   */
  public AutomatonProxy getInputAutomaton();

  /**
   * Gets the set of states of the original automaton, which were merged into
   * the given state.
   */
  public Collection<StateProxy> getOriginalStates(final StateProxy state);
}


// OK, this is not going to work so easily.

// MergingStateMap cannot be implemented in TransitionRelation nor in
// TransBiSimulator, because neither class has enough information.
// Information from both classes must be combined. The only class
// that can put it together will be ObservationEquivalenceStep in
// CompositionalGeneralisedConflictChecker.

// Then it is probably better to implement the 3-step map directly in
// ObservationEquivalenceStep and not use this interface at all.

// Here is how it may look.

// private class ObservationEquivalenceStep extends Step {

//   /**
//    * Demo implementation of getOriginalStates method as above.
//    * Probably not needed in this form. More likely, the convertTrace()
//    * method will use the individual maps directly.
//    */
//   private Collection<StateProxy> getOriginalStates(final StateProxy outstate)
//   {
//     final int outcode = mReverseOutputStateMap.get(outstate);
//     final int[] incodes = mClassMap.get(outcode);
//     final Collection<StateProxy> result =
//       new ArrayList<StateProxy>(incodes.length);
//     for (int i = 0; i < incodes.length; i++) {
//       final StateProxy instate = mOriginalStates[i];
//       result.add(instate);
//     }
//     return result;
//   }

//   //#######################################################################
//   //# Data Members
//   /**
//    * Array of original states. Maps state codes in the input
//    * TransitionRelation to state objects in the input automaton.
//    * Obtained from TransitionRelation.
//    */
//   private final StateProxy[] mOriginalStates;
//   /**
//    * Maps state codes of the output TransitionRelation to list of state
//    * codes in input TransitionRelation. This gives the class of states
//    * merged to form the given state in the simplified automaton. Obtained
//    * from TransBiSimulator.
//    */
//   private final Map<Integer,int[]> mClassMap;
//   /**
//    * Reverse encoding of output states. Maps states in output automaton
//    * (simplified automaton) to state code in output transition relation.
//    * Obtained from TransitionRelation.
//    */
//   private final TObjectIntHashMap<StateProxy> mReverseOutputStateMap;
// }
