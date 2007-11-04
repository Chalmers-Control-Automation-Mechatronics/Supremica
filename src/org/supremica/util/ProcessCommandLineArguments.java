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

import javax.xml.bind.JAXBException;
import net.sourceforge.waters.model.base.AbstractProxyVisitor;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import org.supremica.gui.ide.actions.EditorSaveEncapsulatedPostscriptAction;
import org.supremica.properties.SupremicaProperties;
import org.supremica.Version;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import net.sourceforge.waters.gui.springembedder.SpringEmbedder;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.valid.ValidUnmarshaller;
import org.supremica.automata.IO.ADSUnmarshaller;
import org.supremica.automata.IO.HISCUnmarshaller;
import org.supremica.automata.IO.SupremicaUnmarshaller;
import org.supremica.automata.IO.UMDESUnmarshaller;
import org.supremica.automata.Project;
import org.xml.sax.SAXException;

/**
 * Class responsible for interpreting command line arguments given to Supremica.
 */
public class ProcessCommandLineArguments
{
    /**
     * Processes an array of arguments. Returns a list of files to be opened on startup.
     */
    public static List<File> process(String[] args)
    {
        boolean quit = false;
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
                        AbstractProxyVisitor visitor = new EPSPrinterVisitor(module);
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
        System.out.println("Supremica: " + org.supremica.Version.version());
        System.out.println("More information about Supremica is available at www.supremica.org\n");
        System.out.println("Usage: IDE [OPTION] MODULE_FILES\n");
        System.out.println("Property options: \n  -p, --properties FILE\t\t Load properties from FILE");
        System.out.println("EPS-figure generation: \n  -e, --epsfigs FILE... \t Creates eps-figures from all components in FILEs");
        System.out.println("List: \n  -l, --list [FILE]\t\t List properties with current values (or values in FILE)");
        System.out.println("Help options: \n  -?, -h, --help, --usage\t show this help message");
        System.out.println("Version: \n  -v, --version \t\t show version");
        System.out.println("");
    }
}

/**
 * Visitor for visiting all simpleComponentProxys and output eps-files for the graphs.
 */
class EPSPrinterVisitor
    extends AbstractModuleProxyVisitor
{
    ModuleProxy module;
    
    EPSPrinterVisitor(ModuleProxy module)
    {
        this.module = module;
    }

    /**
     * Visit simpleComponent and output eps-file.
     * The only reason that visitGraphProxy is not used instead is that we need the name...
     */
    public Object visitSimpleComponentProxy(final SimpleComponentProxy proxy) throws VisitorException
    {
        // Create file        
        File file = new File(proxy.getName() + ".eps");

        // Print!
        try
        {
            EditorSaveEncapsulatedPostscriptAction.saveEPS(file, proxy.getGraph(), module);
            System.err.println("Created eps-file: " + file.getAbsolutePath());
        }
        catch (NullPointerException ex)
        {
            System.err.println("Component '" + file.getName().substring(0,file.getName().length()-4) + "' appears to be missing geometry information.");
        }

        // Return null?
        return null;
    }
}
