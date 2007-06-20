package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.SynthesizerDialog;
import org.supremica.gui.AutomataSynthesisWorker;
import java.util.List;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.log.*;

public class AnalyzerSynthesizerAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    private Logger logger = LoggerFactory.createLogger(IDE.class);
    
    public AnalyzerSynthesizerAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setAnalyzerActiveRequired(true);
        
        putValue(Action.NAME, "Synthesize...");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_Y));
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));        
        putValue(Action.SHORT_DESCRIPTION, "Synthesize a supervisor for the selected automata");
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/synthesize16.gif")));        
    }
    
    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }
    
    
    public void doAction()
    {        
        // Retrieve the selected automata and make a sanity check
        Automata selectedAutomata = ide.getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();
        if (!selectedAutomata.sanityCheck(ide.getIDE(), 1, true, true, true, true))
        {
            return;
        }
        
        // Get the current options and allow the user to change them...
        SynthesizerOptions options = new SynthesizerOptions();
        SynthesizerDialog synthesizerDialog = new SynthesizerDialog(ide.getFrame(), selectedAutomata.size(), options);
        synthesizerDialog.show();
        if (!options.getDialogOK())
        {
            return;
        }
        
        AutomataSynthesisWorker worker = new AutomataSynthesisWorker(ide.getIDE(), selectedAutomata, options);       
    }
}
