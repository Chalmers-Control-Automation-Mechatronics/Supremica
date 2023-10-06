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

package net.sourceforge.waters.gui.comp552;

import gnu.trove.set.hash.THashSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.cpp.analysis.NativeControlLoopChecker;
import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.cpp.analysis.NativeLanguageInclusionChecker;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.renderer.EPSGraphPrinter;
import net.sourceforge.waters.gui.renderer.EdgeArrowPosition;
import net.sourceforge.waters.gui.renderer.ModuleRenderingContext;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.renderer.RenderingContext;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.IsomorphismChecker;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

import org.supremica.properties.Config;

import org.xml.sax.SAXException;


/**
 * @author Robi Malik
 */
public class ModelAssessor
{

  //#########################################################################
  //# Main Program Entry Point
  public static void main(final String[] args)
  {
    Config.GUI_EDITOR_EDGEARROW_POSITION.setValue(EdgeArrowPosition.Middle);

    if (args.length != 4) {
      usage();
    }
    final String config = args[0];
    final String classlist = args[1];
    final String inputdir = args[2];
    final String outputdir = args[3];

    try {
      final ModelAssessor assessor =
        new ModelAssessor(config, classlist, inputdir, outputdir);
      assessor.run();
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
      ("USAGE: java ModelAssessor <config> <classlist> <inputdir> <outputdir>");
    System.exit(1);
  }


  //#########################################################################
  //# Constructor
  private ModelAssessor(final String config,
                        final String classlist,
                        final String inputdir,
                        final String outputdir)
    throws IOException, SAXException, ParserConfigurationException,
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
    mModuleFactory = ModuleElementFactory.getInstance();
    mProductDESFactory = ProductDESElementFactory.getInstance();
    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    final SAXModuleMarshaller marshaller =
      new SAXModuleMarshaller(factory, optable);
    mMarshaller = marshaller;
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(marshaller);
    mDocumentManager.registerUnmarshaller(marshaller);
    mSimpleExpressionCompiler = new SimpleExpressionCompiler
      (factory, new CompilationInfo(false, false), optable, false);
    mIsomorphismChecker = new IsomorphismChecker(mProductDESFactory, true, true);
    loadConfiguration(config);
    loadClassList(classlist);
  }


