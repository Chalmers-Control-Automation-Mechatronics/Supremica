
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

package org.supremica.automata.BDD;

import net.sf.javabdd.*;
import org.supremica.log.*;
import org.supremica.util.SupremicaException;
import java.util.*;
import org.supremica.properties.Config;

public class BDDTransitionFactory
{
    private static Logger logger = LoggerFactory.createLogger(BDDTransitionFactory.class);
    
    BDDAutomata bddAutomata;
    
    public BDDTransitionFactory(BDDAutomata bddAutomata)
    {
        this.bddAutomata = bddAutomata;
    }
    
    public BDDTransitions createTransitions()
    {
        BDDPartitioningType partitioningType = BDDPartitioningType.fromDescription(Config.BDD2_PARTITIONING.getAsString());
        
        if (partitioningType == BDDPartitioningType.MONOLITHIC)
        {
            logger.debug("Creating monolithic transitions");
            return new BDDMonolithicTransitions(bddAutomata);
        }
        else if (partitioningType == BDDPartitioningType.CONJUNCTIVE)
        {
            logger.debug("Creating conjunctive transitions");
            return new BDDConjunctiveTransitions(bddAutomata);
        }
        else if (partitioningType == BDDPartitioningType.DISJUNCTIVE)
        {
            logger.debug("Creating disjunctive transitions");
            return new BDDDisjunctiveTransitions(bddAutomata);
        }
        else
        {
            logger.error("Unknown partitioning: " + partitioningType.toString());
            return null;
        }
    }

}
