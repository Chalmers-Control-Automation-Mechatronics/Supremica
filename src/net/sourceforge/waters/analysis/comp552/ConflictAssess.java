//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   ConflictAssess
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.comp552;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.analysis.comp552.ConflictChecker;
import net.sourceforge.waters.cpp.analysis.NativeLanguageInclusionChecker;
import net.sourceforge.waters.cpp.analysis.NativeModelAnalyser;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.valid.ValidUnmarshaller;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.des.ConflictKind;

import org.xml.sax.SAXException;


/**
 * <P>A sample main class for testing the {@link ConflictChecker}
 * class. It provides a simple application that reads a test suite
 * description, runs all tests contained, and prints a result.</P>
 *
 * @author Robi Malik
 */

public class ConflictAssess
{

  //#########################################################################
  //# Constructors
  private ConflictAssess(final File outputfile,
                         final File marksfile,
                         final int minutes,
                         final TeachingSecurityManager secman)
    throws FileNotFoundException, JAXBException, SAXException
  {
    mSecurityManager = secman;
    final ModuleProxyFactory moduleFactory =
      ModuleElementFactory.getInstance();
    mDESFactory = ProductDESElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    final ValidUnmarshaller importer =
      new ValidUnmarshaller(moduleFactory, optable);
    final JAXBModuleMarshaller moduleMarshaller =
      new JAXBModuleMarshaller(moduleFactory, optable);
    final JAXBProductDESMarshaller desMarshaller =
      new JAXBProductDESMarshaller(mDESFactory);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerUnmarshaller(desMarshaller);
    mDocumentManager.registerUnmarshaller(moduleMarshaller);
    mDocumentManager.registerUnmarshaller(importer);

    final OutputStream outstream = new FileOutputStream(outputfile);
    mPrinter = new PrintWriter(outstream);
    final OutputStream marksstream = new FileOutputStream(marksfile);
    mMarker = new PrintWriter(marksstream);
    mFormatter = new DecimalFormat("0.000");

    mStream = null;
    mTerminated = false;
    mNumCorrectAnswers = 0;
    mNumCorrectTraces = 0;
    mNumReversedTraces = 0;

    final Thread terminator = new Terminator(minutes);
    terminator.start();
  }


  //#########################################################################
  //# Running Tests
  private void runSuite(final File filename)
    throws Exception
  {
    try {
      final File dirname = filename.getParentFile();
      mStream = new FileInputStream(filename);
      final Reader streamreader = new InputStreamReader(mStream);
      final BufferedReader reader = new BufferedReader(streamreader);
      System.setSecurityManager(mSecurityManager);
      String line = reader.readLine();
      while (line != null) {
        line = line.trim();
        if (!line.startsWith("#")) {
          final String[] parts = line.split(" +");
          if (parts.length >= 2) {
            final File name = new File(dirname, parts[0]);
            final boolean expect = parts[1].equals("true");
            final List<ParameterBindingProxy> bindings =
              parseBindings(parts, 2);
            runTest(name, bindings, expect);
          }
        }
        line = reader.readLine();
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
      for (int i = start; i + 1 < args.length; i++) {
        final String arg = args[i];
        final int eqpos = arg.indexOf('=', 2);
        if (eqpos > 2) {
          final String name = arg.substring(2, eqpos);
          final String text = arg.substring(eqpos + 1);
          final SimpleExpressionProxy expr = parser.parse(text);
          final ParameterBindingProxy binding =
            moduleFactory.createParameterBindingProxy(name, expr);
          bindings.add(binding);              
        }
      }
      return bindings;
    } else {
      return null;
    }
  }

  private void runTest(final File filename,
                       final List<ParameterBindingProxy> bindings,
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
      des = compiler.compile(bindings);
    }

    synchronized (this) {
      if (mTerminated) {
        return;
      }
      mPrinter.print(des.getName() + " ... ");
      mPrinter.flush();
    }

    ConflictChecker checker;
    boolean result;
    double time;
    try {
      mSecurityManager.setEnabled(true);
      checker = new ConflictChecker(des, mDESFactory);
      final long starttime = System.currentTimeMillis();
      result = checker.run();
      final long stoptime = System.currentTimeMillis();
      time = 0.001 * (stoptime - starttime);
    } catch (final OutOfMemoryError error) {
      checker = null;
      System.gc();
      printException(error);
      return;
    } catch (final Throwable exception) {
      printException(exception);
      return;
    } finally {
      mSecurityManager.setEnabled(false);
    }

    synchronized (this) {
      if (mTerminated) {
        return;
      }
      if (result) {
        mPrinter.print("nonconflicting");
      } else {
        mPrinter.print("conflicting");
      }
      if (result == expect) {
        mPrinter.print(" - ok");
        mNumCorrectAnswers++;
      } else {
        mPrinter.print(" - WRONG");
      }
      mPrinter.println(" <" + mFormatter.format(time) + "s>");
    }

    if (!result && !expect) {
      synchronized (this) {
        mPrinter.print("  Counterexample ... ");
        mPrinter.flush();
      }
      ConflictTraceProxy trace;
      try {
	mSecurityManager.setEnabled(true);
        trace = checker.getCounterExample();
      } catch (final OutOfMemoryError error) {
        checker = null;
        System.gc();
        printException(error);
        return;
      } catch (final Throwable exception) {
        printException(exception);
        return;
      } finally {
	mSecurityManager.setEnabled(false);
      }
      checkCounterExample(des, trace);
    }
  }


