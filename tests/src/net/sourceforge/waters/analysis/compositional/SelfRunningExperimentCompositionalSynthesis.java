//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   SelfRunningExperimentCompositionalSynthesis
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyTools;
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
      final AbstractCompositionalSynthesizer automataSynthesizer =
        new CompositionalAutomataSynthesizer(factory);
      final List<Configuration> configurations = new LinkedList<>();
      final Configuration configWSOE =
        new Configuration(automataSynthesizer,
                          AutomataSynthesisAbstractionProcedureFactory.WSOE);
      configurations.add(configWSOE);
//      final Configuration configWSOEUnsup =
//        new Configuration(automataSynthesizer,
//                          AutomataSynthesisAbstractionProcedureFactory.WSOE_UNSUP);
//      configurations.add(configWSOEUnsup);
      final AbstractCompositionalSynthesizer stateRepresentationSynthesizer =
        new CompositionalStateRepresentationSynthesizer(factory);
      final Configuration configStateRepresent =
        new Configuration(stateRepresentationSynthesizer,
                          StateRepresentationSynthesisAbstractionProcedureFactory.WSOE);
      configurations.add(configStateRepresent);
      final AbstractCompositionalModelAnalyzer.PreselectingMethodFactory
      preselectingFactory = automataSynthesizer.getPreselectingMethodFactory();
      final CompositionalSelectionHeuristicFactory selectionFactory =
        automataSynthesizer.getSelectionHeuristicFactory();
      int methodCount = 0;
      for (final AbstractCompositionalModelAnalyzer.PreselectingMethod
           preselectingMethod : preselectingFactory.getEnumConstants()) {
        for (final SelectionHeuristicCreator
             selectingMethod: selectionFactory.getEnumConstants()) {
          methodCount++;
          for (final Configuration config: configurations) {
            final AbstractCompositionalSynthesizer synthesizer = config.getSynthesizer();
            final AbstractionProcedureCreator method = config.getMethod();
            final String preName = preselectingMethod.toString();
            final String selName = selectingMethod.toString();
            // without supervisor reduction:
            System.out.println
              ("Method " + methodCount + " *** " + config + "/" + preName +
               "/" + selName + " without reduction" + " ***");
            final CompositionalSynthesizerExperiments experiment =
              new CompositionalSynthesizerExperiments
                (methodCount + "_" + config + "_" + preName + "_" +  selName +
                 "_NR.csv",
                 synthesizer, method, preselectingMethod, selectingMethod);
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
    private Configuration(final AbstractCompositionalSynthesizer synthesizer,
                  final AbstractionProcedureCreator factory)
    {
      mSynthesizer = synthesizer;
      mFactory = factory;
    }

    private AbstractCompositionalSynthesizer getSynthesizer()
    {
      return mSynthesizer;
    }

    private AbstractionProcedureCreator getMethod()
    {
      return mFactory;
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
      name += "_" + mFactory.toString();
      return name;
    }

    //#######################################################################
    //# Data members
    private final AbstractCompositionalSynthesizer mSynthesizer;
    private final AbstractionProcedureCreator mFactory;
  }

}
