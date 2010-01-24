package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import net.sourceforge.waters.gui.simulator.Simulation;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;

public class TraceTravelAction extends WatersAction
{


  protected TraceTravelAction(final IDE ide, final int time)
  {
    super(ide);
    mTime = time;
    putValue(Action.NAME, "Travel to Event");
    putValue(Action.SHORT_DESCRIPTION, "Move the simulation to the state after this event was fired");
    updateEnabledStatus();
  }

  public void actionPerformed(final ActionEvent e)
  {
    int currentTime = getObservedSimulation().getCurrentTime();
    while (currentTime != mTime)
    {
      if (mTime < currentTime)
      {
        getObservedSimulation().stepBack();
        currentTime--;
      }
      else if (mTime > currentTime)
      {
        getObservedSimulation().replayStep();
        currentTime++;
      }
    }
  }

  private Simulation getObservedSimulation()
  {
    return ((ModuleContainer)getIDE().getActiveDocumentContainer()).getSimulatorPanel().getSimulation();
  }

  void updateEnabledStatus()
  {
    this.setEnabled(true);
  }

  private final int mTime;

  private static final long serialVersionUID = -4783316648203187306L;

}