//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   IDEAction
//###########################################################################
//# $Id: IDEAction.java,v 1.11 2007-06-25 20:18:48 robi Exp $
//###########################################################################

package org.supremica.gui.ide.actions;

import javax.swing.AbstractButton;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import java.util.List;

public abstract class IDEAction
    extends AbstractAction
{

    protected IDEActionInterface ide;
    private int minimumNumberOfSelectedComponents = 0;
    private boolean editorActiveRequired = false;
    private boolean analyzerActiveRequired = false;

    public IDEAction(List<IDEAction> actionList)
    {
        this.ide = null;

        assert(actionList != null);
        actionList.add(this);
    }

    public void setIDEActionInterface(IDEActionInterface ide)
    {
        this.ide = ide;
    }

    public abstract void doAction();

    public JMenuItem getMenuItem()
    {
        return new JMenuItem(this);
    }

    public void setEditorActiveRequired(boolean required)
    {
        editorActiveRequired = required;
    }
    
    public boolean getEditorActiveRequired()
    {
        return editorActiveRequired;
    }

    public void setAnalyzerActiveRequired(boolean required)
    {
        analyzerActiveRequired = required;
    }
    
    public boolean getAnalyzerActiveRequired()
    {
        return analyzerActiveRequired;
    }

    public void setMinimumNumberOfSelectedComponents(int numberOfComponents)
    {
        minimumNumberOfSelectedComponents = numberOfComponents;
    }

    public boolean isEnabled()
    {
		// *** BUG ***
        return super.isEnabled();
    }

}
