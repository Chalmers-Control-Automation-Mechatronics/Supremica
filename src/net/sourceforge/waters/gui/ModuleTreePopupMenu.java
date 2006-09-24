package net.sourceforge.waters.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.supremica.util.VPopupMenu;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.VariableSubject;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.DeleteEdgeCommand;
import net.sourceforge.waters.gui.command.FlipEdgeCommand;

/**
 * Popup for editing attributes of a node.
 */
class ModuleTreePopupMenu
    extends VPopupMenu
{
    private JMenuItem addVariableItem;
    private JMenuItem deleteVariableItem;
    private JMenuItem deleteComponentItem;
    private JMenuItem editVariableItem;
    private JMenuItem addComponentItem;
    private AbstractSubject mInvokingComponent;
    private ModuleTree mTree;
    private ModuleWindowInterface mWindow;

    public ModuleTreePopupMenu(ModuleTree tree, ModuleWindowInterface moduleWindow, AbstractSubject c)
    {
        mInvokingComponent = c;
        mTree = tree;
        mWindow = moduleWindow;
        init();
    }

    /**
     * Initialize the menu.
     */
    private void init()
    {
        JMenuItem item;

        if(mInvokingComponent instanceof SimpleComponentSubject)
        {
            item = new JMenuItem("Add variable");
            item.setActionCommand("add variable");
            item.addActionListener(mWindow);
            this.add(item);
            addVariableItem = item;

             item = new JMenuItem("Delete component");
             item.setActionCommand("delete component");
             item.addActionListener(mWindow);
             this.add(item);
            deleteComponentItem = item;
 		}

        if(mInvokingComponent instanceof VariableSubject)
        {
            item = new JMenuItem("Delete variable");
            item.setActionCommand("delete variable");
            item.addActionListener(mWindow);
            this.add(item);
            deleteVariableItem = item;

            item = new JMenuItem("Edit variable");
            item.setActionCommand("edit variable");
            item.addActionListener((ActionListener)mWindow);
            this.add(item);
            editVariableItem = item;
        }

        if(mInvokingComponent instanceof ModuleSubject)
        {
            item = new JMenuItem("Add component");
            item.setActionCommand("add simple component");
            item.addActionListener(mWindow);
            this.add(item);
            addComponentItem = item;
        }
    }
}