  //#########################################################################
  //# Main Invocation
  private void run()
    throws FileNotFoundException
  {
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
          Arrays.sort(submitted);
          for (final File file : submitted) {
            processFile(file);
          }
        }
      }
    } finally {
      mOutput.close();
    }
  }

  private void processFile(final File file)
  {
    if (file.isDirectory()) {
      final File[] children = file.listFiles();
      Arrays.sort(children);
      for (final File child : children) {
        processFile(child);
      }
    } else {
      final String name = file.getName();
      final String extension = mMarshaller.getDefaultExtension();
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


  //#########################################################################
  //# Processing the Configuration
  private void loadConfiguration(final String config)
    throws IOException, WatersUnmarshalException, EvalException
  {
    final File file = new File(config);
    final File dir = file.getAbsoluteFile().getParentFile();
    final FileReader stream = new FileReader(file);
    try {
      @SuppressWarnings("resource")
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
          sol = new Solution(module);
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
            new ConflictTest(sol, null, marks, propname);
          } else if (words.length == 4) {
            final String propname = words[1];
            final AbstractTest depends = sol.getTestForProperty(words[2]);
            final float marks = Float.parseFloat(words[3]);
            new ConflictTest(sol, depends, marks, propname);
          }
        } else if (key.equals("loop")) {
          final float marks = Float.parseFloat(words[1]);
          new LoopTest(sol, marks);
        } else if (key.equals("inclusion")) {
          final String propname = words[1];
          if (words.length == 3) {
            final float marks = Float.parseFloat(words[2]);
            new LanguageInclusionTest(sol, null, marks, propname);
          } else if (words.length == 4) {
            final AbstractTest depends = sol.getTestForProperty(words[2]);
            final float marks = Float.parseFloat(words[3]);
            new LanguageInclusionTest(sol, depends, marks, propname);
          }
        } else if (key.equals("exclusion")) {
          final String propname = words[1];
          if (words.length == 3) {
            final float marks = Float.parseFloat(words[2]);
            new LanguageExclusionTest(sol, null, marks, propname);
          } else if (words.length == 4) {
            final AbstractTest depends = sol.getTestForProperty(words[2]);
            final float marks = Float.parseFloat(words[3]);
            new LanguageExclusionTest(sol, depends, marks, propname);
          }
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
      @SuppressWarnings("resource")
      final BufferedReader reader = new BufferedReader(stream);
      mStudents.clear();
      while (true) {
        final String line = reader.readLine();
        if (line == null) {
          break;
        }
        final String[] words = line.split(",");
        if (words.length >= 4) {
          final Student student =
            new Student(words[0], words[1], words[2], words[3]);
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
    String moodleName = file.getName();
    final int pos = moodleName.indexOf(MOODLE_FILE_PATTERN);
    if (pos >= 0) {
      moodleName = moodleName.substring(pos + MOODLE_FILE_PATTERN.length());
    }
    printLaTeXString(moodleName, false);
    mOutput.println("}}");
    mOutput.println();
    mSkip = false;
    try {
      final URI uri = file.toURI();
      final ModuleProxy attempt = mMarshaller.unmarshal(uri);
      printComment(attempt);
      final Solution sol = findSolution(attempt);
      sol.compileProductDES(attempt);
      final ModuleCompiler compiler =
        new ModuleCompiler(mDocumentManager, mProductDESFactory, attempt);
      compiler.setSourceInfoEnabled(true);
      final ProductDESProxy des = compiler.compile();
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

  private Solution findSolution(final ModuleProxy attempt)
  {
    int best = 0;
    Solution result = mDefaultSolution;
    for (final Solution solution : mSolutions) {
      final int count = solution.getNumberOfMatches(attempt);
      if (count > best) {
        best = count;
        result = solution;
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
      final Map<Object,SourceInfo> infomap = compiler.getSourceInfoMap();
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

  private void printMarks(final float marks)
  {
    final int round = Math.round(marks);
    if (Math.abs(marks - round) >= 0.01f) {
      mOutput.print(marks);
      mOutput.print(" marks");
    } else if (round != 1) {
      mOutput.print(round);
      mOutput.print(" marks");
    } else {
      mOutput.print("1 mark");
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
        int count = 1;
        for (int j = i + 1; j < len && text.charAt(j) == '-'; j++) {
          count++;
        }
        if (count == 1 && space && isSpace(text, i + 1)) {
          mOutput.print("---");
        } else if (count > 1 && count <= 10) {
          mOutput.print("---");
          i += count - 1;
        } else if (count > 10) {
          mOutput.println("\\par\\smallskip");
          mOutput.println("\\hrule\\smallskip");
          i += count - 1;
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
      printLaTeXString(msg, true);
    }
    mOutput.println();
  }


  //#########################################################################
  //# Auxiliary Static Methods
  private static EventProxy getSecondaryMarking(final AutomatonProxy aut)
  {
    EventProxy result = null;
    for (final EventProxy event : aut.getEvents()) {
      if (event.getKind() == EventKind.PROPOSITION) {
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
    private Solution(final ModuleProxy module)
    {
      mModule = module;
      if (module == null) {
        mEventDeclNames = Collections.emptySet();
        mPlantNames = Collections.emptySet();
      } else {
        final Collection<EventDeclProxy> decls = module.getEventDeclList();
        mEventDeclNames = new THashSet<String>(decls.size());
        for (final EventDeclProxy decl : decls) {
          final String name = decl.getName();
          mEventDeclNames.add(name);
        }
        final Collection<Proxy> comps = module.getComponentList();
        mPlantNames = new THashSet<String>(comps.size());
        for (final Proxy proxy : comps) {
          if (proxy instanceof SimpleComponentProxy) {
            final SimpleComponentProxy comp = (SimpleComponentProxy) proxy;
            if (comp.getKind() == ComponentKind.PLANT) {
              final String name = comp.getName();
              mPlantNames.add(name);
            }
          }
        }
      }
      mTests = new LinkedList<AbstractTest>();
      mTestLookup = new HashMap<String,AbstractTest>();
    }

    //#######################################################################
    //# Override for java.lang.Object
    @Override
    public String toString()
    {
      return mModule.getName();
    }

    //#######################################################################
    //# Accessing Tests
    private void addTest(final AbstractTest test)
    {
      mTests.add(test);
    }

    private void setTestForProperty(final String propname,
                                    final AbstractTest test)
    {
      mTestLookup.put(propname, test);
    }

    private AbstractTest getTestForProperty(final String propname)
    {
      return mTestLookup.get(propname);
    }

    private float runTests(final ProductDESProxy des)
    {
      skip("\\medskip");
      float marks = 0.0f;
      for (final AbstractTest test : mTests) {
        marks += test.run(des);
      }
      skip("\\smallskip");
      mOutput.print("Recommending ");
      printMarks(marks);
      mOutput.println('.');
      return marks;
    }

    //#######################################################################
    //# Compilation
    /**
     * Compiles the module into a product DES,
     * using event arrays as defined in the given attempt.
     */
    private void compileProductDES(final ModuleProxy attempt)
      throws EvalException
    {
      if (mModule == null) {
        mEventMap = Collections.emptyMap();
        mPlantMap = Collections.emptyMap();
      } else {
        final Collection<EventDeclProxy> theirDecls =
          attempt.getEventDeclList();
        final Map<String,EventDeclProxy> declMap =
          new HashMap<String,EventDeclProxy>(theirDecls.size());
        for (final EventDeclProxy decl : theirDecls) {
          final String name = decl.getName();
          declMap.put(name, decl);
        }
        final Collection<EventDeclProxy> myDecls = mModule.getEventDeclList();
        final Collection<EventDeclProxy> newDecls =
          new ArrayList<EventDeclProxy>(myDecls.size());
        boolean change = false;
        for (final EventDeclProxy myDecl : myDecls) {
          final String name = myDecl.getName();
          final EventDeclProxy theirDecl = declMap.get(name);
          if (theirDecl != null &&
              theirDecl.getKind() == myDecl.getKind() &&
              theirDecl.isObservable() == myDecl.isObservable() &&
              !theirDecl.getRanges().isEmpty()) {
            newDecls.add(theirDecl);
            change = true;
          } else {
            newDecls.add(myDecl);
          }
        }
        final ModuleProxy newModule;
        if (change) {
          final String name = mModule.getName();
          final String comment = mModule.getComment();
          final Collection<ConstantAliasProxy> constants =
            attempt.getConstantAliasList();
          final Collection<Proxy> aliases = mModule.getEventAliasList();
          final Collection<Proxy> components = mModule.getComponentList();
          newModule = mModuleFactory.createModuleProxy
            (name, comment, null, constants, newDecls, aliases, components);
        } else {
          newModule = mModule;
        }
        final ModuleCompiler compiler =
          new ModuleCompiler(mDocumentManager, mProductDESFactory, newModule);
        mProductDES = compiler.compile();
        final Collection<EventProxy> events = mProductDES.getEvents();
        mEventMap = new HashMap<String,EventProxy>(events.size());
        for (final EventProxy event : events) {
          final String name = event.getName();
          final String lower = name.toLowerCase();
          mEventMap.put(lower, event);
        }
        final Collection<AutomatonProxy> automata = mProductDES.getAutomata();
        mPlantMap = new HashMap<String,AutomatonProxy>(automata.size());
      }
    }

    //#######################################################################
    //# Event Check
    private int getNumberOfMatches(final ModuleProxy attempt)
    {
      int matches = 0;
      for (final EventDeclProxy decl : attempt.getEventDeclList()) {
        if (decl.getKind() != EventKind.PROPOSITION) {
          final String name = decl.getName();
          if (hasEventDecl(name)) {
            matches++;
          }
        }
      }
      final Collection<Proxy> comps = attempt.getComponentList();
      for (final Proxy proxy : comps) {
        if (proxy instanceof SimpleComponentProxy) {
          final SimpleComponentProxy comp = (SimpleComponentProxy) proxy;
          if (comp.getKind() == ComponentKind.PLANT) {
            final String name = comp.getName();
            if (hasPlant(name)) {
              matches++;
            }
          }
        }
      }
      return matches;
    }

    private boolean hasEventDecl(final String name)
    {
      return mEventDeclNames.contains(name);
    }

    private boolean hasPlant(final String name)
    {
      return mPlantNames.contains(name);
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
                                         final AutomatonProxy property,
                                         final ComponentKind kind)
    {
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
        case SUPERVISOR:
          automata.add(aut);
          break;
        default:
          break;
        }
      }
      final AutomatonProxy prop = replaceEvents(property, eventmap, kind);
      automata.add(prop);
      return mProductDESFactory.createProductDESProxy(desname, events, automata);
    }

    ProductDESProxy createExclusionModel(final ProductDESProxy des,
                                         final String propname)
    {
      final AutomatonProxy prop1 = getAutomaton(propname);

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
          final AutomatonProxy aut = mProductDESFactory.createAutomatonProxy
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
      return mProductDESFactory.createProductDESProxy(desname, events, automata);
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
          mProductDESFactory.createStateProxy(name, initial, props1);
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
          mProductDESFactory.createTransitionProxy(source1, event1, target1);
        transitions1.add(trans1);
      }
      return mProductDESFactory.createAutomatonProxy
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

    AutomatonProxy getAutomaton(final String name)
    {
      AutomatonProxy found = null;
      for (final AutomatonProxy aut : mProductDES.getAutomata()) {
        if (aut.getName().equals(name)) {
          found = aut;
        }
      }
      if (found == null) {
        throw new IllegalArgumentException
          ("Product DES " + mProductDES.getName() +
           " does not contain any automaton named " + name + "!");
      }
      return found;
    }

    //#######################################################################
    //# Data Members
    private final ModuleProxy mModule;
    private final List<AbstractTest> mTests;
    private final Map<String,AbstractTest> mTestLookup;

    private ProductDESProxy mProductDES;
    private final Collection<String> mEventDeclNames;
    private Collection<String> mPlantNames;
    private Map<String,EventProxy> mEventMap;
    private Map<String,AutomatonProxy> mPlantMap;

    private boolean mPlantOK;

  }


  //#########################################################################
  //# Inner Class AbstractTest
  private abstract class AbstractTest
  {

    //#######################################################################
    //# Constructors
    AbstractTest(final Solution sol, final String name, final float marks)
    {
      this(sol, name, null, marks);
    }

    AbstractTest(final Solution sol, final String name,
                 final AbstractTest depends, final float marks)
    {
      mSolution = sol;
      mName = name;
      mDependency = depends;
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
        mLastResult = check(des);
        if (mLastResult) {
          float marks = mMarks;
          mOutput.print("OK");
          if (mMarks > 0.0f) {
            if (mDependency != null && !mDependency.mLastResult) {
              marks = 0.0f;
            } else {
              mOutput.print(" (");
              printMarks(mMarks);
              mOutput.print(')');
            }
          }
          mOutput.println();
          mOutput.println();
          return marks;
        } else {
          mOutput.println("{\\bf Failed}");
          printDiagnostics();
          mOutput.println();
          return 0.0f;
        }
      } catch (final AnalysisException exception) {
        mLastResult = false;
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
    private final AbstractTest mDependency;
    private final float mMarks;

    private boolean mLastResult;

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
    @Override
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
    @Override
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

    @Override
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
      this(sol, name, null, marks, verifier);
    }

    AbstractModelVerifierTest(final Solution sol,
                              final String name,
                              final AbstractTest depends,
                              final float marks,
                              final ModelVerifier verifier)
    {
      super(sol, name, depends, marks);
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
    @Override
    boolean check(final ProductDESProxy des)
      throws AnalysisException
    {
      mVerifier.setModel(des);
      return mVerifier.run();
    }


    @Override
    void printDiagnostics()
    {
      final CounterExampleProxy counter = mVerifier.getCounterExample();
      final TraceProxy trace = counter.getTraces().get(0);
      if (trace != null) {
        mOutput.print("--- counterexample: ");
        final List<EventProxy> events = trace.getEvents();
        if (events.isEmpty()) {
          mOutput.println("$\\langle$empty$\\rangle$");
        } else {
          final Pattern pattern = Pattern.compile("^exp[0-9]*_");
          boolean first = true;
          int step = 0;
          for (final EventProxy event : events) {
            if (first) {
              first = false;
            } else {
              mOutput.print(", ");
            }
            if (step == trace.getLoopIndex()) {
              mOutput.print("$\\langle$loop begins here$\\rangle$ ");
            }
            step++;
            final String name = event.getName();
            final Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
              final int end = matcher.end();
              final String rest = name.substring(end);
              mOutput.print("$\\langle$ex\\-pect\\-ing ");
              printLaTeXString(rest, false);
              mOutput.print("$\\rangle$");
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
            new NativeControllabilityChecker(mProductDESFactory));
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
      super(sol, "Nonblocking", marks, new NativeConflictChecker(mProductDESFactory));
      mPropertyName = null;
    }

    ConflictTest(final Solution sol,
                 final AbstractTest depends,
                 final float marks,
                 final String propname)
    {
      super(sol, "Property " + propname, depends, marks,
            new NativeConflictChecker(mProductDESFactory));
      mPropertyName = propname;
      sol.setTestForProperty(propname, this);
    }

    //#######################################################################
    //# Overrides for AbstractModelVerifierTest
    @Override
    boolean check(final ProductDESProxy des)
      throws AnalysisException
    {
      if (mPropertyName == null) {
        return super.check(des);
      } else {
        final Solution sol = getSolution();
        final AutomatonProxy property = sol.getAutomaton(mPropertyName);
        final ProductDESProxy propDES =
          sol.createInclusionModel(des, property, ComponentKind.PLANT);
        final EventProxy marking = getSecondaryMarking(property);
        if (marking != null) {
          final ConflictChecker checker = (ConflictChecker) getModelVerifier();
          checker.setConfiguredDefaultMarking(marking);
        }
        return super.check(propDES);
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
      super(sol, "Control-loop check", marks,
            new NativeControlLoopChecker(mProductDESFactory));
    }

  }


  //#########################################################################
  //# Inner Class LanguageInclusionTest
  private class LanguageInclusionTest extends AbstractModelVerifierTest
  {

    //#######################################################################
    //# Constructor
    LanguageInclusionTest(final Solution sol,
                          final AbstractTest depends,
                          final float marks,
                          final String propname)
    {
      super(sol, "Property " + propname, depends, marks,
            new NativeLanguageInclusionChecker(mProductDESFactory));
      mPropertyName = propname;
      sol.setTestForProperty(propname, this);
    }

    //#######################################################################
    //# Overrides for AbstractModelVerifierTest
    @Override
    boolean check(final ProductDESProxy des)
      throws AnalysisException
    {
      final Solution sol = getSolution();
      final AutomatonProxy property = sol.getAutomaton(mPropertyName);
      final ProductDESProxy propDES =
        sol.createInclusionModel(des, property, ComponentKind.PROPERTY);
      return super.check(propDES);
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
                          final AbstractTest depends,
                          final float marks,
                          final String propname)
    {
      super(sol, "Property " + propname, depends, marks,
            new NativeLanguageInclusionChecker(mProductDESFactory));
      mPropertyName = propname;
      sol.setTestForProperty(propname, this);
    }

    //#######################################################################
    //# Overrides for AbstractModelVerifierTest
    @Override
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
      int splitpos = name.indexOf(',');
      mLastName = name.substring(0, splitpos);
      while (name.charAt(++splitpos) == ' ') {
      }
      mFirstName = name.substring(splitpos);
      mPattern = Pattern.compile("^" + mFirstName + " " + mLastName + "_[0-9]+.*$");
      // mPattern = Pattern.compile("^[^0-9_]+_" + mStudentID + "_.*$");
    }

    private Student(final String studid, final String uid,
                    final String last, final String first)
    {
      mStudentID = studid;
      mUserID = uid;
      mLastName = last;
      mFirstName = first;
      mPattern = Pattern.compile("^" + mFirstName + " " + mLastName + "_[0-9]+.*$");
      // mPattern = Pattern.compile("^[^0-9_]+_" + mStudentID + "_.*$");
    }

    //#######################################################################
    //# Interface java.io.FilenameFilter
    @Override
    public boolean accept(final File dir, final String name)
    {
      final Matcher matcher = mPattern.matcher(name);
      return matcher.matches();
    }

    //#######################################################################
    //# Printing
    private void printHeader(final PrintStream output)
    {
      final String name = mLastName + ", " + mFirstName;
      output.println("\\cleardoublepage");
      output.println("\\section*{" + name + "}");
      output.println("\\markboth{" + name + "}{" + name + "}");
      output.println("\\thispagestyle{plain}");
      output.println("{\\bf Family name:}\\quad " + mLastName + "\\\\");
      output.println("{\\bf First name:}\\quad " + mFirstName + "\\\\");
      output.println("{\\bf Student ID:}\\quad " + mStudentID + "\\\\");
      output.println("{\\bf User ID:}\\quad {\\tt " + mUserID + "}");
      output.println();
    }

    //#######################################################################
    //# Data Members
    private final String mStudentID;
    private final String mUserID;
    private final String mFirstName;
    private final String mLastName;
    private final Pattern mPattern;

  }


  //#########################################################################
  //# Data Members
  private final List<Student> mStudents;
  private final List<Solution> mSolutions;
  private final Solution mDefaultSolution;
  private final File mInputDirectory;
  private final File mOutputDirectory;

  private final ModuleProxyFactory mModuleFactory;
  private final ProductDESProxyFactory mProductDESFactory;
  private final DocumentManager mDocumentManager;
  private final ProxyUnmarshaller<ModuleProxy> mMarshaller;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final IsomorphismChecker mIsomorphismChecker;

  private PrintStream mOutput;
  private boolean mSkip;
  private int mAutomatonIndex;


  //#########################################################################
  //# Class Constants
  private static final String MOODLE_FILE_PATTERN = "_assignsubmission_file_";

}
