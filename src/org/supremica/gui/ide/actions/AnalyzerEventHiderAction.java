package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import org.supremica.automata.Automata;
import org.supremica.gui.EventHider;
import org.supremica.gui.ide.IDE;
import org.supremica.log.*;


/**
 * A new action
 */
public class AnalyzerEventHiderAction
    extends IDEAction
{
    @SuppressWarnings("unused")
	private Logger logger = LoggerFactory.createLogger(IDE.class);
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor.
     */
    public AnalyzerEventHiderAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);
        
        putValue(Action.NAME, "Hide events");
        putValue(Action.SHORT_DESCRIPTION, "Hide the identity of events (making them unobservable)");
//        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
//        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/hide16.gif")));
    }
    
    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }
    
    /**
     * The code that is run when the action is invoked.
     */
    public void doAction()
    {
        Automata selectedAutomata = ide.getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();
        
        if (!selectedAutomata.sanityCheck(ide.getIDE(), 1, false, false, true, false))
        {
            return;
        }
        EventHider eventHider = new EventHider(ide.getIDE());
        eventHider.doAction(selectedAutomata, ide.getActiveDocumentContainer().getAnalyzerPanel().getUnselectedAutomata().getUnionAlphabet());
    }
}
