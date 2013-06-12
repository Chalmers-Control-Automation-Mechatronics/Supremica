package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;

import net.sourceforge.waters.gui.util.IconLoader;

import org.supremica.automata.Automata;
import org.supremica.automata.algorithms.Plantifier;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;


/**
 * The plantification action to convert specifications into plants.
 *
 * @author Martin Fabian
 */

public class AnalyzerPlantifyAction
    extends IDEAction
{
    private final Logger logger = LoggerFactory.createLogger(AnalyzerPlantifyAction.class);
	private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public AnalyzerPlantifyAction(final List<IDEAction> actionList)
    {
        super(actionList);
        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);
        putValue(Action.NAME, "Plantify");
        putValue(Action.SHORT_DESCRIPTION, "Turns specifications and supervisors into plants");
        putValue(Action.SMALL_ICON, IconLoader.ICON_PLANT);
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        doAction();
    }

    /**
     * The code that is run when the action is invoked.
     */
    @Override
    public void doAction()
    {
        final Automata selectedAutomata = ide.getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();
        // MinimizationHelper.plantify(selectedAutomata);
		final Plantifier p = new Plantifier(selectedAutomata);
		p.plantify();
		final Automata a = p.getPlantifiedPlants();

		try
		{
			ide.getActiveDocumentContainer().getAnalyzerPanel().addAutomata(a, true);
			// ide.getIDE().repaint();
		}
		catch (final Exception ex)
		{
			logger.error(ex);
		}
    }
}
