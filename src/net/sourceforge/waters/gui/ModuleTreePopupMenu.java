//# -*- indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ModuleTreePopupMenu
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.event.*;
import javax.swing.*;

import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.VariableSubject;

import org.supremica.util.VPopupMenu;


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
    
    public ModuleTreePopupMenu(final ModuleTree tree,
			       final ActionListener listener,
			       final AbstractSubject c)
    {
        mInvokingComponent = c;
        mTree = tree;
        init(listener);
    }
    
    /**
     * Initialize the menu.
     */
    private void init(final ActionListener listener)
    {
        JMenuItem item;
        
        // *** BUG ***
        // These "actions" are executed by the "EditorComponentsPanel",
        // but they should be implemented as WatersAction subclasses!
        // ***
        if(mInvokingComponent instanceof SimpleComponentSubject)
        {
            item = new JMenuItem("Add variable");
            item.setActionCommand("add variable");
            item.addActionListener(listener);
            this.add(item);
            addVariableItem = item;
            
            item = new JMenuItem("Delete component");
            item.setActionCommand("delete component");
            item.addActionListener(listener);
            this.add(item);
            deleteComponentItem = item;

            item = new JMenuItem("Copy component");
            item.setActionCommand("copy component");
            item.addActionListener(listener);
            this.add(item);
            copyComponentItem = item;
            
            item = new JMenuItem("Rename component");
            item.setActionCommand("rename component");
            item.addActionListener(listener);
            this.add(item);
            renameComponentItem = item;

            JMenu typeMenu = new JMenu("Set type");
            this.add(typeMenu);
            
            item = new JMenuItem("Plant");
            item.setActionCommand("toPlantType");
            item.addActionListener(listener);
            typeMenu.add(item);
            toPlantTypeItem = item;
            
            item = new JMenuItem("Specification");
            item.setActionCommand("toSpecificationType");
            item.addActionListener(listener);
            typeMenu.add(item);
            toSpecificationTypeItem = item;
            
            item = new JMenuItem("Supervisor");
            item.setActionCommand("toSupervisorType");
            item.addActionListener(listener);
            typeMenu.add(item);
            toSupervisorTypeItem = item;
        }
        else if(mInvokingComponent instanceof VariableSubject)
        {
            item = new JMenuItem("Delete variable");
            item.setActionCommand("delete variable");
            item.addActionListener(listener);
            this.add(item);
            deleteVariableItem = item;
            
            item = new JMenuItem("Edit variable");
            item.setActionCommand("edit variable");
            item.addActionListener((ActionListener)listener);
            this.add(item);
            editVariableItem = item;
        }
        else if(mInvokingComponent instanceof ModuleSubject)
        {
            item = new JMenuItem("Add component");
            item.setActionCommand("add simple component");
            item.addActionListener(listener);
            this.add(item);
            addComponentItem = item;

            item = new JMenuItem("Show comment");
            item.setActionCommand("show comment");
            item.addActionListener(listener);
            this.add(item);
            showCommentItem = item;
        }
    }
}
