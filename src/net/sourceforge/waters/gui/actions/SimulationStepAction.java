//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   GraphLayoutAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.simulator.EventChooserDialog;
import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.simulator.SimulationObserver;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;
import net.sourceforge.waters.gui.simulator.UncontrollableException;
import net.sourceforge.waters.model.des.EventProxy;

import org.supremica.gui.ide.IDE;


public class SimulationStepAction
  extends WatersSimulationAction
  implements SimulationObserver
{

  //#########################################################################
  //# Constructor
  SimulationStepAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Step");
    putValue(Action.SHORT_DESCRIPTION, "Execute an event");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
    putValue(Action.ACCELERATOR_KEY,
	         KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final SimulatorPanel panel = getActiveSimulatorPanel();
    if (panel != null) {
      final Simulation sim = getObservedSimulation();
      final ArrayList<EventProxy> possibleEvents = sim.getValidTransitions();
      Collections.sort(possibleEvents);
      if (possibleEvents.size() == 1) {
        try {
          sim.step(possibleEvents.get(0));
        } catch (final UncontrollableException exception) {
          // TODO Auto-generated catch block
          System.err.println(exception.toString());
        }
      } else {
        try {
          sim.step(findOptions(possibleEvents));
        } catch (final UncontrollableException exception) {
          // TODO Auto-generated catch block
          System.err.println(exception.toString());
        }
      }
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  void updateEnabledStatus()
  {
    final Simulation sim = getObservedSimulation();
    if (sim == null) {
      setEnabled(false);
    } else {
      setEnabled(sim.getValidTransitions().size() != 0);
    }
  }

  private EventProxy findOptions(final ArrayList<EventProxy> possibleEvents)
  {
    final EventProxy[] possibilities = new EventProxy[possibleEvents.size()];
    for (int looper = 0; looper < possibleEvents.size(); looper++)
      possibilities[looper] = possibleEvents.get(looper);
    final EventChooserDialog dialog = new EventChooserDialog(this.getIDE().getFrame(), possibilities);
    dialog.setVisible(true);
    final EventProxy event = dialog.getSelectedEvent();
    if ((event != null && !dialog.wasCancelled())) {
      for (final EventProxy findEvent : possibleEvents) {
        if (findEvent == event)
          return event;
      }
    }
    return null;
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
