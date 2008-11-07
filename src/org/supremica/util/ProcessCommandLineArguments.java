//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica/Waters IDE
//# PACKAGE: org.supremica.util
//# CLASS:   ProcessCommandLineArguments
//###########################################################################
//# $Id$
//###########################################################################

/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.util;

import java.awt.Dimension;
import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.ControlledToolbar;
import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.renderer.EPSGraphPrinter;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.model.base.AbstractProxyVisitor;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.ForeachComponentProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.valid.ValidUnmarshaller;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.supremica.Version;
import org.supremica.automata.IO.ADSUnmarshaller;
import org.supremica.automata.IO.HISCUnmarshaller;
import org.supremica.automata.IO.SupremicaUnmarshaller;
import org.supremica.automata.IO.UMDESUnmarshaller;
import org.supremica.automata.Project;
import org.supremica.properties.Config;
import org.supremica.properties.SupremicaProperties;

import org.w3c.dom.DOMImplementation;
import org.xml.sax.SAXException;

/**
 * Class responsible for interpreting command line arguments given to Supremica.
 */
public class ProcessCommandLineArguments
{
    /**
     * Processes an array of arguments. Returns a list of files to be
     * opened on startup.
     */
    public static List<File> process(String[] args)
    {
        boolean quit = false;
        boolean verbose = false;
        List<File> filesToOpen = new LinkedList<File>();

        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals("-h") || args[i].equals("-?") || args[i].equals("--help") || args[i].equals("--usage"))
            {
                // Print usage
                printUsage();
                
                // Quit after this
                quit = true;
            }
            if (args[i].equals("--verbose"))
            {
                System.out.println("Verbose mode");
                Config.VERBOSE_MODE.set(true);
                
                // Print usage
                verbose = true;
            }
            else if (args[i].equals("-p") || args[i].equals("--properties"))
            {
                // Load properties
                if (++i < args.length)
                {
                    String fileName = args[i];
                    File propFile = new File(fileName);

                    try
                    {
                        if (!propFile.exists())
                        {
                            System.out.println("Creating property file: " + propFile.getAbsolutePath());
                            propFile.createNewFile();
                        }

                        SupremicaProperties.loadProperties(propFile);
                        if (verbose)
                        {
                            Config.VERBOSE_MODE.set(true);
                        }
                    }
                    catch (Exception e)
                    {
                        System.err.println("Error reading properties file: " + propFile.getAbsolutePath());
                    }
                }
            }
            else if (args[i].equals("-e") || args[i].equals("--epsfigs"))
            {
                // Create eps figs for all components in the supplied file
                while ((i+1 < args.length) && !(args[i+1].startsWith("-")))
                {
                    String fileName = args[++i];
                    File figFile = new File(fileName);

                    // Set up document manager ...
                    DocumentManager documentManager = new DocumentManager();
                    ProductDESImporter importer; 
                    try
                    {
                        final ModuleProxyFactory factory = ModuleSubjectFactory.getInstance();
                        final OperatorTable opTable = CompilerOperatorTable.getInstance();
                        final JAXBModuleMarshaller moduleMarshaller =
                            new JAXBModuleMarshaller(factory, opTable);
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
                    }
                    catch (SAXException ex)
                    {
                        System.err.println("SAXException when initialising document manager: " + ex);
                        return null;
                    }
                    catch (JAXBException ex)
                    {
                        System.err.println("JAXBException when initialising document manager: " + ex);
                        return null;
                    }
                
                    // Do the printing
                    try
                    {                        
                        // Load file
                        DocumentProxy doc = documentManager.load(figFile);

                        // Build module
                        ModuleProxy module;
                        if (doc instanceof ModuleProxy)
                        {
                            module = (ModuleProxy) doc;
                        }
                        else if (doc instanceof Project)
                        {
                            module = importer.importModule((Project) doc);
                        }
                        else
                        {
                            throw new ClassCastException("Unknown document type");
                        }

                        // Loop throgh components and print eps-figures
                        //module.acceptVisitor(new EPSPrinterVisitor(module));
                        final List<Proxy> components = module.getComponentList();
                        AbstractProxyVisitor visitor = new EPSPrinterVisitor(module, verbose);
                        visitor.visitCollection(components);
                    }
                    catch (IOException ex)
                    {
                        System.err.println("IO problem: " + ex);
                    }                   
                    catch (WatersUnmarshalException ex)
                    {
                        System.err.println("Problem unmarshalling: " + ex);
                    }
                    catch (ClassCastException ex)
                    {
                        System.err.println("Only import of modules is supported: " + ex);
                    }                   
                    catch (VisitorException ex)
                    {
                        System.err.println("Problems when visiting module: " + ex);
                    }                   
                }

                // Quit after this (even if there were no files)
                quit = true;
            }
            else if (args[i].equals("--svgfigs"))
            {
                // Create eps figs for all components in the supplied file
                while ((i+1 < args.length) && !(args[i+1].startsWith("-")))
                {
                    String fileName = args[++i];
                    File figFile = new File(fileName);

                    // Set up document manager ...
                    DocumentManager documentManager = new DocumentManager();
                    ProductDESImporter importer; 
                    try
                    {
                        final ModuleProxyFactory factory = ModuleSubjectFactory.getInstance();
                        final OperatorTable opTable = CompilerOperatorTable.getInstance();
                        final JAXBModuleMarshaller moduleMarshaller =
                            new JAXBModuleMarshaller(factory, opTable);
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
                    }
                    catch (SAXException ex)
                    {
                        System.err.println("SAXException when initialising document manager: " + ex);
                        return null;
                    }
                    catch (JAXBException ex)
                    {
                        System.err.println("JAXBException when initialising document manager: " + ex);
                        return null;
                    }
                
                    // Do the printing
                    try
                    {                        
                        // Load file
                        DocumentProxy doc = documentManager.load(figFile);

                        // Build module
                        ModuleProxy module;
                        if (doc instanceof ModuleProxy)
                        {
                            module = (ModuleProxy) doc;
                        }
                        else if (doc instanceof Project)
                        {
                            module = importer.importModule((Project) doc);
                        }
                        else
                        {
                            throw new ClassCastException("Unknown document type");
                        }

                        // Loop throgh components and print eps-figures
                        //module.acceptVisitor(new EPSPrinterVisitor(module));
                        final List<Proxy> components = module.getComponentList();
                        //AbstractProxyVisitor visitor = new EPSPrinterVisitor(module, verbose);
                        //visitor.visitCollection(components);

                        for(Proxy p: components){
                            if(!(p instanceof SimpleComponentProxy))
                                continue;
                            SimpleComponentProxy component = (SimpleComponentProxy)p;
                            ControlledSurface mSurface = new ControlledSurface
                                    ( (GraphSubject) component.getGraph()
                                    , (ModuleSubject) module
                                    , (EditorWindowInterface) null
                                    , (ControlledToolbar) new ControlledToolbar() {

                                            public Tool getTool() {
                                                return ControlledToolbar.Tool.SELECT;
                                            }
                                            public void attach(Observer o) {
                                                throw new UnsupportedOperationException("Not supported yet.");
                                            }
                                            public void detach(Observer o) {
                                                throw new UnsupportedOperationException("Not supported yet.");
                                            }
                                            public void fireEditorChangedEvent(EditorChangedEvent e) {
                                                throw new UnsupportedOperationException("Not supported yet.");
                                            }
                                        }
                                    , (WatersPopupActionManager) null
                                    );
                            
                            //mSurface.setPreferredSize(new Dimension(640, 480));
                            //mSurface.setMinimumSize(new Dimension(640, 480));

                            // Get a DOMImplementation.
                            DOMImplementation domImpl =
                                GenericDOMImplementation.getDOMImplementation();

                            // Create an instance of org.w3c.dom.Document.
                            String svgNS = "http://www.w3.org/2000/svg";
                            org.w3c.dom.Document document = domImpl.createDocument(svgNS, "svg", null);

                            // Create an instance of the SVG Generator.
                            SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

                            // Ask the test to render into the SVG Graphics2D implementation.
                            //Graphics2D
                            final JScrollPane scrollsurface = new JScrollPane(mSurface);

                            JFrame frame = new JFrame("ToolBarDemo");
                            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                            //Add content to the window.
                            frame.add(scrollsurface);

                            //Display the window.
                            frame.pack();
                            frame.setVisible(true);
                            
                            
                            mSurface.print(svgGenerator);

                            // Finally, stream out SVG to the standard output using
                            // UTF-8 encoding.
                            boolean useCSS = true; // we want to use CSS style attributes
                            Writer out = 
                                    new OutputStreamWriter( 
                                      new FileOutputStream(
                                        new File(
                                          (fileName + "-"
                                            + component.getName() 
                                            + ".svg"
                                          ).replace("|", "-").replace(":", "-")
                                        )
                                      )
                                      , "UTF-8"
                                    );
                            svgGenerator.stream(out, useCSS);
                        }
                    }
                    catch (IOException ex)
                    {
                        System.err.println("IO problem: " + ex);
                    }                   
                    catch (WatersUnmarshalException ex)
                    {
                        System.err.println("Problem unmarshalling: " + ex);
                    }
                    catch (final GeometryAbsentException ex)
                    {
                        System.err.println
                            ("Trying to print component without geometry!");
                    }
                    catch (ClassCastException ex)
                    {
                        System.err.println("Only import of modules is supported: " + ex);
                    }                   
                }

                // Quit after this (even if there were no files)
                quit = true;
            }
            else if (args[i].equals("-l") || args[i].equals("--list"))
            {
                System.out.println(SupremicaProperties.getProperties());
                quit = true;
            }
            else if (args[i].equals("-v") || args[i].equals("--version"))
            {
                System.out.println("Supremica version: " + Version.version());
                quit = true;
            }
            else
            {
                String filename = args[i];
                File  currFile = new File(filename);
                if (!currFile.exists())
                {
                    System.out.println("Invalid usage: '" + args[i] + "'.\n");
                    ProcessCommandLineArguments.printUsage();
                    quit = true;
                }
                else
                {
                    filesToOpen.add(currFile);
                }
            }
        }

        if (quit)
        {
            System.exit(0);
        }

        return filesToOpen;
    }

    /**
     * --help
     */
    private static void printUsage()
    {
        System.out.println
                ( "Supremica: " + org.supremica.Version.version() + "\n"
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
                + "-v, --version                show version\n"
                + "\n");
    }
}

