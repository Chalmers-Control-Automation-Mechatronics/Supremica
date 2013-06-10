//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   CompositionalSynthesizerExperiments
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.hisc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.analysis.annotation.ConflictPreorderResult;
import net.sourceforge.waters.analysis.annotation.TRConflictPreorderChecker;
import net.sourceforge.waters.analysis.compositional.CompositionalSimplifier;
import net.sourceforge.waters.analysis.compositional.ConflictAbstractionProcedureFactory;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.EventDeclProxy;


/**
 * This class runs experiments using the {@link TRConflictPreorderChecker}
 * with a variety of applications.
 *
 * @author Robi Malik
 */

public class ConflictPreorderExperiments
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Constructor
  public ConflictPreorderExperiments(final String statsFilename)
    throws FileNotFoundException
  {
    final String outputprop = System.getProperty("waters.test.outputdir");
    final File dir = new File(outputprop);
    ensureDirectoryExists(dir);
    final File statsFile = new File(dir, statsFilename);
    mOut = new FileOutputStream(statsFile);
    mPrintWriter = null;
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    mPrintWriter = new PrintWriter(mOut, true);
    mHasBeenPrinted = false;
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final ConflictChecker checker =
      new NativeConflictChecker(factory);
    final CompositionalSimplifier simplifier =
      new CompositionalSimplifier(factory,
                                  ConflictAbstractionProcedureFactory.NB);
    mHISCCPChecker =
      new HISCCPInterfaceConsistencyChecker(factory, checker, simplifier);
  }

  @Override
  protected void tearDown() throws Exception
  {
    mHISCCPChecker = null;
    mPrintWriter.close();
    mOut.close();
    System.out.println("All experiments complete");
    super.tearDown();
  }


  //#########################################################################
  //# Configuration


  //#########################################################################
  //# Tests
  private void runAllTests() throws Exception
  {
    testHISCCP_aip3syn_as1();

    testDirectPhiloSubsys12();
    testDirectPhiloSubsys123();
    testDirectPhiloSubsys1234();
    testDirectAnn1();
    testDirectAnn2();
    testDirectAnn3();
    testDirectAnn4();
    testDirectAnn5();
    testDirectCP1();
    testDirectCP2();
    testDirectCP3();
    testDirectCP4();
    testDirectCP5();
    testDirectCP6();
    testDirectCP7();
    testDirectCP8();
    testDirectCP9();
    testDirectCP10();
    testDirectSimon();

    testHISCCP_SimpleManufHISCCP1();
    testHISCCP_SimpleManufHISCCP1bad();
    testHISCCP_aip3el12();
    testHISCCP_aip3el12b();
    testHISCCP_aip3el3();
    testHISCCP_aip3el4();
    testHISCCP_rhone_subsystem1_ld();
  }


  // SimpleManufacturingExample
  private void testHISCCP_SimpleManufHISCCP1()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("hisccp", "simple_manuf_multi_hisccp", "subsystem.wmod");
    runHISCCP(des, false, true);
    runHISCCP(des, true, false);
  }

  private void testHISCCP_SimpleManufHISCCP1bad()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("hisccp", "simple_manuf_multi_hisccp",
                     "subsystem_bad.wmod");
    runHISCCP(des, false, false);
    runHISCCP(des, true, false);
  }


  // song_aip
  private void testHISCCP_aip3syn_as1()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("despot", "song_aip", "aip3_syn", "as1.wmod");
    runHISCCP(des, false, false);
    runHISCCP(des, true, false);
  }


  // aip3_hisccp
  private void testHISCCP_aip3el12()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("hisccp", "aip3_hisccp", "el12.wmod");
    runHISCCP(des, false, true);
    runHISCCP(des, true, false);
  }

  private void testHISCCP_aip3el12b()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("hisccp", "aip3_hisccp", "el12b.wmod");
    runHISCCP(des, false, false);
    runHISCCP(des, true, false);
  }

  private void testHISCCP_aip3el3()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("hisccp", "aip3_hisccp", "el3.wmod");
    runHISCCP(des, false, true);
    runHISCCP(des, true, true);
  }

  private void testHISCCP_aip3el4()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("hisccp", "aip3_hisccp", "el4.wmod");
    runHISCCP(des, false, true);
    runHISCCP(des, true, true);
  }


  // rhone_subsystem1
  private void testHISCCP_rhone_subsystem1_ld()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "hisc", "rhone_subsystem1_ld.wmod");
    runHISCCP(des, false, false);
    runHISCCP(des, true, false);
  }


  // Philosophers
  private void testDirectPhiloSubsys12()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "philo_subsys12.wmod");
    runDirect(des, false, true);
    runDirect(des, true, true);
  }

  private void testDirectPhiloSubsys123()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "philo_subsys123.wmod");
    runDirect(des, false, true);
    runDirect(des, true, true);
  }

  private void testDirectPhiloSubsys1234()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "philo_subsys1234.wmod");
    runDirect(des, false, true);
    runDirect(des, true, true);
  }

  private void testDirectCP1()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "cptest01.wdes");
    runDirect(des, false, true);
    runDirect(des, true, true);
  }

  private void testDirectCP2()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "cptest02.wdes");
    runDirect(des, false, true);
    runDirect(des, true, true);
  }

  private void testDirectCP3()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "cptest03.wdes");
    runDirect(des, false, true);
    runDirect(des, true, true);
  }

  private void testDirectCP4()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "cptest04.wdes");
    runDirect(des, false, true);
    runDirect(des, true, true);
  }

  private void testDirectCP5()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "cptest05.wdes");
    runDirect(des, false, true);
    runDirect(des, true, true);
  }

  private void testDirectCP6()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "cptest06.wdes");
    runDirect(des, false, true);
    runDirect(des, true, true);
  }

  private void testDirectCP7()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "cptest07.wdes");
    runDirect(des, false, true);
    runDirect(des, true, true);
  }

  private void testDirectCP8()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "cptest08.wdes");
    runDirect(des, false, true);
    runDirect(des, true, true);
  }

  private void testDirectCP9()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "cptest09.wdes");
    runDirect(des, false, true);
    runDirect(des, true, true);
  }

  private void testDirectCP10()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "cptest10.wdes");
    runDirect(des, false, true);
    runDirect(des, true, true);
  }


  private void testDirectAnn1()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "anntest01.wdes");
    runDirect(des, false, true);
    runDirect(des, true, true);
  }

  private void testDirectAnn2()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "anntest02.wdes");
    runDirect(des, false, true);
    runDirect(des, true, true);
  }

  private void testDirectAnn3()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "anntest03.wdes");
    runDirect(des, false, true);
    runDirect(des, true, true);
  }

  private void testDirectAnn4()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "anntest04.wdes");
    runDirect(des, false, true);
    runDirect(des, true, true);
  }

  private void testDirectAnn5()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "anntest05.wdes");
    runDirect(des, false, true);
    runDirect(des, true, true);
  }

  private void testDirectSimon()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "simon.wmod");
    runDirect(des, false, true);
    runDirect(des, true, true);
  }


  //#########################################################################
  //# Main Method
  public static void main(final String[] args)
  {
    if (args.length == 1) {
      try {
        final String filename = args[0];
        final ConflictPreorderExperiments experiment =
          new ConflictPreorderExperiments(filename);
        experiment.setUp();
        experiment.runAllTests();
        experiment.tearDown();
      } catch (final Throwable exception) {
        System.err.println("FATAL ERROR");
        exception.printStackTrace(System.err);
      }
    } else {
      System.err.println
        ("USAGE: " +
         ProxyTools.getShortClassName(ConflictPreorderExperiments.class) +
         " <outputFilename>");
    }
  }


  //#########################################################################
  //# Invocation
  private void runHISCCP(final ProductDESProxy des,
                         final boolean reverse,
                         final boolean expect)
    throws Exception
  {
    final String name = des.getName();
    printAndLog("HISC-CP " + name + " ...");
    mHISCCPChecker.setModel(des);
    mHISCCPChecker.setReversed(reverse);
    try {
      final boolean result = mHISCCPChecker.run();
      if (result != expect) {
        System.err.println("UNEXPECTED RESULT: " + result + "!");
      }
    } catch (final AnalysisException exception) {
      mPrintWriter.println(name + "," + exception.getMessage());
    } finally {
      final HISCCPVerificationResult stats =
        (HISCCPVerificationResult) mHISCCPChecker.getAnalysisResult();
      final ConflictPreorderResult cpStats = stats.getConflictPreorderResult();
      printStats(name, cpStats);
    }
  }

  private void runDirect(final ProductDESProxy des,
                         final boolean reverse,
                         final boolean expect)
    throws AnalysisException
  {
    final String name = des.getName();
    printAndLog("DIRECT " + name + " ...");
    final Collection<AutomatonProxy> automata = des.getAutomata();
    assert automata.size() == 2 :
      "Model '" + name + "' does not have exactly 2 automata!";
    final Iterator<AutomatonProxy> iter = automata.iterator();
    final AutomatonProxy aut1, aut2;
    if (!reverse) {
      aut1 = iter.next();
      aut2 = iter.next();
    } else {
      aut2 = iter.next();
      aut1 = iter.next();
    }
    final Collection<EventProxy> events = des.getEvents();
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    final EventProxy tau = getTauEvent(des);
    final EventEncoding enc = new EventEncoding(events, translator, tau);
    final int config = TRConflictPreorderChecker.getPreferredInputConfiguration();
    final ListBufferTransitionRelation rel1 =
      new ListBufferTransitionRelation(aut1, enc, config);
    final ListBufferTransitionRelation rel2 =
      new ListBufferTransitionRelation(aut2, enc, config);
    final EventProxy marking = AbstractConflictChecker.getMarkingProposition(des);
    final int markingID = enc.getEventCode(marking);
    final TRConflictPreorderChecker checker =
      new TRConflictPreorderChecker(rel1, rel2, markingID);
    try {
      final boolean result = checker.isLessConflicting();
      if (result != expect) {
        System.err.println("UNEXPECTED RESULT: " + result + "!");
      }
    } catch (final AnalysisException exception) {
      mPrintWriter.println(name + "," + exception.getMessage());
    } finally {
      final ConflictPreorderResult stats = checker.getAnalysisResult();
      printStats(name, stats);
    }
  }

  private EventProxy getTauEvent(final ProductDESProxy des)
  {
    for (final EventProxy event : des.getEvents()) {
      if (!event.isObservable() && event.getName().startsWith(TAU)) {
        return event;
      }
    }
    return null;
  }

  private void printStats(final String name, final ConflictPreorderResult stats)
  {
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


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractAnalysisTest
  @Override
  protected void configure(final ModuleCompiler compiler)
  {
    super.configure(compiler);
    final List<String> accepting =
      Collections.singletonList(EventDeclProxy.DEFAULT_MARKING_NAME);
    compiler.setEnabledPropositionNames(accepting);
    compiler.setHISCCompileMode(HISCCompileMode.HISC_HIGH);
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
  private HISCCPInterfaceConsistencyChecker mHISCCPChecker;
  private final FileOutputStream mOut;
  private PrintWriter mPrintWriter;
  private boolean mHasBeenPrinted;


  //#########################################################################
  //# Data Members
  private static final String TAU = "tau";

}
