//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.OccursChecker;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * A variable selection heuristic used by the {@link EFSMConflictChecker}.
 * The Variable occurrence heuristics selects the local variable that
 * occurs in the largest number of updates in its EFSM.
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public class MaxOccurrenceVariableSelectionHeuristic
  extends NumericSelectionHeuristic<EFSMVariable>
{

  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic
  @Override
  public double getHeuristicValue(final EFSMVariable var)
  {
    final EFSMTransitionRelation efsmTR = var.getTransitionRelation();
    if (efsmTR == null) {
      return 0;
    } else {
      final OccursChecker checker = new OccursChecker();
      final SimpleExpressionProxy varname = var.getVariableName();
      final EFSMEventEncoding encoding = efsmTR.getEventEncoding();
      int occurrences = 0;
      for (int e = EventEncoding.NONTAU; e < encoding.size(); e++) {
        final ConstraintList update = encoding.getUpdate(e);
        if (checker.occurs(varname, update)) {
          occurrences++;
        }
      }
      return -occurrences;
    }
  }

}
