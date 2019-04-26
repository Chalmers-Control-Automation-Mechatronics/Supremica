//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.analysis.efa.efsm;

import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ModuleProxyFactory;


/**
 * A cache to remember unfolded variables while evaluating variable
 * selection heuristics. The cache remembers the last two variables
 * unfolded, as needed when computing the best candidate from a list
 * of variables.
 *
 * @author Robi Malik
 *
 * @see MaxTrueVariableSelectionHeuristic
 * @see MinStatesVariableSelectionHeuristic
 */

class EFSMUnfoldingCache
{

  //#########################################################################
  //# Constructors
  EFSMUnfoldingCache(final EFSMConflictChecker checker)
  {
    mChecker = checker;
    final ModuleProxyFactory factory = checker.getFactory();
    final CompilerOperatorTable optable = checker.getCompilerOperatorTable();
    mUnfolder = new EFSMPartialUnfolder(factory, optable);
    mFirstCaller = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Returns the unfolding of the given variable.
   * @param  var      The variable to be unfolded.
   * @return The unfolded transition relation, which may be a computed or
   *         a cached result.
   */
  EFSMTransitionRelation unfold(final EFSMVariable var)
  {
    if (var == mFirstVariable) {
      return mFirstUnfolding;
    } else if (var == mSecondVariable) {
      return mSecondUnfolding;
    }
    final EFSMSystem system = mChecker.getCurrentEFSMSystem();
    EFSMTransitionRelation unfolded;
    try {
      unfolded = mUnfolder.unfold(var, system);
    } catch (final EvalException | AnalysisException exception) {
      unfolded = null;
    }
    if (mFirstVariable == null) {
      mFirstVariable = var;
      mFirstUnfolding = unfolded;
    } else {
      mSecondVariable = var;
      mSecondUnfolding = unfolded;
    }
    return unfolded;
  }

  /**
   * Registers the given selection heuristic to control the cache.
   * When called the first time, the caller is remember as the heuristic
   * controlling the cache. Only this heuristic can perform reset operations
   * on the cache. When called a second time, the method has no effect.
   * @param  caller   The selection heuristic calling this method,
   *                  which attempts to be registered as the first caller.
   */
  void register(final SelectionHeuristic<EFSMVariable> caller)
  {
    if (mFirstCaller == null) {
      mFirstCaller = caller;
    }
  }

  /**
   * Resets the cache by removing all entries except those for the given
   * variable.
   * @param  caller   The selection heuristic calling this method.
   *                  The request is only honoured if this argument is the
   *                  first caller of the {@link #register(SelectionHeuristic)
   *                  register()} method.
   * @param  var      The variable to be retained. All other cache entries
   *                  are deleted.
   */
  void reset(final SelectionHeuristic<EFSMVariable> caller,
             final EFSMVariable var)
  {
    if (caller == mFirstCaller) {
      if (mFirstVariable == var) {
        // nothing
      } else if (mSecondVariable == var) {
        mFirstVariable = mSecondVariable;
        mFirstUnfolding = mSecondUnfolding;
      } else {
        mFirstVariable = null;
        mFirstUnfolding = null;
      }
      mSecondVariable = null;
      mSecondUnfolding = null;
    }
  }

  /**
   * Clears all cache entries.
   * @param  caller   The selection heuristic calling this method.
   *                  The request is only honoured if this argument is the
   *                  first caller of the {@link #register(SelectionHeuristic)
   *                  register()} method.
   */
  void reset(final SelectionHeuristic<EFSMVariable> caller)
  {
    if (caller == mFirstCaller) {
      mFirstVariable = mSecondVariable = null;
      mFirstUnfolding = mSecondUnfolding = null;
    }
  }


  //#########################################################################
  //# Data Members
  private final EFSMConflictChecker mChecker;
  private final EFSMPartialUnfolder mUnfolder;
  private SelectionHeuristic<EFSMVariable> mFirstCaller;

  private EFSMVariable mFirstVariable;
  private EFSMTransitionRelation mFirstUnfolding;
  private EFSMVariable mSecondVariable;
  private EFSMTransitionRelation mSecondUnfolding;

}
