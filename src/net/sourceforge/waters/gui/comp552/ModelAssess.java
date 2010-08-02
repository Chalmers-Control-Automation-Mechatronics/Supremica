//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Teaching Support
//# PACKAGE: net.sourceforge.waters.analysis.comp552
//# CLASS:   ModelAssess
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.comp552;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.analysis.monolithic.MonolithicControlLoopChecker;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.cpp.analysis.NativeLanguageInclusionChecker;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.renderer.EPSGraphPrinter;
import net.sourceforge.waters.gui.renderer.ModuleRenderingContext;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.renderer.RenderingContext;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.CommandLineTool;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.IsomorphismChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.xml.sax.SAXException;


/**
 * @author Robi Malik
 */
public class ModelAssess
{

  //#########################################################################
  //# Main Program Entry Point
  public static void main(final String[] args)
  {
    if (args.length != 4) {
      usage();
    }
    final String config = args[0];
    final String classlist = args[1];
    final String inputdir = args[2];
    final String outputdir = args[3];

    try {
      final ClassLoader loader = CommandLineTool.class.getClassLoader();
      try {
        final Class<?> lclazz = loader.loadClass(LOGGERFACTORY);
        final Method method0 = lclazz.getMethod("getInstance");
        final Object loggerfactory = method0.invoke(null);
        final Method method = lclazz.getMethod("logToNull");
        method.invoke(loggerfactory);
      } catch (final ClassNotFoundException exception) {
        // No loggers---no trouble ...
      }
      final ModelAssess assess =
        new ModelAssess(config, classlist, inputdir, outputdir);
      assess.run();
    } catch (final IOException exception) {
      System.err.println(ProxyTools.getShortClassName(exception) + " caught!");
      final String msg = exception.getMessage();
      if (msg != null && msg.length() > 0) {
        System.err.println(msg);
      }
      System.exit(1);
    } catch (final Throwable exception) {
      System.err.println("FATAL ERROR:" +
                         ProxyTools.getShortClassName(exception) +
                         " caught in main()!");
      final String msg = exception.getMessage();
      if (msg != null && msg.length() > 0) {
        System.err.println(msg);
      }
      exception.printStackTrace(System.err);
      System.exit(1);
    }
  }

  private static void usage()
  {
    System.err.println
      ("USAGE: java ModelAssess <config> <classlist> <inputdir> <outputdir>");
    System.exit(1);
  }


  //#########################################################################
  //# Constructor
  private ModelAssess(final String config,
                      final String classlist,
                      final String inputdir,
                      final String outputdir)
    throws IOException, JAXBException, SAXException,
           WatersUnmarshalException, EvalException
  {
    mStudents = new LinkedList<Student>();
    mSolutions = new LinkedList<Solution>();
    mDefaultSolution = new Solution(null);
    mInputDirectory = new File(inputdir);
    mOutputDirectory = new File(outputdir);
    if (!mOutputDirectory.exists()) {
      mOutputDirectory.mkdirs();
    }
    mFactory = ProductDESElementFactory.getInstance();
    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    final JAXBModuleMarshaller marshaller =
      new JAXBModuleMarshaller(factory, optable);
    mMarshaller = marshaller;
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(marshaller);
    mDocumentManager.registerUnmarshaller(marshaller);
    mSimpleExpressionCompiler =
      new SimpleExpressionCompiler(factory, optable, false);
    mIsomorphismChecker = new IsomorphismChecker(mFactory, true);
    loadConfiguration(config);
    loadClassList(classlist);
  }


  //#########################################################################
  //# Main Invocation
  private void run()
    throws FileNotFoundException
  {
    final String extension = mMarshaller.getDefaultExtension();
    final File outfile = new File(mOutputDirectory, "assess.tex");
    final OutputStream outstream = new FileOutputStream(outfile);
    mOutput = new PrintStream(outstream);
    mAutomatonIndex = 0;
    try {
      for (final Student student : mStudents) {
        student.printHeader(mOutput);
        final File[] submitted = mInputDirectory.listFiles(student);
        if (submitted == null || submitted.length == 0) {
          mOutput.println("\\bigskip");
          mOutput.println("{\\bf\\itshape Nothing submitted!}");
          mOutput.println();
        } else {
          for (final File file : submitted) {
            final String name = file.getName();
            if (name.endsWith(extension)) {
              processModule(file);
            } else if (name.endsWith("coversheet.rtf")) {
              // ignore
            } else {
              mOutput.println
              ("{\\bf\\itshape Unknown file type: {\\tt " + name + "}}!");
              mOutput.println();
            }
          }
        }
      }
    } finally {
      mOutput.close();
    }
  }


