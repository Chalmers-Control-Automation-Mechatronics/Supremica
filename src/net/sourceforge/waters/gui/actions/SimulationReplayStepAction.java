package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.IconLoader;
import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.simulator.SimulationObserver;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;
import net.sourceforge.waters.model.des.LoopTraceProxy;
import org.supremica.gui.ide.IDE;

public class SimulationReplayStepAction
  extends WatersSimulationAction
  implements SimulationObserver
{
//#########################################################################
  //# Constructor
  SimulationReplayStepAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Replay Step");
    putValue(Action.SHORT_DESCRIPTION, "Replay the next event");
    putValue(Action.SMALL_ICON, IconLoader.ICON_SIMULATOR_REPLAY);
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK));
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final SimulatorPanel panel = getActiveSimulatorPanel();
    if (panel != null) {
      final Simulation sim = getObservedSimulation();
      sim.replayStep();
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
      if (sim.getTrace() != null)
      {
        setEnabled(sim.getEventHistory().size() != sim.getCurrentTime() + 1 || LoopTraceProxy.class.isInstance(sim.getTrace()));
      }
      else
      {
        setEnabled(sim.getEventHistory().size() != sim.getCurrentTime() + 1);
      }
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
}
