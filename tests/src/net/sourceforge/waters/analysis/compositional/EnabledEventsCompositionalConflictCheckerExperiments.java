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
 * This class runs experiments using the {@link CompositionalSynthesizer} with
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
    this(statsFilename, EnabledEventsCompositionalConflictChecker.MustLE, null);
  }

  public EnabledEventsCompositionalConflictCheckerExperiments
    (final String statsFilename,
     final AbstractCompositionalModelAnalyzer.PreselectingMethod preselectingHeuristic,
     final AbstractCompositionalModelAnalyzer.SelectingMethod selectingHeuristic)
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
    final int internalStateLimit = 5000;
    mConflictChecker.setInternalStateLimit(internalStateLimit);
    final int internalTransitionLimit = 1000000;
    mConflictChecker.setInternalTransitionLimit(internalTransitionLimit);
    final int finalStateLimit = 2000000;
    mConflictChecker.setMonolithicStateLimit(finalStateLimit);
    mConflictChecker.setPreselectingMethod(mPreselecting);
    mConflictChecker.setSelectingMethod(mSelecting);
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
    final AbstractCompositionalModelAnalyzer.SelectingMethodFactory factory =
      mConflictChecker.getSelectingMethodFactory();
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
      (ConflictAbstractionProcedureFactory.EENB);
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


    /*
    synthesisTransferline(100);
    synthesisTransferline(200);
    synthesisTransferline(300);
    */


    synthesiseTbedNoderailB();                                //
    synthesisAGV();
    synthesisAGVB();
    synthesissAip0Alps();
    synthesissRhoneSubPatch0();
    synthesisFenCaiWon09B();
    synthesisFenCaiWon09Synth();//
    synthesissFms2003();
    synthesiseFischertechnik();
    synthesiseIPC();
    synthesiseCentralLockingKoordwspBlock();
    synthesiseTbedCtct();     //
    synthesiseTbedNoderailUncont();
    synthesiseTbedNoderailB();
    synthesiseCentralLockingVerriegel3b();//




    testBigBmw();
    testFischertechnik();
    testFZelle();
    testProfisafeI4();
    testProfisafeO4();
    //testRhoneAlps();      //failed to find file
    //testRhoneTough();     //Failed to find file
    testTbedCTCT();
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
    testTip3();                 //This file is giving errors.
    testTip3Bad();
  }


  //#########################################################################
  //# Models




  // #########################################################################
  // # Test Cases --- incremental suite
  public void testBigBmw() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "big_bmw.wmod";
    runModel(group, dir, name, true);
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

  public void testVerriegel4() throws Exception
  {
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "verriegel4.wmod";
    runModel(group, dir, name, true);
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
  /*public void testProfisafeI3HostEFA() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_ihost_efa.wmod";
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", 3);
    final List<ParameterBindingProxy> bindings =
        Collections.singletonList(binding);
    runModel(group, dir, name, bindings, true);
  }*/

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

  // #########################################################################
  // # Test Cases -- Tip
  public void testTip3() throws Exception
  {
    final String group = "tip";
    final String dir = "acsw2006";
    final String name = "tip3.wmod";
    runModel(group, dir, name, true);
  }

  public void testTip3Bad() throws Exception
  {
    final String group = "tip";
    final String dir = "acsw2006";
    final String name = "tip3_bad.wmod";
    runModel(group, dir, name, false);
  }



  // Central locking
  private void synthesiseCentralLockingKoordwspBlock() throws Exception
  {
    runModel("valid", "central_locking", "koordwsp_block.wmod",false);
  }


  private void synthesiseCentralLockingVerriegel3b() throws Exception
  {
    runModel("valid", "central_locking", "verriegel3b.wmod",false);
  }



  // AIP
  private void synthesissAip0Alps() throws Exception
  {
    runModel("tests", "incremental_suite", "aip0alps.wmod",false);
  }

  @SuppressWarnings("unused")
  private void synthesiseAip0Aip() throws Exception
  {
    runModel("tests", "incremental_suite", "aip0aip.wmod",false);
  }


  private void synthesisFenCaiWon09Synth() throws Exception
  {
    runModel("tests", "fencaiwon09", "FenCaiWon09_synth.wmod",false);
  }

  // Train testbed
  private void synthesiseTbedNoderailUncont() throws Exception
  {
    runModel("tests", "incremental_suite", "tbed_uncont.wmod",true);
  }

  private void synthesiseTbedNoderailB() throws Exception
  {
    runModel("tests", "incremental_suite", "tbed_noderail_block.wmod",false);
  }


  private void synthesiseTbedCtct() throws Exception
  {
    runModel("tests", "incremental_suite", "tbed_ctct.wmod",false);
  }

  //AGV
  private void synthesisAGVB() throws Exception
  {
    runModel("tests", "incremental_suite", "agvb.wmod",false);
  }

  private void synthesisAGV() throws Exception
  {
    runModel("tests", "incremental_suite", "agv.wmod",true);
  }

  //
  private void synthesiseIPC() throws Exception
  {
    runModel("tests", "synthesis", "IPC.wmod",true);
  }

  private void synthesissRhoneSubPatch0() throws Exception
  {
    runModel("tests", "hisc", "rhone_subsystem1_patch0.wmod",false);
  }

  private void synthesissFms2003() throws Exception
  {
    runModel("tests", "fms2003", "fms2003_synth1.wmod",false);
  }

  //flexible production cell
  private void synthesiseFischertechnik() throws Exception
  {
    runModel("tests", "incremental_suite", "ftechnik.wmod",false);
  }

  @SuppressWarnings("unused")
  private void synthesisTip3Bad() throws Exception
  {
    runModel("tip", "acsw2006", "tip3_bad.wmod",false);
  }

  private void synthesisFenCaiWon09B() throws Exception
  {
    runModel("tests", "fencaiwon09", "FenCaiWon09b.wmod",false);
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
    runModel("handwritten", null, "transferline_uncont.wmod",false, bindings);
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
  private AbstractCompositionalModelAnalyzer.SelectingMethod mSelecting;
}