  //#########################################################################
  //# Processing the Configuration
  private void loadConfiguration(final String config)
    throws IOException, WatersUnmarshalException, EvalException
  {
    final File file = new File(config);
    final File dir = file.getAbsoluteFile().getParentFile();
    final FileReader stream = new FileReader(file);
    try {
      final BufferedReader reader = new BufferedReader(stream);
      mSolutions.clear();
      Solution sol = null;
      while (true) {
        final String line = reader.readLine();
        if (line == null) {
          break;
        }
        final String[] words = line.split(" ");
        final String key = words[0];
        if (key.equals("solution")) {
          final File modfile = new File(dir, words[1]);
          final URI uri = modfile.toURI();
          final ModuleProxy module = mMarshaller.unmarshal(uri);
          final ModuleCompiler compiler =
            new ModuleCompiler(mDocumentManager, mFactory, module);
          final ProductDESProxy des = compiler.compile();
          sol = new Solution(des);
          mSolutions.add(sol);
        } else if (key.equals("plants")) {
          final float marks = Float.parseFloat(words[1]);
          new PlantTest(sol, marks);
        } else if (key.equals("observability")) {
          final float marks = Float.parseFloat(words[1]);
          new ObservabilityTest(sol, marks);
        } else if (key.equals("controllability")) {
          final float marks = Float.parseFloat(words[1]);
          new ControllabilityTest(sol, marks);
        } else if (key.equals("nonblocking")) {
          if (words.length == 2) {
            final float marks = Float.parseFloat(words[1]);
            new ConflictTest(sol, marks);
          } else if (words.length == 3) {
            final String propname = words[1];
            final float marks = Float.parseFloat(words[2]);
            new ConflictTest(sol, marks, propname);
          }
        } else if (key.equals("loop")) {
          final float marks = Float.parseFloat(words[1]);
          new LoopTest(sol, marks);
        } else if (key.equals("inclusion")) {
          final String propname = words[1];
          final float marks = Float.parseFloat(words[2]);
          new LanguageInclusionTest(sol, marks, propname);
        } else if (key.equals("exclusion")) {
          final String propname = words[1];
          final float marks = Float.parseFloat(words[2]);
          new LanguageExclusionTest(sol, marks, propname);
        }
      }
    } finally {
      stream.close();
    }
  }

  private void loadClassList(final String classlist)
    throws IOException
  {
    final FileReader stream = new FileReader(classlist);
    try {
      final BufferedReader reader = new BufferedReader(stream);
      mStudents.clear();
      while (true) {
        final String line = reader.readLine();
        if (line == null) {
          break;
        }
        final String[] words = line.split("\t");
        if (words.length == 3) {
          final Student student = new Student(words[0], words[1], words[2]);
          mStudents.add(student);
        }
      }
    } finally {
      stream.close();
    }
  }


  //#########################################################################
  //# Processing Modules
  private void processModule(final File file)
  {
    mOutput.print("\\subsection*{Module {\\tt ");
    printLaTeXString(file.getName(), false);
    mOutput.println("}}");
    mOutput.println();
    mSkip = false;
    try {
      final URI uri = file.toURI();
      final ModuleProxy module = mMarshaller.unmarshal(uri);
      printComment(module);
      final ModuleCompiler compiler =
        new ModuleCompiler(mDocumentManager, mFactory, module);
      compiler.setSourceInfoEnabled(true);
      final ProductDESProxy des = compiler.compile();
      final Solution sol = findSolution(des);
      final List<AutomatonProxy> printable = new LinkedList<AutomatonProxy>();
      printProductDES(des, sol, printable);
      printGraphs(printable, compiler);
      sol.runTests(des);
    } catch (final WatersUnmarshalException exception) {
      showException(exception);
    } catch (final IOException exception) {
      showException(exception);
    } catch (final EvalException exception) {
      showException(exception);
    }
  }

  private Solution findSolution(final ProductDESProxy des)
  {
    int best = 0;
    Solution result = mDefaultSolution;
    for (final Solution solution : mSolutions) {
      final int count = solution.getNumberOfMatches(des);
      if (count > 0) {
        best = count;
        result = solution;
      } else if (count == best) {
        result = mDefaultSolution;
      }
    }
    return result;
  }

  private void printComment(final ModuleProxy module)
  {
    skip("\\medskip");
    final String comment = module.getComment();
    if (comment == null || comment.length() == 0) {
      mOutput.println("{\\bf\\itshape No comments!}");
      mOutput.println();
    } else {
      printLaTeXString(comment, true);
    }
  }

  private void printProductDES(final ProductDESProxy des,
                               final Solution sol,
                               final List<AutomatonProxy> printable)
  {
    skip("\\medskip");
    final List<EventProxy> events = new ArrayList<EventProxy>(des.getEvents());
    Collections.sort(events);
    printEventNames(events, EventKind.CONTROLLABLE);
    printEventNames(events, EventKind.UNCONTROLLABLE);
    final List<AutomatonProxy> automata =
      new ArrayList<AutomatonProxy>(des.getAutomata());
    Collections.sort(automata);
    printAutomataNames(automata, sol, ComponentKind.PLANT, printable);
    printAutomataNames(automata, sol, ComponentKind.SPEC, printable);
    printAutomataNames(automata, sol, ComponentKind.SUPERVISOR, printable);
    printAutomataNames(automata, sol, ComponentKind.PROPERTY, printable);
  }

