package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;

import net.sourceforge.waters.gui.util.IconLoader;

import org.supremica.gui.ide.AboutDialog;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

public class HelpAboutAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.createLogger(HelpAboutAction.class);

    public HelpAboutAction(final List<IDEAction> actionList)
    {
        super(actionList);
        putValue(Action.NAME, "About Supremica...");
        putValue(Action.SHORT_DESCRIPTION, "About Supremica");
        putValue(Action.SMALL_ICON, IconLoader.ICON_TOOL_ABOUT);
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        doAction();
    }

    @Override
    public void doAction()
    {
        final AboutDialog aboutDialog = new AboutDialog(ide.getFrame());
        aboutDialog.setVisible(true);
    }
}

