//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
package org.supremica.automata.BDD.EFA;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.algorithms.SynthesisAlgorithm;


/**
 * <P>Different types of partitioning.</P>
 *
 * <P>For automata without variables, e.g. DFAs, the conjunctive partitioning
 * was implemented using JavaBDD before. Arash implemented the disjunctive
 * partitioning technique using JDD. What I did to DFAs is to correct and
 * re-implement Arash's best algorithms and heuristics and improve them (see
 * ICRA11). Besides, combine it with Sajed's guard generation procedure (see
 * ICAART). Regarding EFAs, the disjunctive partitioning which is implemented,
 * is event-based.</P>
 *
 * <P>We may have clock-based partitioning for Timed EFAs (some EFA variables
 * are either global or local clocks). Or, for resource allocation systems,
 * probably a resource-based partitioning can be made. The class needs to be
 * extended when a new partitioning type is introduced. Now, it has only one,
 * just called "partitioning". Moreover, depending on the chosen partitioning,
 * the corresponding work set is initialised as well.</P>
 *
 * @author Zhennan Fei
 */

public class BDDPartitionTypeFactory
{

  private static Logger logger =
    LogManager.getLogger(BDDPartitionTypeFactory.class);

  /*
   * Private constructor: prevent from instantiating instances of this class.
   */
  private BDDPartitionTypeFactory()
  {

  }

  /**
   * Return an instance of the subclass of BDDPartitioningAlgWorker.
   *
   * @param bddExAutomata
   *          the reference to the BDDExtendedAutomata object
   * @param synType
   *          the synthesis algorithm type
   * @return an instance of either BDDPartitionAlgoWorkerEve or
   *         BDDPartitionAlgoWorkerAut
   */
  public static BDDPartitionAlgoWorker
    getPartitioningAlgorithmWorker(final BDDExtendedAutomata bddExAutomata,
                                   final SynthesisAlgorithm synType)
  {
    BDDPartitionAlgoWorker parAlgoWorker = null;

    final BDDPartitionImageOperator imageOperator =
      new BDDPartitionImageOperator();

    if (synType == SynthesisAlgorithm.PARTITIONBDD) {
      if (!bddExAutomata.orgExAutomata.getVars().isEmpty()) {
        logger.info("EFAs: event-based paritioning.");
        final BDDPartitionSet eventPartitions =
          new BDDPartitionSetEve(bddExAutomata);
        final BDDPartitionCoordinator eventCoordinator =
          new BDDPartitionCoordinatorEve(eventPartitions);
        parAlgoWorker =
          new BDDPartitionAlgoWorkerEve(eventPartitions,
                                        eventCoordinator,
                                        imageOperator);
      } else {
        logger.info("DFAs: automaton-based paritioning.");
        final BDDPartitionSet automatonPartitions =
          new BDDPartitionSetAut(bddExAutomata);
        final BDDPartitionCoordinator automatonCoordinator =
          new BDDPartitionCoordinatorAut(automatonPartitions);
        parAlgoWorker =
          new BDDPartitionAlgoWorkerAut(automatonPartitions,
                                        automatonCoordinator,
                                        imageOperator);
      }
    } else if (synType == SynthesisAlgorithm.CLOCKPARTITION) {
      logger.info("TEFAs: the clock-based partitioning algorithm is chosen.");
      throw new UnsupportedOperationException();
    } else if (synType == SynthesisAlgorithm.MINIMALITY_P) {
      logger
        .info("RAS models: the event-based partitioning algorithm is choosen.");
      final BDDPartitionSet eventPartitions =
        new BDDPartitionSetEve(bddExAutomata);
      final BDDPartitionCoordinator eventCoordinator =
        new BDDPartitionCoordinatorEve(eventPartitions);
      parAlgoWorker =
        new BDDPartitionAlgoWorkerRes(eventPartitions,
                                      eventCoordinator,
                                      imageOperator);
    }
    return parAlgoWorker;
  }
}