  private void printEventNames(final List<EventProxy> events,
                               final EventKind kind)
  {
    boolean found = false;
    for (final EventProxy event : events) {
      if (event.getKind() == kind) {
        if (!found) {
          found = true;
          final String kindname =
            ModuleContext.getEventKindToolTip(kind, false);
          mOutput.println("{\\bf " + kindname + " Events:}\\quad");
        } else {
          mOutput.println(',');
        }
        final char latex = kind == EventKind.CONTROLLABLE ? 'c' : 'u';
        mOutput.print("\\" + latex + "event{");
        printLaTeXName(event);
        mOutput.print('}');
      }
    }
    if (found) {
      mOutput.println('.');
      mOutput.println();
    }
  }

  private void printAutomataNames(final List<AutomatonProxy> automata,
                                  final Solution sol,
                                  final ComponentKind kind,
                                  final List<AutomatonProxy> printable)
  {
    final boolean plant = (kind == ComponentKind.PLANT);
    if (plant) {
      sol.resetPlantCheck();
    }
    boolean found = false;
    for (final AutomatonProxy aut : automata) {
      if (aut.getKind() == kind) {
        if (!found) {
          found = true;
          final String kindname = getComponentKindNamePlural(kind);
          mOutput.println("{\\bf " + kindname + ":}\\quad");
        } else {
          mOutput.println(',');
        }
        mOutput.print("\\automaton{");
        printLaTeXName(aut);
        mOutput.print('}');
        if (!plant || !sol.checkPlant(aut)) {
          printable.add(aut);
        }
      }
    }
    if (plant) {
      sol.checkPlantMatched();
    }
    if (found) {
      mOutput.println('.');
      mOutput.println();
    }
  }

  private void printGraphs(final List<AutomatonProxy> automata,
                           final ModuleCompiler compiler)
    throws IOException
  {
    if (!automata.isEmpty()) {
      mOutput.println("\\begin{center}");
      final ModuleProxy module = compiler.getInputModule();
      final ModuleContext mcontext = new ModuleContext(module);
      final RenderingContext rcontext = new ModuleRenderingContext(mcontext);
      final Map<Proxy,SourceInfo> infomap = compiler.getSourceInfoMap();
      for (final AutomatonProxy aut : automata) {
        final SourceInfo info = infomap.get(aut);
        final SimpleComponentProxy comp =
          (SimpleComponentProxy) info.getSourceObject();
        final GraphProxy graph = comp.getGraph();
        final BindingContext bindings = info.getBindingContext();
        final ProxyShapeProducer shaper =
          new ProxyShapeProducer(graph, rcontext,
                                 mSimpleExpressionCompiler, bindings);
        mAutomatonIndex++;
        final String epsname = mAutomatonIndex + ".eps";
        final File epsfile = new File(mOutputDirectory, epsname);
        final EPSGraphPrinter printer =
          new EPSGraphPrinter(graph, shaper, epsfile);
        printer.print();
        final ComponentKind kind = aut.getKind();
        final String kindname = ModuleContext.getComponentKindToolTip(kind);
        mOutput.print("\\autimage{" + epsname + "}{{\\bf " + kindname + "} ");
        printLaTeXName(aut);
        mOutput.println('}');
      }
      mOutput.println("\\end{center}");
      mOutput.println();
      mSkip = false;
    }
  }


  //#########################################################################
  //# LaTeX
  private void skip(final String cmd)
  {
    if (mSkip) {
      mOutput.println(cmd);
    } else {
      mSkip = true;
    }
  }

  private void printLaTeXName(final NamedProxy named)
  {
    final String name = named.getName();
    printLaTeXString(name, false);
  }

