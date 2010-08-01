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
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
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
    if (args.length != 2) {
      usage();
    }
    final String classlist = args[0];
    final String submissions = args[1];

    try {
      final ModelAssess assess = new ModelAssess(classlist, submissions);
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
    System.err.println("USAGE: java ModelAssess <classlist>");
    System.exit(1);
  }


  //#########################################################################
  //# Constructor
  private ModelAssess(final String classlist,
                      final String submissions)
    throws IOException, JAXBException, SAXException
  {
    mStudents = new LinkedList<Student>();
    mSubmissionsDirectory = new File(submissions);
    mOutput = System.out;

    loadClassList(classlist);

    final ModuleProxyFactory factory = ModuleSubjectFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    final JAXBModuleMarshaller marshaller =
      new JAXBModuleMarshaller(factory, optable);
    mMarshaller = marshaller;
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(marshaller);
    mDocumentManager.registerUnmarshaller(marshaller);
  }


  //#########################################################################
  //# Main Invocation
  private void run()
  {
    final String extension = mMarshaller.getDefaultExtension();
    for (final Student student : mStudents) {
      student.printHeader(mOutput);
      final File[] submitted = mSubmissionsDirectory.listFiles(student);
      if (submitted == null || submitted.length == 0) {
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
    final String name = file.getName();
    mOutput.println("\\subsection*{Module " + name + "}");
    mOutput.println();
    try {
      final URI uri = file.toURI();
      final ModuleSubject module = (ModuleSubject) mMarshaller.unmarshal(uri);
      printComment(module);
      final ProductDESProxyFactory factory =
        ProductDESElementFactory.getInstance();
      final ModuleCompiler compiler =
        new ModuleCompiler(mDocumentManager, factory, module);
      final ProductDESProxy des = compiler.compile();
      printProductDES(des);
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
    final String comment = module.getComment();
    if (comment == null || comment.length() == 0) {
      mOutput.println("{\\bf\\itshape No comments!}");
      mOutput.println();
    } else {
      final int len = comment.length();
      boolean blank = true;
      boolean space = true;
      for (int i = 0; i < len; i++) {
        final char ch = comment.charAt(i);
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
        case '\'':
          if (space) {
            mOutput.print("`");
            blank = space = false;
          } else {
            mOutput.print("'");
          }
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
      if (!blank) {
        mOutput.println();
        mOutput.println();
      }
    }
  }

  private void printProductDES(final ProductDESProxy des)
  {
    printEventNames(des, EventKind.CONTROLLABLE);
    printEventNames(des, EventKind.UNCONTROLLABLE);
    final List<AutomatonProxy> printable = new LinkedList<AutomatonProxy>();
    printAutomataNames(des, ComponentKind.PLANT, printable);
    printAutomataNames(des, ComponentKind.SPEC, printable);
    printAutomataNames(des, ComponentKind.SUPERVISOR, printable);
    printAutomataNames(des, ComponentKind.PROPERTY, printable);
  }

  private void printEventNames(final ProductDESProxy des, final EventKind kind)
  {
    boolean found = false;
    for (final EventProxy event : des.getEvents()) {
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
        mOutput.print("\\" + latex + "event{" + event.getName() + '}');
      }
    }
    if (found) {
      mOutput.println('.');
      mOutput.println();
    }
  }

  private void printAutomataNames(final ProductDESProxy des,
                                  final ComponentKind kind,
                                  final List<AutomatonProxy> printable)
  {
    boolean found = false;
    for (final AutomatonProxy aut : des.getAutomata()) {
      if (aut.getKind() == kind) {
        if (!found) {
          found = true;
          final String kindname = ModuleContext.getComponentKindToolTip(kind);
          mOutput.println("{\\bf " + kindname + "s:}\\quad");
        } else {
          mOutput.println(',');
        }
        mOutput.print("\\automaton{" + aut.getName() + '}');
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
  private final File mSubmissionsDirectory;
  private final PrintStream mOutput;

  private final DocumentManager mDocumentManager;
  private final ProxyUnmarshaller<ModuleProxy> mMarshaller;

}
