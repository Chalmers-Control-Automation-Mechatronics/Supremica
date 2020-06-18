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

package net.sourceforge.waters.analysis.compositional;

import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.ProjectingSupervisorReductionFactory;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionFactory;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionMainMethod;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


/**
 * This class can be used to automatically run experiments for
 * automata vs. state representation synthesis, with all possible
 * combinations of heuristics.
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public class SelfRunningExperimentModularAndCompositionalSynthesis
{

  public static void main(final String[] args)
  {
    try {
      //final String outputDir = System.getProperty("waters.test.outputdir");
      final ProductDESProxyFactory factory =
        ProductDESElementFactory.getInstance();
      final ModularAndCompositionalSynthesizer automataSynthesizer =
        new ModularAndCompositionalSynthesizer(factory);
      automataSynthesizer.setDetailedOutputEnabled(false);
      final SupervisorReductionFactory supRed = new ProjectingSupervisorReductionFactory
        (SupervisorReductionMainMethod.SU_WONHAM);
      automataSynthesizer.setSupervisorReductionFactory(supRed);
      automataSynthesizer.setInternalStateLimit(20000);
      automataSynthesizer.setInternalTransitionLimit(1000000);
      final List<Configuration> configurations = new LinkedList<>();
      final Configuration configWSOEUnsup =
        new Configuration(automataSynthesizer);
      configurations.add(configWSOEUnsup);
      final AbstractCompositionalModelAnalyzer.PreselectingMethodFactory
        preselectingFactory = automataSynthesizer.getPreselectingMethodFactory();
      final CompositionalSelectionHeuristicFactory selectionFactory =
        automataSynthesizer.getSelectionHeuristicFactory();
//      final PreselectingMethod[] preSelectingMethods = new PreselectingMethod[] {
//          AbstractCompositionalModelAnalyzer.MustL
//      };
//      final SelectionHeuristicCreator[] selectingMethods = new SelectionHeuristicCreator[] {
//        CompositionalSelectionHeuristicFactory.MinSync
//      };
      int methodCount = 0;
      for (final AbstractCompositionalModelAnalyzer.PreselectingMethod
           preselectingMethod : preselectingFactory.getEnumConstants()) {
        for (final SelectionHeuristicCreator
             selectingMethod: selectionFactory.getEnumConstants()) {
//      for (final AbstractCompositionalModelAnalyzer.PreselectingMethod
//           preselectingMethod : preSelectingMethods) {
//        for (final SelectionHeuristicCreator
//             selectingMethod: selectingMethods) {
          methodCount++;
          for (final Configuration config: configurations) {
            final String preName = preselectingMethod.toString();
            final String selName = selectingMethod.toString();
            final String with = automataSynthesizer.getSupervisorReductionFactory().
              isSupervisedReductionEnabled() ? " with" : " without";
            System.out.println
              ("Method " + methodCount + " *** " + config + "/" + preName +
               "/" + selName + with + " reduction" + " ***");
            final ModularAndCompositionalSynthesizerExperiments experiment =
              new ModularAndCompositionalSynthesizerExperiments
                (methodCount + "_" + config + "_" + preName + "_" +  selName +
                 "_NR.csv", automataSynthesizer, preselectingMethod, selectingMethod);
            experiment.setUp();
            experiment.runAllTests();
            experiment.tearDown();
            // with supervisor reduction:
            //          System.out.println("Method " + methodCount + " *** " + preName + "/" + selName + " with reduction" + " ***");
            //          experiment =
            //            new CompositionalSynthesizerExperiments
            //              (methodCount + "_" + preName + "_" +  selName + "_R.csv",
            //               preselectingMethod, selectingMethod);
            //          experiment.setSupervisorReductionEnabled(true);
            //          experiment.setUp();
            //          experiment.runAllTests();
            //          experiment.tearDown();
          }
        }
      }
    } catch (final Throwable exception) {
      System.err.println("FATAL ERROR");
      exception.printStackTrace(System.err);
    }
  }

  //#########################################################################
  //# Inner class
  private static class Configuration
  {
    private Configuration(final ModularAndCompositionalSynthesizer synthesizer)
    {
      mSynthesizer = synthesizer;
    }

    @Override
    public String toString()
    {
      String name = ProxyTools.getShortClassName(mSynthesizer);
      if (name.startsWith("Compositional")) {
        name = name.substring(13);
      }
      if (name.endsWith("Synthesizer")) {
        name = name.substring(0,name.length()-11);
      }
      return name;
    }

    //#######################################################################
    //# Data members
    private final ModularAndCompositionalSynthesizer mSynthesizer;
  }

}
