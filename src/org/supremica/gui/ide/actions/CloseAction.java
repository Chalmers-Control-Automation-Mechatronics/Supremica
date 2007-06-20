//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica/Waters IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   CloseAction
//###########################################################################
//# $Id: CloseAction.java,v 1.9 2007-06-20 19:43:38 flordal Exp $
//###########################################################################

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.List;
import javax.swing.Action;
import net.sourceforge.waters.model.base.DocumentProxy;
import org.supremica.gui.ide.DocumentContainer;


public class CloseAction
    extends IDEAction
{

    private static final long serialVersionUID = 1L;
    
    public CloseAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        putValue(Action.NAME, "Close");
        putValue(Action.SHORT_DESCRIPTION, "Close module");
    }
    
    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }
    
    public void doAction()
    {
        final DocumentContainer container = ide.getActiveDocumentContainer();
		final DocumentProxy module = container.getDocument();
		final URI uri = module.getLocation();
        ide.remove(container);
        ide.getIDE().getDocumentManager().remove(uri);
    }
}
