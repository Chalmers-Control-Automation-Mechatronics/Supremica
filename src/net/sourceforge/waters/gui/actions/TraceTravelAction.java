//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   TraceTravelAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;

import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;

import org.supremica.gui.ide.IDE;

public class TraceTravelAction extends WatersSimulationAction
{

  //#########################################################################
  //# Constructors
  TraceTravelAction(final IDE ide, final int time)
  {
    super(ide);
    mTime = time;
    putValue(Action.NAME, "Jump to step " + time);
    putValue(Action.SHORT_DESCRIPTION,
             "Reset the simulation to the state after this event was fired");
    setEnabled(true);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent e)
  {
    final SimulatorPanel panel = getActiveSimulatorPanel();
    final Simulation sim = panel.getSimulation();
    sim.setState(mTime);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.actions.WaterSimulationAction
  void updateEnabledStatus()
  {
  }



  //#########################################################################
  //# Data Members
  private final int mTime;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -4783316648203187306L;

}