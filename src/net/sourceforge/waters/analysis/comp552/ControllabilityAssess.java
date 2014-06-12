//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   ControllabilityAssess
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.comp552;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.cpp.analysis.NativeModelAnalyzer;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.xml.sax.SAXException;


/**
 * <P>A main class for testing the {@link BDDControllabilityChecker} class.
 * It provides a simple application that reads a test suite description,
 * runs all tests contained, and prints a result with recommended grades.</P>
 *
 * @author Robi Malik
 */

public class ControllabilityAssess
{

  //#########################################################################
  //# Constructors
  private ControllabilityAssess(final File reportFile,
                         final File progressFile,
                         final int minutes,
                         final long maxBytes,
                         final TeachingSecurityManager secman)
    throws JAXBException, SAXException, IOException
  {
    mSecurityManager = secman;
    final ModuleProxyFactory moduleFactory =
      ModuleElementFactory.getInstance();
    mDESFactory = ProductDESElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    final JAXBModuleMarshaller moduleMarshaller =
      new JAXBModuleMarshaller(moduleFactory, optable);
    final JAXBProductDESMarshaller desMarshaller =
      new JAXBProductDESMarshaller(mDESFactory);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerUnmarshaller(desMarshaller);
    mDocumentManager.registerUnmarshaller(moduleMarshaller);

    mTerminated = false;
    if (progressFile.exists()) {
      boolean crash = false;
      final InputStream stream = new FileInputStream(progressFile);
      final Reader streamreader = new InputStreamReader(stream);
      final BufferedReader reader = new BufferedReader(streamreader);
      String line = reader.readLine();
      while (line != null) {
        line = line.trim();
        final String[] parts = line.split(" +");
        final String key = parts[0];
        if (key.equals("start")) {
          mStartTime = Long.parseLong(parts[1]);
        } else if (key.equals("in")) {
          mStartIndex = Integer.parseInt(parts[1]) + 1;
          crash = true;
        } else if (key.equals("abort")) {
          crash = false;
        } else if (key.equals("result")) {
          mNumCorrectAnswers = Integer.parseInt(parts[1]);
          crash = false;
        } else if (key.equals("trace")) {
          mNumCorrectTraces = Integer.parseInt(parts[1]);
          mNumHalfCorrectTraces = Integer.parseInt(parts[2]);
          crash = false;
        } else if (key.equals("done")) {
          crash = false;
          mTerminated = true;
        }
        line = reader.readLine();
      }
      stream.close();
      final OutputStream progressStream =
        new FileOutputStream(progressFile, true);
      mProgressPrinter = new PrintWriter(progressStream);
      final OutputStream reportStream = new FileOutputStream(reportFile, true);
      mReportPrinter = new PrintWriter(reportStream);
      if (crash) {
        mReportPrinter.println("CRASHED");
        mReportPrinter.flush();
      }
    } else {
      mStartTime = System.currentTimeMillis();
      mStartIndex = 0;
      mNumCorrectAnswers = 0;
      mNumCorrectTraces = 0;
      mNumHalfCorrectTraces = 0;
      final OutputStream progressStream = new FileOutputStream(progressFile);
      mProgressPrinter = new PrintWriter(progressStream);
      mProgressPrinter.println("start " + mStartTime);
      mProgressPrinter.flush();
      final OutputStream reportStream = new FileOutputStream(reportFile);
      mReportPrinter = new PrintWriter(reportStream);
    }

    mFormatter = new DecimalFormat("0.000");
    mStream = null;
    if (!mTerminated) {
      final Thread timoutWatchdog = new TimeoutWatchdog(minutes);
      final Thread memoryWatchdog = new MemoryWatchdog(maxBytes);
      timoutWatchdog.start();
      memoryWatchdog.start();
    }
  }


  //#########################################################################
  //# Running Tests
  private void runSuite(final File filename)
    throws Exception
  {
    try {
      if (!mTerminated) {
        final File dirname = filename.getParentFile();
        mStream = new FileInputStream(filename);
        final Reader streamreader = new InputStreamReader(mStream);
        final BufferedReader reader = new BufferedReader(streamreader);
        System.setSecurityManager(mSecurityManager);
        int index = 0;
        String line = reader.readLine();
        while (line != null) {
          line = line.trim();
          if (!line.startsWith("#")) {
            final String[] parts = line.split(" +");
            if (parts.length >= 2 && index >= mStartIndex) {
              final File name = new File(dirname, parts[0]);
              final boolean expect = parts[1].equals("true");
              final List<ParameterBindingProxy> bindings =
                parseBindings(parts, 2);
              runTest(name, bindings, index, expect);
              if (mTerminated) {
                return;
              }
            }
            index++;
          }
          line = reader.readLine();
        }
        terminate();
      }
    } finally {
      synchronized (this) {
        if (mStream != null) {
          mStream.close();
          mStream = null;
        }
      }
    }
  }

