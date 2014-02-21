//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   CompositionalSynthesizerExperiments
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * This class runs experiments using the {@link CompositionalAutomataSynthesizer} with
 * a variety of configurations. The heuristics for choosing candidates can
 * be varied, as well as the abstraction rules applied and their order.
 *
 * @author Sahar Mohajerani
 */

public class EnabledEventsCompositionalConflictCheckerExperiments
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Constructor
  public EnabledEventsCompositionalConflictCheckerExperiments(final String statsFilename)
    throws FileNotFoundException
  {
    this(statsFilename, EnabledEventsCompositionalConflictChecker.MustL, null);
  }

  public EnabledEventsCompositionalConflictCheckerExperiments
    (final String statsFilename,
     final AbstractCompositionalModelAnalyzer.PreselectingMethod preselectingHeuristic,
     final SelectionHeuristicCreator selectingHeuristic)
    throws FileNotFoundException
  {
    final String outputprop = System.getProperty("waters.test.outputdir");
    final File dir = new File(outputprop);
    ensureDirectoryExists(dir);
    final File statsFile = new File(dir, statsFilename);
    mOut = new FileOutputStream(statsFile);
    mPrintWriter = null;
    mPreselecting = preselectingHeuristic;
    mSelecting = selectingHeuristic;
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mConflictChecker = new EnabledEventsCompositionalConflictChecker(factory);
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    mPrintWriter = new PrintWriter(mOut, true);
    final int internalStateLimit = 5000;      //5000
    mConflictChecker.setInternalStateLimit(internalStateLimit);
    final int internalTransitionLimit = 1000000;  //1000000
    mConflictChecker.setInternalTransitionLimit(internalTransitionLimit);
    final int finalStateLimit = 2000000;   //2000000
    mConflictChecker.setMonolithicStateLimit(finalStateLimit);
    //For the big ones
    mConflictChecker.setMonolithicTransitionLimit(0);
    mConflictChecker.setPreselectingMethod(mPreselecting);
    mConflictChecker.setSelectionHeuristic(mSelecting);
    mConflictChecker.setUsingSpecialEvents(true);
    mPrintWriter.println("InternalStateLimit," + internalStateLimit +
                         ",InternalTransitionLimit," +
                         internalTransitionLimit +
                         ",FinalStateLimit," + finalStateLimit);
    mPrintWriter.println("PreselHeuristic," + mPreselecting +
                         ",SelecHeuristic," + mSelecting);
    mHasBeenPrinted = false;
  }

  @Override
  protected void tearDown() throws Exception
  {
    mConflictChecker = null;
    mPrintWriter.close();
    mOut.close();
    System.out.println("All experiments complete");
    super.tearDown();
  }


  //#########################################################################
  //# Simple Access
  CompositionalConflictChecker getConflictChecker()
  {
    return mConflictChecker;
  }


  //#########################################################################
  //# Configuration
  void setPreselectingHeuristic(final String name)
  {
    final AbstractCompositionalModelAnalyzer.PreselectingMethodFactory factory =
      mConflictChecker.getPreselectingMethodFactory();
    mPreselecting = factory.getEnumValue(name);

  }

  /**
   * Sets the selecting heuristic with the given name
   */
  void setSelectingHeuristic(final String name)
  {
    final CompositionalSelectionHeuristicFactory factory =
      mConflictChecker.getSelectionHeuristicFactory();
    mSelecting = factory.getEnumValue(name);
  }


  //#########################################################################
  //# Invocation
  void runModel(final String group,
                final String subdir,
                final String name,
                final boolean trueResult)
  throws Exception
  {
    runModel(group, subdir, name, trueResult, null);
  }

  void runModel(final String group,
                final String subdir,
                final String name,
                final boolean trueResult,
                final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    printAndLog("Running " + name + " with " +
                mPreselecting + "/" + mSelecting + " ...");
    final String inputprop = System.getProperty("waters.test.inputdir");
    final File inputRoot = new File(inputprop);
    final File rootdir = new File(inputRoot, "waters");
    File dir = new File(rootdir, group);
    if (subdir != null) {
      dir = new File(dir, subdir);
    }
    final File filename = new File(dir, name);
    final ProductDESProxy des = getCompiledDES(filename, bindings);
    mConflictChecker.setModel(des);
    mConflictChecker.setAbstractionProcedureFactory
      (ConflictAbstractionProcedureFactory.EESNB);
    try {
      final boolean result = mConflictChecker.run();

      if(result != trueResult)
        System.err.println(name+ " failed to give correct result");  //error message to test if rule working correctly

    } catch (final AnalysisException exception) {
      mPrintWriter.println(name + "," + exception.getMessage());
    } finally {
      final CompositionalAnalysisResult stats =
        mConflictChecker.getAnalysisResult();
      if (!mHasBeenPrinted) {
        mHasBeenPrinted = true;
        mPrintWriter.print("Model,");
        stats.printCSVHorizontalHeadings(mPrintWriter);
        mPrintWriter.println();
      }
      mPrintWriter.print(name);
      mPrintWriter.print(',');
      stats.printCSVHorizontal(mPrintWriter);
      mPrintWriter.println();
    }
  }


  //#########################################################################
  //# Main Method
  public static void main(final String[] args)
  {
    if (args.length == 3) {
      try {
        final String filename = args[0];
        final String preselectingHeuristic = args[1];
        final String selectingHeuristic = args[2];
        final EnabledEventsCompositionalConflictCheckerExperiments experiment =
          new EnabledEventsCompositionalConflictCheckerExperiments(filename);
        experiment.setPreselectingHeuristic(preselectingHeuristic);
        experiment.setSelectingHeuristic(selectingHeuristic);
        experiment.setUp();
        experiment.runAllTests();
        experiment.tearDown();
      } catch (final Throwable exception) {
        System.err.println("FATAL ERROR");
        exception.printStackTrace(System.err);
      }
    } else {
      System.err.println
        ("Usage: CompositionalSynthesizerExperiments " +
         "<outputFilename> <preselectingHeuristic> <selectingHeuristic>");
    }
  }


  //#########################################################################
  //# Invocation
  void runAllTests() throws Exception
  {

//  For general testing
    testOnlySelfLoop01();
    testFailedTrafficLights();
    testAGV();
    testAGVB();
    testAip0Alps();
    synthesissRhoneSubPatch0();
    testFenCaiWon09();
    testFenCaiWon09b();
    synthesissFms2003();
    testFischertechnik();
   // synthesiseIPC();
    synthesiseCentralLockingKoordwspBlock();
    testTbedCTCT();
    testVerriegel3b();
    testBigBmw();
    testFischertechnik();
    testProfisafeI4();
    testProfisafeO4();
    testTbedUncont();
    testTbedValid();
    testTbedNoderail();
    testTbedNoderailBlock();
    testVerriegel4();
    testVerriegel4B();
    testProfisafeI4Host();
    testProfisafeO4Host();
    testProfisafeO4Slave();
    testProfisafeI5Host();
    testProfisafeO5Host();
    testProfisafeI6Host();
    testProfisafeO6Host();
    testTip3();
   // testTip3Bad();

 /*   //The big ones
    testAGV();
    testAGVB();
    testAip0Aip();
    testAip0Alps();
    testAip0Tough();
    testSongAip(3,true);
    testSongAip(16,false);
    testSongAip(24,false);
    testBigBmw();
    testFenCaiWon09();
    testFenCaiWon09b();
    testFtechnik();
    testProfisafe_i4();
    testProfisafe_i5();
    testProfisafe_i6();
    //testProfisafe_o4();
    //testProfisafe_o5();
    //testProfisafe_o6();
    testTbed_ctct();
    testTbed_hisc();
    testTbed_valid();
    testTip3();
    testTip3_bad();
    testVerriegel3();
    testVerriegel3b();
    testVerriegel4();
    testVerriegel4b();
    test6linka();
    test6linki();
    test6linkp();
    test6linkre();*/
  }


  //#########################################################################
  //# Models




  // #########################################################################
  // # Test Cases --- incremental suite



  public void testOnlySelfLoop01() throws Exception
  {
    final String group = "tests";
    final String dir = "nasty";
    final String name = "onlySelfLoop01.wmod";
    runModel(group, dir, name, true);

  }

  //tiny
  public void testFailedTrafficLights() throws Exception
  {
    final String group = "tests";
    final String dir = "trafficlights2006";
    final String name = "sdh7.wmod";
    runModel(group, dir, name, false);

  }


  public void testFischertechnik() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "ftechnik.wmod";
    runModel(group, dir, name, false);
  }

  public void testFZelle() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "fzelle.wmod";
    runModel(group, dir, name, true);
  }

  public void testProfisafeI4() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4.wmod";
    runModel(group, dir, name, true);
  }

  public void testProfisafeO4() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o4.wmod";
    runModel(group, dir, name, true);
  }





  public void testTbedCTCT() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_ctct.wmod";
    runModel(group, dir, name, false);
  }

  public void testTbedUncont() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_uncont.wmod";
    runModel(group, dir, name, true);
  }

  public void testTbedValid() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_valid.wmod";
    runModel(group, dir, name, true);
  }

  public void testTbedNoderail() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_noderail.wmod";
    runModel(group, dir, name, true);
  }

  public void testTbedNoderailBlock() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_noderail_block.wmod";
    runModel(group, dir, name, false);
  }

  public void testVerriegel4B() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "verriegel4b.wmod";
    runModel(group, dir, name, false);
  }

  // #########################################################################
  // # Test Cases --- profisafe

  public void testProfisafeI4Host() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4_host.wmod";
    runModel(group, dir, name, true);
  }

  public void testProfisafeO4Host() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o4_host.wmod";
    runModel(group, dir, name, true);
  }

  public void testProfisafeO4Slave() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o4_slave.wmod";
    runModel(group, dir, name, true);
  }

  public void testProfisafeI5Host() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i5_host.wmod";
    runModel(group, dir, name, true);
  }

  public void testProfisafeO5Host() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o5_host.wmod";
    runModel(group, dir, name, true);
  }

  public void testProfisafeI6Host() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i6_host.wmod";
    runModel(group, dir, name, true);
  }

  public void testProfisafeO6Host() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o6_host.wmod";
    runModel(group, dir, name, true);
  }

  //#########################################################################
  // Central locking
  private void synthesiseCentralLockingKoordwspBlock() throws Exception
  {
    runModel("valid", "central_locking", "koordwsp_block.wmod",false);
  }

  private void synthesissRhoneSubPatch0() throws Exception
  {
    runModel("tests", "hisc", "rhone_subsystem1_patch0.wmod",false);
  }

  private void synthesissFms2003() throws Exception
  {
    runModel("tests", "fms2003", "fms2003_synth1.wmod",false);
  }


  /////////////////////////////////////////////////////////
  //The big ones
  private void testAGV() throws Exception
  {
    runModel("tests", "incremental_suite", "agv.wmod",true);
  }

  private void testAGVB() throws Exception
  {
    runModel("tests", "incremental_suite", "agvb.wmod",false);
  }

  @SuppressWarnings("unused")
  private void testAip0Aip() throws Exception
  {
    runModel("tests", "incremental_suite", "aip0aip.wmod",true);
  }

  private void testAip0Alps() throws Exception
  {
    runModel("tests", "incremental_suite", "aip0alps.wmod",false);
  }

  @SuppressWarnings("unused")
  private void testAip0Tough() throws Exception
  {
    runModel("tests", "incremental_suite", "aip0tough.wmod",false);
  }

  private void testBigBmw() throws Exception
  {
    runModel("tests", "incremental_suite", "big_bmw.wmod",true);
  }

  private void testFenCaiWon09() throws Exception
  {
    runModel("tests", "fencaiwon09", "FenCaiWon09.wmod",true);
  }

  private void testFenCaiWon09b() throws Exception
  {
    runModel("tests", "fencaiwon09", "FenCaiWon09b.wmod",false);
  }

  @SuppressWarnings("unused")
  private void testFtechnik() throws Exception
  {
    runModel("tests", "incremental_suite", "ftechnik.wmod",false);
  }

  @SuppressWarnings("unused")
  private void testProfisafe_i4() throws Exception
  {
    runModel("tests", "profisafe", "profisafe_i4.wmod",true);
  }

  @SuppressWarnings("unused")
  private void testProfisafe_i5() throws Exception
  {
    runModel("tests", "profisafe", "profisafe_i5.wmod",true);
  }

  @SuppressWarnings("unused")
  private void testProfisafe_i6() throws Exception
  {
    runModel("tests", "profisafe", "profisafe_i6.wmod",true);
  }

  @SuppressWarnings("unused")
  private void testProfisafe_o4() throws Exception
  {
    runModel("tests", "profisafe", "profisafe_o4.wmod",true);
  }

  @SuppressWarnings("unused")
  private void testProfisafe_o5() throws Exception
  {
    runModel("tests", "profisafe", "profisafe_o5.wmod",true);
  }
  @SuppressWarnings("unused")
  private void testProfisafe_o6() throws Exception
  {
    runModel("tests", "profisafe", "profisafe_o6.wmod",true);
  }

  @SuppressWarnings("unused")
  private void testTbed_ctct() throws Exception
  {
    runModel("tests", "incremental_suite", "tbed_ctct.wmod",false);
  }

  @SuppressWarnings("unused")
  private void testTbed_hisc() throws Exception
  {
    runModel("despot", "tbed_hisc", "tbed_hisc.wmod",true);
  }

  @SuppressWarnings("unused")
  private void testTbed_valid() throws Exception
  {
    runModel("tests", "incremental_suite", "tbed_valid.wmod",true);
  }

  private void testTip3() throws Exception
  {
    runModel("tip", "acsw2006", "tip3.wmod",true);
  }

  @SuppressWarnings("unused")
  private void testTip3_bad() throws Exception
  {
    runModel("tip", "acsw2006", "tip3_bad.wmod",false);
  }

  @SuppressWarnings("unused")
  private void testVerriegel3() throws Exception
  {
    runModel("valid", "central_locking", "verriegel3.wmod",true);
  }

  private void testVerriegel3b() throws Exception
  {
    runModel("valid", "central_locking", "verriegel3b.wmod",false);
  }

  private void testVerriegel4() throws Exception
  {
    runModel("tests", "incremental_suite", "verriegel4.wmod",true);
  }

  @SuppressWarnings("unused")
  private void testVerriegel4b() throws Exception
  {
    runModel("tests", "incremental_suite", "verriegel4b.wmod",false);
  }

  @SuppressWarnings("unused")
  private void test6linka() throws Exception
  {
    runModel("tests", "6link", "6linka.wmod",false);
  }

  @SuppressWarnings("unused")
  private void test6linki() throws Exception
  {
    runModel("tests", "6link", "6linki.wmod",false);
  }

  @SuppressWarnings("unused")
  private void test6linkp() throws Exception
  {
    runModel("tests", "6link", "6linkp.wmod",false);
  }

  @SuppressWarnings("unused")
  private void test6linkre() throws Exception
  {
    runModel("tests", "6link", "6linkre.wmod",false);
  }

  @SuppressWarnings("unused")
  private void synthesisTransferline(final int n) throws Exception
  {
    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
    final IntConstantProxy expr = factory.createIntConstantProxy(n);
    final ParameterBindingProxy binding =
      factory.createParameterBindingProxy("N", expr);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    final long start = System.currentTimeMillis();
    runModel("handwritten", null, "transferline_uncont1.wmod",false, bindings);
    final long stop = System.currentTimeMillis();
    @SuppressWarnings("resource")
    final Formatter formatter = new Formatter(System.out);
    final float difftime = 0.001f * (stop - start);
    formatter.format("%.3f s\n", difftime);
  }

  @SuppressWarnings("unused")
  private void testSongAip(final int n, final boolean expected) throws Exception
  {
    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
    final IntConstantProxy expr = factory.createIntConstantProxy(n);
    final ParameterBindingProxy binding =
      factory.createParameterBindingProxy("N", expr);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    final long start = System.currentTimeMillis();
    runModel("despot", "song_aip/maip3_syn", "aip1efa.wmod",expected, bindings);
    final long stop = System.currentTimeMillis();
    @SuppressWarnings("resource")
    final Formatter formatter = new Formatter(System.out);
    final float difftime = 0.001f * (stop - start);
    formatter.format("%.3f s\n", difftime);
  }


  //#########################################################################
  //# Logging
  private void printAndLog(final String msg)
  {
    System.out.println(msg);
    getLogger().info(msg);
  }


  //#########################################################################
  //# Data Members
  private CompositionalConflictChecker mConflictChecker;
  private final FileOutputStream mOut;
  private PrintWriter mPrintWriter;
  private boolean mHasBeenPrinted;

  private AbstractCompositionalModelAnalyzer.PreselectingMethod
    mPreselecting;
  private SelectionHeuristicCreator mSelecting;

}
