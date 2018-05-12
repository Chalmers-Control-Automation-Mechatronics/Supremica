//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2018 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui.ide;

import java.awt.Component;
import java.awt.Insets;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import net.sourceforge.waters.gui.actions.IDECopyAction;
import net.sourceforge.waters.gui.actions.IDECutAction;
import net.sourceforge.waters.gui.actions.IDEDeleteAction;
import net.sourceforge.waters.gui.actions.IDEPasteAction;
import net.sourceforge.waters.gui.actions.InsertEventDeclAction;
import net.sourceforge.waters.gui.actions.InsertSimpleComponentAction;
import net.sourceforge.waters.gui.actions.InsertVariableAction;
import net.sourceforge.waters.gui.actions.SimulationBackToStartAction;
import net.sourceforge.waters.gui.actions.SimulationJumpToEndAction;
import net.sourceforge.waters.gui.actions.SimulationReplayStepAction;
import net.sourceforge.waters.gui.actions.SimulationResetAction;
import net.sourceforge.waters.gui.actions.SimulationStepAction;
import net.sourceforge.waters.gui.actions.SimulationStepBackAction;
import net.sourceforge.waters.gui.actions.ToolEdgeAction;
import net.sourceforge.waters.gui.actions.ToolGroupNodeAction;
import net.sourceforge.waters.gui.actions.ToolNodeAction;
import net.sourceforge.waters.gui.actions.ToolSelectAction;
import net.sourceforge.waters.gui.actions.WatersRedoAction;
import net.sourceforge.waters.gui.actions.WatersUndoAction;
import net.sourceforge.waters.gui.editor.ZoomSelector;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.Subject;
import net.sourceforge.waters.gui.observer.ToolbarChangedEvent;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;

import org.supremica.gui.ide.actions.Actions;
import org.supremica.gui.ide.actions.NewAction;
import org.supremica.gui.ide.actions.OpenAction;
import org.supremica.gui.ide.actions.SaveAction;


/**
 * <P>
 * The IDE's main toolbar.
 * </P>
 *
 * <P>
 * This class is a straightforward extension of Swing's {@link JToolBar}, with
 * some additional support for changing and reading the current graph drawing
 * tool.
 * </P>
 *
 * <P>
 * The toolbar is updated dynamically when panels are switched, e.g., when the
 * user changes from the editor to the analyser. This is achieved by registering
 * an {@link Observer} on the {@link IDE}.
 * </P>
 *
 * @author Knut &Aring;kesson, Simon Ware, Robi Malik
 */