  private static long parseBytes(String arg)
  {
    int len = arg.length();
    final long factor ;
    if (arg.endsWith("g")) {
      len--;
      factor = 1L << 30;
    } else if (arg.endsWith("m")) {
      len--;
      factor = 1L << 20;
    } else if (arg.endsWith("k")) {
      len--;
      factor = 1L << 10;
    } else {
      factor = 1L;
    }
    if (factor > 1L) {
      arg = arg.substring(0, len);
    }
    return factor * Long.parseLong(arg);
  }

  private List<ParameterBindingProxy> parseBindings(final String[] args,
                                                    final int start)
    throws ParseException
  {
    if (start < args.length) {
      final List<ParameterBindingProxy> bindings =
        new ArrayList<ParameterBindingProxy>(args.length - start);
      final ModuleProxyFactory moduleFactory =
        ModuleElementFactory.getInstance();
      final OperatorTable optable = CompilerOperatorTable.getInstance();
      final ExpressionParser parser =
        new ExpressionParser(moduleFactory, optable);
      for (int i = start; i < args.length; i++) {
        final String arg = args[i];
        final int eqpos = arg.indexOf('=');
        final String name = arg.substring(0, eqpos);
        final String text = arg.substring(eqpos + 1);
        final SimpleExpressionProxy expr = parser.parse(text);
        final ParameterBindingProxy binding =
          moduleFactory.createParameterBindingProxy(name, expr);
        bindings.add(binding);
      }
      return bindings;
    } else {
      return null;
    }
  }

  private void runTest(final File filename,
                       final List<ParameterBindingProxy> bindings,
                       final int index,
                       final boolean expect)
    throws Exception
  {
    final DocumentProxy doc = mDocumentManager.load(filename);
    ProductDESProxy des = null;
    if (doc instanceof ProductDESProxy) {
      des = (ProductDESProxy) doc;
    } else {
      final ModuleProxy module = (ModuleProxy) doc;
      final ModuleCompiler compiler =
        new ModuleCompiler(mDocumentManager, mDESFactory, module);
      final Collection<String> empty = Collections.emptyList();
      compiler.setEnabledPropertyNames(empty);
      des = compiler.compile(bindings);
    }

    synchronized (this) {
      if (mTerminated) {
        return;
      }
      mReportPrinter.print(des.getName());
      if (bindings != null) {
        mReportPrinter.print(" <");
        boolean first = true;
        for (final ParameterBindingProxy binding : bindings) {
          if (first) {
            first = false;
          } else {
            mReportPrinter.print(", ");
          }
          mReportPrinter.print(binding);
        }
        mReportPrinter.print('>');
      }
      mReportPrinter.print(" ... ");
      mReportPrinter.flush();
      mProgressPrinter.println("in " + index);
      mProgressPrinter.flush();
    }

    BDDControllabilityChecker checker;
    boolean result;
    double time;
    try {
      mSecurityManager.setEnabled(true);
      checker = new BDDControllabilityChecker(des, mDESFactory);
      final long starttime = System.currentTimeMillis();
      result = checker.run();
      final long stoptime = System.currentTimeMillis();
      time = 0.001 * (stoptime - starttime);
      if (result == expect) {
        mNumCorrectAnswers++;
      }
      mProgressPrinter.println("result " + mNumCorrectAnswers);
    } catch (final OutOfMemoryError error) {
      checker = null;
      printException(error);
      mProgressPrinter.println("result " + mNumCorrectAnswers);
      return;
    } catch (final Throwable exception) {
      printException(exception);
      mProgressPrinter.println("result " + mNumCorrectAnswers);
      return;
    } finally {
      mProgressPrinter.flush();
      mSecurityManager.setEnabled(false);
      System.gc();  // Garbage collect all BDDs so init() can be called again.
    }

    synchronized (this) {
      if (mTerminated) {
        return;
      }
      if (result) {
        mReportPrinter.print("controllable");
      } else {
        mReportPrinter.print("not controllable");
      }
      if (result == expect) {
        mReportPrinter.print(" - ok");
      } else {
        mReportPrinter.print(" - WRONG");
      }
      mReportPrinter.println(" <" + mFormatter.format(time) + "s>");
    }

    if (!result && !expect) {
      synchronized (this) {
        mReportPrinter.print("  Counterexample ... ");
        mReportPrinter.flush();
        mProgressPrinter.println("in " + index);
        mProgressPrinter.flush();
      }
      SafetyTraceProxy trace;
      try {
        mSecurityManager.setEnabled(true);
        trace = checker.getCounterExample();
      } catch (final OutOfMemoryError error) {
        checker = null;
        System.gc();
        printException(error);
        mProgressPrinter.println
          ("trace " + mNumCorrectTraces + " " + mNumHalfCorrectTraces);
        return;
      } catch (final Throwable exception) {
        printException(exception);
        mProgressPrinter.println
          ("trace " + mNumCorrectTraces + " " + mNumHalfCorrectTraces);
        return;
      } finally {
        mProgressPrinter.flush();
        mSecurityManager.setEnabled(false);
      }
      checkCounterExample(des, trace);
      mProgressPrinter.println
        ("trace " + mNumCorrectTraces + " " + mNumHalfCorrectTraces);
    }
  }


