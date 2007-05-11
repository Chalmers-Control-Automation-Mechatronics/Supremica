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
package org.supremica.properties;

import java.util.*;
import java.io.*;
import javax.swing.text.html.Option;
import org.supremica.util.BDD.Options;

/**
 * Properties for Supremica.
 * All properties are added in the Config class.
 */
public final class SupremicaProperties
{
    private SupremicaProperties()
    {
    }
    
    public static Iterator<Property> iterator()
    {
        return Property.iterator();
    }
    
    public static String getProperties()
    {
        StringBuffer sb = new StringBuffer();
        for (Iterator<Property> it = iterator(); it.hasNext(); )
        {
            Property currProperty = it.next();
            sb.append("# " + currProperty.getComment() + "\n");
            sb.append(currProperty.toString() + "\n\n");
        }
        return sb.toString();
        
    }
    
    public static void loadProperties(File theFile)
    throws FileNotFoundException, IOException
    {
        propertyFile = theFile;
        updateProperties(propertyFile);
    }
    
    /**
     * Load properties from file.
     */
    private static void updateProperties(File propertyFile)
    throws FileNotFoundException, IOException
    {
        Properties propertiesFromFile = buildProperties(propertyFile);
        for (Enumeration e = propertiesFromFile.keys(); e.hasMoreElements(); )
        {
            String newKey = (String)e.nextElement();
            String newValue = propertiesFromFile.getProperty(newKey);
            
            Property orgProperty = Property.getProperty(newKey);
            if (orgProperty == null)
            {
                System.err.println("Unknown property: " + newKey);
            }
            else if (orgProperty.isImmutable())
            {
                System.err.println("Property \"" + newKey + "\" is immutable");                
            }
            else
            {
                try
                {
                    orgProperty.set(newValue);
                }
                catch (IllegalArgumentException ex)
                {
                    System.err.println("Invalid argument to key: " + newKey);
                }
            }
        }
        
        // Update values in BDD.Options based on the current Config.
        updateBDDOptions(false);        
    }
    
    public static void saveProperties()
    throws FileNotFoundException, IOException
    {
        supremicaProperties.saveProperties(false);
    }
    
    
    public static void saveProperties(boolean saveAll)
    throws FileNotFoundException, IOException
    {
        if (propertyFile != null)
        {
            saveProperties(propertyFile, saveAll);
        }
        else
        {
            System.err.println("Could not write to configuration file, unknown file name.");
        }
    }
    
