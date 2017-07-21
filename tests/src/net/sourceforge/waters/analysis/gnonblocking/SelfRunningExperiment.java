//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.analysis.gnonblocking;

/**
 * This class can be used to automatically run experiments for different
 * properties with all possible combinations of heuristics.
 *
 * @author Rachel Francis
 */
public class SelfRunningExperiment
{

  public SelfRunningExperiment(final String property,
                               final String filename,
                               final int rules)
    throws Exception
  {
    for (int preselecting = 0; preselecting < 3; preselecting++) {
      final String preselectingHeuristic =
        getPreselectingHeuristic(preselecting);
      for (int selecting = 0; selecting < 6; selecting++) {
        final String selectingHeuristic = getSelectingHeuristic(selecting);
        final String entirefilename =
          filename + '_' + preselecting + '_' + selecting + ".csv";
        if (property.equals("standardnonblocking")) {
          final CompositionalStandardConflictCheckerExperiments experiment =
            new CompositionalStandardConflictCheckerExperiments
              (entirefilename, preselectingHeuristic,
               selectingHeuristic, rules);
          experiment.setUp();
          experiment.runAllTests();
          experiment.tearDown();
        } else if (property.equals("generalisednonblocking")) {
          final CompositionalGeneralisedConflictCheckerExperiments experiment =
            new CompositionalGeneralisedConflictCheckerExperiments
              (entirefilename, preselectingHeuristic,
               selectingHeuristic, rules);
          experiment.setUp();
          experiment.runAllTests();
          experiment.tearDown();
        }
      }
    }
  }

  private static String getSelectingHeuristic(final int selecting)
  {
    String heuristic = "";
    if (selecting == 0) {
      heuristic = "maxl";
    } else if (selecting == 1) {
      heuristic = "maxlt";
    } else if (selecting == 2) {
      heuristic = "maxc";
    } else if (selecting == 3) {
      heuristic = "maxct";
    } else if (selecting == 4) {
      heuristic = "mins";
    } else if (selecting == 5) {
      heuristic = "minsc";
    }
    return heuristic;
  }

  private static String getPreselectingHeuristic(final int preselecting)
  {
    if (preselecting == 0) {
      return "mustl";
    } else if (preselecting == 1) {
      return "maxs";
    } else if (preselecting == 2) {
      return "mint";
    }
    return "";
  }
}
