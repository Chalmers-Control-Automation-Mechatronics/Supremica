// How to synthesize using the BDD synthesizer
// The options are set in an instance of org\supremica\automata\algorithms\EditorSynthesizerOptions.java
// The options seem to be remembered, so need only be set up once, can be done manually
// Code below is extracted from src\org\supremica\gui\ide\actions\EditorSynthesizerAction.java
// This code can be run by the Editor > Run script...
// Use CreateOpenModule.java as template
package Lupremica;

import java.util.Vector;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.actions.Actions;
import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.BDD.EFA.BDDExtendedSynthesizer;
import org.supremica.gui.ide.actions.EditorSynthesizerAction;
import org.supremica.automata.algorithms.EditorSynthesizerOptions;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.model.base.EventKind;
import org.supremica.automata.algorithms.SynthesisType;
import org.supremica.automata.algorithms.SynthesisAlgorithm;
import org.apache.logging.log4j.Logger;

public class BDDSynthesisScript
{
    private final IDE ide;

    public BDDSynthesisScript(final IDE ide)
    {
        this.ide = ide;

		final Logger logger = ide.getTheLog();

		// Look up the editorSynthesizerAction
		final Actions actions = ide.getActions();
		EditorSynthesizerAction editorSynthesizerAction =
				(EditorSynthesizerAction)actions.editorSynthesizerAction;

        final ModuleSubject module =
              ide.getActiveDocumentContainer().getEditorPanel().getModuleSubject();

    	// get the stored or default options
    	final EditorSynthesizerOptions options = new EditorSynthesizerOptions();
		options.setSaveInFile(true);
		options.setPrintGuard(true);
		options.setSynthesisType(SynthesisType.NONBLOCKING_CONTROLLABLE);
		options.setSynthesisAlgorithm(SynthesisAlgorithm.PARTITIONBDD);

        // collect controllable event names from the module
        final Vector<String> controllableEventNames = new Vector<String>();
        for (final EventDeclSubject sigmaS : module.getEventDeclListModifiable())
        {
            if (sigmaS.getKind() == EventKind.CONTROLLABLE)
            {
                controllableEventNames.add(sigmaS.getName());
            }
        }

        final ExtendedAutomata exAutomata = new ExtendedAutomata(module);

        BDDExtendedSynthesizer bddSynthesizer = null;
        RuntimeException synthesisException = null;

        try
        {
            bddSynthesizer = new BDDExtendedSynthesizer(exAutomata, options);
            bddSynthesizer.synthesize(options);
            bddSynthesizer.generateGuard(controllableEventNames, options);
            final java.io.File saveFile = new java.io.File("R:/BDDsynthOutput.txt");
            editorSynthesizerAction.saveOrPrintGuards(bddSynthesizer, controllableEventNames,
                                    options.getSaveInFile(), options.getPrintGuard(),
                                    saveFile);

            if (options.getAddGuards())
                bddSynthesizer.addGuardsToAutomata();

            // Cleanup...
            bddSynthesizer.done();
            bddSynthesizer = null;
        }
        catch (final RuntimeException e)
        {
            synthesisException = e;
        }
        catch(final OutOfMemoryError e)
        {
			synthesisException = new RuntimeException(e.getCause());
			logger.error("BDDSynthesisScript out of memory during BDD synthesis "
			          + " (" + Runtime.getRuntime().maxMemory()/(1024*1024) + " Mib)");
      		e.printStackTrace();
		}
		finally
        {
            if (bddSynthesizer != null)
                bddSynthesizer.done();

            if (synthesisException != null)
                throw synthesisException;
        }
    }
}