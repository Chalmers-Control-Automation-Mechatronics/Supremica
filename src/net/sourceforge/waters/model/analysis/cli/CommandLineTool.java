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
import java.util.Properties;

import net.sourceforge.waters.config.Version;
import net.sourceforge.waters.external.valid.ValidUnmarshaller;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.DefaultAnalysisResult;
import net.sourceforge.waters.model.analysis.EnumFactory;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.ProxyResult;
import net.sourceforge.waters.model.analysis.Watchdog;
import net.sourceforge.waters.model.analysis.des.AnalysisOperation;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.CompilerOptions;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.SAXCounterExampleMarshaller;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.marshaller.SAXProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.options.AnalysisOptionPage;
import net.sourceforge.waters.model.options.BooleanOption;
import net.sourceforge.waters.model.options.Configurable;
import net.sourceforge.waters.model.options.EnumOption;
import net.sourceforge.waters.model.options.FileOption;
import net.sourceforge.waters.model.options.LeafOptionPage;
import net.sourceforge.waters.model.options.Option;
import net.sourceforge.waters.model.options.OptionFileManager;
import net.sourceforge.waters.model.options.PositiveIntOption;
import net.sourceforge.waters.model.options.WatersOptionPages;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.LoggerConfig;


/**
 * @author Robi Malik
 */

public class CommandLineTool implements Configurable
{

  //#########################################################################
  //# Constructors
  /**
   * Dummy constructor to prevent instantiation of this class.
   */
  private CommandLineTool()
  {
  }


  //#########################################################################
  //# Main Method for Testing
  /**
   * Main method.
   * This is the main method to parse the command line, create a model
   * analyser, and run one or more analysis/verification attempts.
   */
  public static void main(final String[] args)
  {
    final CommandLineTool tool = new CommandLineTool();
    tool.run(args);
  }

