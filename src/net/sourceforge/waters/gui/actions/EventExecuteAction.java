package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import net.sourceforge.waters.gui.IconLoader;
import net.sourceforge.waters.gui.simulator.EventStatus;
import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.model.des.EventProxy;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;

class EventExecuteAction extends WatersAction
{

  EventExecuteAction(final IDE ide, final EventProxy event)
  {
    super(ide);
    mEvent = event;
    final String name = event.getName();
    if (name.length() <= 32) {
      putValue(Action.NAME, "Execute Event " + event.getName());
    } else {
      putValue(Action.NAME, "Execute Event");
    }
    putValue(Action.SHORT_DESCRIPTION, "Execute this event");
    putValue(Action.SMALL_ICON, IconLoader.ICON_SIMULATOR_STEP);
    updateEnabledStatus();
  }

  public void actionPerformed(final ActionEvent e)
  {
    final Simulation sim = getObservedSimulation();
    sim.step(mEvent);
  }

  void updateEnabledStatus()
  {
    setEnabled(eventCanBeFired());
  }

  private Simulation getObservedSimulation()
  {
    return ((ModuleContainer)getIDE().getActiveDocumentContainer()).getSimulatorPanel().getSimulation();
  }

  private boolean eventCanBeFired()
  {
    final Simulation sim = getObservedSimulation();
    if (sim == null) {
      getIDE().error("Simulation has not been set!");
      return false;
    } else {
      final EventStatus status = sim.getEventStatus(mEvent);
      return status.canBeFired();
    }
  }

  private final EventProxy mEvent;

  private static final long serialVersionUID = -4783316648203187306L;

}
