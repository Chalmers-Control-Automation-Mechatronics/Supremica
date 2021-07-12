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
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.config.Version;
import net.sourceforge.waters.external.valid.ValidUnmarshaller;
import net.sourceforge.waters.gui.renderer.EPSGenerator;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.options.OptionFileManager;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

import org.apache.logging.log4j.LogManager;

import org.supremica.automata.Project;
import org.supremica.automata.IO.ADSUnmarshaller;
import org.supremica.automata.IO.HISCUnmarshaller;
import org.supremica.automata.IO.SupremicaUnmarshaller;
import org.supremica.automata.IO.UMDESUnmarshaller;
import org.supremica.properties.ConfigPages;

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
   * Processes an array of arguments. Returns a list of files to be opened
   * on startup.
   */
  public static List<File> process(final String[] args)
  {
    boolean quit = false;
    boolean verbose = false;
    final List<File> filesToOpen = new LinkedList<File>();

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-h") || args[i].equals("-?") ||
          args[i].equals("--help") || args[i].equals("--usage")) {
        // Print usage
        printUsage();
        // Quit after this
        quit = true;
      } else if (args[i].equals("-p") || args[i].equals("--properties")) {
        // Load properties
        if (++i < args.length) {
          final String fileName = args[i];
          final File propFile = new File(fileName);
          try {
            if (!propFile.isFile()) {
              LogManager.getLogger().warn("Creating property file {}",
                                          propFile.getAbsolutePath());
              propFile.createNewFile();
            }
            OptionFileManager.loadProperties(ConfigPages.ROOT, propFile);
          } catch (final IOException exception) {
            LogManager.getLogger().error("Error reading properties file {}",
                                         propFile.getAbsolutePath());
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

            final EPSGenerator generator = new EPSGenerator(module, verbose);
            generator.generateEPS(module);
          } catch (final IOException ex) {
            System.err.println("IO problem: " + ex);
          } catch (final WatersUnmarshalException ex) {
            System.err.println("Problem unmarshalling: " + ex);
          } catch (final ParseException ex) {
            System.err.println("Problem importing to module: " + ex);
          } catch (final ClassCastException ex) {
            System.err.println("Only import of modules is supported: " + ex);
          }
        }
        // Quit after this (even if there were no files)
        quit = true;
      } else if (args[i].equals("-v") || args[i].equals("--version")) {
        System.out.println(Version.getInstance().toString());
        quit = true;
      } else if (args[i].equals("--verbose")) {
        verbose = true;
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
               + "Usage: IDE [options] <module files>\n"
               + "\n"
               + "Options:\n"
               + "-p, --properties <file>  Load properties from <file>\n"
               + "-e, --epsfigs <file>     Create eps-figures from all components in <file>\n"
               + "-?, -h, --help, --usage  Show this help message\n"
               + "--verbose                Be extra verbose\n"
               + "-v, --version            Show version\n");
  }
}
