//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.model.analysis.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.coobs.CoobservabilityAttributeFactory;
import net.sourceforge.waters.config.Version;
import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.des.AnalysisOperation;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.options.AnalysisOptionPage;
import net.sourceforge.waters.model.options.WatersOptionPages;

import org.apache.logging.log4j.LogManager;


/**
 * A test class for the command line tool (<CODE>wcheck</CODE>).
 * This test is used to ensure that command line arguments are recognised
 * and used to select and configure the model analysers correctly.
 *
 * @author Robi Malik
 */

public class CommandLineToolTest
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    return new TestSuite(CommandLineToolTest.class);
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Constructors
  public CommandLineToolTest()
  {
  }

  public CommandLineToolTest(final String name)
  {
    super(name);
  }


  //#########################################################################
  //# Test Cases - specific combinations of factory/algorithm
  // controllability
  public void testAnalyzer_ModularControllability()
    throws Exception
  {
    final File file = getInputWdes("handwritten", "small_factory_2");
    final String[] args = new String[]
      {"-mod", "-cont", file.toString()};
    testCommandLine("mod-cont", args, true);
  }

  public void testAnalyzer_ModularBDDControllability()
    throws Exception
  {
    final File file = getInputWdes("handwritten", "small_factory_2");
    final String[] args = new String[]
      {"-mod", "-cont", file.toString(),
       "-chain", "-bdd", "-pack", "buddy", "-v", "-dynamic"};
    testCommandLine("mod-bdd-cont", args, "DEBUG Depth .*", "true \\(.*");
  }

  public void testAnalyzer_ModularTRCompBDDControllability()
    throws Exception
  {
    final File file = getInputWdes("handwritten", "small_factory_2");
    final String[] args = new String[]
      {"-mod", "-cont", file.toString(), "-stats",
        "-chain", "-trcomp", "-islimit", "100",
        "-chain", "-bdd", "-pack", "java", "-v"};
    testCommandLine("mod-trcomp-bdd-cont", args,
                    "DEBUG SpecialEventsFinder .*", "DEBUG Depth .*",
                    "true \\(.*");
  }

  public void testAnalyzer_MonolithicControllability()
    throws Exception
  {
    final File file = getInputWmod("handwritten", "small_factory_2u");
    final String[] args = new String[]
      {"-mono", "-cont", file.toString()};
    testCommandLine("mono-cont", args, false, "counterexample:", "!Statistics");
  }


  // coobservability
  public void testAnalyzer_ModularCoobservability()
    throws Exception
  {
    final File file = getInputWmod("tests", "coobservability", "fms2016coobs");
    final String[] args = new String[]
      {"-mod", "-coobs", file.toString(), "-stats"};
    testCommandLine("mod-coobs", args, false, "counterexample:",
                    "Statistics:", "Number of supervisor sites in model: .*");
  }

  public void testAnalyzer_ModularCoobservabilityTimeout()
    throws Exception
  {
    final File file = getInputWmod("tests", "coobservability", "verriegel4coobs");
    final File annsFile = getInputCann("tests", "coobservability", "verriegel4ann2");
    final String[] args = new String[]
      {"-mod", "-coobs", file.toString(), "-ann", annsFile.toString(),
       "-heuristic", "LateNotAccept", "-chain", "-trmono", "-ndm",
       "-timeout", "30"};
    testCommandLine("mod-coobs-timeout", args, "!true.*", "TIMEOUT \\(.*");
  }

  public void testAnalyzer_SlicingCoobservability()
    throws Exception
  {
    final File file = getInputWmod("tests", "coobservability", "verriegel4coobs");
    final File annsFile = getInputCann("tests", "coobservability", "verriegel4ann1");
    final String[] args = new String[]
      {"-slice", "-coobs", file.toString(), "-ann", annsFile.toString()};
    testCommandLine("slice-coobs", args, true);
  }


  // diagnosability
  public void testAnalyzer_MonolithicDiagnosability()
    throws Exception
  {
    final File file = getInputWmod("tests", "diagnosability", "five_faults");
    final String[] args = new String[]
      {"-mono", "-diag", file.toString()};
    testCommandLine("mono-diag", args, false,
                    "\"The fault-class 'F1' is not diagnosable.\"",
                    "TRACE #1: faulty.*", "TRACE #2: non-faulty.*");
  }

  public void testAnalyzer_MonolithicDiagnosabilityFault()
    throws Exception
  {
    final File file = getInputWmod("tests", "diagnosability", "five_faults");
    final String[] args = new String[]
      {"-mono", "-diag", file.toString(), "-fault", "F3"};
    testCommandLine("mono-diag-fault", args, true);
  }

  public void testAnalyzer_NativeControllability()
    throws Exception
  {
    final File file = getInputWdes("handwritten", "small_factory_2");
    final String[] args = new String[]
      {"-native", "-cont", file.toString()};
    testCommandLine("native-cont", args, true);
  }

  public void testAnalyzer_TRCompControllability()
    throws Exception
  {
    final File file1 = getInputWmod("handwritten", "small_factory_2");
    final File file2 = getInputWmod("handwritten", "small_factory_2u");
    final String[] args = new String[]
      {"-trcomp", "-cont",
       file1.toString(), file2.toString(), "-q", "-nout"};
    testCommandLine("trcomp-cont", args,
                    "small_factory_2 ... true \\(.*",
                    "small_factory_2u ... false \\(.*");
  }


  // language inclusion
  public void testAnalyzer_BDDLanguageInclusionGreedy()
    throws Exception
  {
    final File file = getInputWmod("tests", "trafficlights2006", "ac61part");
    final String[] args = new String[]
      {"-bdd", "-lang", "-part", "greedy", file.toString()};
    testCommandLine("bdd-lang", args, false, "counterexample:");
  }

  public void testAnalyzer_BDDLanguageInclusionProperty()
    throws Exception
  {
    final File file = getInputWmod("tests", "nasty", "just_property");
    final String[] args = new String[]
      {"-bdd", "-lang", file.toString(), "-pack", "java",
       "-property", "the_property", "-nout"};
    testCommandLine("bdd-lang-property", args, false,
                    "!DEBUG", "!counterexample:", "!Statistics:");
  }

  public void testAnalyzer_TRCompLanguageInclusion()
    throws Exception
  {
    final File file = getInputWmod("tests", "nasty", "five_properties");
    final String[] args = new String[]
      {"-trcomp", "-lang",
       file.toString(), "-property", "prop[3]"};
    testCommandLine("trcomp-lang", args, true);
  }


  // nonblocking
  public void testAnalyzer_NativeGNonblocking()
    throws Exception
  {
    final File file = getInputWmod("tests", "generalisedNonblocking", "g1");
    final String[] args = new String[]
      {"-native", "-conf", file.toString(),
       "-premarking", ":alpha", "-marking", ":accepting"};
    testCommandLine("native-gnonblocking", args, true);
  }

  public void testAnalyzer_NativeLanguageInclusion()
    throws Exception
  {
    final File file = getInputWmod("tests", "nasty", "just_property");
    final String[] args = new String[]
      {"-native", "-lang", file.toString(),
       "-property", "the_property", "-stats"};
    testCommandLine("native-lang", args, false,
                    "counterexample:", "Statistics:");
  }

  public void testAnalyzer_NativeSIC5()
    throws Exception
  {
    final File file = getInputWmod("despot", "testHISC", "hisc8_low2");
    final String[] args = new String[]
      {"-native", "-sic5", file.toString()};
    testCommandLine("native-sic5", args, false, ".*SIC property V.*");
  }

  public void testAnalyzer_TRCompConflict()
    throws Exception
  {
    final File file = getInputWmod("handwritten", "bad_factory");
    final String[] args = new String[]
      {"-trcomp", "-conf", file.toString()};
    testCommandLine("trcomp-conf", args, false, "counterexample:");
  }

  public void testAnalyzer_TRCompConflictProperties()
    throws Exception
  {
    final File file = getInputWmod("valid", "central_locking", "verriegel2");
    final File props = getInputProperties("tests", "nasty", "junit");
    final String[] args = new String[]
      {"-trcomp", "-conf", file.toString(),
       "-p", props.toString(), "-q", "-stats"};
    testCommandLine("trcomp-conf-p", args, true,
                    ".*SelfloopSubsumptionTRSimplifier.*", "!WARN.*");
  }

  public void testAnalyzer_TRCompSIC6()
    throws Exception
  {
    final File file = getInputWmod("despot", "testHISC", "hisc2_low1");
    final String[] args = new String[]
      {"-trcomp", "-sic6", "-method", "GNB", file.toString(), "-stats"};
    testCommandLine("trcomp-sic6", args, false,
                    ".*SIC property VI.*",
                    ".*ObservationEquivalenceTRSimplifier.*");
  }


  // synthesis
  public void testAnalyzer_MonolithicSynthesisReduced()
    throws Exception
  {
    final File file = getInputWmod("tests", "synthesis", "ransomware_sample");
    final String[] args = new String[]
      {"-mono", "-synth", "-red", "Small cliques", file.toString()};
    testCommandLine("mono-synth-red", args, true, " *S:4", "! *S:5");
  }

  public void testAnalyzer_MonolithicSynthesisSaved()
    throws Exception
  {
    final String name = "small_factory_2";
    final File file = getInputWmod("tests", "synthesis", name);
    final File saveFile = new File(getOutputDirectory(), name + ".wmod");
    final String[] args = new String[]
      {"-mono", "-synth", "-o", saveFile.toString(), file.toString()};
    testCommandLine("mono-synth-saved", args, true,
                    "supervisor saved to " + saveFile);
    final DocumentManager manager = getDocumentManager();
    final ModuleProxy module = (ModuleProxy) manager.load(saveFile);
    final List<Proxy> components = module.getComponentList();
    assertEquals("Unexpected number of components in output!",
                 1, components.size());
    final SimpleComponentProxy comp = (SimpleComponentProxy) components.get(0);
    assertEquals("Unexpected component kind of output supervisor!",
                 ComponentKind.SUPERVISOR, comp.getKind());
  }




  //#########################################################################
  //# Test Cases - specific combinations of options
  public void testOption_CompileOnly()
    throws Exception
  {
    final String renamed = "cell_renamed";
    final File file = getInputWmod("handwritten", "cell");
    final File output = getOutputWdes(renamed);
    final String[] args = new String[]
      {"-c", file.toString(), "-o", output.toString(), "-v"};
    testCommandLine("c", args, "Compiled product DES saved to .*");
    assertTrue("Compiler output file not found!", output.canRead());
    final DocumentManager manager = getDocumentManager();
    final ProductDESProxy des = (ProductDESProxy) manager.load(output);
    final String name = des.getName();
    assertEquals("Unexpected name of out product DES!", renamed, name);
    final Collection<AutomatonProxy> automata = des.getAutomata();
    assertEquals("Unexpected number of automata in compiler output!",
                 7, automata.size());
  }

  public void testOption_AnnotationsFile()
    throws Exception
  {
    final File file = getInputWmod("handwritten", "transferline");
    final File annsFile = getInputCann("handwritten", "transferline");
    final File output = getOutputWdes("transferline3");
    final String[] args = new String[]
      {"-c", file.toString(), "-D", "N=3", "-ann", annsFile.toString(),
       "-o", output.toString()};
    testCommandLine("ann", args);
    assertTrue("Compiler output file not found!", output.canRead());
    final DocumentManager manager = getDocumentManager();
    final ProductDESProxy des = (ProductDESProxy) manager.load(output);

    final Pattern pattern = Pattern.compile("\\[([0-9])\\]");
    for (final EventProxy event : des.getEvents()) {
      final String name = event.getName();
      final Matcher matcher = pattern.matcher(name);
      if (matcher.find()) {
        final Map<String,String> attribs = event.getAttributes();
        final String group = matcher.group(1);
        final int index = Integer.parseInt(group);
        if (index > 0) {
          if (event.getKind() == EventKind.CONTROLLABLE) {
            final String key =
              CoobservabilityAttributeFactory.CONTROLLABITY_KEY + group;
            assertEquals("Unexpected controllability attribute for event '" +
                         name + "'!", group, attribs.get(key));
          }
          if (event.isObservable()) {
            final String key =
              CoobservabilityAttributeFactory.OBSERVABITY_KEY + group;
            assertEquals("Unexpected observability attribute for event '" +
                         name + "'!", group, attribs.get(key));
          }
        }
        if (index < 3) {
          final String next = Integer.toString(index + 1);
          if (name.startsWith("tu_load")) {
            final String key =
              CoobservabilityAttributeFactory.CONTROLLABITY_KEY + next;
            assertEquals("Unexpected controllability attribute for event '" +
                         name + "'!", next, attribs.get(key));
          } else if (name.startsWith("tu_accept")) {
            final String key =
              CoobservabilityAttributeFactory.OBSERVABITY_KEY + next;
            assertEquals("Unexpected observability attribute for event '" +
                         name + "'!", next, attribs.get(key));
          }
        }
      }
    }
  }

  public void testOption_D()
    throws Exception
  {
    final File file = getInputWmod("handwritten", "transferline");
    final String[] args = new String[]
      {"-native", "-count", "-D", "N=2", "-stats", file.toString()};
    testCommandLine("binding", args, true, "Total number of states: 410");
  }

  public void testOption_End()
    throws Exception
  {
    final File file = getInputWmod("handwritten", "small_factory_2u");
    final String[] args = new String[]
      {"-mod", "-cont", "--", file.toString()};
    testCommandLine("end", args, false);
  }

  public void testOption_EndBad()
    throws Exception
  {
    final File file = getInputWmod("tests", "nasty", "just_property");
    final String[] args =
      new String[] {"-mono", "-cont",
                    "--", "-opt", file.toString()};
    testCommandLine("end-bad", args,
                    "FATAL ERROR \\(BadFileTypeException\\)");
  }

  public void testOption_FslimitBad()
    throws Exception
  {
    final File file = getInputWmod("handwritten", "small_factory_2");
    final String[] args =
      new String[] {"-mono", "-cont",
                    "-fslimit", "xxx", file.toString()};
    testCommandLine("fslimit-bad", args,
                    "Option -fslimit xxx does not specify an integer\\.");
  }

  public void testOption_FslimitOverflow()
    throws Exception
  {
    final File file = getInputWmod("handwritten", "small_factory_2");
    final String[] args =
      new String[] {"-mono", "-cont", "-fslimit", "3", file.toString()};
    testCommandLine("fslimit-overflow", args, "!true.*", "OVERFLOW \\(.*");
  }

  public void testOption_Help()
    throws Exception
  {
    final String[] args =
      new String[] {"-mono", "-diag", "-help"};
    testCommandLine("help", args,
                    "=MonolithicDiagnosabilityVerifier supports the following options:",
                    "=-fslimit <n>.*",
                    "=ModuleCompiler supports the following options:",
                    "=-opt\\|-nopt.*",
                    "=CommandLineTool supports the following options:",
                    "=-quiet\\|-q.*",
                    "=Java VM supports the following options:",
                    "=-Xmx<memory>.*");
  }

  public void testOption_HelpChain()
    throws Exception
  {
    final String[] args =
      new String[] {"-trcomp", "-conf", "-chain", "-native", "-help"};
    testCommandLine("help-chain", args,
                    "=NativeConflictChecker supports the following options:",
                    "-mode <value>.*",
                    "=TRCompositionalConflictChecker supports the following options:",
                    ".*NB0, NB1.*",
                    "=ModuleCompiler supports the following options:",
                    "=-opt\\|-nopt.*",
                    "=CommandLineTool supports the following options:",
                    "=-quiet\\|-q.*");
  }

  public void testOption_Name()
    throws Exception
  {
    final String[] args = new String[] {"@name", "junit"};
    testCommandLine("name", args, "USAGE:",
                                  "junit -c.*",
                                  "junit <.*",
                                  "Possible algorithms are:",
                                  "Possible operations are:");
  }

  public void testOption_MarkingBad()
    throws Exception
  {
    final File file = getInputWmod("handwritten", "dining_philosophers");
    final String[] args = new String[]
      {"-bdd", "-conf", file.toString(), "-pack", "cudd", "-marking", "eaten"};
    testCommandLine("marking-bad", args, "FATAL.*", ".*'eaten'.*");
  }

  public void testOption_Quiet()
    throws Exception
  {
    final File file = getInputWmod("handwritten", "small_factory_2");
    final String[] args = new String[]
      {"-bdd", "-conf", file.toString(), "-q"};
    testCommandLine("quiet", args, "small_factory_2 ... true \\(.*", "!DEBUG.*");
  }

  public void testOption_Timeout()
    throws Exception
  {
    final File file = getInputWmod("tests", "incremental_suite", "agv");
    final String[] args = new String[]
      {"-native", "-conf", "-timeout", "1", file.toString()};
    testCommandLine("timeout", args, "!true.*", "TIMEOUT \\(.*");
  }

  public void testOption_Unsupported()
    throws Exception
  {
    final String[] args = new String[]
      {"-mono", "-diag", "-verose"};
    testCommandLine("unsupported", args, "Unsupported option -verose.*");
  }

  public void testOption_Verbose()
    throws Exception
  {
    final File file = getInputWmod("handwritten", "controlled_philosophers");
    final String[] args = new String[]
      {"-bdd", "-conf", file.toString(),
       "-marking", "eaten[0]", "-verbose", "-pack", "cudd"};
    testCommandLine("verbose", args,
                    "DEBUG Depth .*", "DEBUG Coreachability .*");
  }

  public void testOption_Version()
    throws Exception
  {
    final String[] args = new String[] {"@name", "wcheck", "-version"};
    testCommandLine("version", args, Version.getInstance().getTitle());
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.junit.AbstractWatersTest
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    ExitException.setTestMode(true);
    for (final AnalysisOperation operation : AnalysisOperation.values()) {
      final AnalysisOptionPage page = operation.getOptionPage();
      page.restoreDefaultValues();
    }
    WatersOptionPages.COMPILER.restoreDefaultValues();
  }

  @Override
  protected void setUpLog4J()
  {
    // Skip---command line tool handles log4j by itself.
  }


  //#########################################################################
  //# Auxiliary Methods
  private File getInputWmod(final String... path)
  {
    return getInputFile(path, ".wmod");
  }

  private File getInputWdes(final String... path)
  {
    return getInputFile(path, ".wdes");
  }

  private File getInputCann(final String... path)
  {
    return getInputFile(path, ".cann");
  }

  private File getOutputWdes(final String... path)
  {
    return getOutputFile(path, ".wdes");
  }

  private File getInputProperties(final String... path)
  {
    return getInputFile(path, ".properties");
  }

  private void testCommandLine(final String name,
                               final String[] args,
                               final String... outputPatterns)
    throws Exception
  {
    testCommandLine(name, args, null, outputPatterns);
  }

  private void testCommandLine(final String name,
                               final String[] args,
                               final Boolean expectedResult,
                               final String... outputPatterns)
    throws Exception
  {
    final File dir = getOutputDirectory();
    final String logName = name + ".log";
    final File logFile = new File(dir, logName);
    final PrintStream output = new PrintStream(logFile);
    final PrintStream sysOut = System.out;
    final PrintStream sysErr = System.err;
    try {
      System.setOut(output);
      System.setErr(output);
      CommandLineTool.main(args);
    } finally {
      LogManager.shutdown();
      output.close();
      System.setOut(sysOut);
      System.setErr(sysErr);
    }

    if (outputPatterns.length == 0) {
      return;
    }
    final List<PatternHandler> patterns =
      new ArrayList<>(outputPatterns.length + 1);
    final List<PatternHandler> antiPatterns =
      new ArrayList<>(outputPatterns.length);
    if (expectedResult != null) {
      final String resultRegex =
        String.format("([a-zA-Z0-9]+ \\.\\.\\. )?%b \\(.*", expectedResult);
      final PatternHandler resultPattern = new PatternHandler(resultRegex);
      patterns.add(resultPattern);
    }
    for (final String regex : outputPatterns) {
      final PatternHandler pattern = new PatternHandler(regex);
      if (pattern.isAntiPattern()) {
        antiPatterns.add(pattern);
      } else {
        patterns.add(pattern);
      }
    }
    final Iterator<PatternHandler> iter = patterns.iterator();
    PatternHandler pattern = iter.next();

    final FileReader reader = new FileReader(logFile);
    final BufferedReader buffered = new BufferedReader(reader);
    try {
      String line;
      while ((line = buffered.readLine()) != null) {
        for (final PatternHandler anti : antiPatterns) {
          if (anti.matches(line)) {
            fail("Unexpected output: " + anti.toString() + "!");
          }
        }
        if (pattern != null && pattern.matches(line)) {
          if (pattern.isAntiPattern()) {
            antiPatterns.add(pattern);
          }
          if (iter.hasNext()) {
            pattern = iter.next();
          } else {
            pattern = null;
            if (antiPatterns.isEmpty()) {
              break;
            }
          }
        }
      }
      if (pattern != null) {
        fail("Missing output: " + pattern.toString() + "!");
      }
    } finally {
      buffered.close();
    }
  }


  //#########################################################################
  //# Inner Class PatternHandler
  private static class PatternHandler
  {
    //#######################################################################
    //# Constructor
    private PatternHandler(final String regex)
    {
      final String tail;
      switch (regex.charAt(0)) {
      case '>':
        tail = regex.substring(1);
        break;
      case '=':
        tail = regex.substring(1);
        mOneOff = true;
        break;
      case '!':
        tail = regex.substring(1);
        mAnti = true;
        break;
      default:
        tail = regex;
        break;
      }
      mPattern = Pattern.compile(tail);
    }

    //#######################################################################
    //# Overrides for java.lang.Object
    @Override
    public String toString()
    {
      return mPattern.toString();
    }

    //#######################################################################
    //# Matching
    private boolean matches(final String line)
    {
      final Matcher matcher = mPattern.matcher(line);
      if (matcher.matches()) {
        mAnti |= mOneOff;
        return true;
      } else {
        return false;
      }
    }

    private boolean isAntiPattern()
    {
      return mAnti;
    }

    //#######################################################################
    //# Data Members
    private final Pattern mPattern;
    private boolean mOneOff;
    private boolean mAnti;
  }

}
