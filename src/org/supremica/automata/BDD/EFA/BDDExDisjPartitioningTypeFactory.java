package org.supremica.automata.BDD.EFA;

import org.supremica.automata.algorithms.SynthesisAlgorithm;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

/**
 * Based on the selected partitioning algorithm by the user, the factory 
 * will do the partition and work set initialization.  
 * 
 * @author Zhennan
 */
public class BDDExDisjPartitioningTypeFactory {

    private static Logger logger = LoggerFactory.createLogger(BDDExDisjPartitioningTypeFactory.class);

    public static BDDExDisjDepSets getDepSets(BDDExtendedAutomata bddExAutomata, SynthesisAlgorithm synType) {

        BDDExDisjDepSets depSets = null;
        BDDExDisjAbstractWorkSet workset = null;

        if (synType == SynthesisAlgorithm.PARTITIONBDD_Event) {
            logger.info("Choose the event based paritioning algorithm ...");
            depSets = new BDDExDisjEventDepSets(bddExAutomata);
            workset = new BDDExDisjWorkSetImpl(depSets, depSets.getSize());
            depSets.setWorkSet(workset);
        } else if (synType == SynthesisAlgorithm.PARTITIONBDD_Automaton) {
            logger.info("Choose the automaton based partitioning algorithm ...");
            BDDExDisjEventDepSets eventDepSets = new BDDExDisjEventDepSets(bddExAutomata);
            depSets = new BDDExDisjAutmatonDepSets(bddExAutomata, eventDepSets);
            workset = new BDDExDisjWorkSetImpl(depSets, depSets.getSize());
            depSets.setWorkSet(workset);
        } 
        else if (synType == SynthesisAlgorithm.PARTITIONBDD_Variable) {
            BDDExDisjEventDepSets eventDepSets = new BDDExDisjEventDepSets(bddExAutomata);;
            if (bddExAutomata.orgExAutomata.getVars() == null) {
                logger.info("Not an EFA model. Choose the automaton based partitioning algorithm (default for DFA)");
                depSets = new BDDExDisjAutmatonDepSets(bddExAutomata, eventDepSets);
                workset = new BDDExDisjWorkSetImpl(depSets, depSets.getSize());
                depSets.setWorkSet(workset);
            } else {
                logger.info("Choose the variable based paritioning algorithm");
                depSets = new BDDExDisjVariableDepSets(bddExAutomata, eventDepSets);
                workset = new BDDExDisjWorkSetImpl(depSets, depSets.getSize());
                depSets.setWorkSet(workset);
            }
        }
        return depSets;
    }
}
