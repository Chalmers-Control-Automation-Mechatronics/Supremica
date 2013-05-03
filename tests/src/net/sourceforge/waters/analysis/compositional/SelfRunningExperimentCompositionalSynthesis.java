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
      final CompositionalSynthesizer synthesizer =
        new CompositionalSynthesizer(factory);
      final AbstractCompositionalModelAnalyzer.PreselectingMethodFactory
      preselectingFactory = synthesizer.getPreselectingMethodFactory();
      final AbstractCompositionalModelAnalyzer.SelectingMethodFactory
      selectingFactory = synthesizer.getSelectingMethodFactory();
      for (final AbstractCompositionalModelAnalyzer.PreselectingMethod
           preselectingMethod : preselectingFactory.getEnumConstants()) {
        for (final AbstractCompositionalModelAnalyzer.SelectingMethod
             selectingMethod: selectingFactory.getEnumConstants()) {
          final String preName = preselectingMethod.toString();
          final String selName = selectingMethod.toString();
          System.out.println("*** " + preName + "/" + selName + " ***");
          final CompositionalSynthesizerExperiments experiment =
            new CompositionalSynthesizerExperiments
              (preName + "_" +  selName + ".csv",
               preselectingMethod, selectingMethod);
          experiment.setUp();
          experiment.runAllTests();
          experiment.tearDown();
        }
      }
    } catch (final Throwable exception) {
      System.err.println("FATAL ERROR");
      exception.printStackTrace(System.err);
    }
  }
}
