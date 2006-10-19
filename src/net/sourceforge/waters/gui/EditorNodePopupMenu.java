//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorNodePopupMenu
//###########################################################################
//# $Id: EditorNodePopupMenu.java,v 1.21 2006-10-19 12:18:05 flordal Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.event.*;
import java.awt.geom.Point2D;
import javax.swing.*;

import org.supremica.util.VPopupMenu;

import net.sourceforge.waters.gui.command.AddEventCommand;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.CreateEdgeCommand;
import net.sourceforge.waters.gui.command.DeleteNodeCommand;
import net.sourceforge.waters.gui.command.RecallLabelCommand;
import net.sourceforge.waters.gui.command.RemoveEventCommand;
import net.sourceforge.waters.gui.command.SetNodeInitialCommand;
import net.sourceforge.waters.gui.command.ToggleNodeInitialCommand;
import net.sourceforge.waters.gui.renderer.LabelProxyShape;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.xsd.base.EventKind;
import org.supremica.gui.ide.IDE;


/**
 * Popup for editing attributes of a node.
 */
class EditorNodePopupMenu
    extends VPopupMenu
    implements ActionListener
{
    private SimpleNodeSubject node;
    private ControlledSurface parent;

    private JMenuItem deleteItem;
    private JMenuItem initialItem;
    private JMenuItem recallItem;
    private JMenuItem markItem;
    private JMenuItem forbidItem;
    private JMenuItem clearItem;
    private JMenuItem createSelfLoop;

    private static final ImageIcon markedStateIcon = new ImageIcon(IDE.class.getResource("/icons/MarkedState16.gif"));
    private static final ImageIcon initialStateIcon = new ImageIcon(IDE.class.getResource("/icons/InitialState16.gif"));
    private static final ImageIcon forbiddenStateIcon = new ImageIcon(IDE.class.getResource("/icons/ForbiddenState16.gif"));
    private static final ImageIcon stateIcon = new ImageIcon(IDE.class.getResource("/icons/State16.gif"));

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
            item.setIcon(initialStateIcon);
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
            item.setIcon(initialStateIcon);
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
        item.setIcon(markedStateIcon);
        this.add(item);
        // Disable if there are no propositions
        if (!node.getPropositions().getEventList().isEmpty())
        {
            item.setEnabled(false);
            item.setToolTipText("State is marked already");
        }
        markItem = item;

        item = new JMenuItem("Forbid state");
        item.addActionListener(this);
        item.setIcon(forbiddenStateIcon);
        this.add(item);
        // Disable if there are no propositions
        if (!node.getPropositions().getEventList().isEmpty())
        {
            item.setEnabled(false);
            item.setToolTipText("State is marked already");
        }
        forbidItem = item;

        item = new JMenuItem("Clear propositions");
        item.addActionListener(this);
        item.setIcon(stateIcon);
        this.add(item);
        // Disable if there are no propositions
        if (node.getPropositions().getEventList().isEmpty())
        {
            item.setEnabled(false);
            item.setToolTipText("State has no propositions");
        }
        clearItem = item;

        item = new JMenuItem("Create self loop");
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
            parent.getEditorInterface().getUndoInterface()
                  .executeCommand(new RecallLabelCommand(node.getLabelGeometry()));
        }

        if (e.getSource() == markItem)
        {
            EventDeclSubject d = new EventDeclSubject(EventDeclProxy.DEFAULT_MARKING_NAME,
                EventKind.PROPOSITION);
            if (!parent.getModule().getEventDeclListModifiable()
            .containsName(EventDeclProxy.DEFAULT_MARKING_NAME))
            {
                parent.getModule().getEventDeclListModifiable().add(d);
            }
            Command c = new AddEventCommand
                (node.getPropositions(),
                new SimpleIdentifierSubject(EventDeclProxy.DEFAULT_MARKING_NAME),
                0);
            parent.getEditorInterface().getUndoInterface().executeCommand(c);
        }

        if (e.getSource() == forbidItem)
        {
            // First remove all other propositions
            while (node.getPropositions().getEventList().size() > 0)
            {
                AbstractSubject proposition = node.getPropositions().getEventListModifiable().get(0);
                Command c = new RemoveEventCommand
                    (node.getPropositions(),
                    proposition);
                parent.getEditorInterface().getUndoInterface().executeCommand(c);
            }

            EventDeclSubject d = new EventDeclSubject(EventDeclProxy.DEFAULT_FORBIDDEN_NAME,
                EventKind.PROPOSITION);
            if (!parent.getModule().getEventDeclListModifiable()
            .containsName(EventDeclProxy.DEFAULT_FORBIDDEN_NAME))
            {
                parent.getModule().getEventDeclListModifiable().add(d);
            }
            Command c = new AddEventCommand
                (node.getPropositions(),
                new SimpleIdentifierSubject(EventDeclProxy.DEFAULT_FORBIDDEN_NAME),
                0);
            parent.getEditorInterface().getUndoInterface().executeCommand(c);
        }

        if (e.getSource() == clearItem)
        {
            // Should not be a loop... one undo should do it all!
            while (node.getPropositions().getEventList().size() > 0)
            {
                AbstractSubject proposition = node.getPropositions().getEventListModifiable().get(0);
                Command c = new RemoveEventCommand
                    (node.getPropositions(),
                    proposition);
                parent.getEditorInterface().getUndoInterface().executeCommand(c);
            }
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
