//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   ConflictCheckMode
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

/**
 * Enumeration of conflict check algorithms to configure a
 * {@link NativeConflictChecker}.
 *
 * @author Robi Malik
 */

public enum ConflictCheckMode
{

  /**
   * Conflict check is performed in two passes. In the first pass, the entire
   * state space of the synchronous product is constructed and stored. Also
   * all transitions discovered during the first pass (except selfloops and
   * multiple transitions) are stored and used for faster coreachability
   * computation in the second pass. In the second pass, a backwards search
   * starting from the marked states is performed to determine whether all the
   * states constructed in the first pass are coreachable.
   */
  STORED_BACKWARDS_TRANSITIONS,

  /**
   * Conflict check is performed in two passes. In the first pass, the entire
   * state space of the synchronous product is constructed and stored. In the
   * second pass, a backwards search starting from the marked states is
   * performed to determine whether all the states constructed in the first
   * pass are coreachable. The backwards search explores the reverse
   * transition relation computed from the component transitions.
   */
  COMPUTED_BACKWARDS_TRANSITIONS,

  /**
   * Conflict check is performed using Tarjan's algorithm to find strongly
   * connected components. The model is determined to be nonblocking when a
   * <I>blocking</I> strongly connected component is encountered, i.e., a
   * strongly connected component without any marked states and without any
   * transitions to another strongly connected components. The implementation
   * is based on an iterative version of Tarjan's algorithm and explores
   * transitions only in the forward direction.
   */
  NO_BACKWARDS_TRANSITIONS

}