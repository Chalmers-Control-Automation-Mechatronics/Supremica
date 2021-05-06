//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.analysis.efa.unified;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import net.sourceforge.waters.analysis.options.Configurable;
import net.sourceforge.waters.analysis.options.FileOption;
import net.sourceforge.waters.analysis.options.FlagOption;
import net.sourceforge.waters.analysis.options.LeafOptionPage;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.analysis.options.PositiveIntOption;
import net.sourceforge.waters.external.valid.ValidUnmarshaller;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.ProxyResult;
import net.sourceforge.waters.model.analysis.Watchdog;
import net.sourceforge.waters.model.analysis.cli.ArgumentSource;
import net.sourceforge.waters.model.analysis.cli.CommandLineOptionContext;
import net.sourceforge.waters.model.analysis.cli.VerboseLogConfigurationFactory;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.LoggerConfig;


/**
 * Prototype command line tool to invoke UnifiedEFAConflictChecker.
 *
 * @author Robi Malik
 */

public class UnifiedEFACommandLineTool
  implements ArgumentSource, Configurable
{

  //#########################################################################
  //# Constructors
  private UnifiedEFACommandLineTool()
  {
  }


  //#########################################################################
  //# Main Method for Testing
  /**
   * Main method.
   * This is a main method to check whether one or more modules are
   * nonblocking using {@link UnifiedEFAConflictChecker}.
   */
  public static void main(final String[] args) {
    new UnifiedEFACommandLineTool().run(args);
  }

  public void run(final String[] args)
  {

    final ConfigurationFactory cfactory =
      new VerboseLogConfigurationFactory(mVerbosity);
    ConfigurationFactory.setConfigurationFactory(cfactory);

    boolean noargs = false;
    final Formatter formatter = new Formatter(System.out);

    try {
      if (args.length < 1) {
        usage();
      }

      mContext = new CommandLineOptionContext();

      final ModuleProxyFactory moduleFactory =
        ModuleElementFactory.getInstance();
      final OperatorTable optable = CompilerOperatorTable.getInstance();
      final ExpressionParser parser =
        new ExpressionParser(moduleFactory, optable);
      List<ParameterBindingProxy> bindings = null;

      final List<String> argList = new LinkedList<String>();
      for (int i = 0; i < args.length; i++) {
        final String arg = args[i];
        if (noargs) {
          argList.add(arg);
        } else if (arg.startsWith("-D")) {
          final int eqpos = arg.indexOf('=', 2);
          if (eqpos > 2) {
            final String name = arg.substring(2, eqpos);
            final String text = arg.substring(eqpos + 1);
            final SimpleExpressionProxy expr = parser.parse(text);
            final ParameterBindingProxy binding =
              moduleFactory.createParameterBindingProxy(name, expr);
            if (bindings == null) {
              bindings = new LinkedList<ParameterBindingProxy>();
            }
            bindings.add(binding);
          } else {
            argList.add(arg);
          }
        } else if (arg.equals("--")) {
          noargs = true;
          argList.add(arg);
        } else {
          argList.add(arg);
        }
      }

      final ValidUnmarshaller importer =
        new ValidUnmarshaller(moduleFactory, optable);
      final SAXModuleMarshaller moduleMarshaller =
        new SAXModuleMarshaller(moduleFactory, optable, false);
      final DocumentManager docManager = new DocumentManager();
      docManager.registerUnmarshaller(moduleMarshaller);
      docManager.registerUnmarshaller(importer);

      checker = new UnifiedEFAConflictChecker(moduleFactory);
      checker.setDocumentManager(docManager);
      checker.setBindings(bindings);

      final ListIterator<String> argIter = argList.listIterator();
      mContext.addArgumentSource(this);
      mContext.addConfigurable(this);
      mContext.addArgumentSource(checker);
      mContext.addConfigurable(checker);
      mContext.parse(argIter);
      mContext.configure(this);
      mContext.configure(checker);

      if (mVerbosity != null) {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        final LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(mVerbosity);
        ctx.updateLoggers();
      }
      if (mTimeout > 0) {
        final Watchdog watchdog = new Watchdog(checker, mTimeout);
        watchdog.start();
      }

      boolean first = true;
      for (final String name : argList) {
        final File filename = new File(name);
        final ModuleProxy module = (ModuleProxy) docManager.load(filename);
        final String fullName =
          ModuleCompiler.getParametrizedName(module, bindings);
        System.out.print(fullName + " ... ");
        if (mVerbosity != null) {
          System.out.println();
        } else {
          System.out.flush();
        }
        final long start = System.currentTimeMillis();
        checker.setModel(module);
        boolean additions = false;
        try {
          checker.run();
          final AnalysisResult result = checker.getAnalysisResult();
          final long stop = System.currentTimeMillis();
          final boolean satisfied = result.isSatisfied();
          final double numstates = result.getTotalNumberOfStates();
          final float difftime = 0.001f * (stop - start);
          final int numnodes = result.getPeakNumberOfNodes();
          if (numstates < 0 && numnodes < 0) {
            formatter.format("%b (%.3f s)\n", satisfied, difftime);
          } else if (numnodes < 0 || numnodes == (int) numstates) {
            formatter.format("%b (%.0f states, %.3f s)\n",
                             satisfied, numstates, difftime);
          } else if (numstates < 0) {
            formatter.format("%b (%d nodes, %.3f s)\n",
                             satisfied, numnodes, difftime);
          } else {
            formatter.format("%b (%.0f states, %d nodes, %.3f s)\n",
                             satisfied, numstates, numnodes, difftime);
          }
          if (mVerbosity != null && result instanceof ProxyResult<?>) {
            final ProxyResult<?> proxyResult = (ProxyResult<?>) result;
            final Proxy proxy = proxyResult.getComputedProxy();
            if (proxy != null) {
              final String description = proxyResult.getResultDescription();
              System.out.println(SEPARATOR);
              System.out.println(description + ":");
              System.out.print(proxy);
              additions = true;
            }
          }
        } catch (final OutOfMemoryError error) {
          final long stop = System.currentTimeMillis();
          final float difftime = 0.001f * (stop - start);
          formatter.format("OUT OF MEMORY (%.3f s)\n", difftime);
          final AnalysisResult result = checker.getAnalysisResult();
          if (result != null) {
            final OverflowException overflow = new OverflowException(error);
            result.setException(overflow);
          }
        } catch (final OverflowException overflow) {
          final long stop = System.currentTimeMillis();
          final float difftime = 0.001f * (stop - start);
          formatter.format("OVERFLOW (%.3f s)\n", difftime);
        } catch (final AnalysisAbortException abort) {
          final long stop = System.currentTimeMillis();
          final float difftime = 0.001f * (stop - start);
          formatter.format("TIMEOUT (%.3f s)\n", difftime);
        }
        final AnalysisResult result = checker.getAnalysisResult();
        if (result != null) {
          if (mStats) {
            System.out.println(SEPARATOR);
            System.out.println("Statistics:");
            result.print(System.out);
            additions = true;
          }
          if (mCsv != null) {
            if (first) {
              mCsv.print("Model,");
              result.printCSVHorizontalHeadings(mCsv);
              mCsv.println();
            }
            if (fullName.indexOf(',') >= 0) {
              mCsv.print('"');
              mCsv.print(fullName);
              mCsv.print('"');
            } else {
              mCsv.print(fullName);
            }
            mCsv.print(',');
            result.printCSVHorizontal(mCsv);
            mCsv.println();
          }
        }
        if (additions) {
          System.out.println(SEPARATOR);
        }
        first = false;
      }

    } catch (final EvalException | AnalysisException |
                   WatersUnmarshalException | IOException exception) {
      showSupportedException(exception);
    } catch (final Throwable exception) {
      System.err.println("FATAL ERROR !!!");
      System.err.print(ProxyTools.getShortClassName(exception));
      System.err.println(" caught in main()!");
      exception.printStackTrace(System.err);
    } finally {
      if (mCsv != null) {
        mCsv.close();
      }
      formatter.close();
    }
  }


  //#########################################################################
  //# Configuration
  @Override
  public List<Option<?>> getOptions(final OptionPage page)
  {
    final List<Option<?>> options = new LinkedList<>();
    page.append(options, OPTION_UnifiedEFACommandLineTool_Verbose);
    page.append(options, OPTION_UnifiedEFACommandLineTool_Quiet);
    page.append(options, OPTION_UnifiedEFACommandLineTool_Stats);
    page.append(options, OPTION_UnifiedEFACommandLineTool_Timeout);
    page.append(options, OPTION_UnifiedEFACommandLineTool_Csv);
    page.append(options, OPTION_UnifiedEFACommandLineTool_Help);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(OPTION_UnifiedEFACommandLineTool_Verbose)) {
      mVerbosity = Level.ALL;
    } else if (option.hasID(OPTION_UnifiedEFACommandLineTool_Quiet)) {
      mVerbosity = Level.OFF;
    } else if (option.hasID(OPTION_UnifiedEFACommandLineTool_Stats)) {
      mStats = true;
    } else if (option.hasID(OPTION_UnifiedEFACommandLineTool_Timeout)) {
      final PositiveIntOption opt = (PositiveIntOption) option;
      mTimeout = opt.getIntValue();
    } else if (option.hasID(OPTION_UnifiedEFACommandLineTool_Csv)) {
      final FileOption opt = (FileOption) option;
      OutputStream csvstream;
      try {
        csvstream = new FileOutputStream(opt.getValue(), true);
        mCsv = new PrintWriter(csvstream);
      } catch (final FileNotFoundException exception) {
        throw new RuntimeException(exception);
      }
    } else if (option.hasID(OPTION_UnifiedEFACommandLineTool_Help)) {
      System.err.print(ProxyTools.getShortClassName(checker));
      System.err.println(" supports the following command line options:");
      mContext.showHelpMessage(System.err);
      System.exit(0);
    }
  }

  public void registerOptions(final OptionPage page) {
    page.add(new FlagOption(OPTION_UnifiedEFACommandLineTool_Verbose, null,
                            "Verbose output",
                            "-verbose", "-v"));
    page.add(new FlagOption(OPTION_UnifiedEFACommandLineTool_Quiet, null,
                            "Quiet output",
                            "-quiet", "-q"));
    page.add(new FlagOption(OPTION_UnifiedEFACommandLineTool_Stats, null,
                            "Output statistics",
                            "-stats"));
    page.add(new PositiveIntOption(OPTION_UnifiedEFACommandLineTool_Timeout, null,
                            "Output statistics",
                            "-timeout"));
    page.add(new FileOption(OPTION_UnifiedEFACommandLineTool_Csv, null,
                            "CSV output file location",
                            "-csv"));
    page.add(new FlagOption(OPTION_UnifiedEFACommandLineTool_Help, null,
                            "Print this message", "-help"));
  }

  @Override
  public void addArguments(final CommandLineOptionContext context,
                           final Configurable configurable,
                           final LeafOptionPage page)
  {
    if (configurable == this) {
      registerOptions(page);
      context.generateArgumentsFromOptions(page, this);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private static void usage()
  {
    System.err.println
      ("USAGE: java UnifiedEFACommandLineTool [options] <file> ...");
    System.exit(1);
  }

  private static void showSupportedException(final Throwable exception)
  {
    System.err.print("FATAL ERROR (");
    System.err.print(ProxyTools.getShortClassName(exception));
    System.err.println(")");
    final String msg = exception.getMessage();
    if (msg != null) {
      System.err.println(exception.getMessage());
    }
  }


  //#########################################################################
  //# Data Members
  private Level mVerbosity = Level.DEBUG;
  private boolean mStats = false;
  private int mTimeout = -1;
  private PrintWriter mCsv = null;

  private CommandLineOptionContext mContext;
  private UnifiedEFAConflictChecker checker;

  //#########################################################################
  //# Class Constants

  private static final String SEPARATOR =
    "------------------------------------------------------------";

  public static final String OPTION_UnifiedEFACommandLineTool_Verbose =
    "UnifiedEFACommandLineTool.Verbose";
  public static final String OPTION_UnifiedEFACommandLineTool_Quiet =
    "UnifiedEFACommandLineTool.Quiet";
  public static final String OPTION_UnifiedEFACommandLineTool_Stats =
    "UnifiedEFACommandLineTool.Stats";
  public static final String OPTION_UnifiedEFACommandLineTool_Timeout =
    "UnifiedEFACommandLineTool.Timeout";
  public static final String OPTION_UnifiedEFACommandLineTool_Csv =
    "UnifiedEFACommandLineTool.Csv";

  public static final String OPTION_UnifiedEFACommandLineTool_Help =
    "UnifiedEFACommandLineTool.Help";

  public static final String OPTION_UnifiedEFACommandLineTool_PreferLocal =
    "UnifiedEFACommandLineTool.PreferLocal";
  public static final String OPTION_UnifiedEFACommandLineTool_SimplifierFactory =
    "UnifiedEFACommandLineTool.SimplifierFactory";
  public static final String OPTION_UnifiedEFACommandLineTool_InternalStateLimit =
    "UnifiedEFACommandLineTool.InternalStateLimit";
  public static final String OPTION_UnifiedEFACommandLineTool_InternalTransitionLimit =
    "UnifiedEFACommandLineTool.InternalTransitionLimit";

  public static final String OPTION_UnifiedEFACommandLineTool_CompositionSelectionHeuristic =
    "UnifiedEFACommandLineTool.CompositionSelectionHeuristic";
  public static final String OPTION_UnifiedEFACommandLineTool_VariableSelectionHeuristic =
    "UnifiedEFACommandLineTool.VariableSelectionHeuristic";


}
