

package org.supremica.gui.ide;

import java.awt.*;
import javax.swing.*;
import org.supremica.gui.MenuHandler;
import org.supremica.util.VPopupMenu;
import org.supremica.log.*;

import org.supremica.gui.ide.actions.IDEActionInterface;

class AnalyzerPopupMenu
    extends VPopupMenu
{
    private static Logger logger = LoggerFactory.createLogger(AnalyzerPopupMenu.class);

    private static final long serialVersionUID = 1L;
    private IDEActionInterface ide;

    public AnalyzerPopupMenu(JFrame parent, IDEActionInterface ide)
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
        add(viewMenu);
        viewMenu.add(ide.getActions().analyzerViewAutomatonAction.getMenuItem());
        viewMenu.add(ide.getActions().analyzerViewAlphabetAction.getMenuItem());
        viewMenu.add(ide.getActions().analyzerViewStatesAction.getMenuItem());
        viewMenu.add(ide.getActions().analyzerViewModularStructureAction.getMenuItem());
        addSeparator();
        add(ide.getActions().analyzerWorkbenchAction.getMenuItem());
        addSeparator();
        add(ide.getActions().analyzerSynchronizerAction.getMenuItem());
        add(ide.getActions().analyzerSynthesizerAction.getMenuItem());
        add(ide.getActions().analyzerVerifierAction.getMenuItem());
        addSeparator();
        add(ide.getActions().analyzerExploreStatesAction.getMenuItem());
        add(ide.getActions().analyzerFindStatesAction.getMenuItem());
  		addSeparator();
        add(ide.getActions().analyzerStatisticsAction.getMenuItem());
        addSeparator();
        add(ide.getActions().analyzerDeleteSelectedAction.getMenuItem());
        add(ide.getActions().analyzerDeleteAllAction.getMenuItem());
    }
}