  private void printLaTeXString(final String text, final boolean newline)
  {
    final int len = text.length();
    boolean blank = true;
    boolean space = true;
    for (int i = 0; i < len; i++) {
      final char ch = text.charAt(i);
      switch (ch) {
      case '\n':
        if (!blank) {
          mOutput.println();
          mOutput.println();
          blank = space = true;
        }
        break;
      case ' ':
      case '\t':
        if (!blank && !space) {
          mOutput.print(' ');
          space = true;
        }
        break;
      case '"':
        if (space) {
          mOutput.print("``");
          blank = space = false;
        } else {
          mOutput.print("''");
        }
        break;
      case '\'':
        if (space) {
          mOutput.print("`");
          blank = space = false;
        } else {
          mOutput.print("'");
        }
        break;
      case '-':
        if (space && isSpace(text, i + 1)) {
          mOutput.print("---");
        } else {
          mOutput.print(ch);
        }
        blank = space = false;
        break;
      case '#':
      case '$':
      case '&':
      case '%':
        mOutput.print('\\');
        mOutput.print(ch);
        blank = space = false;
        break;
      case '_':
        mOutput.print("\\uscore{}");
        blank = space = false;
        break;
      case '^':
      case '~':
        mOutput.print('\\');
        mOutput.print(ch);
        mOutput.print("{}");
        blank = space = false;
        break;
      case '\\':
        mOutput.print("$\\backslash$");
        blank = space = false;
        break;
      case '<':
      case '>':
        mOutput.print('$');
        mOutput.print(ch);
        mOutput.print('$');
        blank = space = false;
        break;
      case '{':
      case '}':
        mOutput.print("$\\");
        mOutput.print(ch);
        mOutput.print('$');
        blank = space = false;
        break;
      default:
        mOutput.print(ch);
        blank = space = false;
        break;
      }
    }
    if (newline && !blank) {
      mOutput.println();
      mOutput.println();
    }
  }

  private boolean isSpace(final String text, final int index)
  {
    if (index >= text.length()) {
      return true;
    } else {
      final char ch = text.charAt(index);
      return ch == ' ' || ch == '\n' || ch == '\t';
    }
  }


  //#########################################################################
  //# Error Reporting
  private void showException(final Throwable exception)
  {
    final String ename = ProxyTools.getShortClassName(exception);
    mOutput.println("{\\bf\\itshape " + ename + " caught!}");
    final String msg = exception.getMessage();
    if (msg != null && msg.length() > 0) {
      mOutput.println("\\\\");
      mOutput.println(msg);
    }
    mOutput.println();
  }


  //#########################################################################
  //# Auxiliary Static Methods
  private static EventProxy getSecondaryMarking(final ProductDESProxy des)
  {
    EventProxy result = null;
    for (final EventProxy event : des.getEvents()) {
      if (event.getKind() == EventKind.PROPOSITION &&
          !event.getName().equals(EventDeclProxy.DEFAULT_MARKING_NAME)) {
        if (result == null) {
          result = event;
        } else {
          return null;
        }
      }
    }
    return result;
  }

  private static String getComponentKindNamePlural(final ComponentKind kind)
  {
    switch (kind) {
    case PLANT:
      return "Plants";
    case SPEC:
      return "Specifications";
    case PROPERTY:
      return "Properties";
    case SUPERVISOR:
      return "Supervisors";
    default:
      throw new IllegalArgumentException
        ("Unknown component kind " + kind + "!");
    }
  }


  //#########################################################################
  //# Inner Class Solution
  private class Solution
  {

    //#######################################################################
    //# Constructor
    private Solution(final ProductDESProxy des)
    {
      mProductDES = des;
      if (des == null) {
        mEventMap = Collections.emptyMap();
        mPlantMap = Collections.emptyMap();
      } else {
        final Collection<EventProxy> events = des.getEvents();
        mEventMap = new HashMap<String,EventProxy>(events.size());
        for (final EventProxy event : events) {
          final String name = event.getName();
          final String lower = name.toLowerCase();
          mEventMap.put(lower, event);
        }
        final Collection<AutomatonProxy> automata = des.getAutomata();
        mPlantMap = new HashMap<String,AutomatonProxy>(automata.size());
      }
      mTests = new LinkedList<AbstractTest>();
    }

    //#######################################################################
    //# Accessing Tests
    private void addTest(final AbstractTest test)
    {
      mTests.add(test);
    }

   private float runTests(final ProductDESProxy des)
    {
      skip("\\medskip");
      float marks = 0.0f;
      for (final AbstractTest test : mTests) {
        marks += test.run(des);
      }
      skip("\\smallskip");
      mOutput.printf("Recommending %.1f marks.", marks);
      mOutput.println();
      return marks;
    }

    //#######################################################################
    //# Event Check
    private int getNumberOfMatches(final ProductDESProxy des)
    {
      int matches = 0;
      if (mEventMap != null) {
        for (final EventProxy event : des.getEvents()) {
          if (event.getKind() != EventKind.PROPOSITION) {
            final String name = event.getName();
            if (getEvent(name) != null) {
              matches++;
            }
          }
        }
      }
      return matches;
    }

    private EventProxy getEvent(final String name)
    {
      if (mEventMap == null) {
        return null;
      } else {
        final String lower = name.toLowerCase();
        return mEventMap.get(lower);
      }
    }

    //#######################################################################
    //# Plant Check
    private void resetPlantCheck()
    {
      mPlantOK = true;
      if (mProductDES != null) {
        for (final AutomatonProxy aut : mProductDES.getAutomata()) {
          if (aut.getKind() == ComponentKind.PLANT) {
            final String name = aut.getName();
            final String lower = name.toLowerCase();
            mPlantMap.put(lower, aut);
          }
        }
      }
    }

