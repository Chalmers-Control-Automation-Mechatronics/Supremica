package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.simulator.SimulationObserver;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;
import net.sourceforge.waters.gui.util.IconLoader;
import net.sourceforge.waters.model.des.LoopTraceProxy;
import net.sourceforge.waters.model.des.TraceProxy;

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
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final SimulatorPanel panel = getObservedSimulatorPanel();
    if (panel != null) {
      final Simulation sim = panel.getSimulation();
      sim.replayStep();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  @Override
  void updateEnabledStatus()
  {
    final SimulatorPanel panel = getObservedSimulatorPanel();
    if (panel == null) {
      setEnabled(false);
      return;
    }
    final Simulation sim = panel.getSimulation();
    if (sim.getCurrentTime() < sim.getHistorySize() - 1) {
      setEnabled(true);
      return;
    }
    final TraceProxy trace = sim.getTrace();
    setEnabled(trace != null && trace instanceof LoopTraceProxy);
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
