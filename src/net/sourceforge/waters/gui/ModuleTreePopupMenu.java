package net.sourceforge.waters.gui;

import java.awt.event.*;
import javax.swing.*;
import org.supremica.util.VPopupMenu;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.VariableSubject;

/**
 * Popup for editing attributes of a node.
 */
class ModuleTreePopupMenu
    extends VPopupMenu
{
    private JMenuItem addComponentItem;
    private JMenuItem addVariableItem;
    private JMenuItem copyComponentItem;
    private JMenuItem deleteComponentItem;
    private JMenuItem deleteVariableItem;
    private JMenuItem editVariableItem;
    private JMenuItem renameComponentItem;
    private JMenuItem showCommentItem;
    private JMenuItem toPlantTypeItem;
    private JMenuItem toSpecificationTypeItem;
    private JMenuItem toSupervisorTypeItem;    
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
        
        // These "actions" are typically executed by the "EditorComponentsPanel"
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

            item = new JMenuItem("Copy component");
            item.setActionCommand("copy component");
            item.addActionListener(mWindow);
            this.add(item);
            copyComponentItem = item;
            
            item = new JMenuItem("Rename component");
            item.setActionCommand("rename component");
            item.addActionListener(mWindow);
            this.add(item);
            renameComponentItem = item;

            JMenu typeMenu = new JMenu("Set type");
            this.add(typeMenu);
            
            item = new JMenuItem("Plant");
            item.setActionCommand("toPlantType");
            item.addActionListener(mWindow);
            typeMenu.add(item);
            toPlantTypeItem = item;
            
            item = new JMenuItem("Specification");
            item.setActionCommand("toSpecificationType");
            item.addActionListener(mWindow);
            typeMenu.add(item);
            toSpecificationTypeItem = item;
            
            item = new JMenuItem("Supervisor");
            item.setActionCommand("toSupervisorType");
            item.addActionListener(mWindow);
            typeMenu.add(item);
            toSupervisorTypeItem = item;
        }
        else if(mInvokingComponent instanceof VariableSubject)
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
        else if(mInvokingComponent instanceof ModuleSubject)
        {
            item = new JMenuItem("Add component");
            item.setActionCommand("add simple component");
            item.addActionListener(mWindow);
            this.add(item);
            addComponentItem = item;

            item = new JMenuItem("Show comment");
            item.setActionCommand("show comment");
            item.addActionListener(mWindow);
            this.add(item);
            showCommentItem = item;
        }
    }
}