    private boolean checkPlant(final AutomatonProxy aut)
    {
      if (mProductDES == null) {
        return true;
      } else {
        final String name0 = aut.getName();
        final String lower0 = name0.toLowerCase();
        final AutomatonProxy plant0 = mPlantMap.get(lower0);
        if (plant0 != null) {
          try {
            mIsomorphismChecker.checkIsomorphism(aut, plant0);
            mPlantMap.remove(lower0);
            return true;
          } catch (final AnalysisException exception) {
            // not yet found ...
          }
        }
        for (final Map.Entry<String,AutomatonProxy> entry :
             mPlantMap.entrySet()) {
          try {
            final AutomatonProxy plant1 = entry.getValue();
            mIsomorphismChecker.checkIsomorphism(aut, plant1);
            final String lower1 = entry.getKey();
            mPlantMap.remove(lower1);
            return true;
          } catch (final AnalysisException exception) {
            // not yet found ...
          }
        }
        if (plant0 == null) {
          mOutput.print(" {\\bf (Added?)}");
        } else {
          mOutput.print(" {\\bf (Modified?)}");
        }
        return mPlantOK = false;
      }
    }

    private boolean checkPlantMatched()
    {
      if (mPlantMap.isEmpty()) {
        return true;
      } else {
        mOutput.print(" {\\bf (Missing: }");
        final List<AutomatonProxy> missing =
          new ArrayList<AutomatonProxy>(mPlantMap.values());
        Collections.sort(missing);
        boolean first = true;
        for (final AutomatonProxy plant : missing) {
          if (first) {
            first = false;
          } else {
            mOutput.print(", ");
          }
          printLaTeXName(plant);
        }
        mOutput.print(')');
        return mPlantOK = false;
      }
    }

    private boolean isPlantOK()
    {
      return mPlantOK;
    }

    //#######################################################################
    //# Model Creation
    ProductDESProxy createInclusionModel(final ProductDESProxy des,
                                         final String propname,
                                         final ComponentKind kind)
    {
      AutomatonProxy prop1 = null;
      for (final AutomatonProxy aut : mProductDES.getAutomata()) {
        if (aut.getName().equals(propname)) {
          prop1 = aut;
        }
      }
      if (prop1 == null) {
        throw new IllegalArgumentException
          ("Product DES " + mProductDES.getName() +
           " does not contain any property named " + propname + "!");
      }

      final String desname = des.getName();
      final Collection<EventProxy> events0 = des.getEvents();
      final Collection<EventProxy> events =
        new ArrayList<EventProxy>(events0.size() + 1);
      events.addAll(events0);
      final Map<EventProxy,EventProxy> eventmap =
        new HashMap<EventProxy,EventProxy>(events0.size());
      for (final EventProxy event : events) {
        final String name = event.getName();
        final String lower = name.toLowerCase();
        final EventProxy source = mEventMap.get(lower);
        if (source != null) {
          eventmap.put(source, event);
        }
      }
      for (final EventProxy source : mProductDES.getEvents()) {
        if (!eventmap.containsKey(source)) {
          events.add(source);
        }
      }
      final Collection<AutomatonProxy> automata0 = des.getAutomata();
      final Collection<AutomatonProxy> automata =
        new ArrayList<AutomatonProxy>(automata0.size() + 1);
      for (final AutomatonProxy aut : automata0) {
        switch (aut.getKind()) {
        case PLANT:
        case SPEC:
          automata.add(aut);
          break;
        default:
          break;
        }
      }
      final AutomatonProxy prop = replaceEvents(prop1, eventmap, kind);
      automata.add(prop);
      return mFactory.createProductDESProxy(desname, events, automata);
    }

    ProductDESProxy createExclusionModel(final ProductDESProxy des,
                                         final String propname)
    {
      AutomatonProxy prop1 = null;
      for (final AutomatonProxy aut : mProductDES.getAutomata()) {
        if (aut.getName().equals(propname)) {
          prop1 = aut;
        }
      }
      if (prop1 == null) {
        throw new IllegalArgumentException
          ("Product DES " + mProductDES.getName() +
           " does not contain any property named " + propname + "!");
      }

      final String desname = des.getName();
      final Collection<EventProxy> events0 = des.getEvents();
      final Collection<EventProxy> events =
        new ArrayList<EventProxy>(events0.size() + 1);
      events.addAll(events0);
      final Map<EventProxy,EventProxy> eventmap =
        new HashMap<EventProxy,EventProxy>(events0.size());
      for (final EventProxy event : events) {
        final String name = event.getName();
        final String lower = name.toLowerCase();
        final EventProxy source = mEventMap.get(lower);
        if (source != null) {
          eventmap.put(source, event);
        }
      }
      for (final EventProxy source : mProductDES.getEvents()) {
        if (!eventmap.containsKey(source)) {
          events.add(source);
        }
      }
      final Collection<AutomatonProxy> automata0 = des.getAutomata();
      final Collection<AutomatonProxy> automata =
        new ArrayList<AutomatonProxy>(automata0.size() + 1);
      for (final AutomatonProxy aut0 : automata0) {
        switch (aut0.getKind()) {
        case PLANT:
        case SPEC:
          final String autname = aut0.getName();
          final Collection<EventProxy> autevents = aut0.getEvents();
          final Collection<StateProxy> states = aut0.getStates();
          final Collection<TransitionProxy> transitions = aut0.getTransitions();
          final AutomatonProxy aut = mFactory.createAutomatonProxy
            (autname, ComponentKind.PROPERTY, autevents, states, transitions);
          automata.add(aut);
          break;
        default:
          break;
        }
      }
      final AutomatonProxy prop =
        replaceEvents(prop1, eventmap, ComponentKind.PLANT);
      automata.add(prop);
      return mFactory.createProductDESProxy(desname, events, automata);
    }

