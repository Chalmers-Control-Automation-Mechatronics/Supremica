//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   DesktopSwitchStateAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.Action;

import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.module.NodeProxy;

import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;


public class DesktopSwitchStateAction extends WatersAction
{

  //#########################################################################
  //# Constructors
  protected DesktopSwitchStateAction(final IDE ide,
                                     final AutomatonProxy autoToChange,
                                     final NodeProxy node)
  {
    super(ide);
    mAutomaton = autoToChange;
    mState = null;
    String name = null;
    final ModuleContainer container =
      (ModuleContainer) ide.getActiveDocumentContainer();
    final Map<Object,SourceInfo> infomap = container.getSourceInfoMap();
    for (final StateProxy state : mAutomaton.getStates()) {
      if (infomap.get(state).getSourceObject() == node) {
        mState = state;
        name = state.getName();
        if (name.length() > 32) {
          name = null;
        }
        break;
      }
    }
    if (name == null) {
      putValue(Action.NAME, "Change to this State");
    } else {
      putValue(Action.NAME, "Change to State " + name);
    }
    putValue(Action.SHORT_DESCRIPTION,
             "Change the automaton state to this state");
    setEnabled(true);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent e)
  {
    getSimulation().setState(mAutomaton, mState);
  }


  //#########################################################################
  //# Auxiliary Methods
  public Simulation getSimulation()
  {
    final IDE ide = getIDE();
    final DocumentContainer container = ide.getActiveDocumentContainer();
    if (container == null || !(container instanceof ModuleContainer)) {
      return null;
    }
    final ModuleContainer mcontainer = (ModuleContainer) container;
    final Component panel = mcontainer.getActivePanel();
    if (panel instanceof SimulatorPanel) {
      return ((SimulatorPanel) panel).getSimulation();
    } else {
      return null;
    }
  }


  //#########################################################################
  //# Data Members
  private final AutomatonProxy mAutomaton;
  private StateProxy mState;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -1644229513613033199L;

}
