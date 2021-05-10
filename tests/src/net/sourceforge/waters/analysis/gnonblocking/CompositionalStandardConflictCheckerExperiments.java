//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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
 * with a variety of configurations for models with only one marking
 * proposition. The heuristics for choosing candidates are varied, as well as
 * the abstraction rules applied and the order of them.
 *
 * @author Rachel Francis
 */
public class CompositionalStandardConflictCheckerExperiments extends
    CompositionalConflictCheckerExperiments
{

  // #######################################################################
  // # Constructor
  public CompositionalStandardConflictCheckerExperiments(
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
      final CompositionalStandardConflictCheckerExperiments experiment =
        new CompositionalStandardConflictCheckerExperiments
          (filename, preselectingHeuristic, selectingHeuristic, rules);
      experiment.setUp();
      experiment.runAllTests();
      experiment.tearDown();
    } else {
      System.err
          .println("Usage: CompositionalGeneralisedConflictCheckerExperiments outputFilename preselectingHeuristic selectingHeuristic listOfRulesSelection");
    }
  }

  protected void runAllTests() throws Exception
  {
    verifyBig_BMW();
    verify_ftechnik();
    verify_verriegel4();
    verify_verriegel4b();
    verify_rhone_alps();
    verify_tbed_ctct();
    verify_fzelle();
    verify_tbed_uncont();
    verify_tbed_noderail();
    verify_tbed_noderailb();
    verify_tbed_valid();
    verify_profisafe_i4_host();
    verify_profisafe_i5_host();

  }

  // #######################################################################
  // # Models
  private void verifyBig_BMW() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "big_bmw.wmod";
    runModel(group, dir, name);
  }

  private void verify_ftechnik() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "ftechnik.wmod";
    runModel(group, dir, name);
  }

  private void verify_fzelle() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "fzelle.wmod";
    runModel(group, dir, name);
  }

  private void verify_rhone_alps() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "aip0alps.wmod";
    runModel(group, dir, name);
  }

  private void verify_tbed_ctct() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_ctct.wmod";
    runModel(group, dir, name);
  }

  private void verify_tbed_uncont() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_uncont.wmod";
    runModel(group, dir, name);
  }

  private void verify_tbed_noderailb() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_noderailb.wmod";
    runModel(group, dir, name);
  }

  private void verify_tbed_noderail() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_noderail.wmod";
    runModel(group, dir, name);
  }

  private void verify_tbed_valid() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_valid.wmod";
    runModel(group, dir, name);
  }

  private void verify_verriegel4() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "verriegel4.wmod";
    runModel(group, dir, name);
  }

  private void verify_verriegel4b() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "verriegel4b.wmod";
    runModel(group, dir, name);
  }

  private void verify_profisafe_i4_host() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4_host.wmod";
    runModel(group, dir, name);
  }

  private void verify_profisafe_i5_host() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i5_host.wmod";
    runModel(group, dir, name);
  }
}