    AutomatonProxy replaceEvents(final AutomatonProxy aut,
                                 final Map<EventProxy,EventProxy> eventmap,
                                 final ComponentKind kind)
    {
      final String autname = aut.getName();
      final Collection<EventProxy> events0 = aut.getEvents();
      final Collection<EventProxy> events1 = getReplacement(eventmap, events0);
      final Collection<StateProxy> states0 = aut.getStates();
      final Collection<StateProxy> states1 =
        new ArrayList<StateProxy>(states0.size());
      final Map<StateProxy,StateProxy> statemap =
        new HashMap<StateProxy,StateProxy>(states0.size());
      for (final StateProxy state0 : states0) {
        final String name = state0.getName();
        final boolean initial = state0.isInitial();
        final Collection<EventProxy> props0 = state0.getPropositions();
        final Collection<EventProxy> props1 = getReplacement(eventmap, props0);
        final StateProxy state1 =
          mFactory.createStateProxy(name, initial, props1);
        states1.add(state1);
        statemap.put(state0, state1);
      }
      final Collection<TransitionProxy> transitions0 = aut.getTransitions();
      final Collection<TransitionProxy> transitions1 =
        new ArrayList<TransitionProxy>(transitions0.size());
      for (final TransitionProxy trans0 : transitions0) {
        final StateProxy source0 = trans0.getSource();
        final StateProxy source1 = statemap.get(source0);
        final EventProxy event0 = trans0.getEvent();
        final EventProxy event1 = getReplacement(eventmap, event0);
        final StateProxy target0 = trans0.getTarget();
        final StateProxy target1 = statemap.get(target0);
        final TransitionProxy trans1 =
          mFactory.createTransitionProxy(source1, event1, target1);
        transitions1.add(trans1);
      }
      return mFactory.createAutomatonProxy
        (autname, kind, events1, states1, transitions1);
    }

    Collection<EventProxy> getReplacement
      (final Map<EventProxy,EventProxy> eventmap,
       final Collection<EventProxy> events0)
    {
      if (events0.isEmpty()) {
        return Collections.emptyList();
      } else {
        final Collection<EventProxy> events1 =
          new ArrayList<EventProxy>(events0.size());
        for (final EventProxy event0 : events0) {
          final EventProxy event1 = getReplacement(eventmap, event0);
          events1.add(event1);
        }
        return events1;
      }
    }

    EventProxy getReplacement(final Map<EventProxy,EventProxy> eventmap,
                              final EventProxy event)
    {
      final EventProxy event1 = eventmap.get(event);
      if (event1 == null) {
        return event;
      } else {
        return event1;
      }
    }

    //#######################################################################
    //# Data Members
    private final ProductDESProxy mProductDES;
    private final Map<String,EventProxy> mEventMap;
    private final Map<String,AutomatonProxy> mPlantMap;
    private final List<AbstractTest> mTests;

    private boolean mPlantOK;

  }


  //#########################################################################
  //# Inner Class AbstractTest
  private abstract class AbstractTest
  {

    //#######################################################################
    //# Constructor
    AbstractTest(final Solution sol, final String name, final float marks)
    {
      mSolution = sol;
      mName = name;
      mMarks = marks;
      sol.addTest(this);
    }

    //######################################################################
    //# Simple Access
    Solution getSolution()
    {
      return mSolution;
    }

