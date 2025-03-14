//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
package org.supremica.automata.BDD.EFA;

import java.io.File;

import net.sf.javabdd.BDD;

import net.sourceforge.waters.analysis.bdd.BDDPackage;
import net.sourceforge.waters.model.expr.ParseException;

import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.FlowerEFABuilder;
import org.supremica.automata.algorithms.EditorSynthesizerOptions;
import org.supremica.automata.algorithms.SynthesisAlgorithm;
import org.supremica.automata.algorithms.SynthesisType;
import org.supremica.properties.Config;
import org.supremica.util.ActionTimer;


/**
 * Automata the construction of EFAs from a RAS, and the computation of the
 * unsafe states
 *
 * @author zhennan
 */

public class RASAlgorithmsRunner
{

  public static void main(final String[] args)
  {

    final File ras = new File(args[0]);

    final EditorSynthesizerOptions options = new EditorSynthesizerOptions();
    options.setSynthesisType(SynthesisType.UNSAFETY);
    options.setSynthesisAlgorithm(SynthesisAlgorithm.valueOf(args[1]));

    final FlowerEFABuilder fbuilder = new FlowerEFABuilder(ras, null);
    final ExtendedAutomata exAutomata = fbuilder.getEFAforRAS();

    Config.BDD2_BDD_LIBRARY.setValue(BDDPackage.BUDDY);
    try {
      Config.BDD2_INITIAL_NODE_TABLE_SIZE.set(args[2]);
      Config.BDD2_CACHE_SIZE.set(args[3]);
      Config.BDD2_CACHE_RATIO.set(args[4]);
    } catch (final ParseException exception) {
      System.err.println(exception.getMessage());
      System.exit(1);
    }

    final BDDExtendedSynthesizer synthesizer =
      new BDDExtendedSynthesizer(exAutomata, options);

    final BDDExtendedManager manager = synthesizer.bddAutomata.getManager();

    System.out.println("RAS instance name: " + ras.getName());

    final ActionTimer timer = new ActionTimer();
    timer.start();

    try {

      BDD boundaryUnsafeStates = null;

      if (options.getSynthesisAlgorithm() == SynthesisAlgorithm.MINIMALITY_C)
        boundaryUnsafeStates = manager.computeBoundaryUnsafeStatesClassic();
      else if (options
        .getSynthesisAlgorithm() == SynthesisAlgorithm.MINIMALITY_M)
        boundaryUnsafeStates =
          manager.computeBoundaryUnsafeStatesAlternative();
      else if (options
        .getSynthesisAlgorithm() == SynthesisAlgorithm.MINIMALITY_P)
        boundaryUnsafeStates =
          manager.computeBoundaryUnsafeStatesEventPartitioning();

      timer.stop();
      System.out.println("Time for computing the boundary unsafe states is: "
                         + timer.elapsedTime() / 1000);

      try {
        timer.restart();
        manager.removeLargerStates(boundaryUnsafeStates);
        timer.stop();
        System.out.println(
                           "Time for minimizing the boundary unsafe states is: "
                           + timer.elapsedTime() / 1000);
        System.out.println();
      } catch (final OutOfMemoryError e2) {
        timer.stop();
        System.out
          .println("OutOfMemoryError when minimizing the boundary unsafe states");
        System.out.println();
      }

    } catch (final OutOfMemoryError e1) {
      timer.stop();
      System.out
        .println("OutOfMemoryError when computing the boundary unsafe states");
      System.out.println();
    }
  }

}
