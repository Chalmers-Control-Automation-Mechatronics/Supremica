package net.sourceforge.waters.gui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.module.NodeProxy;

import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;

public class DesktopSwitchStateAction extends WatersAction
{

  protected DesktopSwitchStateAction(final IDE ide, final AutomatonProxy autoToChange, final NodeProxy node)
  {
    super(ide);
    mAutomaton = autoToChange;
    mState = null;
    for (final StateProxy state : mAutomaton.getStates())
    {
      if (getSimulation().getContainer().getSourceInfoMap().get(state).getSourceObject() == node)
        mState = state;
    }
    putValue(Action.NAME, "Change to this State");
    putValue(Action.SHORT_DESCRIPTION, "Change the automata state to this state");
    setEnabled(true);
  }

  public void actionPerformed(final ActionEvent e)
  {
    getSimulation().setState(mAutomaton, mState);
  }

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

  private final AutomatonProxy mAutomaton;
  private StateProxy mState;

  private static final long serialVersionUID = -1644229513613033199L;
}

