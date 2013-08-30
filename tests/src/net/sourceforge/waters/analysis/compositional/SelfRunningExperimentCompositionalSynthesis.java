//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   SelfRunningExperimentCompositionalSynthesis
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


/**
 * This class can be used to automatically run experiments for different
 * properties with all possible combinations of heuristics.
 *
 * @author Sahar Mohajerani
 */

public class SelfRunningExperimentCompositionalSynthesis
{

  public static void main(final String[] args)
  {
    try {
      //final String outputDir = System.getProperty("waters.test.outputdir");
      final ProductDESProxyFactory factory =
        ProductDESElementFactory.getInstance();
      final CompositionalAutomataSynthesizer synthesizer =
        new CompositionalAutomataSynthesizer(factory);
      final AbstractCompositionalModelAnalyzer.PreselectingMethodFactory
      preselectingFactory = synthesizer.getPreselectingMethodFactory();
      final AbstractCompositionalModelAnalyzer.SelectingMethodFactory
      selectingFactory = synthesizer.getSelectingMethodFactory();
      int methodCount = 0;
      final AutomataSynthesisAbstractionProcedureFactory[] methods = {
         AutomataSynthesisAbstractionProcedureFactory.WSOE,
         AutomataSynthesisAbstractionProcedureFactory.WSOE_UNSUP
      };
      for (final AbstractCompositionalModelAnalyzer.PreselectingMethod
           preselectingMethod : preselectingFactory.getEnumConstants()) {
        for (final AbstractCompositionalModelAnalyzer.SelectingMethod
             selectingMethod: selectingFactory.getEnumConstants()) {
          methodCount++;
          for (final AutomataSynthesisAbstractionProcedureFactory method: methods) {
            final String preName = preselectingMethod.toString();
            final String selName = selectingMethod.toString();
            // without supervisor reduction:
            System.out.println
              ("Method " + methodCount + " *** " + preName + "/" + selName +
               "/" + method + " without reduction" + " ***");
            final CompositionalSynthesizerExperiments experiment =
              new CompositionalSynthesizerExperiments
              (methodCount + "_" + preName + "_" +  selName + "_" + method +"_NR.csv",
               method, preselectingMethod, selectingMethod);
            experiment.setSupervisorReductionEnabled(false);
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
}
