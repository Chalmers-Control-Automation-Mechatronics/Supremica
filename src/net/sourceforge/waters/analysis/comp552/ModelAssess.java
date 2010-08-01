//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Teaching Support
//# PACKAGE: net.sourceforge.waters.analysis.comp552
//# CLASS:   ModelAssess
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.comp552;

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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.renderer.EPSGraphPrinter;
import net.sourceforge.waters.gui.renderer.ModuleRenderingContext;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.renderer.RenderingContext;
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
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
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
    if (args.length != 3) {
      usage();
    }
    final String classlist = args[0];
    final String inputdir = args[1];
    final String outputdir = args[2];

    try {
      final ModelAssess assess =
        new ModelAssess(classlist, inputdir, outputdir);
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
      ("USAGE: java ModelAssess <classlist> <inputdir> <outputdir>");
    System.exit(1);
  }


  //#########################################################################
  //# Constructor
  private ModelAssess(final String classlist,
                      final String inputdir,
                      final String outputdir)
    throws IOException, JAXBException, SAXException
  {
    mStudents = new LinkedList<Student>();
    mInputDirectory = new File(inputdir);
    mOutputDirectory = new File(outputdir);
    if (!mOutputDirectory.exists()) {
      mOutputDirectory.mkdirs();
    }

    loadClassList(classlist);

    final ModuleProxyFactory factory = ModuleSubjectFactory.getInstance();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    final JAXBModuleMarshaller marshaller =
      new JAXBModuleMarshaller(factory, optable);
    mMarshaller = marshaller;
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(marshaller);
    mDocumentManager.registerUnmarshaller(marshaller);
    mSimpleExpressionCompiler = new SimpleExpressionCompiler(factory, optable);
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
  //# Processing the Class List
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
      final ModuleSubject module = (ModuleSubject) mMarshaller.unmarshal(uri);
      printComment(module);
      final ProductDESProxyFactory factory =
        ProductDESElementFactory.getInstance();
      final ModuleCompiler compiler =
        new ModuleCompiler(mDocumentManager, factory, module);
      compiler.setSourceInfoEnabled(true);
      final ProductDESProxy des = compiler.compile();
      final List<AutomatonProxy> printable = new LinkedList<AutomatonProxy>();
      printProductDES(des, printable);
      printGraphs(printable, compiler);
    } catch (final WatersUnmarshalException exception) {
      showException(exception);
    } catch (final IOException exception) {
      showException(exception);
    } catch (final EvalException exception) {
      showException(exception);
    }
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
    printAutomataNames(automata, ComponentKind.PLANT, printable);
    printAutomataNames(automata, ComponentKind.SPEC, printable);
    printAutomataNames(automata, ComponentKind.SUPERVISOR, printable);
    printAutomataNames(automata, ComponentKind.PROPERTY, printable);
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
                                  final ComponentKind kind,
                                  final List<AutomatonProxy> printable)
  {
    boolean found = false;
    for (final AutomatonProxy aut : automata) {
      if (aut.getKind() == kind) {
        if (!found) {
          found = true;
          final String kindname = ModuleContext.getComponentKindToolTip(kind);
          mOutput.println("{\\bf " + kindname + "s:}\\quad");
        } else {
          mOutput.println(',');
        }
        mOutput.print("\\automaton{");
        printLaTeXName(aut);
        mOutput.print('}');
        switch (kind) {
        case PLANT:
          // check it ...
          break;
        default:
          printable.add(aut);
          break;
        }
      }
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
      case '_':
        mOutput.print('\\');
        mOutput.print(ch);
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
  private final File mInputDirectory;
  private final File mOutputDirectory;

  private final DocumentManager mDocumentManager;
  private final ProxyUnmarshaller<ModuleProxy> mMarshaller;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;

  private PrintStream mOutput;
  private boolean mSkip;
  private int mAutomatonIndex;
}
