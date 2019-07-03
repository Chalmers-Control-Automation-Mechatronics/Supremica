//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.gui.renderer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.external.valid.ValidUnmarshaller;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.DescendingModuleProxyVisitor;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

import org.supremica.automata.Project;
import org.supremica.automata.IO.SupremicaUnmarshaller;
import org.supremica.gui.ide.DefaultAttributeFactory;
import org.supremica.properties.SupremicaProperties;

import org.xml.sax.SAXException;


/**
 * Command line tool to generate EPS images from a Waters module.
 *
 * @author Hugo Flordal, Robi Malik
 */

public class EPSGenerator extends DescendingModuleProxyVisitor
{

  //#########################################################################
  //# Main Method
  public static void main(final String[] args)
  {
    boolean verbose = true;
    boolean noArgs = false;
    File outputDir = null;

    try {
      if (args.length == 0) {
        usage();
      }

      final ModuleProxyFactory factory =
        ModuleElementFactory.getInstance();
      final List<String> names = new LinkedList<String>();
      for (int i = 0; i < args.length; i++) {
        final String arg = args[i];
        if (noArgs) {
          names.add(arg);
        } else if (arg.equals("-o")) {
          if (++i < args.length) {
            outputDir = new File(args[i]);
          } else {
            usage();
          }
        } else if (arg.equals("-p") || arg.equals("--properties")) {
          // Load properties
          if (++i < args.length) {
            final File propFile = new File(args[i]);
            SupremicaProperties.loadProperties(propFile);
          } else {
            usage();
          }
        } else if (arg.equals("-q") || arg.equals("--quiet")) {
          verbose = false;
        } else if (arg.equals("--")) {
          noArgs = true;
        } else {
          names.add(arg);
        }
      }

      final OperatorTable optable = CompilerOperatorTable.getInstance();
      final SAXModuleMarshaller moduleUnmarshaller =
        new SAXModuleMarshaller(factory, optable, true);
      final ValidUnmarshaller validUnmarshaller =
        new ValidUnmarshaller(factory, optable);
      final ProxyUnmarshaller<Project> supremicaUnmarshaller =
        new SupremicaUnmarshaller(factory);
      final DocumentManager docManager = new DocumentManager();
      docManager.registerUnmarshaller(moduleUnmarshaller);
      docManager.registerUnmarshaller(validUnmarshaller);
      docManager.registerUnmarshaller(supremicaUnmarshaller);
      final ProductDESImporter importer = new ProductDESImporter(factory);
      final EPSGenerator generator = new EPSGenerator(outputDir, verbose);

      // Loop through modules and generate images
      for (final String name : names) {
        // Load file
        final File fileName = new File(name);
        final DocumentProxy doc = docManager.load(fileName);
        // Build module
        ModuleProxy module;
        if (doc instanceof ModuleProxy) {
          module = (ModuleProxy) doc;
        } else if (doc instanceof Project) {
          final Project project = (Project) doc;
          module = importer.importModule(project);
        } else {
          throw new ClassCastException("Unknown document type!");
        }
        if (verbose) {
          System.out.println(module.getName() + " ... ");
        }
        // Generate images
        generator.generateEPS(module);
      }

    } catch (final WatersUnmarshalException | IOException |
                   SAXException | ParseException exception) {
      System.err.print("FATAL ERROR (");
      System.err.print(ProxyTools.getShortClassName(exception));
      System.err.println(")");
      final String msg = exception.getMessage();
      if (msg != null) {
        System.err.println(exception.getMessage());
      }
    } catch (final Throwable exception) {
      System.err.println("FATAL ERROR !!!");
      System.err.print(ProxyTools.getShortClassName(exception));
      System.err.println(" caught in main()!");
      exception.printStackTrace(System.err);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private static void usage()
  {
    System.err.println
      ("USAGE: java " + ProxyTools.getShortClassName(EPSGenerator.class) +
       " [-o directory] [-p properties] [-q] file1 file2 ...");
    System.exit(1);
  }


  //#########################################################################
  //# Constructor
  private EPSGenerator(final File outputDir, final boolean verbose)
  {
    mOutputDirectory = outputDir;
    mVerbose = verbose;
  }


  //#######################################################################
  //# Invocation
  private void generateEPS(final ModuleProxy module)
    throws IOException
  {
    try {
      final ModuleContext context = new ModuleContext(module);
      mContext = new ModuleRenderingContext(context);
      module.acceptVisitor(this);
    } catch (final VisitorException exception) {
      final Throwable cause = exception.getCause();
      if (cause instanceof IOException) {
        throw (IOException) cause;
      } else {
        throw exception.getRuntimeException();
      }
    } finally {
      mContext = null;
    }
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.base.ProxyVisitor
  /**
   * Skip everything except simple components.
   */
  @Override
  public Object visitProxy(final Proxy comp)
  {
    return null;
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  /**
   * Visit the component list of a module.
   */
  @Override
  public Object visitModuleProxy(final ModuleProxy module)
    throws VisitorException
  {
    final Collection<Proxy> components = module.getComponentList();
    return visitCollection(components);
  }

  /**
   * Visit simple component and output .eps file. The only reason that
   * visitGraphProxy() is not used instead is that we need the name ...
   */
  @Override
  public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
    throws VisitorException
  {
    final String name = comp.getName();
    final Map<String,String> attribs = comp.getAttributes();
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
      final File file = new File(mOutputDirectory, name + ".eps");
      try {
        final EPSGraphPrinter printer =
          new EPSGraphPrinter(graph, mContext, file);
        printer.print();
        if (mVerbose) {
          System.out.println("Wrote " + file.getAbsolutePath());
        }
      } catch (final IOException exception) {
        throw wrap(exception);
      }
    }
    // Return any value ...
    return null;
  }


  //#######################################################################
  //# Data Members
  private final File mOutputDirectory;
  private final boolean mVerbose;
  private RenderingContext mContext;

}
