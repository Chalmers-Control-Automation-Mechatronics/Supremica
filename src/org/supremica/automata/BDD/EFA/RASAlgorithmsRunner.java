package org.supremica.automata.BDD.EFA;

import java.io.File;
import net.sf.javabdd.BDD;
import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.FlowerEFABuilder;
import org.supremica.automata.algorithms.EditorSynthesizerOptions;
import org.supremica.automata.algorithms.SynthesisAlgorithm;
import org.supremica.automata.algorithms.SynthesisType;
import org.supremica.properties.Config;
import org.supremica.util.ActionTimer;

/**
 * Automata the construction of EFAs from a RAS, and the computation of the unsafe states
 * @author zhennanfei
 */
public class RASAlgorithmsRunner {
    
    public static void main (String[] args) {
        
        File ras = new File(args[0]); 
      
        EditorSynthesizerOptions options = new EditorSynthesizerOptions();
        options.setSynthesisType(SynthesisType.UNSAFETY);
        options.setSynthesisAlgorithm(SynthesisAlgorithm.valueOf(args[1]));
        
        FlowerEFABuilder fbuilder = new FlowerEFABuilder(ras, null);
        ExtendedAutomata exAutomata = fbuilder.getEFAforRAS();
        
        Config.BDD2_INITIALNODETABLESIZE.set(args[2]);
        Config.BDD2_CACHESIZE.set(args[3]);
        Config.BDD2_CACHERATIO.set(args[4]);
        Config.BDD2_MAXINCREASENODES.set(args[5]);
     
        
        BDDExtendedSynthesizer synthesizer =
                new BDDExtendedSynthesizer(exAutomata, options);
        
        BDDExtendedManager manager = synthesizer.bddAutomata.getManager();

        System.out.println("RAS instance name: " + ras.getName());
        
        ActionTimer timer = new ActionTimer();
        timer.start();
        
        try {
            
            BDD boundaryUnsafeStates = null;
            
            if (options.getSynthesisAlgorithm() == SynthesisAlgorithm.MINIMALITY_C)
                boundaryUnsafeStates = manager.computeBoundaryUnsafeStatesClassic();
            else if (options.getSynthesisAlgorithm() == SynthesisAlgorithm.MINIMALITY_M)
                boundaryUnsafeStates = manager.computeBoundaryUnsafeStatesAlternative();
            
            timer.stop();
            System.out.println("Time for computing the boundary unsafe states is: "
                    + timer.elapsedTime() / 1000);
            
            try {
                timer.restart();
                manager.removeLargerStates(boundaryUnsafeStates);
                timer.stop();
                System.out.println("Time for minimizing the boundary unsafe states is: "
                        + timer.elapsedTime() / 1000);
                System.out.println();
            } catch (OutOfMemoryError e2) {
                timer.stop();
                System.out.println("OutOfMemoryError when minimizing the boundary unsafe states");
                System.out.println();
            }
            
        } catch (OutOfMemoryError e1) {
            timer.stop();
            System.out.println("OutOfMemoryError when computing the boundary unsafe states");
            System.out.println();
        }     
    }
    
}
