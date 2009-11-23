package net.sourceforge.waters.gui.simulator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.model.des.EventProxy;

import org.supremica.gui.ide.ModuleContainer;


public class TunnelActionListener implements ActionListener, Observer
{

  public TunnelActionListener(final JTable parent, final ModuleContainer module)
  {
    this.sim = ((SimulationTable) parent.getModel()).getSim();
    this.parent = parent;
    module.attach(this);
  }

  public void update()
  {
    this.sim = ((SimulationTable) parent.getModel()).getSim();
  }

  public void update(final EditorChangedEvent e)
  {
    this.sim = ((SimulationTable) parent.getModel()).getSim();
  }

  public void actionPerformed(final ActionEvent e)
  {
    final ArrayList<EventProxy> possibleEvents = sim.getValidTransitions();
    Collections.sort(possibleEvents);
    if (possibleEvents.size() == 0)
      System.err.println("ERROR: BLOCKING: There are no possible transitions");
    else if (possibleEvents.size() == 1) {
      try {
        sim.singleStepMutable(possibleEvents.get(0));
      } catch (final UncontrollableException exception) {
        // TODO Auto-generated catch block
        System.err.println(exception.toString());
      }
    } else {
      try {
        sim.singleStepMutable(findOptions(possibleEvents));
      } catch (final UncontrollableException exception) {
        // TODO Auto-generated catch block
        System.err.println(exception.toString());
      }
    }

  }

  private EventProxy findOptions(final ArrayList<EventProxy> possibleEvents)
  {
    final Object[] possibilities = possibleEvents.toArray();
    final EventProxy event =
        (EventProxy) JOptionPane
            .showInputDialog(
                parent,
                "There are multiple events possible. Which one do you wish to fire?",
                "Multiple Options available", JOptionPane.QUESTION_MESSAGE,
                null, // The supremica icon goes here
                possibilities, possibilities[0]);

    // If a string was returned, say so.
    if ((event != null)) {
      for (final EventProxy findEvent : possibleEvents) {
        if (findEvent == event)
          return event;
      }
    }
    JOptionPane
        .showMessageDialog(
            parent,
            "ERROR: The choice you made was not a valid choice, so no event will be fired",
            "Illegal Choice", JOptionPane.ERROR_MESSAGE); // ,supremicaIcon
    return null;
  }

  Simulation sim;
  JTable parent;

}
