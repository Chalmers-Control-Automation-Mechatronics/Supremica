package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.SynchronizationDialog;
import org.supremica.gui.AutomataSynchronizerWorker;
import java.util.List;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.log.*;
import javax.swing.JOptionPane;

public class AnalyzerSynchronizerAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    private Logger logger = LoggerFactory.createLogger(IDE.class);
    
    public AnalyzerSynchronizerAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setAnalyzerActiveRequired(true);
        setMinimumNumberOfSelectedComponents(2);
        
        putValue(Action.NAME, "Synchronize...");
        putValue(Action.SHORT_DESCRIPTION, "Synchronize the selected automata");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/synchronize16.gif")));
    }
    
    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }
    
    public void doAction()
    {
        // Retrieve the selected automata and make a sanity check
        Automata selectedAutomata = ide.getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();
        if (!selectedAutomata.sanityCheck(ide.getIDE(), 2, true, false, true, true))
        {
            return;
        }
        
        // Get the current options
        SynchronizationOptions synchronizationOptions;
        
        try
        {
            synchronizationOptions = new SynchronizationOptions();
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(ide.getFrame(), "Error constructing synchronizationOptions: " + ex.getMessage(), "Alert", JOptionPane.ERROR_MESSAGE);
            logger.debug(ex.getStackTrace());
            
            return;
        }
        
        // Start a dialog to allow the user changing the options
        SynchronizationDialog synchronizationDialog = new SynchronizationDialog(ide.getFrame(), synchronizationOptions);
        
        synchronizationDialog.show();
        
        if (!synchronizationOptions.getDialogOK())
        {
            return;
        }
        
        // Start worker thread - perform the task.
        new AutomataSynchronizerWorker(ide.getIDE(), selectedAutomata, "", synchronizationOptions);
    }
}