  //#########################################################################
  //# Counterexample Verification
  private boolean checkCounterExample(final ProductDESProxy des,
                                      final SafetyTraceProxy trace)
    throws AnalysisException
  {
    if (trace == null) {
      printMalformedCounterExample(trace, "is NULL");
      return false;
    }
    final ControllabilityCounterExampleChecker checker =
      new ControllabilityCounterExampleChecker(false);
    if (checker.checkCounterExample(des, trace)) {
      printGoodCounterExample(trace);
      mNumCorrectTraces++;
      return true;
    } else {
      final String diag = checker.getDiagnostics();
      printMalformedCounterExample(trace, diag);
      return false;
    }
  }

  private synchronized void printMalformedCounterExample
    (final TraceProxy trace, final String msg)
  {
    mReportPrinter.println("BAD (" + msg + "):");
    printCounterExample(trace);
  }

  private synchronized void printGoodCounterExample
    (final TraceProxy trace)
  {
    printGoodCounterExample(trace, null);
  }

  private synchronized void printGoodCounterExample
    (final TraceProxy trace, final String msg)
  {
    mReportPrinter.print("ok");
    if (msg != null) {
      mReportPrinter.print(msg);
    }
    mReportPrinter.println(':');
    printCounterExample(trace);
  }

  private void printCounterExample
    (final TraceProxy trace)
  {
    final List<EventProxy> traceevents = trace.getEvents();
    if (traceevents.isEmpty()) {
      mReportPrinter.println("  <empty>");
    } else {
      final StringBuilder buffer = new StringBuilder("  ");
      int count = 0;
      boolean first = true;
      for (final EventProxy event : traceevents) {
        if (first) {
          first = false;
        } else {
          buffer.append(", ");
        }
        final String name = event == null ? "(null)" : event.getName();
        if (buffer.length() + name.length() > 77) {
          mReportPrinter.println(buffer);
          buffer.delete(2, buffer.length());
        }
        if (count++ <= 100) {
          buffer.append(name);
        } else {
          buffer.append("...");
          break;
        }
      }
      if (buffer.length() > 2) {
        mReportPrinter.println(buffer);
      }
    }
    mReportPrinter.flush();
  }

  private void printException(final Throwable exception)
  {
    printException(exception, false);
  }

  private synchronized void printException(final Throwable exception,
                                           final boolean crashing)
  {
    if (!mTerminated) {
      final String shortname = ProxyTools.getShortClassName(exception);
      if (crashing) {
        mReportPrinter.print(shortname);
        mReportPrinter.print(' ');
        mReportPrinter.flush();
      } else {
        mReportPrinter.println(shortname);
      }
      exception.printStackTrace(System.err);
    }
  }


  //#########################################################################
  //# Cleaning Up
  private void printScore()
  {
    mReportPrinter.println();
    final double score =
      0.5 * (mNumCorrectAnswers + mNumCorrectTraces) +
      0.25 * mNumHalfCorrectTraces;
    final NumberFormat formatter =
      new DecimalFormat((mNumHalfCorrectTraces & 1) == 0 ? "0.0" : "0.00");
    final String marks = formatter.format(score);
    mReportPrinter.println("Recommending " + marks + " marks.");
    mProgressPrinter.println("done " + mNumCorrectAnswers + " " + marks);
  }

  private void close()
  {
    try {
      mReportPrinter.close();
      mProgressPrinter.close();
      if (mStream != null) {
        mStream.close();
        mStream = null;
      }
    } catch (final IOException exception) {
      // ignore
    }
  }

  private void terminate()
  {
    terminate(null);
  }

