package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterJob;
import javax.print.StreamPrintServiceFactory;
import javax.swing.ImageIcon;
import org.supremica.gui.ide.IDE;

/**
 * Action for exporting a graph to postscipt.
 */
public class EditorSavePostscriptAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor.
     */
    public EditorSavePostscriptAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(false);
        
        putValue(Action.NAME, "Save as Postscript");
        putValue(Action.SHORT_DESCRIPTION, "Save currently viewed automaton as Postscript");
        //putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Print16.gif")));
                
        setEnabled(false);
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
        try
        {
            ide.getActiveModuleContainer().getEditorPanel().getActiveEditorWindowInterface().exportPostscript();
        }
        catch (NullPointerException ex)
        {
            // This action should only be enabled when theres an editor panel open!
            ide.getIDE().info("Must have an editor panel open.");
        }
    }  

    /**
     * Should be enabled only when a postscript service can be found AND 
     * when a component is being edited (checked elsewhere).
     */
    public boolean isEnabled()
    {
        // If there are no other objections, just make sure there is a postscript service!
        if (super.isEnabled())
        {
            // Look for print service
            String psMimeType = "application/postscript";
            StreamPrintServiceFactory[] factories =
                PrinterJob.lookupStreamPrintServices(psMimeType);
            // Found one?
            boolean serviceFound = (factories.length > 0);
            if (!serviceFound)
            {
                putValue(Action.SHORT_DESCRIPTION, "No Postscript print service was found on the system.");
            }
            return serviceFound;
        }
        return false;
    }    
}