  //#########################################################################
  //# Counterexample Verification
  private boolean checkCounterExample(final ProductDESProxy des,
                                      final ConflictTraceProxy trace)
  {
    if (trace == null) {
      printMalformedCounterExample(trace, "is NULL");
      return false;
    }
    final List<EventProxy> traceevents = trace.getEvents();
    final Collection<EventProxy> events = des.getEvents();
    boolean containsnull = false;
    for (final EventProxy event : traceevents) {
      if (event == null) {
        containsnull = true;
      } else if (!events.contains(event)) {
        printMalformedCounterExample(trace, "contains bad events");
        return false;
      }
    }
    return checkCounterExample(des, trace, containsnull, false);
  }

  private boolean checkCounterExample(final ProductDESProxy des,
                                      final ConflictTraceProxy trace,
                                      final boolean containsnull,
                                      final boolean reversed)
  {
    final Collection<AutomatonProxy> automata = des.getAutomata();
    final int size = automata.size();
    final Map<AutomatonProxy,StateProxy> tuple =
      new HashMap<AutomatonProxy,StateProxy>(size);
    for (final AutomatonProxy aut : automata) {
      final StateProxy state = checkCounterExample(aut, trace);
      if (state == null) {
        if (!reversed && !checkReversedCounterExample(des, trace)) {
          printMalformedCounterExample
            (trace, "not accepted by component " + aut.getName());
        }
        return false;
      }
      tuple.put(aut, state);
    }
    final ProductDESProxy ldes = createLanguageInclusionModel(des, tuple);
    final LanguageInclusionChecker checker =
      new NativeLanguageInclusionChecker(ldes, mDESFactory);
    final boolean blocking;
    try {
      blocking = checker.run();
    } catch (final AnalysisException exception) {
      printException(exception);
      return false;
    }
    if (!blocking) {
      if (!reversed && !checkReversedCounterExample(des, trace)) {
        final SafetyTraceProxy ltrace = checker.getCounterExample();
        printMalformedCounterExample(trace, ltrace);
      }
      return false;
    } else if (reversed) {
      return true;      
    } else if (containsnull) {
      printGoodCounterExample(trace, " (BUT CONTAINS null)");
      mNumCorrectTraces++;
      return true;
    } else {
      printGoodCounterExample(trace);
      mNumCorrectTraces++;
      return true;
    }
  }

  private boolean checkReversedCounterExample(final ProductDESProxy des,
                                              final ConflictTraceProxy trace)
  {
    final List<EventProxy> origlist = trace.getEvents();
    final List<EventProxy> reversedlist = new LinkedList<EventProxy>();
    for (final EventProxy event : origlist) {
      reversedlist.add(0, event);
    }
    final String name = trace.getName() + ":reversed";
    final ConflictKind kind = trace.getKind();
    final ConflictTraceProxy reversedtrace =
      mDESFactory.createConflictTraceProxy(name, des, reversedlist, kind);
    if (checkCounterExample(des, reversedtrace, false, true)) {
      printGoodCounterExample(trace, " (BUT REVERSED ORDER)");
      mNumReversedTraces++;
      return true;
    } else {
      return false;
    }
  }

  private StateProxy checkCounterExample(final AutomatonProxy aut,
                                         final ConflictTraceProxy trace)
  {
    final Collection<EventProxy> events = aut.getEvents();
    final Collection<StateProxy> states = aut.getStates();
    final Collection<TransitionProxy> transitions = aut.getTransitions();
    StateProxy current = null;
    for (final StateProxy state : states) {
      if (state.isInitial()) {
        current = state;
        break;
      }
    }
    if (current == null) {
      return null;
    }
    final List<EventProxy> traceevents = trace.getEvents();
    for (final EventProxy event : traceevents) {
      if (events.contains(event)) {
        boolean found = false;
        for (final TransitionProxy trans : transitions) {
          if (trans.getSource() == current && trans.getEvent().equals(event)) {
            current = trans.getTarget();
            found = true;
            break;
          }
        }
        if (!found) {
          return null;
        }
      }
    }
    return current;
  }

