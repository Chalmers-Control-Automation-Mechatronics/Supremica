//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   CompositionalSynthesizerExperiments
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * This class runs experiments using the {@link CompositionalSynthesizer} with
 * a variety of configurations. The heuristics for choosing candidates can
 * be varied, as well as the abstraction rules applied and their order.
 *
 * @author Sahar Mohajerani
 */
public class CompositionalSynthesizerExperiments
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Constructor
  public CompositionalSynthesizerExperiments(final String statsFilename)
    throws FileNotFoundException
  {
    this(statsFilename, null, null);
  }

  public CompositionalSynthesizerExperiments
    (final String statsFilename,
     final AbstractCompositionalModelAnalyzer.PreselectingMethod preselectingHeuristic,
     final AbstractCompositionalModelAnalyzer.SelectingMethod selectingHeuristic)
    throws FileNotFoundException
  {
    mOut = new FileOutputStream(statsFilename);
    mPrintWriter = null;
    mPreselecting = preselectingHeuristic;
    mSelecting = selectingHeuristic;
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mSynthesizer = new CompositionalSynthesizer(factory);
    mPrintWriter = new PrintWriter(mOut, true);
    final int internalStateLimit = 5000;
    mSynthesizer.setInternalStateLimit(internalStateLimit);
    final int internalTransitionLimit = 1000000;
    mSynthesizer.setInternalTransitionLimit(internalTransitionLimit);
    final int finalStateLimit = 2000000;
    mSynthesizer.setMonolithicStateLimit(finalStateLimit);
    final int finalTransitionLimit = 0;
    mSynthesizer.setMonolithicTransitionLimit(finalTransitionLimit);
    mSynthesizer.setPreselectingMethod(mPreselecting);
    mSynthesizer.setSelectingMethod(mSelecting);
    mPrintWriter.println("InternalStateLimit," + internalStateLimit +
                         ",InternalTransitionLimit," +
                         internalTransitionLimit +
                         ",FinalStateLimit," + finalStateLimit +
                         ",FinalTransitionLimit," + finalTransitionLimit);
    mPrintWriter.println("PreselHeuristic," + mPreselecting +
                         ",SelecHeuristic," + mSelecting);

    // TODO This may fail to print all headings. May have to print the
    // headings from the analysis result obtained from the first test.
    final CompositionalSynthesisResult stats =
      new CompositionalSynthesisResult();
    mPrintWriter.print("Model,");
    stats.printCSVHorizontalHeadings(mPrintWriter);
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
  CompositionalSynthesizer getSynthesizer()
  {
    return mSynthesizer;
  }


  //#########################################################################
  //# Configuration
  void setPreselectingHeuristic(final String name)
  {
    // TODO Do this like setSelectingHeuristic()
    /*
    if (mPreselecting.equals("mint")) {
      mSynthesizer.setPreselectingHeuristic(mSynthesizer
        .createHeuristicMinT());
    } else if (mPreselecting.equals("maxs")) {
      mSynthesizer.setPreselectingHeuristic(mSynthesizer
        .createHeuristicMaxS());
    } else if (mPreselecting.equals("mustl")) {
      mSynthesizer.setPreselectingHeuristic(mSynthesizer
        .createHeuristicMustL());
    } else {
      System.err
        .println("Error: Preselecting Heuristic not specified correctly, it must be one of: mint, maxs, mustl");
    }
    */
  }

  /**
   * Sets the selecting heuristic with the given name
   */
  void setSelectingHeuristic(final String name)
  {
    final AbstractCompositionalModelAnalyzer.SelectingMethodFactory factory =
      mSynthesizer.getSelectingMethodFactory();
    mSelecting = factory.getEnumValue(name);
  }


  //#########################################################################
  //# Invocation
  void runModel(final String group, final String subdir, final String name)
  throws Exception
  {
    System.out.println("Running " + name + " ...");
    final String inputprop = System.getProperty("waters.test.inputdir");
    final File inputRoot = new File(inputprop);
    final File rootdir = new File(inputRoot, "waters");
    final File groupdir = new File(rootdir, group);
    final File dir = new File(groupdir, subdir);
    final File filename = new File(dir, name);
    final ProductDESProxy des = getCompiledDES(filename, null);
    mSynthesizer.setModel(des);
    try {
      mSynthesizer.run();
    } catch (final Exception e) {
      System.out.print(e.getMessage());
      mPrintWriter.println(name + "," + e.getMessage());
    } finally {
      final CompositionalSynthesisResult stats =
        (CompositionalSynthesisResult) mSynthesizer.getAnalysisResult();
      mPrintWriter.print(name);
      mPrintWriter.print(',');
      stats.printCSVHorizontal(mPrintWriter);
      mPrintWriter.println();
    }
  }


  //#########################################################################
  //# Data Members
  private CompositionalSynthesizer mSynthesizer;
  private final FileOutputStream mOut;
  private PrintWriter mPrintWriter;

  private final AbstractCompositionalModelAnalyzer.PreselectingMethod
    mPreselecting;
  private AbstractCompositionalModelAnalyzer.SelectingMethod mSelecting;
}
