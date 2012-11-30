//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   CompositionalSynthesizerExperiments
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ProductDESResult;
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

public class MonolithicSynthesizerExperiments
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Constructor
  public MonolithicSynthesizerExperiments(final String statsFilename)
    throws FileNotFoundException
  {
    final String outputprop = System.getProperty("waters.test.outputdir");
    final File dir = new File(outputprop);
    ensureDirectoryExists(dir);
    final File statsFile = new File(dir, statsFilename);
    mOut = new FileOutputStream(statsFile);
    mPrintWriter = null;
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mSynthesizer = new MonolithicSynthesizer(factory);
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    mPrintWriter = new PrintWriter(mOut, true);
    final int finalStateLimit = 10000000;
    mSynthesizer.setNodeLimit(finalStateLimit);
    mHasBeenPrinted = false;
  }

  @Override
  protected void tearDown() throws Exception
  {
    mSynthesizer = null;
    mPrintWriter.close();
    mOut.close();
    System.out.println("All experiments complete");
    super.tearDown();
  }


  //#########################################################################
  //# Simple Access
  MonolithicSynthesizer getSynthesizer()
  {
    return mSynthesizer;
  }


  //#########################################################################
  //# Configuration



  //#########################################################################
  //# Invocation
  void runModel(final String group,
                final String subdir,
                final String name)
  throws Exception
  {
    runModel(group, subdir, name, null);
  }

  void runModel(final String group,
                final String subdir,
                final String name,
                final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    printAndLog("Running " + name + " ...");
    final String inputprop = System.getProperty("waters.test.inputdir");
    final File inputRoot = new File(inputprop);
    final File rootdir = new File(inputRoot, "waters");
    File dir = new File(rootdir, group);
    if (subdir != null) {
      dir = new File(dir, subdir);
    }
    final File filename = new File(dir, name);
    final ProductDESProxy des = getCompiledDES(filename, bindings);
    mSynthesizer.setModel(des);
    try {
      mSynthesizer.run();
    } catch (final AnalysisException exception) {
      mPrintWriter.println(name + "," + exception.getMessage());
    } finally {
      final ProductDESResult stats = mSynthesizer.getAnalysisResult();
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
    if (args.length == 1) {
      try {
        final String filename = args[0];
        final MonolithicSynthesizerExperiments experiment =
          new MonolithicSynthesizerExperiments(filename);
        experiment.setUp();
        experiment.runAllTests();
        experiment.tearDown();
      } catch (final Throwable exception) {
        System.err.println("FATAL ERROR");
        exception.printStackTrace(System.err);
      }
    } else {
      System.err.println
        ("Usage: MonolithicSynthesizerExperiments " +
         "<outputFilename>");
    }
  }


  //#########################################################################
  //# Invocation
  void runAllTests() throws Exception
  {
    synthesisTransferline(2);
    synthesisTransferline(3);
    synthesiseIPC();
    synthesissRhoneSubPatch0();
    synthesissRhoneSubPatch1();
    synthesisPhilosophers(8);
    synthesisPhilosophers(9);
    synthesisPhilosophers(10);
    synthesisTransferline(4);

//    synthesiseTbedNoderailB();
//    synthesiseCentralLockingKoordwspBlock();
    /*synthesisAGV();
    synthesisAGVB();
    synthesissAip0Alps();
    synthesissRhoneSubPatch0();
//    synthesissAip0Aip();
    synthesisFenCaiWon09B();
//    synthesisFenCaiWon09Synth();
    synthesissFms2003();
    synthesiseFischertechnik();
    synthesiseIPC();
    synthesiseCentralLockingKoordwspBlock();
//    synthesisAip0tough();
//    synthesiseTbedCtct();
    synthesiseTbedNoderailUncont();
    synthesiseTbedNoderailB();*/
//    synthesiseCentralLockingVerriegel3b();
//    synthesiseVerrigel4B();
//    synthesiseFlexibleManufacturingSystem();
//  synthesisLargestCoherent();
  }


  //#########################################################################
  //# Models
  // Central locking
  @SuppressWarnings("unused")
  private void synthesiseCentralLockingKoordwspBlock() throws Exception
  {
    runModel("valid", "central_locking", "koordwsp_block.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesiseCentralLockingVerriegel3b() throws Exception
  {
    runModel("valid", "central_locking", "verriegel3b.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesiseVerrigel4B() throws Exception
  {
    runModel("valid", "central_locking", "verriegel4b.wmod");
  }

  // AIP
  @SuppressWarnings("unused")
  private void synthesissAip0Alps() throws Exception
  {
    runModel("tests", "incremental_suite", "aip0alps.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesiseAip0Aip() throws Exception
  {
    runModel("tests", "incremental_suite", "aip0aip.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesisFenCaiWon09Synth() throws Exception
  {
    runModel("tests", "fencaiwon09", "FenCaiWon09_synth.wmod");
  }

  // Train testbed
  @SuppressWarnings("unused")
  private void synthesiseTbedNoderailUncont() throws Exception
  {
    runModel("tests", "incremental_suite", "tbed_uncont.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesiseTbedNoderailB() throws Exception
  {
    runModel("tests", "incremental_suite", "tbed_noderail_block.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesiseTbedCtct() throws Exception
  {
    runModel("tests", "incremental_suite", "tbed_ctct.wmod");
  }

  //AGV
  @SuppressWarnings("unused")
  private void synthesisAGVB() throws Exception
  {
    runModel("tests", "incremental_suite", "agvb.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesisAGV() throws Exception
  {
    runModel("tests", "incremental_suite", "agv.wmod");
  }

  //
  private void synthesiseIPC() throws Exception
  {
    runModel("tests", "synthesis", "IPC.wmod");
  }

  private void synthesissRhoneSubPatch0() throws Exception
  {
    runModel("tests", "hisc", "rhone_subsystem1_patch0.wmod");
  }

  private void synthesissRhoneSubPatch1() throws Exception
  {
    runModel("tests", "hisc", "rhone_subsystem1_patch1.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesissFms2003() throws Exception
  {
    runModel("tests", "fms2003", "fms2003_synth1.wmod");
  }

  //flexible production cell
  @SuppressWarnings("unused")
  private void synthesiseFischertechnik() throws Exception
  {
    runModel("tests", "incremental_suite", "ftechnik.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesisTip3Bad() throws Exception
  {
    runModel("tip", "acsw2006", "tip3_bad.wmod");
  }

  @SuppressWarnings("unused")
  private void synthesisFenCaiWon09B() throws Exception
  {
    runModel("tests", "fencaiwon09", "FenCaiWon09b.wmod");
  }

  private void synthesisTransferline(final int n) throws Exception
  {
    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
    final IntConstantProxy expr = factory.createIntConstantProxy(n);
    final ParameterBindingProxy binding =
      factory.createParameterBindingProxy("N", expr);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    final long start = System.currentTimeMillis();
    runModel("handwritten", null, "transferline_uncont.wmod", bindings);
    final long stop = System.currentTimeMillis();
    @SuppressWarnings("resource")
    final Formatter formatter = new Formatter(System.out);
    final float difftime = 0.001f * (stop - start);
    formatter.format("%.3f s\n", difftime);
  }

  private void synthesisPhilosophers(final int n) throws Exception
  {
    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
    final IntConstantProxy expr = factory.createIntConstantProxy(n);
    final ParameterBindingProxy binding =
      factory.createParameterBindingProxy("N", expr);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    final long start = System.currentTimeMillis();
    runModel("handwritten", null, "dining_philosophers.wmod", bindings);
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
  private MonolithicSynthesizer mSynthesizer;
  private final FileOutputStream mOut;
  private PrintWriter mPrintWriter;
  private boolean mHasBeenPrinted;
}
