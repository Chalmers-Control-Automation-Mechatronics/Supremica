package org.supremica.automata.BDD.EFA;

import org.supremica.automata.algorithms.SynthesisAlgorithm;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

/**
 * Different types of partitioning. 
 * 
 * For automata without variables, e.g. DFAs, the conjunctive partitioning was implemented using JavaBDD before.
 * Arash implemented the disjunctive partitioning technique using JDD. What I did to DFAs is to re-implement Arash's
 * best algorithms and heuristics and improve them (see ICRA11). Besides, combine it with Sajed's guard
 * generation procedure (see ICAART). Regarding pure EFAs, right now only the disjunctive partitioning technique
 * is implemented for EFAs, which is event-based.
 * 
 * We may have clock-based partitioning for Timed EFAs (some EFA variables are either global or local clocks).
 * Or, for resource allocation systems, probably a resource-based partitioning can be made. 
 * The class needs to be extended when a new partitioning type is introduced. Now, it has only one, just called
 * "partitioning". Moreover, depending on the chosen partitioning, the corresponding work set is initialized as well. 
 * 
 * @author  Zhennan
 * @version 2.0
 */

public class BDDPartitionTypeFactory {

    private static Logger logger = LoggerFactory.createLogger(BDDPartitionTypeFactory.class);
    
    /* Private constructor: prevent from instantiating instances of this class. */
    private BDDPartitionTypeFactory () {
        
    }
    
    /** 
     * Return an instance of the subclass of BDDPartitioningAlgWorker. 
     * 
     * @param bddExAutomata the reference to the BDDExtendedAutomata object
     * @param synType the synthesis algorithm type
     * @return an instance of either BDDPartitionAlgoWorkerEve or BDDPartitionAlgoWorkerAut
     */
    public static BDDPartitionAlgoWorker getPartitioningAlgorithmWorker(BDDExtendedAutomata bddExAutomata, SynthesisAlgorithm synType) {
        BDDPartitionAlgoWorker parAlgoWorker = null; 
        if (synType == SynthesisAlgorithm.PARTITIONBDD) {
            if (!bddExAutomata.orgExAutomata.getVars().isEmpty()) {
                logger.info("EFAs: the event-based paritioning algorithm is chosen.");
                BDDPartitionSet eventPartitions = new BDDPartitionSetEve(bddExAutomata);
                BDDPartitionCoordinator eventCoordinator = new BDDPartitionCoordinatorEve(eventPartitions);
                parAlgoWorker = new BDDPartitionAlgoWorkerEve(eventPartitions, eventCoordinator);
                // TEST!!
                //parAlgoWorker = new BDDPartitionAlgoWorkerRan(eventPartitions, eventCoordinator);
            } else {
                logger.info("DFAs: the automaton-based paritioning algorithm is chosen.");
                BDDPartitionSet automatonPartitions = new BDDPartitionSetAut(bddExAutomata);
                BDDPartitionCoordinator automatonCoordinator = new BDDPartitionCoordinatorAut(automatonPartitions);
                parAlgoWorker = new BDDPartitionAlgoWorkerAut(automatonPartitions, automatonCoordinator);
            }
        } else if (synType == SynthesisAlgorithm.CLOCKPARTITION) {
            logger.info("TEFAs: the clock-based partitioning algorithm is chosen.");
            throw new UnsupportedOperationException();
        } else if (synType == SynthesisAlgorithm.MINIMALITY_P) {
            logger.info("RAS models: the resource-based partitioning algorithm is choosen.");
            BDDPartitionSet eventPartitions = new BDDPartitionSetEve(bddExAutomata);
            BDDPartitionCoordinator eventCoordinator = new BDDPartitionCoordinatorEve(eventPartitions);
            parAlgoWorker = new BDDPartitionAlgoWorkerRan(eventPartitions, eventCoordinator);
        }
        return parAlgoWorker;
    }
}
