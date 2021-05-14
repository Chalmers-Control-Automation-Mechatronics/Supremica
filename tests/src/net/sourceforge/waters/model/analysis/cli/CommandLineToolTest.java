//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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
import java.security.Permission;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.options.AnalysisOptionPage;
import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.analysis.des.AnalysisOperation;

import org.apache.logging.log4j.LogManager;


/**
 * A test class for the command line tool (<CODE>wcheck</CODE>).
 * This test is used to ensure that command line arguments are recognised
 * and used to select and configure the model analysers correctly.
 *
 * @author Robi Malik
 */

public class CommandLineToolTest
  extends AbstractWatersTest
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
  //# Test Cases
  public void testConflictBadFactory()
    throws Exception
  {
    final String name = "bad_factory";
    final File file = getInputWMOD("handwritten", name);
    final String[] args = getArgs("TRCompositional", "ConflictChecker", file);
    testCommandLine(name, args, false, "counterexample:");
  }

  public void testConflictControlledPhilosophers()
    throws Exception
  {
    final String name = "controlled_philosophers";
    final File file = getInputWMOD("handwritten", name);
    final String[] args = getArgs("BDD", "ConflictChecker", file,
                                  "-marking", "eaten[0]", "-verbose");
    testCommandLine(name, args,
                    "DEBUG Depth .*", "DEBUG Coreachability .*");
  }

  public void testConflictDiningPhilosophers()
    throws Exception
  {
    final String name = "dining_philosophers";
    final File file = getInputWMOD("handwritten", name);
    final String[] args = getArgs("BDD", "ConflictChecker", file,
                                  "-marking", "eaten");
    testCommandLine(name, args, "FATAL.*", ".*'eaten'.*");
  }

  public void testConflictG1()
    throws Exception
  {
    final String name = "g1";
    final File file = getInputWMOD("tests", "generalisedNonblocking", name);
    final String[] args = getArgs("Native", "ConflictChecker", file,
                                  "-premarking", ":alpha",
                                  "-marking", ":accepting");
    testCommandLine(name, args, true);
  }

  public void testConflictSmallFactory2()
    throws Exception
  {
    final String name = "small_factory_2";
    final File file = getInputWMOD("handwritten", name);
    final String[] args = getArgs("BDD", "ConflictChecker", file, "-q");
    testCommandLine(name, args, "small_factory_2 ... true \\(.*", "!DEBUG.*");
  }

  public void testControllabilitySmallFactory2()
    throws Exception
  {
    final String name = "small_factory_2";
    final File file = getInputWMOD("handwritten", name);
    final String[] args = getArgs("Native", "ControllabilityChecker", file);
    testCommandLine(name, args, true);
  }

  public void testControllabilitySmallFactory2u()
    throws Exception
  {
    final String name = "small_factory_2u";
    final File file = getInputWMOD("handwritten", name);
    final String[] args = getArgs("Monolithic", "ControllabilityChecker", file);
    testCommandLine(name, args, false, "counterexample:", "!Statistics");
  }

  public void testDiagnosabilityNotDiag1()
    throws Exception
  {
    final String name = "notDiag_1";
    final File file = getInputWMOD("tests", "diagnosability", name);
    final String[] args = getArgs("Monolithic", "DiagnosabilityChecker", file);
    testCommandLine(name, args, false,
                    "TRACE #1: faulty.*", "TRACE #2: non-faulty.*");
  }

  public void testLanguageInclusionJustProperty()
    throws Exception
  {
    final String name = "just_property";
    final File file = getInputWMOD("tests", "nasty", name);
    final String[] args = getArgs("BDD", "LanguageInclusionChecker", file,
                                  "-property", "the_property", "-stats");
    testCommandLine(name, args, false, "counterexample:", "Statistics:");
  }

  public void testHelpOption()
    throws Exception
  {
    final String[] args =
      new String[] {"Monolithic", "DiagnosabilityChecker", "-help"};
    testCommandLine("help", args,
                    "=MonolithicDiagnosabilityVerifier supports the following options:",
                    "=-fslimit <n>.*",
                    "=ModuleCompiler supports the following options:",
                    "=-opt\\|-nopt.*",
                    "=CommandLineTool supports the following options:",
                    "=-quiet\\|-q.*");
  }

  public void testUnsupportedOption()
    throws Exception
  {
    final String name = "controlled_philosophers";
    final File file = getInputWMOD("handwritten", name);
    final String[] args = getArgs("Monolithic", "DiagnosabilityChecker",
                                  file, "-verose");
    testCommandLine(name, args, "Unsupported option -verose.*");
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.junit.AbstractWatersTest
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    System.setSecurityManager(new NoExitSecurityManager());
    for (final AnalysisOperation operation : AnalysisOperation.values()) {
      final AnalysisOptionPage page = operation.getOptionPage();
      page.restoreDefaultValues();
    }
  }

  @Override
  protected void tearDown() throws Exception
  {
    System.setSecurityManager(null); // or save and restore original
    super.tearDown();
  }

  @Override
  protected void setUpLog4J()
  {
    // Skip---command line tool handles log4j by itself.
  }


  //#########################################################################
  //# Auxiliary Methods
  private File getInputWMOD(final String... path)
  {
    return getInputFile(path, ".wmod");
  }

  private String[] getArgs(final String factory,
                           final String check,
                           final File file,
                           final String... extras)
  {
    final String[] args = new String[extras.length + 3];
    args[0] = factory;
    args[1] = check;
    args[2] = file.toString();
    System.arraycopy(extras, 0, args, 3, extras.length);
    return args;
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
    final StringBuilder builder = new StringBuilder();
    for (int i = 0; i < 2; i++) {
      if (i < args.length) {
        final String arg = args[i];
        if (arg.startsWith("-")) {
          builder.append(arg.substring(1));
        } else {
          builder.append(arg);
        }
        builder.append('.');
      }
    }
    builder.append(name);
    builder.append(".log");
    final String logName = builder.toString();

    final File dir = getOutputDirectory();
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
      System.setOut(sysOut);
      System.setErr(sysErr);
      output.close();
    }

    final List<PatternHandler> patterns =
      new ArrayList<>(outputPatterns.length + 1);
    final List<PatternHandler> antiPatterns =
      new ArrayList<>(outputPatterns.length);
    if (expectedResult != null) {
      final String resultRegex = String.format("%b \\(.*", expectedResult);
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


  //#########################################################################
  //# Inner Class NoExitSecurityManager
  private static class NoExitSecurityManager extends SecurityManager
  {
    //#######################################################################
    //# Overrides for java.lang.SecurityManager
    @Override
    public void checkPermission(final Permission perm)
    {
      // allow anything.
    }

    @Override
    public void checkPermission(final Permission perm, final Object context)
    {
      // allow anything.
    }

    @Override
    public void checkExit(final int status)
    {
      super.checkExit(status);
      throw new ExitException();
    }
  }


  //#########################################################################
  //# Inner Class ExitException
  private static class ExitException extends SecurityException
  {
    //#######################################################################
    //# Constructor
    private ExitException()
    {
      super("Terminated by System.exit()");
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = -840490238005753878L;
  }

}