  private synchronized void printMalformedCounterExample
    (final ConflictTraceProxy conftrace, final SafetyTraceProxy langtrace)
  {
    printMalformedCounterExample(conftrace, "does not lead to blocking state");
    mPrinter.println("  A marked state can be reached as follows:");
    printCounterExample(langtrace);
  }

  private synchronized void printMalformedCounterExample
    (final TraceProxy trace, final String msg)
  {
    mPrinter.println("BAD (" + msg + "):");
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
    mPrinter.print("ok");
    if (msg != null) {
      mPrinter.print(msg);
    }
    mPrinter.println(':');
    printCounterExample(trace);
  }

  private void printCounterExample
    (final TraceProxy trace)
  {
    final List<EventProxy> traceevents = trace.getEvents();
    if (traceevents.isEmpty()) {
      mPrinter.println("  <empty>");
    } else {
      final StringBuffer buffer = new StringBuffer("  ");
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
          mPrinter.println(buffer);
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
        mPrinter.println(buffer);
      }
    }
  }

  private synchronized void printException(final Throwable exception)
  {
    if (!mTerminated) {
      final String fullname = exception.getClass().getName();
      final int start = fullname.lastIndexOf('.');
      final String shortname = fullname.substring(start + 1);
      mPrinter.println(shortname);
      exception.printStackTrace(System.err);
    }
  }


  //#########################################################################
  //# Coreachability Model
  private ProductDESProxy createLanguageInclusionModel
    (final ProductDESProxy des, final Map<AutomatonProxy,StateProxy> inittuple)
  {
    final Collection<EventProxy> oldevents = des.getEvents();
    final int numevents = oldevents.size();
    final Collection<EventProxy> newevents =
      new ArrayList<EventProxy>(numevents);
    EventProxy oldmarking = null;
    EventProxy newmarking = null;
    for (final EventProxy oldevent : oldevents) {
      if (oldevent.getKind() == EventKind.PROPOSITION) {
        final String eventname = oldevent.getName();
        if (eventname.equals(EventDeclProxy.DEFAULT_MARKING_NAME)) {
          oldmarking = oldevent;
          newmarking =
            mDESFactory.createEventProxy(eventname, EventKind.UNCONTROLLABLE);
          newevents.add(newmarking);
        }
      } else {
        newevents.add(oldevent);
      }
    }
    if (oldmarking == null) {
      throw new IllegalArgumentException
        ("Default marking proposition not found in model!");
    }
    final Collection<AutomatonProxy> oldautomata = des.getAutomata();
    final int numaut = oldautomata.size();
    final Collection<AutomatonProxy> newautomata =
      new ArrayList<AutomatonProxy>(numaut + 1);
    for (final AutomatonProxy oldaut : oldautomata) {
      final StateProxy init = inittuple.get(oldaut);
      final AutomatonProxy newaut =
        createLanguageInclusionAutomaton(oldaut, init, oldmarking, newmarking);
      newautomata.add(newaut);
    }
    final AutomatonProxy prop = createPropertyAutomaton(newmarking);
    newautomata.add(prop);
    final String name = des.getName() + ":coreachability";
    return mDESFactory.createProductDESProxy(name, newevents, newautomata);
  }

  private AutomatonProxy createLanguageInclusionAutomaton
    (final AutomatonProxy aut,
     final StateProxy newinit,
     final EventProxy oldmarking,
     final EventProxy newmarking)
  {
    final Collection<EventProxy> oldevents = aut.getEvents();
    final int numevents = oldevents.size();
    final Collection<EventProxy> newevents =
      new ArrayList<EventProxy>(numevents);
    for (final EventProxy oldevent : oldevents) {
      if (oldevent == oldmarking) {
        newevents.add(newmarking);
      } else if (oldevent.getKind() != EventKind.PROPOSITION) {
        newevents.add(oldevent);
      }
    }
    final Collection<StateProxy> oldstates = aut.getStates();
    final int numstates = oldstates.size();
    final Collection<StateProxy> newstates =
      new ArrayList<StateProxy>(numstates);
    final Map<StateProxy,StateProxy> statemap =
      new HashMap<StateProxy,StateProxy>(numstates);
    final Collection<TransitionProxy> oldtransitions = aut.getTransitions();
    final int numtrans = oldtransitions.size();
    final Collection<TransitionProxy> newtransitions =
      new ArrayList<TransitionProxy>(numstates + numtrans);
    for (final StateProxy oldstate : oldstates) {
      final String statename = oldstate.getName();
      final StateProxy newstate =
        mDESFactory.createStateProxy(statename, oldstate == newinit, null);
      newstates.add(newstate);
      statemap.put(oldstate, newstate);
      if (oldstate.getPropositions().contains(oldmarking)) {
        final TransitionProxy trans =
          mDESFactory.createTransitionProxy(newstate, newmarking, newstate);
        newtransitions.add(trans);
      }
    }
    for (final TransitionProxy oldtrans : oldtransitions) {
      final StateProxy oldsource = oldtrans.getSource();
      final StateProxy newsource = statemap.get(oldsource);
      final StateProxy oldtarget = oldtrans.getTarget();
      final StateProxy newtarget = statemap.get(oldtarget);
      final EventProxy event = oldtrans.getEvent();
      final TransitionProxy newtrans =
        mDESFactory.createTransitionProxy(newsource, event, newtarget);
      newtransitions.add(newtrans);
    }
    final String autname = aut.getName();
    final ComponentKind kind = aut.getKind();
    return mDESFactory.createAutomatonProxy
      (autname, kind, newevents, newstates, newtransitions);
  }

  private AutomatonProxy createPropertyAutomaton(final EventProxy newmarking)
  {
    final String name = ":never:" + newmarking.getName();
    final Collection<EventProxy> events =
      Collections.singletonList(newmarking);
    final StateProxy state = mDESFactory.createStateProxy("s0", true, null);
    final Collection<StateProxy> states = Collections.singletonList(state);
    return mDESFactory.createAutomatonProxy
      (name, ComponentKind.PROPERTY, events, states, null);
  }


  //#########################################################################
  //# Cleaning Up
  private void printScore()
  {
    mPrinter.println();
    final double score =
      0.5 * (mNumCorrectAnswers + mNumCorrectTraces) +
      0.25 * mNumReversedTraces;
    final NumberFormat formatter =
      new DecimalFormat((mNumReversedTraces & 1) == 0 ? "0.0" : "0.00");
    final String marks = formatter.format(score);
    mPrinter.println("Recommending " + marks + " marks.");
    mMarker.println(marks);
  }

  private void close()
  {
    try {
      mPrinter.close();
      mMarker.close();
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
        mPrinter.println(msg);
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
   * Please refer to the class documentation ({@link ConflictAssess})
   * for more detailed information.
   * @param  args    Array of file names from the command line.
   */
  public static void main(String[] args)
  {
    ConflictAssess assessor = null;
    try {
      if (args.length < 3) {
        System.err.println
          ("USAGE: java " + ConflictAssess.class.getName() +
           " <input> <output> <marks> <minutes> <readable-dir> ...");
        System.exit(1);
      }
      final File inputfile = new File(args[0]);
      final File outputfile = new File(args[1]);
      final File marksfile = new File(args[2]);
      final int minutes = Integer.parseInt(args[3]);
      final TeachingSecurityManager secman = new TeachingSecurityManager();
      secman.addReadWriteDirectory("");
      for (int i = 4; i < args.length; i++) {
        secman.addReadOnlyDirectory(args[i]);
      }
      secman.close();
      assessor = new ConflictAssess(outputfile, marksfile, minutes, secman);
      assessor.runSuite(inputfile);
      assessor.terminate();
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
    System.exit(0);
  }


  //#########################################################################
  //# Local Class Terminator
  private class Terminator extends Thread
  {

    private Terminator(final int minutes)
    {
      mMinutes = minutes;
    }

    public void run()
    {
      try {
        Thread.sleep(60000 * mMinutes);
        terminate("TIMEOUT <" + mMinutes + "min overall>");
      } catch (final InterruptedException exception) {
        terminate("FATAL ERROR (InterruptedException)");
      }
      mSecurityManager.setEnabled(false);
      System.exit(0);
    }

    private final int mMinutes;

  }


  //#########################################################################
  //# Data Members
  private final TeachingSecurityManager mSecurityManager;
  private final ProductDESProxyFactory mDESFactory;
  private final DocumentManager mDocumentManager;
  private final PrintWriter mPrinter;
  private final PrintWriter mMarker;
  private final NumberFormat mFormatter;

  private InputStream mStream;
  private boolean mTerminated;
  private int mNumCorrectAnswers;
  private int mNumCorrectTraces;
  private int mNumReversedTraces;

}
