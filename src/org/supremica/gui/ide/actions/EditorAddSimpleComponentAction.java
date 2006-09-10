package org.supremica.gui.ide.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;

public class EditorAddSimpleComponentAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    
    public EditorAddSimpleComponentAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setEditorActiveRequired(true);
        
        putValue(Action.NAME, "New Automaton...");
        putValue(Action.SHORT_DESCRIPTION, "Add new simple component to the project");
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/automaton16.gif")));
    }
    
    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }
    
    public void doAction()
    {
        ModuleContainer activeModule = ide.getActiveModuleContainer();
        activeModule.getEditorPanel().getEditorPanelInterface().addComponent();
    }
}
