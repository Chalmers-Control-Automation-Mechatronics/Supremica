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

package net.sourceforge.waters.analysis.gnonblocking;

import java.io.FileNotFoundException;


/**
 * This class runs experiments using the CompositionalGeneralisedConflictChecker
 * with a variety of configurations for models with multiple marking
 * propositions. The heuristics for choosing candidates are varied, as well as
 * the abstraction rules applied and the order of them.
 *
 * @author Rachel Francis
 */
public class CompositionalGeneralisedConflictCheckerExperiments extends
    CompositionalConflictCheckerExperiments
{

  // #######################################################################
  // # Constructor
  public CompositionalGeneralisedConflictCheckerExperiments(
                                                            final String statsFilename,
                                                            final String preselectingHeuristic,
                                                            final String selectingHeuristic,
                                                            final int rules)
      throws FileNotFoundException
  {
    super(statsFilename, preselectingHeuristic, selectingHeuristic, rules);
  }

  // #######################################################################
  // # Invocation
  public static void main(final String[] args) throws Exception
  {
    if (args.length == 4) {
      final String filename = args[0];
      final String preselectingHeuristic = args[1];
      final String selectingHeuristic = args[2];
      final int rules = Integer.decode(args[3]);
      final CompositionalGeneralisedConflictCheckerExperiments experiment =
          new CompositionalGeneralisedConflictCheckerExperiments(filename, preselectingHeuristic, selectingHeuristic, rules);
      experiment.setUp();
      experiment.runAllTests();
      experiment.tearDown();
    } else {
      System.err
          .println("Usage: CompositionalGeneralisedConflictCheckerExperiments outputFilename preselectingHeuristic selectingHeuristic listOfRulesSelection");
    }
  }

  void runAllTests() throws Exception
  {
    verify_aip3_syn_as1();
    verify_aip3_syn_as2();
    verify_aip3_syn_as3();
    verify_aip3_syn_io();
    verify_aip3_syn_tu1();
    verify_aip3_syn_tu2();
    verify_aip3_syn_tu3();
    verify_aip3_syn_tu4();
  }

  // #######################################################################
  // # Models
  private void verify_aip3_syn_as1() throws Exception
  {
    runModel("despot", "song_aip/aip3_syn", "as1.wmod");
  }

  private void verify_aip3_syn_as2() throws Exception
  {
    runModel("despot", "song_aip/aip3_syn", "as2.wmod");
  }

  private void verify_aip3_syn_as3() throws Exception
  {
    runModel("despot", "song_aip/aip3_syn", "as3.wmod");
  }

  private void verify_aip3_syn_io() throws Exception
  {
    runModel("despot", "song_aip/aip3_syn", "io.wmod");
  }

  private void verify_aip3_syn_tu1() throws Exception
  {
    runModel("despot", "song_aip/aip3_syn", "tu1.wmod");
  }

  private void verify_aip3_syn_tu2() throws Exception
  {
    runModel("despot", "song_aip/aip3_syn", "tu2.wmod");
  }

  private void verify_aip3_syn_tu3() throws Exception
  {
    runModel("despot", "song_aip/aip3_syn", "tu3.wmod");
  }

  private void verify_aip3_syn_tu4() throws Exception
  {
    runModel("despot", "song_aip/aip3_syn", "tu4.wmod");
  }
}