public class IDEToolBar
  extends JToolBar
  implements Observer, Subject
{

  //#########################################################################
  //# Constructor
  public IDEToolBar(final IDE ide)
  {
    mIDE = ide;
    mObservers = new LinkedList<Observer>();
    mTool = Tool.SELECT;
    setRollover(true);
    setFloatable(false);
    final Actions actions = mIDE.getActions();
    final ButtonGroup group = new ButtonGroup();
    mSelectToolButton =
      createToolButton(actions.getAction(ToolSelectAction.class), group, true);
    mNodeToolButton =
      createToolButton(actions.getAction(ToolNodeAction.class), group, false);
    mGroupNodeToolButton =
      createToolButton(actions.getAction(ToolGroupNodeAction.class),
                       group, false);
    mEdgeToolButton =
      createToolButton(actions.getAction(ToolEdgeAction.class), group, false);
    mZoomSelector = new ZoomSelector(mIDE);
    createButtons();
    mIDE.attach(this);
  }


  //#########################################################################
  //# Accessing the Drawing Tool
  /**
   * Gets the current graph drawing tool.
   */
  public Tool getTool()
  {
    return mTool;
  }

  /**
   * Changes the current graph drawing tool. A {@link ToolbarChangedEvent}
   * will be sent to all registered listeners on this toolbar and the
   * {@link IDE}.
   */
  public void setTool(final Tool tool)
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
  @Override
  public void attach(final Observer observer)
  {
    mObservers.add(observer);
  }

  @Override
  public void detach(final Observer observer)
  {
    mObservers.remove(observer);
  }

  @Override
  public void fireEditorChangedEvent(final EditorChangedEvent event)
  {
    // Just in case they try to register or unregister observers
    // in response to the update ...
    final List<Observer> copy = new LinkedList<Observer>(mObservers);
    for (final Observer observer : copy) {
      observer.update(event);
    }
    mIDE.fireEditorChangedEvent(event);
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  @Override
  public void update(final EditorChangedEvent event)
  {
    switch (event.getKind()) {
    case CONTAINER_SWITCH:
    case MAINPANEL_SWITCH:
    case SUBPANEL_SWITCH:
      removeAll();
      createButtons();
      revalidate();
      repaint();
      break;
    default:
      break;
    }
  }


  //#########################################################################
  //# Creating the Buttons
  private JToggleButton createToolButton(final Action action,
                                         final ButtonGroup group,
                                         final boolean selected)
  {
    final JToggleButton button = new JToggleButton(action);
    button.setText("");
    button.setMargin(INSETS);
    button.setSelected(selected);
    button.setFocusable(false);
    group.add(button);
    return button;
  }

  private void createButtons()
  {
    final Actions actions = mIDE.getActions();
    addAction(actions.getAction(NewAction.class));
    addAction(actions.getAction(OpenAction.class));
    addAction(actions.getAction(SaveAction.class));
    //addAction(actions.editorPrintAction);
    addSeparator();
    addAction(actions.getAction(IDEDeleteAction.class));
    addAction(actions.getAction(IDECutAction.class));
    addAction(actions.getAction(IDECopyAction.class));
    addAction(actions.getAction(IDEPasteAction.class));
    addSeparator();
    addAction(actions.getAction(WatersUndoAction.class));
    addAction(actions.getAction(WatersRedoAction.class));
    final Component panel = getActivePanel();
    if (panel != null) {
      if (panel instanceof EditorPanel) {
        addSeparator();
        addAction(actions.getAction(InsertEventDeclAction.class));
        addAction(actions.getAction(InsertSimpleComponentAction.class));
        addAction(actions.getAction(InsertVariableAction.class));
        final EditorPanel editorPanel = (EditorPanel) panel;
        if (editorPanel.getActiveEditorWindowInterface() != null) {
          addSeparator();
          add(mSelectToolButton);
          add(mNodeToolButton);
          add(mGroupNodeToolButton);
          add(mEdgeToolButton);
          addSeparator();
          add(mZoomSelector);
        }
      } else if (panel instanceof SimulatorPanel) {
        addSeparator();
        addAction(actions.getAction(SimulationResetAction.class));
        addAction(actions.getAction(SimulationBackToStartAction.class));
        addAction(actions.getAction(SimulationStepBackAction.class));
        addAction(actions.getAction(SimulationStepAction.class));
        addAction(actions.getAction(SimulationReplayStepAction.class));
        addAction(actions.getAction(SimulationJumpToEndAction.class));
        // addAction(actions.getAction(SimulationAutoStepAction.class));
      }
    }
  }

  private void addAction(final Action action)
  {
    final JButton button = add(action);
    button.setFocusable(false);
    button.setMargin(INSETS);
  }

  private Component getActivePanel()
  {
    final DocumentContainer container = mIDE.getActiveDocumentContainer();
    if (container == null) {
      return null;
    } else {
      return container.getActivePanel();
    }
  }


  //#########################################################################
  //# Inner Enumeration Tool
  public enum Tool
  {
    SELECT,
    NODE,
    GROUPNODE,
    EDGE;
  }


  //#########################################################################
  //# Data Members
  private final IDE mIDE;
  private final List<Observer> mObservers;

  private final JToggleButton mSelectToolButton;
  private final JToggleButton mNodeToolButton;
  private final JToggleButton mGroupNodeToolButton;
  private final JToggleButton mEdgeToolButton;
  private final ZoomSelector mZoomSelector;

  private Tool mTool;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
  private static final Insets INSETS = new Insets(0, 0, 0, 0);

}
