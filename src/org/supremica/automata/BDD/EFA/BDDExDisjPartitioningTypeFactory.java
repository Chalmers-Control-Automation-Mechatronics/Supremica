package org.supremica.automata.BDD.EFA;

import org.supremica.automata.algorithms.SynthesisAlgorithm;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

/**
 * Based on the selected partitioning algorithm by the user, the factory 
 * will do the partition and work set initialization.  
 * 
 * More partitioning approaches are coming. 
 * 
 * @author Zhennan
 */
public class BDDExDisjPartitioningTypeFactory {

    private static Logger logger = LoggerFactory.createLogger(BDDExDisjPartitioningTypeFactory.class);

    public static BDDExDisjDepSets getDepSets(BDDExtendedAutomata bddExAutomata, SynthesisAlgorithm synType) {

        BDDExDisjDepSets depSets = null;
        BDDExDisjAbstractWorkSet workset = null;

        if (synType == SynthesisAlgorithm.PARTITIONBDD_Event) {
            logger.info("Choose the symbolic paritioning algorithm.");
            depSets = new BDDExDisjEventDepSets(bddExAutomata);
            workset = new BDDExDisjWorkSetImpl(depSets, depSets.getSize());
            depSets.setWorkSet(workset);
        }
        return depSets;
    }
}
