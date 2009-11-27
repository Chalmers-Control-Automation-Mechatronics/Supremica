package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JDesktopPane;
import net.sourceforge.waters.model.des.AutomatonProxy;

import org.supremica.gui.ide.ModuleContainer;

public class AutomatonDesktopPane extends JDesktopPane implements SimulationObserver
{

  //#########################################################################
  //# Constructor

  public AutomatonDesktopPane(final ModuleContainer container, final Simulation mSim)
  {
    super();
    onReOpen(container, mSim);
  }

  //#########################################################################
  //# Mutator Methods
  public void addAutomaton(final AutomatonProxy automaton, final ModuleContainer container, final Simulation mSim, final int clicks)
  {
    if (!openAutomaton.containsKey(automaton))
    {
      if (clicks == 2)
      {
        final AutomatonInternalFrame newFrame = new AutomatonInternalFrame(automaton, this, container, mSim);
        add(newFrame);
        newFrame.moveToFront();
        openAutomaton.put(automaton, newFrame);
      }
    }
    else
    {
      selectAutomaton(clicks, automaton);
    }
  }

  public void removeAutomaton(final AutomatonProxy automaton)
  {
    if (openAutomaton.containsKey(automaton))
      openAutomaton.remove(automaton);
  }

  public void onReOpen(final ModuleContainer container, final Simulation mSim)
  {
    for (final AutomatonProxy proxy : oldOpen)
    {
      addAutomaton(proxy, container, mSim, 2);
    }
  }

  private void selectAutomaton(final int clicks, final AutomatonProxy automaton)
  {
    if (clicks == 1)
    {
      openAutomaton.get(automaton).setFocusable(true);
    }
    else if (clicks == 2)
    {
      openAutomaton.get(automaton).setFocusable(true);
      openAutomaton.get(automaton).moveToFront();
    }
  }

  //#########################################################################
  //# Interface SimulationObserver
  public void simulationChanged(final SimulationChangeEvent event)
  {
    oldOpen = new ArrayList<AutomatonProxy>();
    if (event.getKind() == SimulationChangeEvent.MODEL_CHANGED)
    {
      for (final AutomatonProxy automaton : openAutomaton.keySet())
      {
        oldOpen.add(automaton);
        openAutomaton.get(automaton).dispose();
        removeAutomaton(automaton);
      }
    }
  }

  //#########################################################################
  //# Data Members
  HashMap<AutomatonProxy, AutomatonInternalFrame> openAutomaton = new HashMap<AutomatonProxy, AutomatonInternalFrame>();
  ArrayList<AutomatonProxy> oldOpen = new ArrayList<AutomatonProxy>();

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -5528014241244952875L;


}
