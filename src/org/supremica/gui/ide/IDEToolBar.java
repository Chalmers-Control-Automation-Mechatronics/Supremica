//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   IDEToolBar
//###########################################################################
//# $Id$
//###########################################################################


package org.supremica.gui.ide;

import java.awt.Insets;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.JToggleButton;

import net.sourceforge.waters.gui.ControlledToolbar;
import net.sourceforge.waters.gui.actions.IDECopyAction;
import net.sourceforge.waters.gui.actions.IDECutAction;
import net.sourceforge.waters.gui.actions.IDEDeleteAction;
import net.sourceforge.waters.gui.actions.IDEPasteAction;
import net.sourceforge.waters.gui.actions.InsertEventDeclAction;
import net.sourceforge.waters.gui.actions.InsertSimpleComponentAction;
import net.sourceforge.waters.gui.actions.InsertVariableAction;
import net.sourceforge.waters.gui.actions.ToolEdgeAction;
import net.sourceforge.waters.gui.actions.ToolGroupNodeAction;
import net.sourceforge.waters.gui.actions.ToolNodeAction;
import net.sourceforge.waters.gui.actions.ToolSelectAction;
import net.sourceforge.waters.gui.actions.WatersRedoAction;
import net.sourceforge.waters.gui.actions.WatersUndoAction;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.ToolbarChangedEvent;

import org.supremica.gui.ide.actions.Actions;
import org.supremica.gui.ide.actions.NewAction;
import org.supremica.gui.ide.actions.OpenAction;
import org.supremica.gui.ide.actions.SaveAction;


/**
 * <P>The IDE's main toolbar.</P>
 *
 * <P>Presently, there is only one toolbar in the IDE, which contains
 * buttons for file operations, editing, and the graph drawing tools. Some
 * buttons are enabled and disabled as the user switches between panels,
 * but there is no switching of toolbars.</P>
 *
 * <P>This class is a straightforward extension of Swing's {@link
 * JToolBar}, with some additional support for changing and reading the
 * current graph drawing tool.</P>
 *
 * @author Knut &Aring;kesson, Simon Ware, Robi Malik
 */

public class IDEToolBar
    extends JToolBar
    implements ControlledToolbar
{

    //#######################################################################
    //# Constructor
    public IDEToolBar(final IDE ide)
    {
        mIDE = ide;
        mObservers = new LinkedList<Observer>();
        mTool = ControlledToolbar.Tool.SELECT;
        setRollover(true);
        setFloatable(false);

		final Actions actions = ide.getActions();
        addAction(actions.getAction(NewAction.class));
        addAction(actions.getAction(OpenAction.class));
        addAction(actions.getAction(SaveAction.class));
        addAction(actions.editorPrintAction);
        addSeparator();
        addAction(actions.getAction(IDEDeleteAction.class));
        addAction(actions.getAction(IDECutAction.class));
        addAction(actions.getAction(IDECopyAction.class));
        addAction(actions.getAction(IDEPasteAction.class));
        addSeparator();
		addAction(actions.getAction(WatersUndoAction.class));
		addAction(actions.getAction(WatersRedoAction.class));
        addSeparator();
		addAction(actions.getAction(InsertSimpleComponentAction.class));
        //Need better icons first for these two :-(
		//addAction(actions.getAction(InsertVariableAction.class));
		//addAction(actions.getAction(InsertEventDeclAction.class));
        addSeparator();
        //addAction(actions.editorStopEmbedderAction);
        //addSeparator();
        final ButtonGroup group = new ButtonGroup();
		addAction(actions.getAction(ToolSelectAction.class), group, true);
		addAction(actions.getAction(ToolNodeAction.class), group, false);
		addAction(actions.getAction(ToolGroupNodeAction.class), group, false);
		addAction(actions.getAction(ToolEdgeAction.class), group, false);
    }


    //#######################################################################
    //# Accessing the Drawing Tool
    /**
     * Gets the current graph drawing tool.
     */
    public ControlledToolbar.Tool getTool()
    {
        return mTool;
    }

    /**
     * Changes the current graph drawing tool. A {@link ToolbarChangedEvent}
     * will be sent to all registered listeners on this toolbar and the
     * {@link IDE}.
     */
    public void setTool(final ControlledToolbar.Tool tool)
    {
        if (tool != mTool) {
            mTool = tool;
            final EditorChangedEvent event =
                new ToolbarChangedEvent(this, tool);
            fireEditorChangedEvent(event);
        }
    }


    //#######################################################################
    //# Interface net.sourceforge.waters.gui.observer.Subject
    public void attach(final Observer observer)
    {
        mObservers.add(observer);
    }

    public void detach(final Observer observer)
    {
        mObservers.remove(observer);
    }

    public void fireEditorChangedEvent(final EditorChangedEvent event)
    {
        // Just in case they try to register or deregister observers
        // in response to the update ...
        final List<Observer> copy = new LinkedList<Observer>(mObservers);
        for (final Observer observer : copy) {
            observer.update(event);
        }
        mIDE.fireEditorChangedEvent(event);
    }


    //#######################################################################
    //# Creating the Buttons
	private void addAction(final Action action)
	{
		final JButton button = add(action);
        button.setFocusable(false);
		button.setMargin(INSETS);
	}

	private void addAction(final Action action,
                           final ButtonGroup group,
                           final boolean selected)
	{
		final JToggleButton button = new JToggleButton(action);
		button.setText("");
		button.setMargin(INSETS);
        button.setSelected(selected);
        button.setFocusable(false);
		group.add(button);
		add(button);
	}


    //#######################################################################
    //# Data Members
    private final IDE mIDE;
    private final List<Observer> mObservers;
    private ControlledToolbar.Tool mTool;


    //#######################################################################
    //# Class Constants
    private static final Insets INSETS = new Insets(0, 0, 0, 0);

}