    //######################################################################
    //# Invocation
    private float run(final ProductDESProxy des)
    {
      printLaTeXString(mName, false);
      mOutput.print(" ... ");
      try {
        final boolean result = check(des);
        if (result) {
          mOutput.print("OK");
          if (mMarks > 0.0f) {
            final int round = (int) Math.round(mMarks);
            if (Math.abs(mMarks - round) >= 0.01f) {
              mOutput.printf(" (%.1f marks)", mMarks);
            } else if (round != 1) {
              mOutput.print(" (" + round + " marks)");
            } else {
              mOutput.print(" (1 mark)");
            }
          }
          mOutput.println();
          mOutput.println();
          return mMarks;
        } else {
          mOutput.println("{\\bf Failed}");
          printDiagnostics();
          mOutput.println();
          return 0.0f;
        }
      } catch (final AnalysisException exception) {
        final String ename = ProxyTools.getShortClassName(exception);
        mOutput.println("{\\bf " + ename + "!}");
        mOutput.println();
        return 0.0f;
      }
    }

    //######################################################################
    //# Abstract Methods
    abstract boolean check(ProductDESProxy des) throws AnalysisException;

    void printDiagnostics()
    {
    }

    //#######################################################################
    //# Data Members
    private final Solution mSolution;
    private final String mName;
    private final float mMarks;

  }


  //#########################################################################
  //# Inner Class PlantTest
  private class PlantTest extends AbstractTest
  {

    //#######################################################################
    //# Constructor
    PlantTest(final Solution sol, final float marks)
    {
      super(sol, "Plants", marks);
    }

    //#########################################################################
    //# Overrides for AbstractTest
    boolean check(final ProductDESProxy des)
    {
      final Solution sol = getSolution();
      return sol.isPlantOK();
    }

  }


  //#########################################################################
  //# Inner Class ObservabilityTest
  private class ObservabilityTest extends AbstractTest
  {

    //#######################################################################
    //# Constructor
    ObservabilityTest(final Solution sol, final float marks)
    {
      super(sol, "Observability", marks);
    }

    //#########################################################################
    //# Overrides for AbstractTest
    boolean check(final ProductDESProxy des)
    {
      mSpec = null;
      mEvent = null;
      for (final AutomatonProxy aut : des.getAutomata()) {
        if (aut.getKind() == ComponentKind.SPEC) {
          for (final EventProxy event : aut.getEvents()) {
            if (!event.isObservable()) {
              mSpec = aut;
              mEvent = event;
              return false;
            }
          }
        }
      }
      return true;
    }

    void printDiagnostics()
    {
      mOutput.print("--- specification ");
      printLaTeXName(mSpec);
      mOutput.print(" uses unobservable event ");
      printLaTeXName(mEvent);
      mOutput.println('.');
    }

    //#######################################################################
    //# Data Members
    private AutomatonProxy mSpec;
    private EventProxy mEvent;

  }


  //#########################################################################
  //# Inner Class AbstractModelVerifierTest
  private abstract class AbstractModelVerifierTest extends AbstractTest
  {

    //#######################################################################
    //# Constructor
    AbstractModelVerifierTest(final Solution sol,
                              final String name,
                              final float marks,
                              final ModelVerifier verifier)
    {
      super(sol, name, marks);
      mVerifier = verifier;
    }

    //#########################################################################
    //# Simple Access
    ModelVerifier getModelVerifier()
    {
      return mVerifier;
    }

    //#########################################################################
    //# Overrides for AbstractTest
    boolean check(final ProductDESProxy des)
      throws AnalysisException
    {
      mVerifier.setModel(des);
      return mVerifier.run();
    }


    void printDiagnostics()
    {
      final TraceProxy trace = mVerifier.getCounterExample();
      if (trace != null) {
        mOutput.print("--- counterexample: ");
        final List<EventProxy> events = trace.getEvents();
        if (events.isEmpty()) {
          mOutput.println("$\\langle$empty$\\rangle$");
        } else {
          final Pattern pattern = Pattern.compile("^exp[0-9]*_");
          boolean first = true;
          for (final EventProxy event : events) {
            if (first) {
              first = false;
            } else {
              mOutput.print(", ");
            }
            final String name = event.getName();
            final Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
              final int end = matcher.end();
              final String rest = name.substring(end);
              mOutput.print("$\\langle$expecting ");
              printLaTeXString(rest, false);
              mOutput.print("$\\rangle$");
              break;
            } else {
              printLaTeXName(event);
            }
          }
          mOutput.println();
        }
      }
    }

