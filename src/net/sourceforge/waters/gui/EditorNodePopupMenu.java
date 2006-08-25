//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorNodePopupMenu
//###########################################################################
//# $Id: EditorNodePopupMenu.java,v 1.14 2006-08-25 02:12:52 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import javax.swing.*;

import org.supremica.util.VPopupMenu;

import net.sourceforge.waters.gui.command.AddEventCommand;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.CreateEdgeCommand;
import net.sourceforge.waters.gui.command.DeleteNodeCommand;
import net.sourceforge.waters.gui.command.SetNodeInitialCommand;
import net.sourceforge.waters.gui.command.ToggleNodeInitialCommand;
import net.sourceforge.waters.gui.renderer.LabelProxyShape;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.IndexedIdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * Popup for editing attributes of a node.
 */
class EditorNodePopupMenu
  extends VPopupMenu
  implements ActionListener
{
  public static final String DEFAULTNAME = "omega";

  private SimpleNodeSubject node;
  private ControlledSurface parent;
    
  private JMenuItem deleteItem;
  private JMenuItem initialItem;
  private JMenuItem recallItem;
  private JMenuItem markItem;
  private JMenuItem clearItem;
  private JMenuItem createSelfLoop;
    
  public EditorNodePopupMenu(ControlledSurface parent, SimpleNodeSubject node)
  {
    this.parent = parent;
    this.node = node;
        
    init();
  }
    
  /**
   * Initialize the menu.
   */
  private void init()
  {
    JMenuItem item;
        
    item = new JMenuItem("Delete node");
    item.addActionListener(this);
    this.add(item);
    deleteItem = item;
    if (parent.getGraph().isDeterministic())
        {
            item = new JMenuItem("Make initial");
            item.addActionListener(this);
            this.add(item);
            // Disable "initial" is node already is initial
            if (node.isInitial())
            {
                item.setEnabled(false);
                item.setToolTipText("State is already initial");
            }
        }
        else
        {
            item = new JMenuItem("Toggle initial");
            item.addActionListener(this);
            this.add(item);
        }
        initialItem = item;
        
        item = new JMenuItem("Recall label");
        item.addActionListener(this);
        this.add(item);
        // Disable "recall" if label is in right position (or maybe instead if it is close enough?)
        if ((node.getLabelGeometry().getOffset().getX() == LabelProxyShape.DEFAULTOFFSETX) &&
            (node.getLabelGeometry().getOffset().getY() == LabelProxyShape.DEFAULTOFFSETY))
        {
            item.setEnabled(false);
            item.setToolTipText("Label is already in default position");
        }
        recallItem = item;
        
        item = new JMenuItem("Mark state");
        item.addActionListener(this);
        this.add(item);
        // Disable if there are no propositions
        if (!node.getPropositions().getEventList().isEmpty())
        {
            item.setEnabled(false);
            item.setToolTipText("State is marked already");
        }
        markItem = item;
        
        item = new JMenuItem("Clear marking");
        item.addActionListener(this);
        this.add(item);
        // Disable if there are no propositions
        if (node.getPropositions().getEventList().isEmpty())
        {
            item.setEnabled(false);
            item.setToolTipText("State has no marking");
        }
        clearItem = item;
        item = new JMenuItem("Create Self Loop");
        item.addActionListener(this);
        this.add(item);
        createSelfLoop = item;
    }
    
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == deleteItem)
        {
            Command deleteNode = new DeleteNodeCommand(parent.getGraph(), node);
            parent.getEditorInterface().getUndoInterface().executeCommand(deleteNode);
        }
        
        if (e.getSource() == initialItem)
        {
            Command initial;
            if (parent.getGraph().isDeterministic())
            {
                initial = new SetNodeInitialCommand(parent.getGraph(), node);
            }
            else
            {
                initial = new ToggleNodeInitialCommand(node);
            }
            parent.getEditorInterface().getUndoInterface().executeCommand(initial);
        }
        
        if (e.getSource() == recallItem)
        {
            System.out.println("Re-Implement Later with Command");
            /*final EditorLabel label = parent.getLabel(node);
              label.setOffset(EditorLabel.DEFAULTOFFSETX,
              EditorLabel.DEFAULTOFFSETY);*/
        }
        
        if (e.getSource() == markItem) {
          EventDeclSubject d = new EventDeclSubject(DEFAULTNAME,
                                                    EventKind.PROPOSITION);
          if (!parent.getModule().getEventDeclListModifiable()
              .containsName(DEFAULTNAME)) {
            parent.getModule().getEventDeclListModifiable().add(d);
          }
          Command c = new AddEventCommand
            (node.getPropositions(),
             new IndexedIdentifierSubject(DEFAULTNAME),
             0);
          parent.getEditorInterface().getUndoInterface().executeCommand(c);
        }
        
        if (e.getSource() == clearItem)
        {
            System.out.println("Re-Implement Later with Command");
            /* node.clearPropositions();*/
        }
        
        if (e.getSource() == createSelfLoop)
        {
            Point2D p = node.getPointGeometry().getPoint();
            Command selfLoop = new CreateEdgeCommand(parent.getGraph(), node, node, p, p);
            parent.getEditorInterface().getUndoInterface().executeCommand(selfLoop);
        }
        
        this.hide();
        parent.repaint();
    }
}
