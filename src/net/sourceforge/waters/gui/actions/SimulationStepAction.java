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
import java.util.List;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.IconLoader;
import net.sourceforge.waters.gui.simulator.EventChooserDialog;
import net.sourceforge.waters.gui.simulator.NonDeterministicException;
import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.simulator.SimulationObserver;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;
import net.sourceforge.waters.gui.simulator.Step;
import net.sourceforge.waters.xsd.base.EventKind;

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
    putValue(Action.SMALL_ICON, IconLoader.ICON_SIMULATOR_STEP);
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
  }

  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final SimulatorPanel panel = getActiveSimulatorPanel();
    if (panel != null) {
      final Simulation sim = getObservedSimulation();
      final List<Step> possibleEvents = sim.getValidTransitions();
      if (possibleEvents.size() == 1) {
        try {
          sim.step(possibleEvents.get(0));
        } catch (final NonDeterministicException exception) {
          // TODO Auto-generated catch block
          System.err.println(exception.toString());
        }
      } else {
        try {
          sim.step(findOptions(possibleEvents));
        } catch (final NonDeterministicException exception) {
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

  private Step findOptions(final List<Step> possibleEvents)
  {
    final JLabel[] possibilities = new JLabel[possibleEvents.size()];
    final Step[] events = new Step[possibleEvents.size()];
    for (int looper = 0; looper < possibleEvents.size(); looper++)
    {
      final Step step = possibleEvents.get(looper);
      final JLabel toAdd = new JLabel(step.toString());
      if (step.getEvent().getKind() == EventKind.CONTROLLABLE)
        toAdd.setIcon(IconLoader.ICON_CONTROLLABLE);
      else if (step.getEvent().getKind() == EventKind.UNCONTROLLABLE)
        toAdd.setIcon(IconLoader.ICON_UNCONTROLLABLE);
      else
        toAdd.setIcon(IconLoader.ICON_PROPOSITION);
      possibilities[looper] = toAdd;
      events[looper] = step;
    }
    final EventChooserDialog dialog = new EventChooserDialog(this.getIDE().getFrame(), possibilities, events);
    dialog.setVisible(true);
    final Step step = dialog.getSelectedStep();
    if ((step != null && !dialog.wasCancelled())) {
      for (final Step findEvent : possibleEvents) {
        if (findEvent == step)
          return step;
      }
    }
    return null;
  }

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
}