  private synchronized void terminate(final String msg)
  {
    if (!mTerminated) {
      mTerminated = true;
      if (msg != null) {
        mReportPrinter.println(msg);
      }
      printScore();
      close();
    }
  }


  //#########################################################################
  //# Main Method for Testing
  /**
   * Main method.
   * This is a main method to check a set of files for controllability.
   * Please refer to the class documentation ({@link ControllabilityAssess})
   * for more detailed information.
   * @param  args    Array of file names from the command line.
   */
  public static void main(final String[] args)
  {
    final PrintWriter writer = new PrintWriter(System.err);
    final PatternLayout layout = new PatternLayout("%-5p %m%n");
    final Appender appender = new WriterAppender(layout, writer);
    appender.setName("stderr");
    final Logger root = Logger.getRootLogger();
    root.setLevel(Level.ERROR);
    root.addAppender(appender);

    ControllabilityAssess assessor = null;
    try {
      if (args.length < 3) {
        System.err.println
          ("USAGE: java " + ControllabilityAssess.class.getName() +
           " <input> <output> <marks> <minutes> <maxbytes> <readable-dir> ...");
        System.exit(1);
      }
      final File inputfile = new File(args[0]);
      final File outputfile = new File(args[1]);
      final File marksfile = new File(args[2]);
      final int minutes = Integer.parseInt(args[3]);
      final long maxBytes = parseBytes(args[4]);
      final TeachingSecurityManager secman = new TeachingSecurityManager();
      secman.addReadWriteDirectory("");
      for (int i = 5; i < args.length; i++) {
        secman.addReadOnlyDirectory(args[i]);
      }
      secman.addLibrary("waters");
      secman.addLibrary("buddy");
      secman.addLibrary("cudd");
      secman.close();
      assessor =
        new ControllabilityAssess(outputfile, marksfile, minutes, maxBytes, secman);
      assessor.runSuite(inputfile);
      assessor.terminate();
      System.exit(0);
    } catch (final Throwable exception) {
      System.err.println("FATAL ERROR !!!");
      System.err.println(exception.getClass().getName() +
                         " caught in main()!");
      exception.printStackTrace(System.err);
      System.exit(1);
    } finally {
      if (assessor != null) {
        assessor.close();
      }
    }
  }


  //#########################################################################
  //# Local Class TimeoutWatchdog
  private class TimeoutWatchdog extends Thread
  {

    //#######################################################################
    //# Constructor
    private TimeoutWatchdog(final int minutes)
    {
      mMinutes = minutes;
    }

    //#######################################################################
    //# Interface java.lang.Runnable
    @Override
    public void run()
    {
      try {
        final long endTime = mStartTime + 60000 * mMinutes;
        final long delta = endTime - System.currentTimeMillis();
        if (delta > 0) {
          Thread.sleep(delta);
        }
        terminate("TIMEOUT <" + mMinutes + "min overall>");
      } catch (final InterruptedException exception) {
        terminate("FATAL ERROR (InterruptedException)");
      }
      mSecurityManager.setEnabled(false);
      System.exit(0);
    }

    //#######################################################################
    //# Data Members
    private final int mMinutes;

  }


  //#########################################################################
  //# Local Class MemoryWatchdog
  private class MemoryWatchdog extends Thread
  {

    //#######################################################################
    //# Constructor
    private MemoryWatchdog(final long maxBytes)
    {
      mLimit = maxBytes;
    }

    //#######################################################################
    //# Interface java.lang.Runnable
    @Override
    public void run()
    {
      try {
        do {
          Thread.sleep(5000);
        } while (NativeModelAnalyzer.getPeakMemoryUsage() <= mLimit);
        synchronized (ControllabilityAssess.this) {
          mReportPrinter.println("OUT OF MEMORY");
          mProgressPrinter.println("abort");
        }
        close();
      } catch (final InterruptedException exception) {
        terminate("FATAL ERROR (InterruptedException)");
      } finally {
        mSecurityManager.setEnabled(false);
        System.exit(0);
      }
    }

    //#######################################################################
    //# Data Members
    private final long mLimit;

  }


  //#########################################################################
  //# Data Members
  private final TeachingSecurityManager mSecurityManager;
  private final ProductDESProxyFactory mDESFactory;
  private final DocumentManager mDocumentManager;
  private final PrintWriter mReportPrinter;
  private final PrintWriter mProgressPrinter;
  private final NumberFormat mFormatter;

  private InputStream mStream;
  private long mStartTime;
  private int mStartIndex;
  private boolean mTerminated;
  private int mNumCorrectAnswers;
  private int mNumCorrectTraces;
  private int mNumHalfCorrectTraces;

}
