
/*************************** ScheduleAction.java ***************/
package org.supremica.gui.useractions;

import java.awt.event.*;
import javax.swing.*;
import org.supremica.log.*;
import org.supremica.gui.*;

/**
 *      Implements the dialog box allowing to choose different scheduling techniques.
 */
public class ScheduleAction
	extends AbstractAction
{
    private static Logger logger = LoggerFactory.createLogger(ScheduleAction.class);

    public ScheduleAction()
    {
            super("Schedule...", null);

            putValue(SHORT_DESCRIPTION, "Schedule selected automata (experimental)");
    }

    /**
     *      Calls the ScheduleDialog if the number of selected automata is equal to one.
     *      (Otherwise, synchronization is strongly recommended.)
     */
    public void actionPerformed(ActionEvent e)
    {
        launchScheduleDialog();
    }

    private void launchScheduleDialog()
    {
        ScheduleDialog dlg = null;

        try 
        {
            dlg = new ScheduleDialog(ActionMan.getGui());
            dlg.setVisible(true);
        }
        catch (Exception ex) 
        {
            if (ex.getMessage().contains("javax") || ex.getMessage().contains("java.awt"))
            {}
            else
            {
                if (dlg != null)
                {
                    dlg.done();
                }
            }
        }
    }
}
