//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Supremica/Waters IDE
//# PACKAGE: org.supremica.util
//# CLASS:   ProcessCommandLineArguments
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.analysis.options.OptionFileManager;
import net.sourceforge.waters.config.Version;
import net.sourceforge.waters.external.valid.ValidUnmarshaller;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.renderer.EPSGraphPrinter;
import net.sourceforge.waters.gui.renderer.GeometryChecker;
import net.sourceforge.waters.gui.renderer.ModuleRenderingContext;
import net.sourceforge.waters.gui.renderer.RenderingContext;
import net.sourceforge.waters.model.base.DefaultProxyVisitor;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

import org.supremica.automata.Project;
import org.supremica.automata.IO.ADSUnmarshaller;
import org.supremica.automata.IO.HISCUnmarshaller;
import org.supremica.automata.IO.SupremicaUnmarshaller;
import org.supremica.automata.IO.UMDESUnmarshaller;
import org.supremica.gui.ide.DefaultAttributeFactory;

import org.xml.sax.SAXException;


/**
 * Class responsible for interpreting command line arguments given to
 * Supremica.
 *
 * @author Hugo Flordal
 */

public class ProcessCommandLineArguments
{
  /**
   * Processes an array of arguments. Returns a list of files to be opened on
   * startup.
   */
  public static List<File> process(final String[] args)
  {
    boolean quit = false;
    final boolean verbose = false;
    final List<File> filesToOpen = new LinkedList<File>();

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-h") || args[i].equals("-?")
          || args[i].equals("--help") || args[i].equals("--usage")) {
        // Print usage
        printUsage();
        // Quit after this
        quit = true;
      }
      if (args[i].equals("-p") || args[i].equals("--properties")) {
        // Load properties
        if (++i < args.length) {
          final String fileName = args[i];
          final File propFile = new File(fileName);
          try {
            if (!propFile.exists()) {
              System.out.println("Creating property file: "
                                 + propFile.getAbsolutePath());
              propFile.createNewFile();
            }
            OptionFileManager.loadProperties(propFile);
          } catch (final Exception e) {
            System.err.println("Error reading properties file: "
                               + propFile.getAbsolutePath());
          }
        }
      } else if (args[i].equals("-e") || args[i].equals("--epsfigs")) {
        // Create eps figs for all components in the supplied file
        while ((i + 1 < args.length) && !(args[i + 1].startsWith("-"))) {
          final String fileName = args[++i];
          final File figFile = new File(fileName);

          // Set up document manager ...
          final DocumentManager documentManager = new DocumentManager();
          ProductDESImporter importer;
          try {
            final ModuleProxyFactory factory =
              ModuleSubjectFactory.getInstance();
            final OperatorTable opTable = CompilerOperatorTable.getInstance();
            final SAXModuleMarshaller moduleMarshaller =
              new SAXModuleMarshaller(factory, opTable);
            final ProxyUnmarshaller<Project> supremicaUnmarshaller =
              new SupremicaUnmarshaller(factory);
            final ProxyUnmarshaller<ModuleProxy> validUnmarshaller =
              new ValidUnmarshaller(factory, opTable);
            final ProxyUnmarshaller<ModuleProxy> hiscUnmarshaller =
              new HISCUnmarshaller(factory);
            final ProxyUnmarshaller<ModuleProxy> umdesUnmarshaller =
              new UMDESUnmarshaller(factory);
            final ProxyUnmarshaller<ModuleProxy> adsUnmarshaller =
              new ADSUnmarshaller(factory);
            // Add unmarshallers in order of importance ...
            // (shows up in the file-open dialog)
            documentManager.registerUnmarshaller(moduleMarshaller);
            documentManager.registerUnmarshaller(supremicaUnmarshaller);
            documentManager.registerUnmarshaller(validUnmarshaller);
            documentManager.registerUnmarshaller(hiscUnmarshaller);
            documentManager.registerUnmarshaller(umdesUnmarshaller);
            documentManager.registerUnmarshaller(adsUnmarshaller);

            importer = new ProductDESImporter(factory);
          } catch (final SAXException | ParserConfigurationException exception) {
            System.err.println("Exception when initialising document manager: " +
                               exception);
            return null;
          }

          // Do the printing
          try {
            // Load file
            final DocumentProxy doc = documentManager.load(figFile);

            // Build module
            ModuleProxy module;
            if (doc instanceof ModuleProxy) {
              module = (ModuleProxy) doc;
            } else if (doc instanceof Project) {
              module = importer.importModule((Project) doc);
            } else {
              throw new ClassCastException("Unknown document type");
            }

            // Loop through components and print eps-figures
            //module.acceptVisitor(new EPSPrinterVisitor(module));
            final List<Proxy> components = module.getComponentList();
            final DefaultProxyVisitor visitor =
              new EPSPrinterVisitor(module, verbose);
            visitor.visitCollection(components);
          } catch (final IOException ex) {
            System.err.println("IO problem: " + ex);
          } catch (final WatersUnmarshalException ex) {
            System.err.println("Problem unmarshalling: " + ex);
          } catch (final ParseException ex) {
            System.err.println("Problem importing to module: " + ex);
          } catch (final ClassCastException ex) {
            System.err.println("Only import of modules is supported: " + ex);
          } catch (final VisitorException ex) {
            System.err.println("Problems when visiting module: " + ex);
          }
        }
        // Quit after this (even if there were no files)
        quit = true;
      } else if (args[i].equals("-v") || args[i].equals("--version")) {
        System.out.println(Version.getInstance().toString());
        quit = true;
      } else {
        final String filename = args[i];
        final File currFile = new File(filename);
        if (!currFile.exists()) {
          System.out.println("Invalid usage: '" + args[i] + "'.\n");
          ProcessCommandLineArguments.printUsage();
          quit = true;
        } else {
          filesToOpen.add(currFile);
        }
      }
    }

    if (quit) {
      System.exit(0);
    }

    return filesToOpen;
  }

  /**
   * --help
   */
  private static void printUsage()
  {
    System.out
      .println(Version.getInstance().toString() + "\n"
               + "More information about Supremica is available at www.supremica.org\n"
               + "\n"
               + "Usage: IDE [OPTION] MODULE_FILES\n"
               + "\n"
               + "Options:\n"
               + "-p, --properties FILE        Load properties from FILE\n"
               + "-e, --epsfigs FILE...        Create eps-figures from all components in FILEs\n"
               + "--svgfigs FILE               Create svg-figures from all components in FILE\n"
               + "-l, --list [FILE]            List properties with current values (or values in FILE)\n"
               + "-?, -h, --help, --usage      Show this help message\n"
               + "--verbose                    be extra verbose\n"
               + "-v, --version                show version\n" + "\n");
  }
}


