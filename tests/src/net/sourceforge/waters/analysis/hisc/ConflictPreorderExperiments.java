//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.analysis.hisc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
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
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.kindtranslator.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
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
  public ConflictPreorderExperiments(final String statsFilename,
                                     final String latexName)
    throws FileNotFoundException
  {
    final String outputprop = System.getProperty("waters.test.outputdir");
    final File dir = new File(outputprop);
    ensureDirectoryExists(dir);
    final File statsFile = new File(dir, statsFilename);
    mOut = new FileOutputStream(statsFile);
    mPrintWriter = null;
    mLaTeXName = latexName;
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
  //# Tests
  private void runAllTests() throws Exception
  {
    mCurrentGroup = null;
    mMaxLCLevel = 0;
    final List<List<ConflictPreorderRecord>> groups =
      new LinkedList<List<ConflictPreorderRecord>>();
    testHISCCP_aip3syn_as1();

    final List<ConflictPreorderRecord> philo =
      new ArrayList<ConflictPreorderRecord>(3);
    mCurrentGroup = philo;
    testDirectPhiloSubsys12();
    testDirectPhiloSubsys123();
    testDirectPhiloSubsys1234();
    groups.add(philo);

    final List<ConflictPreorderRecord> direct =
      new ArrayList<ConflictPreorderRecord>(16);
    mCurrentGroup = direct;
    testDirectAnn1();
    testDirectAnn2();
    testDirectAnn3();
    testDirectAnn4();
    testDirectAnn5();
    //testDirectCP1();
    testDirectCP2();
    testDirectCP3();
    testDirectCP4();
    testDirectCP5();
    //testDirectCP6();
    //testDirectCP7();
    testDirectCP8();
    testDirectCP9();
    //testDirectCP10();
    testDirectSimon();
    Collections.sort(direct);
    groups.add(direct);

    final List<ConflictPreorderRecord> hisccp =
      new ArrayList<ConflictPreorderRecord>(7);
    mCurrentGroup = hisccp;
    testHISCCP_SimpleManufHISCCP1();
    testHISCCP_SimpleManufHISCCP1bad();
    testHISCCP_aip1sub1ld();
    testHISCCP_aip3el12();
    testHISCCP_aip3el12b();
    testHISCCP_aip3el3();
    testHISCCP_aip3el4();
    groups.add(hisccp);

    final FileOutputStream latexStream = new FileOutputStream(mLaTeXName);
    final PrintWriter writer = new PrintWriter(latexStream);
    printLaTeX(writer, groups);
    writer.close();
  }


  // SimpleManufacturingExample
  private void testHISCCP_SimpleManufHISCCP1()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("hisccp", "simple_manuf_multi_hisccp", "subsystem.wmod");
    runHISCCP("smm", des, false, true);
    runHISCCP("", des, true, false);
  }

  private void testHISCCP_SimpleManufHISCCP1bad()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("hisccp", "simple_manuf_multi_hisccp",
                     "subsystem_bad.wmod");
    runHISCCP("smm\\_bad", des, false, false);
    runHISCCP("", des, true, false);
  }


  // song_aip
  private void testHISCCP_aip3syn_as1()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("despot", "song_aip", "aip3_syn", "as1.wmod");
    runHISCCP("aip3as1", des, false, false);
    runHISCCP("", des, true, false);
  }


  // aip1sub1ld
  private void testHISCCP_aip1sub1ld()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "hisc", "aip1sub1ld.wmod");
    runHISCCP("aip1sub1", des, false, false);
    runHISCCP("", des, true, false);
  }


  // aip3_hisccp
  private void testHISCCP_aip3el12()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("hisccp", "aip3_hisccp", "el12.wmod");
    runHISCCP("aip3el12", des, false, true);
    runHISCCP("", des, true, false);
  }

  private void testHISCCP_aip3el12b()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("hisccp", "aip3_hisccp", "el12b.wmod");
    runHISCCP("aip3el12b", des, false, false);
    runHISCCP("", des, true, false);
  }

  private void testHISCCP_aip3el3()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("hisccp", "aip3_hisccp", "el3.wmod");
    runHISCCP("aip3el3", des, false, true);
    runHISCCP("", des, true, true);
  }

  private void testHISCCP_aip3el4()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("hisccp", "aip3_hisccp", "el4.wmod");
    runHISCCP("aip3el4", des, false, true);
    runHISCCP("", des, true, true);
  }


  // Philosophers
  private void testDirectPhiloSubsys12()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "philo_subsys12.wmod");
    runDirect("$\\subsys1 \\confle \\abssys1$", des, false, true);
    runDirect("$\\abssys1 \\confle \\subsys1$", des, true, true);
  }

  private void testDirectPhiloSubsys123()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "philo_subsys123.wmod");
    runDirect("$\\subsys{1,2} \\confle \\abssys{1,2}$", des, false, true);
    runDirect("$\\abssys{1,2} \\confle \\subsys{1,2}$", des, true, true);
  }

  private void testDirectPhiloSubsys1234()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "philo_subsys1234.wmod");
    runDirect("$\\subsys{1,2,3} \\confle \\abssys{1,2,3}$", des, false, true);
    runDirect("$\\abssys{1,2,3} \\confle \\subsys{1,2,3}$", des, true, true);
  }

  @SuppressWarnings("unused")
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

  @SuppressWarnings("unused")
  private void testDirectCP6()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "conflict_preorder", "cptest06.wdes");
    runDirect(des, false, true);
    runDirect(des, true, true);
  }

  @SuppressWarnings("unused")
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

  @SuppressWarnings("unused")
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
    if (args.length == 1 || args.length == 2) {
      try {
        final String filename = args[0];
        final String latexName = args.length == 2 ? args[1] : null;
        final ConflictPreorderExperiments experiment =
          new ConflictPreorderExperiments(filename, latexName);
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
         " <outputFilename> <latexName>");
    }
  }


  //#########################################################################
  //# Invocation
  private void runHISCCP(final String label,
                         final ProductDESProxy des,
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
      printStats(name, label, reverse, cpStats);
    }
  }

  private void runDirect(final ProductDESProxy des,
                         final boolean reverse,
                         final boolean expect)
    throws AnalysisException
  {
    runDirect(null, des, reverse, expect);
  }

  private void runDirect(final String label,
                         final ProductDESProxy des,
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
    final EventProxy marking = AbstractConflictChecker.findMarkingProposition(des);
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
      printStats(name, label, reverse, stats);
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

  private void printStats(final String name,
                          final String label,
                          final boolean reverse,
                          final ConflictPreorderResult stats)
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

    if (mCurrentGroup != null) {
      final ConflictPreorderRecord record =
        new ConflictPreorderRecord(label, reverse, stats);
      mCurrentGroup.add(record);
      final int level = stats.getMaxLCLevel();
      if (level > mMaxLCLevel) {
        mMaxLCLevel = level;
      }
    }
  }

  private void printLaTeX(final PrintWriter writer,
                          final List<List<ConflictPreorderRecord>> groups)
  {
    writer.print("\\begin{tabular}{|>{\\hskip4.6em}rr<{) }|r|r|r|r|r|");
    for (int l = 0; l <= mMaxLCLevel; l++) {
      writer.print("r|");
    }
    writer.println("r|c|}");
    writer.println("\\hline");
    writer.println("\\multicolumn{2}{|c|}{\\bf Instance} &");
    writer.println("  \\multicolumn{2}{c|}{\\bf States} &");
    writer.println("  \\multicolumn{1}{c|}{\\bf Pairs} &");
    writer.println("  \\multicolumn{1}{c|}{\\bf Triples} &");
    writer.print("  \\multicolumn{");
    writer.print(mMaxLCLevel + 1);
    writer.println("}{c|}{\\bf \\LC-Pairs} &");
    writer.println("  \\bf Time & \\bf Res. \\\\");
    writer.println("\\multicolumn{2}{|c|}{$A \\confle B$} &");
    writer.println("  \\scriptsize$|Q_A|$ & \\scriptsize$|Q_B|$ &");
    writer.println("  \\scriptsize$|\\Pairs|$ &");
    writer.println("  \\multicolumn{1}{c|}{\\scriptsize$|\\mc|$} &");
    for (int l = 0; l <= mMaxLCLevel; l++) {
      writer.print("  \\multicolumn{1}{c|}{\\scriptsize $|\\LC^");
      writer.print(l);
      writer.println("|$} &");
    }
    writer.println("  \\multicolumn{1}{c|}{[s]} & $\\confle$ \\\\");
    writer.println("\\hline");
    for (final List<ConflictPreorderRecord> group : groups) {
      writer.println("\\hline");
      int lineno = 0;
      for (final ConflictPreorderRecord record : group) {
        record.printLaTeX(writer, lineno++);
      }
    }
    writer.println("\\end{tabular}");
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
  //# Inner Class
  private class ConflictPreorderRecord
    implements Comparable<ConflictPreorderRecord>
  {

    //#######################################################################
    //# Constructor
    private ConflictPreorderRecord(final String label,
                                   final boolean reverse,
                                   final ConflictPreorderResult stats)
    {
      mLabel = label;
      mReversed = reverse;
      mStats = stats;
    }

    //#######################################################################
    //# Interface java.util.Comparable<ConflictPreorderRecord>
    @Override
    public int compareTo(final ConflictPreorderRecord record)
    {
      final int size1 =
        mStats.getFirstAutomatonStates() + mStats.getSecondAutomatonStates();
      final int size2 =
        record.mStats.getFirstAutomatonStates() +
        record.mStats.getSecondAutomatonStates();
      if (size1 != size2) {
        if (size1 < size2) {
          return -1;
        } else {
          return 1;
        }
      }
      if (mReversed != record.mReversed) {
        if (!mReversed) {
          return -1;
        } else {
          return 1;
        }
      }
      return 0;
    }

    //#######################################################################
    //# Printing
    private void printLaTeX(final PrintWriter writer, final int lineno)
    {
      if (mLabel != null) {
        if (mLabel.startsWith("$")) {
          writer.print("\\multicolumn{2}{|c|}{");
        } else {
          writer.print("\\multicolumn{2}{|r<{ }|}{\\sf ");
        }
        writer.print(mLabel);
        writer.print("}");
      } else {
        final int groupno = (lineno >> 1) + 1;
        if (groupno < 10) {
          writer.print(' ');
        }
        if (!mReversed) {
          writer.print(groupno);
          writer.print(". & a");
        } else {
          writer.print("   & b");
        }
      }
      writer.println(" &");
      writer.print("  ");
      writer.print(mStats.getFirstAutomatonStates());
      writer.print(" & ");
      writer.print(mStats.getSecondAutomatonStates());
      writer.print(" & ");
      writer.print(mStats.getTotalLCPairs());
      writer.print(" & ");
      writer.print(mStats.getPeakMCTriples());
      writer.println(" &");
      writer.print("  ");
      int prev = 0;
      for (int l = 0; l <= mMaxLCLevel; l++) {
        final int delta;
        if (l <= mStats.getMaxLCLevel()) {
          final int lc = mStats.getTotalLCPairs(l);
          delta = lc - prev;
          prev = lc;
        } else {
          delta = 0;
        }
        writer.print(delta);
        writer.print(" & ");
      }
      @SuppressWarnings("resource")
      final Formatter formatter = new Formatter(writer);
      final float seconds = 0.001f * mStats.getRunTime();
      formatter.format("%.2f", seconds);
      writer.print(" & ");
      if (mStats.isSatisfied()) {
        writer.print("\\ltrue");
      } else {
        writer.print("\\lfalse");
      }
      writer.println(" \\\\");
      if (mReversed) {
        writer.println("\\hline");
      }
    }

    //#######################################################################
    //# Data Members
    private final String mLabel;
    private final boolean mReversed;
    private final ConflictPreorderResult mStats;
  }


  //#########################################################################
  //# Data Members
  private HISCCPInterfaceConsistencyChecker mHISCCPChecker;
  private List<ConflictPreorderRecord> mCurrentGroup;
  private int mMaxLCLevel;

  private final FileOutputStream mOut;
  private PrintWriter mPrintWriter;
  private final String mLaTeXName;
  private boolean mHasBeenPrinted;


  //#########################################################################
  //# Data Members
  private static final String TAU = "tau";

}