/**
 * Visitor for visiting all simple components and output eps-files
 * for the graphs. 
 */
class EPSPrinterVisitor
    extends AbstractModuleProxyVisitor
{
    final boolean verbose;
    
    //#######################################################################
    //# Constructor
    EPSPrinterVisitor(final ModuleProxy module, boolean verbose)
    {
        mModule = module;
        this.verbose = verbose;
    }


    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    /**
     * Visit the children of foreach constructs in the component list.
     */
    public Object visitForeachComponentProxy
        (final ForeachComponentProxy foreach)
        throws VisitorException
    {
        final Collection<Proxy> body = foreach.getBody();
        return visitCollection(body);
    }

    /**
     * Visit simpleComponent and output eps-file.
     * The only reason that visitGraphProxy is not used instead is that we
     * need the name ...
     */
    public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
        throws VisitorException
    {
        try {
            final String name = comp.getName();
            final File file = new File(name + ".eps");
            final GraphProxy graph = comp.getGraph();
            final EPSGraphPrinter printer =
                new EPSGraphPrinter(graph, mModule, file);
            printer.print();

            // Log
            if (verbose)
            {
                System.out.println("Wrote " + file.getAbsolutePath());
            }
            
            // Return any value ...
            return null;
        } catch (final IOException exception) {
            throw wrap(exception);
        }
    }

    //#######################################################################
    //# Data Members
    final ModuleProxy mModule;

}
