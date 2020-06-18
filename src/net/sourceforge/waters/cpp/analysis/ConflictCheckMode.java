//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
