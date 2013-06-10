//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   CompositionalSynthesizerExperiments
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * This class runs experiments using the {@link EFSMConflictChecker} with
 * a variety of configurations. The heuristics for choosing candidates can
 * be varied, as well as the abstraction rules applied and their order.
 *
 * @author Sahar Mohajerani
 */

public class EFSMConflictCheckerExperiments
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Constructor
  public EFSMConflictCheckerExperiments(final String statsFilename)
    throws FileNotFoundException
  {
    this(statsFilename, null);
  }

  public EFSMConflictCheckerExperiments
    (final String statsFilename,
     final CompositionSelectionHeuristic compositionSelectionHeuristic)
    throws FileNotFoundException
  {
    final String outputprop = System.getProperty("waters.test.outputdir");
    final File dir = new File(outputprop);
    ensureDirectoryExists(dir);
    final File statsFile = new File(dir, statsFilename);
    mOut = new FileOutputStream(statsFile);
    mPrintWriter = null;
    mCompositionSelectionHeuristic = compositionSelectionHeuristic;
    mModuleProxyFactory = ModuleElementFactory.getInstance();
    mConflictChecker = new EFSMConflictChecker(mModuleProxyFactory);
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    mPrintWriter = new PrintWriter(mOut, true);
    // final int internalStateLimit = 5000;
    // mConflictChecker.setInternalStateLimit(internalStateLimit);
    final int internalTransitionLimit = 1000000;
    mConflictChecker.setInternalTransitionLimit(internalTransitionLimit);
    // final int finalStateLimit = 2000000;
    // mConflictChecker.setMonolithicStateLimit(finalStateLimit);
    if (mCompositionSelectionHeuristic != null) {
      mConflictChecker.setCompositionSelectionHeuristic(mCompositionSelectionHeuristic);
    }
    mPrintWriter.println("InternalTransitionLimit," +
                         internalTransitionLimit);
    mPrintWriter.println("CompositionSelectionHeuristic," +
                         mCompositionSelectionHeuristic);
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
  EFSMConflictChecker getConflictChecker()
  {
    return mConflictChecker;
  }


  //#########################################################################
  //# Configuration


  //#########################################################################
  //# Invocation
  void runModel(final String group,
                final String name)
  throws Exception
  {
    runModel(group, null, name, null);
  }

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
    printAndLog("Running " + name + " with " +
                mCompositionSelectionHeuristic + " ...");
    final String inputprop = System.getProperty("waters.test.inputdir");
    final File inputRoot = new File(inputprop);
    final File rootdir = new File(inputRoot, "waters");
    File dir = new File(rootdir, group);
    if (subdir != null) {
      dir = new File(dir, subdir);
    }
    final File filename = new File(dir, name);
    final ModuleProxy module = getModule(filename);
    mConflictChecker.setModel(module);
    mConflictChecker.setBindings(bindings);
    try {
      mConflictChecker.run();
    } catch (final AnalysisException exception) {
      mPrintWriter.println(name + "," + exception.getMessage());
    } finally {
      final EFSMConflictCheckerAnalysisResult stats =
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


  public ModuleProxy getModule(final File file)
    throws WatersUnmarshalException, IOException
  {
    final DocumentManager manager = getDocumentManager();
    return (ModuleProxy) manager.load(file);
  }


  //#########################################################################
  //# Main Method
  public static void main(final String[] args)
  {
    if (args.length == 1) {
      try {
        final String filename = args[0];
        final EFSMConflictCheckerExperiments experiment =
          new EFSMConflictCheckerExperiments(filename);
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
         "<outputFilename>");
    }
  }


  //#########################################################################
  //# Invocation
  void runAllTests() throws Exception
  {
    efsmConflictChecker_sieve6();
  }


  //#########################################################################
  //# Models
  // Central locking
  private void efsmConflictChecker_sieve6() throws Exception
  {
    runModel("efa", "prime_sieve6.wmod");
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
    runModel("handwritten", null, "transferline_uncont.wmod", bindings);
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
  private final ModuleProxyFactory mModuleProxyFactory;
  private EFSMConflictChecker mConflictChecker;
  private final FileOutputStream mOut;
  private PrintWriter mPrintWriter;
  private boolean mHasBeenPrinted;

  private final CompositionSelectionHeuristic mCompositionSelectionHeuristic;
}
