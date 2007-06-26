

package org.supremica.gui.ide;

import java.awt.Frame;
import javax.swing.*;
import org.supremica.util.VPopupMenu;
import org.supremica.log.*;

import org.supremica.gui.ide.actions.IDEActionInterface;
import org.supremica.properties.Config;

class AnalyzerPopupMenu
    extends VPopupMenu
{
    private static Logger logger = LoggerFactory.createLogger(AnalyzerPopupMenu.class);
    
    private static final long serialVersionUID = 1L;
    private IDEActionInterface ide;
    
    public AnalyzerPopupMenu(Frame parent, IDEActionInterface ide)
    {
        setInvoker(parent);
        this.ide = ide;
        
        try
        {
            initPopups();
        }
        catch (Exception ex)
        {
            logger.error(ex);
        }
    }
    
    private void initPopups()
    throws Exception
    {
        JMenu viewMenu = new JMenu("View");
        viewMenu.setToolTipText("Different graphical representations of the selected automata");
        viewMenu.setIcon(new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Zoom16.gif")));
        add(viewMenu);
        viewMenu.add(ide.getActions().analyzerViewAutomatonAction.getMenuItem());
        viewMenu.add(ide.getActions().analyzerViewAlphabetAction.getMenuItem());
        viewMenu.add(ide.getActions().analyzerViewStatesAction.getMenuItem());
        viewMenu.add(ide.getActions().analyzerViewModularStructureAction.getMenuItem());
        addSeparator();
        add(ide.getActions().analyzerSynchronizerAction.getMenuItem());
        add(ide.getActions().analyzerSynthesizerAction.getMenuItem());
        add(ide.getActions().analyzerVerifierAction.getMenuItem());
        add(ide.getActions().analyzerMinimizeAction.getMenuItem());
        addSeparator();
        add(ide.getActions().analyzerPlantifyAction.getMenuItem());
        add(ide.getActions().analyzerEventHiderAction.getMenuItem());
        add(ide.getActions().analyzerPurgeAction.getMenuItem());
        addSeparator();
        add(ide.getActions().analyzerExploreStatesAction.getMenuItem());
        add(ide.getActions().analyzerFindStatesAction.getMenuItem());
        add(ide.getActions().analyzerWorkbenchAction.getMenuItem());
        addSeparator();
        add(ide.getActions().analyzerStatisticsAction.getMenuItem());
        add(ide.getActions().analyzerExportAction.getMenuItem());
        addSeparator();
        //add(ide.getActions().analyzerDeleteSelectedAction.getMenuItem());
        //add(ide.getActions().analyzerDeleteAllAction.getMenuItem());
        add(ide.getActions().analyzerRenameAction.getMenuItem());
        add(ide.getActions().analyzerSendToEditorAction.getMenuItem());

        if (Config.INCLUDE_EXPERIMENTAL_ALGORITHMS.get())
        {
            JMenu experimentMenu = new JMenu("Experimental");
            experimentMenu.setToolTipText("Experimental functions (under development)");
            experimentMenu.setIcon(new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/development/Jar16.gif")));
            add(experimentMenu);

            experimentMenu.add(ide.getActions().analyzerPredictSizeAction.getMenuItem());
            experimentMenu.add(ide.getActions().analyzerCountReachableAction.getMenuItem());
            experimentMenu.add(ide.getActions().analyzerScheduleAction.getMenuItem());

            experimentMenu.addSeparator();
            
            experimentMenu.add(ide.getActions().simulatorLaunchAnimatorAction.getMenuItem());
            experimentMenu.add(ide.getActions().simulatorLaunchSimulatorAction.getMenuItem());

            experimentMenu.addSeparator();
            
            // To try out new code, use this action...
            experimentMenu.add(ide.getActions().analyzerExperimentAction.getMenuItem());
        }
    }
}

