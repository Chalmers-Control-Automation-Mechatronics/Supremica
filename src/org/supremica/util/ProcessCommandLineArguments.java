
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

import org.supremica.properties.Config;
import org.supremica.properties.SupremicaProperties;
import org.supremica.Version;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import org.supremica.gui.ide.actions.IDEActionInterface;

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
                printUsage();
                quit = true;
            }
            else if (args[i].equals("-p") || args[i].equals("--properties"))
            {
                if (i + 1 < args.length)
                {
                    String fileName = args[i + 1];
                    i++;
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
            else if (args[i].equals("-noEditor"))
            {
				Config.GENERAL_INCLUDE_EDITOR.set(false);
            }
            else if (args[i].equals("-l") || args[i].equals("--list"))
            {
                System.out.println(SupremicaProperties.getProperties());
                //quit = true;
            }
            else if (args[i].equals("-v") || args[i].equals("--version"))
            {
                System.out.println("Supremica version: " + Version.version());
                //quit = true;
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

        /*
        // I'd like to flush the output to the console, but it's intercepted by the logger...
        // So nothing gets output if we quit here!
        System.out.println();
        System.err.println();
        */

        if (quit)
        {
            System.exit(0);
        }

        return filesToOpen;
    }

    private static void printUsage()
    {
        System.out.println("Supremica: " + org.supremica.Version.version());
        System.out.println("More information about Supremica is available at www.supremica.org\n");
        System.out.println("Usage: Supremica [OPTION] MODULE_FILES\n");
        System.out.println("Property options: \n  -p, --properties FILE\t\t Load properties from FILE");
        System.out.println("List: \n  -l, --list [FILE]\t\t List properties with current values (or values in FILE)");
        System.out.println("Help options: \n  -?, -h, --help, --usage\t show this help message");
        System.out.println("Version: \n  -v, --version \t\t show version");
        System.out.println("");
    }

}