    /**
     * Save the property list to the configuration file.
     *
     * @param propertyFile is the name of the config file
     * @param saveAll if this is true all mutable properties are saved to file
     * otherwise only those properties that values different from the default value is saved.
     */
    private static void saveProperties(File propertyFile, boolean saveAll)
    throws FileNotFoundException, IOException
    {
        // Update config from the current values in BDD.Options 
        // (WHY!!? IT SHOULD BE THE OTHER WAY AROUND OR THEY ARE LOST?! /hguo)
        //updateBDDOptions(true);    // first sync from BDD options
        updateBDDOptions(false);    // Send the new Config values to BDD.Options
        
        OutputStream os = new FileOutputStream(propertyFile);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "8859_1"));
        
        writer.write("# Supremica configuration file\n");
        writer.write("# Created: " + new Date().toString() + "\n\n");
        
        for (Iterator<Property> currIt = SupremicaProperties.iterator(); currIt.hasNext(); )
        {
            Property currProperty = currIt.next();
            if ((saveAll && !currProperty.isImmutable()) || currProperty.currentValueDifferentFromDefaultValue())
            {
                writer.append("# " + currProperty.getComment() + "\n");
                writer.append(currProperty.getPropertyType() + "." + currProperty.getKey() + " " + currProperty.valueToEscapedString()  + "\n\n");
            }
        }
        
        writer.flush();
        os.close();
    }
    
    private static Properties buildProperties(File theFile)
    throws FileNotFoundException, IOException
    {
        FileInputStream theStream = new FileInputStream(theFile);
        return buildProperties(new BufferedInputStream(theStream));
    }
    
    private static Properties buildProperties(InputStream inStream)
    throws IOException
    {
        Properties newProperties = new Properties();
        newProperties.load(inStream);
        return newProperties;
    }
    
    /*
     * The problem is that we got two copies of BDD Options.
     * This will make sure they are both updated
     *
     * TO DO: Rewrite the Option code in the BDD Package to
     * support the new style property handling.
     */
    public static void updateBDDOptions(boolean from_Options)
    {
        if (from_Options)
        {
            // Options -> Config
            //Config.BDD_ALGORITHM.set(Options.algo_family);
            Config.BDD_ALGORITHM.set(Options.REACH_ALGO_NAMES[Options.algo_family]);
            Config.BDD_SHOW_GROW.set(Options.show_grow);
            Config.BDD_SIZE_WATCH.set(Options.size_watch);
            Config.BDD_ALTER_PCG.set(Options.user_alters_PCG);
            Config.BDD_DEBUG_ON.set(Options.debug_on);
            Config.BDD_PROFILE_ON.set(Options.profile_on);
            Config.BDD_UC_OPTIMISTIC.set(Options.uc_optimistic);
            Config.BDD_NB_OPTIMISTIC.set(Options.nb_optimistic);
            Config.BDD_LOCAL_SATURATION.set(Options.local_saturation);
            Config.BDD_TRACE_ON.set(Options.trace_on);
            Config.BDD_COUNT_ALGO.set(Options.count_algo);
            Config.BDD_LI_ALGO.set(Options.inclusion_algorithm);
            Config.BDD_ORDER_ALGO.set(Options.ordering_algorithm);
            Config.BDD_ORDERING_FORCE_COST.set(Options.ordering_force_cost);
            Config.BDD_AS_HEURISTIC.set(Options.as_heuristics);
            Config.BDD_FRONTIER_TYPE.set(Options.frontier_strategy);
            Config.BDD_H1.set(Options.es_heuristics);
            Config.BDD_H2.set(Options.ndas_heuristics);
            Config.BDD_DSSI_HEURISTIC.set(Options.dssi_heuristics);
            Config.BDD_PARTITION_MAX.set(Options.max_partition_size);
            Config.BDD_ENCODING_ALGO.set(Options.encoding_algorithm);
            Config.BDD_LIB_PATH.set(Options.extraLibPath);
            Config.BDD_SUP_REACHABILITY.set(Options.sup_reachability_type);
            Config.BDD_DISJ_OPTIMIZER_ALGO.set(Options.disj_optimizer_algo);
            Config.BDD_TRANSITION_OPTIMIZER_ALGO.set(Options.transition_optimizer_algo);
            Config.BDD_INTERLEAVED_VARIABLES.set(Options.interleaved_variables);
            Config.BDD_LEVEL_GRAPHS.set(Options.show_level_graph);
        }
        else
        {
            // Config -> Options
            //Options.algo_family = Config.BDD_ALGORITHM.get();
            Options.algo_family = indexOf(Config.BDD_ALGORITHM.get(), Options.REACH_ALGO_NAMES);
            Options.show_grow = Config.BDD_SHOW_GROW.get();
            Options.size_watch = Config.BDD_SIZE_WATCH.get();
            Options.user_alters_PCG = Config.BDD_ALTER_PCG.get();
            Options.debug_on = Config.BDD_DEBUG_ON.get();
            Options.uc_optimistic = Config.BDD_UC_OPTIMISTIC.get();
            Options.nb_optimistic = Config.BDD_NB_OPTIMISTIC.get();
            Options.local_saturation = Config.BDD_LOCAL_SATURATION.get();
            Options.trace_on = Config.BDD_TRACE_ON.get();
            Options.profile_on = Config.BDD_PROFILE_ON.get();
            Options.count_algo = Config.BDD_COUNT_ALGO.get();
            Options.inclusion_algorithm = Config.BDD_LI_ALGO.get();
            Options.ordering_algorithm = Config.BDD_ORDER_ALGO.get();
            Options.ordering_force_cost = Config.BDD_ORDERING_FORCE_COST.get();
            Options.as_heuristics = Config.BDD_AS_HEURISTIC.get();
            Options.frontier_strategy = Config.BDD_FRONTIER_TYPE.get();
            Options.es_heuristics = Config.BDD_H1.get();
            Options.ndas_heuristics = Config.BDD_H2.get();
            Options.dssi_heuristics = Config.BDD_DSSI_HEURISTIC.get();
            Options.max_partition_size = Config.BDD_PARTITION_MAX.get();
            Options.encoding_algorithm = Config.BDD_ENCODING_ALGO.get();
            Options.extraLibPath = Config.BDD_LIB_PATH.get();
            Options.sup_reachability_type = Config.BDD_SUP_REACHABILITY.get();
            Options.disj_optimizer_algo = Config.BDD_DISJ_OPTIMIZER_ALGO.get();
            Options.transition_optimizer_algo = Config.BDD_TRANSITION_OPTIMIZER_ALGO.get();
            Options.interleaved_variables = Config.BDD_INTERLEAVED_VARIABLES.get();
            Options.show_level_graph = Config.BDD_LEVEL_GRAPHS.get();
        }
    }
    
    /**
     * Returns the index of object in objects. For the BDD options.
     */
    private static int indexOf(String object, String[] objects)
    {
        for (int i=0; i< objects.length; i++)
        {
            if (object.equals(objects[i]))
                return i;
        }
        return -1;
    }
    
    private static SupremicaProperties supremicaProperties;
    private static Config config = Config.getInstance();
    private static File propertyFile = null;
    
    static
    {
        supremicaProperties = new SupremicaProperties();
        // Update values in BDD.Options based on Config.
        updateBDDOptions(false);
    }
    
    public static void main(String[] args)
    {
        System.out.println(supremicaProperties.toString());
    }
}
