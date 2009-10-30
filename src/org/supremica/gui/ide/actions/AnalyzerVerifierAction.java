package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.VerificationDialog;
import org.supremica.gui.AutomataVerificationWorker;
import java.util.List;
import javax.swing.KeyStroke;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.automata.algorithms.minimization.*;

import org.supremica.log.*;

public class AnalyzerVerifierAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
	private Logger logger = LoggerFactory.createLogger(IDE.class);
    
    public AnalyzerVerifierAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setAnalyzerActiveRequired(true);
        
        putValue(Action.NAME, "Verify...");
        putValue(Action.SHORT_DESCRIPTION, "Run verification on the selected automata");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_V));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/verify16.gif")));        
    }
    
    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }
    
    public void doAction()
    {
        // Retrieve the selected automata and make a sanity check
        Automata selectedAutomata = ide.getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();
        if (!selectedAutomata.sanityCheck(ide.getFrame(), 1, true, false, true, true))
        {
            return;
        }
        
        // Get the current options and allow the user to change them...
        VerificationOptions vOptions = new VerificationOptions();
        MinimizationOptions mOptions = MinimizationOptions.getDefaultVerificationOptions();
        VerificationDialog verificationDialog = new VerificationDialog(ide.getIDE(), vOptions, mOptions);
        verificationDialog.show();
        if (!vOptions.getDialogOK())
        {
            return;
        }
        if (vOptions.getVerificationType() == VerificationType.LANGUAGEINCLUSION)
        {
            vOptions.setInclusionAutomata(ide.getActiveDocumentContainer().getAnalyzerPanel().getUnselectedAutomata());
        }
        SynchronizationOptions sOptions = SynchronizationOptions.getDefaultVerificationOptions();
        
        // Work!
        new AutomataVerificationWorker(ide.getIDE(), selectedAutomata,
        							   vOptions, sOptions, mOptions);
    }
    
}