  public void run(final String[] args)
  {

    final ConfigurationFactory cfactory =
      new VerboseLogConfigurationFactory(mVerbosity);
    ConfigurationFactory.setConfigurationFactory(cfactory);
    final Formatter formatter = new Formatter(System.out);
    mFactory = ProductDESElementFactory.getInstance();

    try {
      int firstOptionIndex;
      if (args.length >= 2 && args[0].equals("@name")) {
        firstOptionIndex = 2;
        mToolName = args[1];
      } else {
        firstOptionIndex = 0;
      }
      final AnalysisOperation operation;
      if (args.length == firstOptionIndex + 1 &&
          args[firstOptionIndex].equals("-version")) {
        Version.printConsoleInfo(System.out);
        ExitException.testFriendlyExit(0);
        return;
      } else if (args.length >= firstOptionIndex + 1 &&
                 args[firstOptionIndex].equals("-c")) {
        firstOptionIndex++;
        operation = null;
      } else if (args.length >= firstOptionIndex + 2) {
        final String algorithmArg = args[firstOptionIndex++];
        final String operationArg = args[firstOptionIndex++];
        if (!algorithmArg.startsWith("-") || !operationArg.startsWith("-")) {
          usage();
        }
        final ModelAnalyzerFactoryLoader factoryLoader = parseEnumValue
          ("algorithm", ModelAnalyzerFactoryLoader.getEnumFactory(), algorithmArg);
        operation = parseEnumValue
          ("operation", AnalysisOperation.getEnumFactory(), operationArg);
        final ModelAnalyzerFactory factory =
          factoryLoader.getModelAnalyzerFactory();
        try {
          mAnalyzer = operation.createModelAnalyzer(factory, mFactory);
        } catch (final AnalysisConfigurationException exception) {
          failUnsupportedAlgorithm(operation, factory);
        }
      } else {
        usage();
        return;
      }


      final List<String> argList = new LinkedList<>();
      for (int i = firstOptionIndex; i < args.length; i++) {
        final String arg = args[i];
        argList.add(arg);
      }
      mContext = new CommandLineOptionContext();
      final LeafOptionPage toolPage =
        mContext.createCommandLineToolOptionPage();
      mContext.registerArguments(toolPage, this, true);
      final ModuleCompiler dummyCompiler =
        new ModuleCompiler(null, null, null);
      mContext.registerArguments(WatersOptionPages.COMPILER, dummyCompiler);
      if (operation != null) {
        final AnalysisOptionPage analyserPage = operation.getOptionPage();
        mContext.registerArguments(analyserPage, mAnalyzer);
      }
      final ListIterator<String> argIter = argList.listIterator();
      mContext.parse(argIter);
      mContext.configure(this);

      final ModuleProxyFactory moduleFactory =
        ModuleElementFactory.getInstance();
      final OperatorTable optable = CompilerOperatorTable.getInstance();
      final ValidUnmarshaller validUnmarshaller =
        new ValidUnmarshaller(moduleFactory, optable);
      final SAXModuleMarshaller moduleMarshaller =
        new SAXModuleMarshaller(moduleFactory, optable, false);
      final SAXProductDESMarshaller desMarshaller =
        new SAXProductDESMarshaller(mFactory);
      final SAXCounterExampleMarshaller traceMarshaller =
        new SAXCounterExampleMarshaller(mFactory);
      final DocumentManager docManager = new DocumentManager();
      docManager.registerUnmarshaller(desMarshaller);
      docManager.registerUnmarshaller(moduleMarshaller);
      docManager.registerUnmarshaller(validUnmarshaller);
      docManager.registerMarshaller(desMarshaller);
      docManager.registerMarshaller(moduleMarshaller);
      docManager.registerMarshaller(traceMarshaller);

      if (mVerbosity != null) {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        final LoggerConfig loggerConfig =
          config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(mVerbosity);
        ctx.updateLoggers();
      }

      final Watchdog watchdog = new Watchdog(mAnalyzer, mTimeout);
      if (mTimeout > 0) {
        watchdog.start();
      }

      for (final String name : argList) {
        final long start0 = System.currentTimeMillis();
        final File filename = new File(name);
        final DocumentProxy doc = docManager.load(filename);
        final String fullName;
        ProductDESProxy des;
        long compileTime = -1;
        if (doc instanceof ProductDESProxy) {
          des = (ProductDESProxy) doc;
          fullName = des.getName();
        } else {
          final long compileStart = System.currentTimeMillis();
          final ModuleProxy module = (ModuleProxy) doc;
          final List<ParameterBindingProxy> bindings =
            CompilerOptions.PARAMETER_BINDINGS.getValue();
          fullName = ModuleCompiler.getParametrizedName(module, bindings);
          final ModuleCompiler compiler =
            new ModuleCompiler(docManager, mFactory, module);
          if (operation != null) {
            operation.preConfigure(compiler);
          }
          mContext.configure(compiler);
          compiler.setOutputName(mOutputFile);
          watchdog.addAbortable(compiler);
          try {
            des = compiler.compile();
            final long stop = System.currentTimeMillis();
            compileTime = stop - compileStart;
          } catch (final OverflowException exception) {
            final long stop = System.currentTimeMillis();
            final float difftime = 0.001f * (stop - start0);
            final String label = exception.getOverflowKind().getLabel();
            formatter.format("%s (%.3f s)\n", label, difftime);
            final AnalysisResult result = new DefaultAnalysisResult(mAnalyzer);
            compileTime = stop - compileStart;
            result.setCompileTime(compileTime);
            result.setException(exception);
            writeCSV(fullName, result);
            continue;
          } finally {
            watchdog.removeAbortable(compiler);
          }
        }
        if (mAnnotator != null) {
          des = mAnnotator.apply(des);
        }
        if (operation == null) {
          if (mOutputFile != null) {
            docManager.saveAs(des, mOutputFile);
            if (mVerbosity.isLessSpecificThan(Level.INFO)) {
              formatter.format("Compiled product DES saved to %s\n",
                               mOutputFile.toString());
            }
          }
          continue;
        }

        mContext.setProductDES(des);
        mContext.configure(mAnalyzer);
        mAnalyzer.setModel(des);
        final long setUpTime = System.currentTimeMillis() - start0;
        for (int i = 0; i < mRepetitions; i++) {
          System.out.print(fullName + " ... ");
          final long start1 = System.currentTimeMillis();

          boolean additions = false;
          try {
            if (mVerbosity != Level.OFF) {
              System.out.println();
            } else {
              System.out.flush();
            }
            mAnalyzer.run();
            final AnalysisResult result = mAnalyzer.getAnalysisResult();
            final long stop = System.currentTimeMillis();
            final boolean satisfied = result.isSatisfied();
            final double numstates = result.getTotalNumberOfStates();
            final float difftime = 0.001f * (stop - start1 + setUpTime);
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
            if (result instanceof ProxyResult<?>) {
              final ProxyResult<?> proxyResult = (ProxyResult<?>) result;
              final Proxy resultProxy = proxyResult.getComputedProxy();
              if (resultProxy != null) {
                final String description = proxyResult.getResultDescription();
                if (mOutputFile != null && resultProxy instanceof DocumentProxy) {
                  final DocumentProxy resultDoc;
                  if (resultProxy instanceof ProductDESProxy &&
                    SAXModuleMarshaller.WMOD_FILE_FILTER.accept(mOutputFile)) {
                    final ProductDESProxy resultDES = (ProductDESProxy) resultProxy;
                    final ProductDESImporter importer =
                      new ProductDESImporter(moduleFactory, docManager);
                    resultDoc = importer.importModule(resultDES);
                  } else {
                    resultDoc = (DocumentProxy) resultProxy;
                  }
                  docManager.saveAs(resultDoc, mOutputFile);
                  if (mVerbosity.isLessSpecificThan(Level.INFO)) {
                    formatter.format("%s saved to %s\n",
                                     description, mOutputFile.toString());
                  }
                } else {
                  System.out.println(SEPARATOR);
                  System.out.println(description + ":");
                  System.out.print(resultProxy);
                  additions = true;
                }
              }
            }
          } catch (final OutOfMemoryError error) {
            final long stop = System.currentTimeMillis();
            final float difftime = 0.001f * (stop - start1 + setUpTime);
            formatter.format("OUT OF MEMORY (%.3f s)\n", difftime);
            final AnalysisResult result = mAnalyzer.getAnalysisResult();
            if (result != null) {
              final OverflowException overflow = new OverflowException(error);
              result.setException(overflow);
            }
            i = mRepetitions;
          } catch (final OverflowException overflow) {
            final long stop = System.currentTimeMillis();
            final float difftime = 0.001f * (stop - start1 + setUpTime);
            final String label = overflow.getOverflowKind().getLabel();
            final String msg = overflow.getMessage();
            if (mVerbosity != Level.OFF && msg != null) {
              System.out.println(msg);
            }
            formatter.format("%s (%.3f s)\n", label, difftime);
            i = mRepetitions;
          }
          final AnalysisResult result = mAnalyzer.getAnalysisResult();
          if (result != null) {
            if (compileTime >= 0) {
              result.setCompileTime(compileTime);
            }
            if (mStats) {
              System.out.println(SEPARATOR);
              System.out.println("Statistics:");
              System.out.println("Automata in model: " +
                des.getAutomata().size());
              System.out.println("Events in model: " +
                des.getEvents().size());
              result.print(System.out);
              additions = true;
            }
            writeCSV(fullName, result);
          }
          if (additions) {
            System.out.println(SEPARATOR);
          }
        }
      }

    } catch (final AnalysisException |
                   WatersUnmarshalException | IOException exception) {
      showSupportedException(exception);
    } catch (final ExitException exception) {
      System.out.println(exception.getMessage());
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
  //# Interface net.sourceforge.waters.model.options.Configurable
  @Override
  public List<Option<?>> getOptions(final LeafOptionPage page)
  {
    final List<Option<?>> options = new LinkedList<>();
    page.append(options, CommandLineOptionContext.
                         OPTION_CommandLineTool_AnnotationsFile);
    page.append(options, CommandLineOptionContext.
                         OPTION_CommandLineTool_Csv);
    page.append(options, CommandLineOptionContext.
                         OPTION_CommandLineTool_Properties);
    page.append(options, CommandLineOptionContext.
                         OPTION_CommandLineTool_Quiet);
    page.append(options, CommandLineOptionContext.
                         OPTION_CommandLineTool_Repetitions);
    page.append(options, CommandLineOptionContext.
                         OPTION_CommandLineTool_Stats);
    page.append(options, CommandLineOptionContext.
                         OPTION_CommandLineTool_Timeout);
    page.append(options, CommandLineOptionContext.
                         OPTION_CommandLineTool_Verbose);
    page.append(options, CommandLineOptionContext.
                         OPTION_CommandLineTool_Xml);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(CommandLineOptionContext.
                     OPTION_CommandLineTool_AnnotationsFile)) {
      final FileOption opt = (FileOption) option;
      final File file = opt.getValue();
      if (file != null) {
        try {
          mAnnotator = new EventAnnotator(mFactory, file);
        } catch (final FileNotFoundException exception) {
          LogManager.getLogger().error("Can't open annotations file {}.", file);
          ExitException.testFriendlyExit(1);
        } catch (final IOException exception) {
          LogManager.getLogger().error("Error reading annotations file {}: {}",
                                       file, exception.getMessage());
          ExitException.testFriendlyExit(1);
        }
      }
    } else if (option.hasID(CommandLineOptionContext.OPTION_CommandLineTool_Csv)) {
      final FileOption opt = (FileOption) option;
      final File file = opt.getValue();
      if (file != null) {
        try {
          final OutputStream stream = new FileOutputStream(file, true);
          mCsv = new PrintWriter(stream);
        } catch (final FileNotFoundException exception) {
          LogManager.getLogger().error("Can't open output file {}.", file);
          ExitException.testFriendlyExit(1);
        }
      }
    } else if (option.hasID(CommandLineOptionContext.
                            OPTION_CommandLineTool_Properties)) {
      final FileOption opt = (FileOption) option;
      final File file = opt.getValue();
      if (file != null) {
        try {
          final Properties properties = OptionFileManager.loadProperties(file);
          WatersOptionPages.WATERS_ROOT.loadProperties(properties);
        } catch (final FileNotFoundException exception) {
          LogManager.getLogger().error("Properties file {} not found.", file);
          ExitException.testFriendlyExit(1);
        } catch (final IOException exception) {
          LogManager.getLogger().error("Error reading properties file {}.", file);
          ExitException.testFriendlyExit(1);
        }
      }
    } else  if (option.hasID(CommandLineOptionContext.
                             OPTION_CommandLineTool_Quiet)) {
      final BooleanOption flag = (BooleanOption) option;
      if (flag.getBooleanValue()) {
        mVerbosity = Level.OFF;
      }
    } else if (option.hasID(CommandLineOptionContext.
                            OPTION_CommandLineTool_Repetitions)) {
      final PositiveIntOption opt = (PositiveIntOption) option;
      mRepetitions = opt.getIntValue();
    } else if (option.hasID(CommandLineOptionContext.
                            OPTION_CommandLineTool_Stats)) {
      final BooleanOption flag = (BooleanOption) option;
      mStats = flag.getBooleanValue();
    } else if (option.hasID(CommandLineOptionContext.
                            OPTION_CommandLineTool_Timeout)) {
      final PositiveIntOption opt = (PositiveIntOption) option;
      final int timeout = opt.getIntValue();
      if (timeout < Integer.MAX_VALUE) {
        mTimeout = timeout;
      }
    } else if (option.hasID(CommandLineOptionContext.
                            OPTION_CommandLineTool_Verbose)) {
      final BooleanOption flag = (BooleanOption) option;
      if (flag.getBooleanValue()) {
        mVerbosity = Level.ALL;
      }
    } else if (option.hasID(CommandLineOptionContext.
                            OPTION_CommandLineTool_Xml)) {
      final FileOption opt = (FileOption) option;
      mOutputFile = opt.getValue();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private <E extends Enum<E>> E parseEnumValue(final String description,
                                               final EnumFactory<E> factory,
                                               final String text)
  {
    final EnumOption<E> option =
      new EnumOption<>("cli." + description, description, factory);
    try {
      option.set(text);
    } catch (final ParseException exception) {
      System.err.println(exception.getMessage());
      option.dumpEnumeration(System.err, 0);
      ExitException.testFriendlyExit(1);
    }
    return option.getValue();
  }

  private void writeCSV(final String fullName, final AnalysisResult result)
  {
    if (mCsv != null) {
      if (mCsvFirst) {
        mCsvFirst = false;
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

  private void usage()
  {
    System.err.println("USAGE:");
    System.err.print(mToolName);
    System.err.println(" -c [options] <file> ...");
    System.err.print(mToolName);
    System.err.println(" <algorithm> <operation> [options] <file> ...");
    ModelAnalyzerFactoryLoader.getEnumFactory().
      dumpEnumeration(System.err, 0, "algorithms", false);
    AnalysisOperation.getEnumFactory().
      dumpEnumeration(System.err, 0, "operations", false);
    ExitException.testFriendlyExit(1);
  }

  private void failUnsupportedAlgorithm(final AnalysisOperation operation,
                                        final ModelAnalyzerFactory factory)
  {
    System.err.print(ProxyTools.getShortClassName(factory));
    System.err.print(" does not support ");
    System.err.print(operation.getLongAnalysisName());
    System.err.println(".");
    final AnalysisOptionPage page = operation.getOptionPage();
    final EnumOption<ModelAnalyzerFactoryLoader> selector =
      page.getCurrentPageSelectorOption();
    selector.dumpEnumeration(System.err, 0, "algorithms", false);
    ExitException.testFriendlyExit(1);
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
  private Level mVerbosity = Level.INFO;
  private boolean mStats = false;
  private int mTimeout = -1;
  private int mRepetitions = 1;
  private PrintWriter mCsv = null;
  private EventAnnotator mAnnotator;
  private File mOutputFile = null;
  private String mToolName =
    "java " + ProxyTools.getShortClassName(CommandLineTool.class);
  private boolean mCsvFirst = true;

  private ProductDESProxyFactory mFactory;
  private CommandLineOptionContext mContext;
  private ModelAnalyzer mAnalyzer;


  //#########################################################################
  //# Class Constants
  private static final String SEPARATOR =
    "------------------------------------------------------------";

}