    //#######################################################################
    //# Data Members
    private final ModelVerifier mVerifier;

  }


  //#########################################################################
  //# Inner Class ControllabilityTest
  private class ControllabilityTest extends AbstractModelVerifierTest
  {

    //#######################################################################
    //# Constructor
    ControllabilityTest(final Solution sol, final float marks)
    {
      super(sol, "Controllability", marks,
            new NativeControllabilityChecker(mFactory));
    }

  }


  //#########################################################################
  //# Inner Class ConflictTest
  private class ConflictTest extends AbstractModelVerifierTest
  {

    //#######################################################################
    //# Constructor
    ConflictTest(final Solution sol, final float marks)
    {
      super(sol, "Nonblocking", marks, new NativeConflictChecker(mFactory));
      mPropertyName = null;
    }

    ConflictTest(final Solution sol,
                 final float marks,
                 final String propname)
    {
      super(sol, "Property " + propname, marks,
            new NativeConflictChecker(mFactory));
      mPropertyName = propname;
    }

    //#######################################################################
    //# Overrides for AbstractModelVerifierTest
    boolean check(final ProductDESProxy des)
      throws AnalysisException
    {
      if (mPropertyName == null) {
        return super.check(des);
      } else {
        final Solution sol = getSolution();
        final ProductDESProxy propdes =
          sol.createInclusionModel(des, mPropertyName, ComponentKind.PLANT);
        final EventProxy marking = getSecondaryMarking(propdes);
        if (marking != null) {
          final ConflictChecker checker = (ConflictChecker) getModelVerifier();
          checker.setMarkingProposition(marking);
        }
        return super.check(propdes);
      }
    }

    //#######################################################################
    //# Data Members
    private final String mPropertyName;

  }


  //#########################################################################
  //# Inner Class LoopTest
  private class LoopTest extends AbstractModelVerifierTest
  {

    //#######################################################################
    //# Constructor
    LoopTest(final Solution sol, final float marks)
    {
      super(sol, "Loop check", marks,
            new MonolithicControlLoopChecker(mFactory));
    }

  }


  //#########################################################################
  //# Inner Class LanguageInclusionTest
  private class LanguageInclusionTest extends AbstractModelVerifierTest
  {

    //#######################################################################
    //# Constructor
    LanguageInclusionTest(final Solution sol,
                          final float marks,
                          final String propname)
    {
      super(sol, "Property " + propname, marks,
            new NativeLanguageInclusionChecker(mFactory));
      mPropertyName = propname;
    }

    //#######################################################################
    //# Overrides for AbstractModelVerifierTest
    boolean check(final ProductDESProxy des)
      throws AnalysisException
    {
      final Solution sol = getSolution();
      final ProductDESProxy propdes =
        sol.createInclusionModel(des, mPropertyName, ComponentKind.PROPERTY);
      return super.check(propdes);
    }

    //#######################################################################
    //# Data Members
    private final String mPropertyName;

  }


  //#########################################################################
  //# Inner Class LanguageExclusionTest
  private class LanguageExclusionTest extends AbstractModelVerifierTest
  {

    //#######################################################################
    //# Constructor
    LanguageExclusionTest(final Solution sol,
                          final float marks,
                          final String propname)
    {
      super(sol, "Property " + propname, marks,
            new NativeLanguageInclusionChecker(mFactory));
      mPropertyName = propname;
    }

    //#######################################################################
    //# Overrides for AbstractModelVerifierTest
    boolean check(final ProductDESProxy des)
      throws AnalysisException
    {
      final Solution sol = getSolution();
      final ProductDESProxy propdes =
        sol.createExclusionModel(des, mPropertyName);
      return super.check(propdes);
    }

    //#######################################################################
    //# Data Members
    private final String mPropertyName;

  }


  //#########################################################################
  //# Inner Class Student
  private static class Student implements FilenameFilter
  {

    //#######################################################################
    //# Constructor
    private Student(final String studid, final String uid, final String name)
    {
      mStudentID = studid;
      mUserID = uid;
      mName = name;
      mFilterBegin = uid + "-";
    }

    //#######################################################################
    //# Interface java.io.FilenameFilter
    public boolean accept(final File dir, final String name)
    {
      return name.startsWith(mFilterBegin);
    }

    //#######################################################################
    //# Printing
    private void printHeader(final PrintStream output)
    {
      output.println("\\cleardoublepage");
      output.println("\\section*{" + mName + "}");
      output.println("\\markboth{" + mName + "}{" + mName + "}");
      output.println("\\thispagestyle{plain}");
      output.println("{\\bf Full name:}\\quad " + mName + "\\\\");
      output.println("{\\bf Student ID:}\\quad " + mStudentID + "\\\\");
      output.println("{\\bf User ID:}\\quad {\\tt " + mUserID + "}");
      output.println();
    }

    //#######################################################################
    //# Data Members
    private final String mStudentID;
    private final String mUserID;
    private final String mName;
    private final String mFilterBegin;

  }


  //#########################################################################
  //# Data Members
  private final List<Student> mStudents;
  private final List<Solution> mSolutions;
  private final Solution mDefaultSolution;
  private final File mInputDirectory;
  private final File mOutputDirectory;

  private final ProductDESProxyFactory mFactory;
  private final DocumentManager mDocumentManager;
  private final ProxyUnmarshaller<ModuleProxy> mMarshaller;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final IsomorphismChecker mIsomorphismChecker;

  private PrintStream mOutput;
  private boolean mSkip;
  private int mAutomatonIndex;


  //#########################################################################
  //# Class Constants
  private static final String LOGGERFACTORY =
    "org.supremica.log.LoggerFactory";

}
