//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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
import java.util.LinkedList;
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

import org.xml.sax.SAXException;


/**
 * <P>An abstract base class for all assessors of the COMP552 programming
 * assignments. The assessor reads a test suite description,
 * runs all tests contained, and prints a result with recommended grades.</P>
 *
 * @author Robi Malik
 */

abstract class AbstractAssessor
{

  //#########################################################################
  //# Constructor
  AbstractAssessor()
    throws JAXBException, SAXException, IOException
  {
    mDESFactory = ProductDESElementFactory.getInstance();
    final ModuleProxyFactory moduleFactory =
      ModuleElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    final JAXBModuleMarshaller moduleMarshaller =
      new JAXBModuleMarshaller(moduleFactory, optable);
    final JAXBProductDESMarshaller desMarshaller =
      new JAXBProductDESMarshaller(mDESFactory);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerUnmarshaller(desMarshaller);
    mDocumentManager.registerUnmarshaller(moduleMarshaller);
    mFormatter = new DecimalFormat("0.000");
  }


  //#########################################################################
  //# Simple Access
  ProductDESProxyFactory getFactory()
  {
    return mDESFactory;
  }


  //#########################################################################
  //# Set Up
  void processCommandLine(final String[] args)
  {
    try {
      if (args.length < 3) {
        System.err.println
          ("USAGE: java " + ProxyTools.getShortClassName(this) +
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
      configure(outputfile, marksfile, minutes, maxBytes, secman);
      runSuite(inputfile);
      terminate();
      System.exit(0);
    } catch (final Throwable exception) {
      System.err.println("FATAL ERROR !!!");
      System.err.println(exception.getClass().getName() +
                         " caught in main()!");
      exception.printStackTrace(System.err);
      System.exit(1);
    } finally {
      close();
    }
  }

  void configure(final File reportFile,
                 final File progressFile,
                 final int minutes,
                 final long maxBytes,
                 final TeachingSecurityManager secman)
    throws JAXBException, SAXException, IOException
  {
    mSecurityManager = secman;

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

    mStream = null;
    if (!mTerminated) {
      final Thread timoutWatchdog = new TimeoutWatchdog(minutes);
      final Thread memoryWatchdog = new MemoryWatchdog(maxBytes);
      timoutWatchdog.start();
      memoryWatchdog.start();
    }
  }


  //#########################################################################
  //# Hooks
  abstract ModelChecker createChecker(ProductDESProxy des);

  abstract AbstractCounterExampleChecker createCounterExampleChecker();

  abstract TraceProxy createAlternateTrace(String name,
                                           ProductDESProxy des,
                                           List<EventProxy> events);

  abstract String getResultText(boolean result);


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

    ModelChecker checker;
    boolean result;
    double time;
    try {
      mSecurityManager.setEnabled(true);
      checker = createChecker(des);
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
      final String text = getResultText(result);
      mReportPrinter.print(text);
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
      TraceProxy trace;
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
  boolean checkCounterExample(final ProductDESProxy des,
                              final TraceProxy trace)
    throws AnalysisException
  {
    final AbstractCounterExampleChecker checker = createCounterExampleChecker();
    if (checker.checkCounterExample(des, trace)) {
      mNumCorrectTraces++;
      printGoodCounterExample(trace);
      return true;
    }
    final String diagnostics = checker.getDiagnostics();
    if (trace != null && isHalfCorrectCounterExample(des, trace, checker)) {
      mNumHalfCorrectTraces++;
      return false;
    }
    printMalformedCounterExample(trace, diagnostics);
    return false;
  }

  boolean isHalfCorrectCounterExample(final ProductDESProxy des,
                                      final TraceProxy trace,
                                      final AbstractCounterExampleChecker checker)
    throws AnalysisException
  {
    final String name = trace.getName() + ":reversed";
    final List<EventProxy> reversedList = new LinkedList<>();
    for (final EventProxy event : trace.getEvents()) {
      reversedList.add(0, event);
    }
    final TraceProxy reversedTrace =
      createAlternateTrace(name, des, reversedList);
    if (checker.checkCounterExample(des, reversedTrace)) {
      printMalformedCounterExample(trace, "is in reversed order");
      return true;
    } else {
      return false;
    }
  }

  synchronized void printMalformedCounterExample
    (final TraceProxy trace, final String msg)
  {
    final String line1, line2;
    final int splitPos = msg.indexOf('\n');
    if (splitPos >= 0) {
      line1 = msg.substring(0, splitPos);
      line2 = msg.substring(splitPos + 1);
    } else {
      line1 = msg;
      line2 = null;
    }
    mReportPrinter.print("BAD (" + line1 + ")");
    if (trace != null) {
      mReportPrinter.println(":");
      printCounterExample(trace);
      if (line2 != null) {
        mReportPrinter.println(line2);
      }
    } else {
      mReportPrinter.println();
    }
  }

  synchronized void printGoodCounterExample
    (final TraceProxy trace)
  {
    printGoodCounterExample(trace, null);
  }

  synchronized void printGoodCounterExample
    (final TraceProxy trace, final String msg)
  {
    mReportPrinter.print("ok");
    if (msg != null) {
      mReportPrinter.print(msg);
    }
    mReportPrinter.println(':');
    printCounterExample(trace);
  }

  private void printCounterExample(final TraceProxy trace)
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
        synchronized (AbstractAssessor.this) {
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
  private final ProductDESProxyFactory mDESFactory;
  private final DocumentManager mDocumentManager;
  private final NumberFormat mFormatter;
  private TeachingSecurityManager mSecurityManager;
  private PrintWriter mReportPrinter;
  private PrintWriter mProgressPrinter;

  private InputStream mStream;
  private long mStartTime;
  private int mStartIndex;
  private boolean mTerminated;
  private int mNumCorrectAnswers;
  private int mNumCorrectTraces;
  private int mNumHalfCorrectTraces;

}
