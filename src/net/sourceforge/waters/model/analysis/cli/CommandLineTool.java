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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import net.sourceforge.waters.model.options.BooleanOption;
import net.sourceforge.waters.model.options.Configurable;
import net.sourceforge.waters.model.options.FileOption;
import net.sourceforge.waters.model.options.LeafOptionPage;
import net.sourceforge.waters.model.options.Option;
import net.sourceforge.waters.model.options.PositiveIntOption;
import net.sourceforge.waters.external.valid.ValidUnmarshaller;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.DefaultAnalysisResult;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.ProxyResult;
import net.sourceforge.waters.model.analysis.Watchdog;
import net.sourceforge.waters.model.analysis.des.AnalysisOperation;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.CompilerOptions;
import net.sourceforge.waters.model.compiler.EvalAbortException;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.marshaller.SAXProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.options.AnalysisOptionPage;
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

    try {
      if (args.length < 2) {
        usage();
      }

      mContext = new CommandLineOptionContext();

      final ModuleProxyFactory moduleFactory =
        ModuleElementFactory.getInstance();
      final ProductDESProxyFactory desFactory =
        ProductDESElementFactory.getInstance();
      final OperatorTable optable = CompilerOperatorTable.getInstance();
      final String wrapperName = null;

      final String factoryName = args[0];
      final List<String> argList = new LinkedList<String>();
      for (int i = 1; i < args.length; i++) {
        final String arg = args[i];
        argList.add(arg);
      }

      final ClassLoader loader = CommandLineTool.class.getClassLoader();
      final ValidUnmarshaller importer =
        new ValidUnmarshaller(moduleFactory, optable);
      final SAXModuleMarshaller moduleMarshaller =
        new SAXModuleMarshaller(moduleFactory, optable, false);
      final SAXProductDESMarshaller desMarshaller =
        new SAXProductDESMarshaller(desFactory);
      final DocumentManager docManager = new DocumentManager();
      docManager.registerUnmarshaller(desMarshaller);
      docManager.registerUnmarshaller(moduleMarshaller);
      docManager.registerUnmarshaller(importer);

      final Iterator<String> iter = argList.iterator();
      final String checkName = iter.next();
      iter.remove();

      final ModelAnalyzerFactoryLoader factoryLoader =
        EnumCommandLineArgument.parse(mContext, ModelAnalyzerFactoryLoader.class,
                                      "model analyser factory", factoryName);
      final AnalysisOperation operation =
        EnumCommandLineArgument.parse(mContext, AnalysisOperation.class,
                                      "operation", checkName);
      final ModelAnalyzerFactory factory =
        factoryLoader.getModelAnalyzerFactory();
      mAnalyzer = operation.createModelAnalyzer(factory, desFactory);
      final ModelAnalyzer wrapper;
      final boolean keepPropositions;
      if (wrapperName == null) {
        wrapper = mAnalyzer;
        keepPropositions = mAnalyzer instanceof ConflictChecker ||
                           mAnalyzer instanceof SupervisorSynthesizer;
      } else { // TODO wrappers no longer work ...
        @SuppressWarnings("unchecked")
        final Class<ModelVerifier> clazz =
          (Class<ModelVerifier>) loader.loadClass(wrapperName);
        final Package pack = CommandLineTool.class.getPackage();
        final String ifaceName = pack.getName() + ".des." + checkName;
        final Class<?> iface = loader.loadClass(ifaceName);
        final Constructor<ModelVerifier> constructor =
          clazz.getConstructor(iface, ProductDESProxyFactory.class);
        wrapper = constructor.newInstance(mAnalyzer, desFactory);
        keepPropositions = true;
      }

      final LeafOptionPage toolPage =
        mContext.createCommandLineToolOptionPage();
      mContext.registerArguments(toolPage, this, true);
      final ModuleCompiler dummyCompiler =
        new ModuleCompiler(null, null, null);
      mContext.registerArguments(WatersOptionPages.COMPILER, dummyCompiler);
      final AnalysisOptionPage analyserPage = operation.getOptionPage();
      mContext.registerArguments(analyserPage, mAnalyzer);

      final Collection<String> empty = Collections.emptyList();
      final ListIterator<String> argIter = argList.listIterator();
      mContext.parse(argIter);
      mContext.configure(this);

      if (mVerbosity != null) {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        final LoggerConfig loggerConfig =
          config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(mVerbosity);
        ctx.updateLoggers();
      }

      final Watchdog watchdog = new Watchdog(wrapper, mTimeout);
      if (mTimeout > 0 && mTimeout < Integer.MAX_VALUE) {
        watchdog.start();
      }

      for (final String name : argList) {
        final File filename = new File(name);
        final DocumentProxy doc = docManager.load(filename);
        final ProductDESProxy des;
        final String fullName;
        long compileTime = -1;
        if (doc instanceof ProductDESProxy) {
          des = (ProductDESProxy) doc;
          fullName = des.getName();
        } else {
          final long start = System.currentTimeMillis();
          final ModuleProxy module = (ModuleProxy) doc;
          final List<ParameterBindingProxy> bindings =
            CompilerOptions.PARAMETER_BINDINGS.getValue();
          fullName = ModuleCompiler.getParametrizedName(module, bindings);
          final ModuleCompiler compiler =
            new ModuleCompiler(docManager, desFactory, module);
          if (!keepPropositions) {
            compiler.setEnabledPropositionNames(empty);
          }
          compiler.setEnabledPropertyNames(empty);
          mContext.configure(compiler);
          watchdog.addAbortable(compiler);
          try {
            des = compiler.compile();
            final long stop = System.currentTimeMillis();
            compileTime = stop - start;
          } catch (final EvalAbortException exception) {
            final long stop = System.currentTimeMillis();
            compileTime = stop - start;
            showSupportedException(exception);
            final AnalysisResult result = new DefaultAnalysisResult(wrapper);
            result.setCompileTime(compileTime);
            result.setException(exception);
            writeCSV(fullName, result);
            continue;
          } finally {
            watchdog.removeAbortable(compiler);
          }
        }
        System.out.print(fullName + " ... ");
        if (mVerbosity != Level.OFF) {
          System.out.println();
        } else {
          System.out.flush();
        }

        final long start = System.currentTimeMillis();
        mContext.setProductDES(des);
        mContext.configure(mAnalyzer);
        wrapper.setModel(des);
        boolean additions = false;
        try {
          wrapper.run();
          final AnalysisResult result = wrapper.getAnalysisResult();
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
          if (result instanceof ProxyResult<?>) {
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
          final AnalysisResult result = wrapper.getAnalysisResult();
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
        final AnalysisResult result = wrapper.getAnalysisResult();
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

    } catch (final EvalException | AnalysisException |
                   WatersUnmarshalException | IOException exception) {
      showSupportedException(exception);
    } catch (final InvocationTargetException exception) {
      final Throwable cause = exception.getCause();
      showSupportedException(cause);
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
                         OPTION_CommandLineTool_Verbose);
    page.append(options, CommandLineOptionContext.
                         OPTION_CommandLineTool_Quiet);
    page.append(options, CommandLineOptionContext.
                         OPTION_CommandLineTool_Stats);
    page.append(options, CommandLineOptionContext.
                         OPTION_CommandLineTool_Timeout);
    page.append(options, CommandLineOptionContext.
                         OPTION_CommandLineTool_Csv);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(CommandLineOptionContext.
                     OPTION_CommandLineTool_Verbose)) {
      final BooleanOption flag = (BooleanOption) option;
      if (flag.getBooleanValue()) {
        mVerbosity = Level.ALL;
      }
    } else if (option.hasID(CommandLineOptionContext.
                            OPTION_CommandLineTool_Quiet)) {
      final BooleanOption flag = (BooleanOption) option;
      if (flag.getBooleanValue()) {
        mVerbosity = Level.OFF;
      }
    } else if (option.hasID(CommandLineOptionContext.
                            OPTION_CommandLineTool_Stats)) {
      final BooleanOption flag = (BooleanOption) option;
      mStats = flag.getBooleanValue();
    } else if (option.hasID(CommandLineOptionContext.
                            OPTION_CommandLineTool_Timeout)) {
      final PositiveIntOption opt = (PositiveIntOption) option;
      mTimeout = opt.getIntValue();
    } else if (option.hasID(CommandLineOptionContext.
                            OPTION_CommandLineTool_Csv)) {
      final FileOption opt = (FileOption) option;
      final File file = opt.getValue();
      if (file != null) {
        try {
          final OutputStream stream = new FileOutputStream(file, true);
          mCsv = new PrintWriter(stream);
        } catch (final FileNotFoundException exception) {
          throw new WatersRuntimeException(exception);
        }
      }
    }
  }


  //#########################################################################
  //# Auxiliary Methods
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

  private static void usage()
  {
    System.err.println
      ("USAGE: java CommandLineTool <factory>  <checker> [options] <file> ...");
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
  private boolean mCsvFirst = true;

  private CommandLineOptionContext mContext;
  private ModelAnalyzer mAnalyzer;


  //#########################################################################
  //# Class Constants
  private static final String SEPARATOR =
    "------------------------------------------------------------";

}
