package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.JLabel;

import net.sourceforge.waters.gui.IconLoader;
import net.sourceforge.waters.gui.simulator.EventChooserDialog;
import net.sourceforge.waters.gui.simulator.NonDeterministicException;
import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.simulator.Step;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;

public class EventExecuteAction extends WatersAction
{

  protected EventExecuteAction(final IDE ide, final EventProxy event)
  {
    super(ide);
    mEvent = event;
    putValue(Action.NAME, "Execute Event");
    putValue(Action.SHORT_DESCRIPTION, "Execute an event");
    putValue(Action.SMALL_ICON, IconLoader.ICON_SIMULATOR_STEP);
    updateEnabledStatus();
  }

  public void actionPerformed(final ActionEvent e)
  {
    if (eventCanBeFired(getObservedSimulation(), mEvent))
      try {
        fireEvent(mEvent);
      } catch (final NonDeterministicException exception) {
        getIDE().error("Non-determinism detected, but not caught");
      }
    else
      getIDE().error("That event is blocked");
  }

  private Simulation getObservedSimulation()
  {
    return ((ModuleContainer)getIDE().getActiveDocumentContainer()).getSimulatorPanel().getSimulation();
  }

  private boolean eventCanBeFired(final Simulation sim, final EventProxy event)
  {
    if (sim == null)
      getIDE().error("Simulation has not been set");
    for (final Step possibleStep : sim.getValidTransitions())
    {
      if (possibleStep.getEvent() == event)
        return true;
    }
    return false;
  }

  private void fireEvent(final EventProxy node) throws NonDeterministicException
  {

    final ArrayList<JLabel> labels = new ArrayList<JLabel>();
    final ArrayList<Step> steps = new ArrayList<Step>();
    for (final Step step: getObservedSimulation().getValidTransitions())
    {
      if (step.getEvent() == node)
      {
        final JLabel toAdd = new JLabel(step.toString());
        if (node.getKind() == EventKind.CONTROLLABLE)
          toAdd.setIcon(IconLoader.ICON_CONTROLLABLE);
        else if (node.getKind() == EventKind.UNCONTROLLABLE)
          toAdd.setIcon(IconLoader.ICON_UNCONTROLLABLE);
        else
          toAdd.setIcon(IconLoader.ICON_PROPOSITION);
        labels.add(toAdd);
        steps.add(step);
      }
    }
    if (labels.size() == 0)
      getIDE().error(": That event cannot be fired");
    else if (labels.size() == 1)
      getObservedSimulation().step(steps.get(0));
    else
    {
      final JLabel[] arrayLabels = new JLabel[labels.size()];
      final Step[] arraySteps = new Step[steps.size()];
      for (int looper = 0; looper < labels.size(); looper++)
      {
        arrayLabels[looper] = labels.get(looper);
        arraySteps[looper] = steps.get(looper);
      }
      final EventChooserDialog dialog = new EventChooserDialog(getIDE(), arrayLabels, arraySteps);
      dialog.setVisible(true);
      if (!dialog.wasCancelled())
      {
        getObservedSimulation().step(dialog.getSelectedStep());
      }
    }
  }

  void updateEnabledStatus()
  {
    this.setEnabled(eventCanBeFired(getObservedSimulation(), mEvent));
  }

  private final EventProxy mEvent;

  private static final long serialVersionUID = -4783316648203187306L;

}