/**
 * Visitor for visiting all simple components and producing .eps files for the
 * graphs.
 */
class EPSPrinterVisitor extends DefaultModuleProxyVisitor
{

  //#######################################################################
  //# Constructor
  EPSPrinterVisitor(final ModuleProxy module, final boolean verbose)
  {
    final ModuleContext mcontext = new ModuleContext(module);
    mContext = new ModuleRenderingContext(mcontext);
    mVerbose = verbose;
  }

  //#######################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  /**
   * Skip everything except simple components.
   */
  @Override
  public Object visitComponentProxy(final ComponentProxy comp)
  {
    return null;
  }

  /**
   * Visit the children of foreach constructs in the component list.
   */
  @Override
  public Object visitForeachProxy(final ForeachProxy foreach)
    throws VisitorException
  {
    final Collection<Proxy> body = foreach.getBody();
    return visitCollection(body);
  }

  /**
   * Visit simpleComponent and output eps-file. The only reason that
   * visitGraphProxy is not used instead is that we need the name ...
   */
  @Override
  public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
  {
    final Map<String,String> attribs = comp.getAttributes();
    final String name = comp.getName();
    final GraphProxy graph = comp.getGraph();
    if (attribs.containsKey(DefaultAttributeFactory.EPS_SUPPRESS_KEY)) {
      if (mVerbose) {
        System.out.println("Not generating EPS for " + name +
          ": suppressed.");
      }
    } else if (!GeometryChecker.hasGeometry(graph)) {
      if (mVerbose) {
        System.out.println("Not generating EPS for " + name +
          ": missing geometry.");
      }
    } else {
      final File file = new File(name + ".eps");
      try {
        final EPSGraphPrinter printer =
          new EPSGraphPrinter(graph, mContext, file);
        printer.print();
        // Log
        if (mVerbose) {
          System.out.println("Wrote " + file.getAbsolutePath());
        }
      } catch (final IOException exception) {
        if (mVerbose) {
          System.out.println("Failed generating EPS for " + name + ": " +
            exception.getMessage());
        }
      }
    }
    // Return any value ...
    return null;
  }


  //#######################################################################
  //# Data Members
  private final RenderingContext mContext;
  private final boolean mVerbose;

}
