package net.sourceforge.waters.gui.simulator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.model.des.EventProxy;

import org.supremica.gui.ide.ModuleContainer;

public class TunnelActionListener implements ActionListener, Observer
{

  public TunnelActionListener(JTable parent, ModuleContainer module)
  {
    this.sim = ((AbstractTunnelTable)parent.getModel()).getSim();
    this.parent = parent;
    module.attach(this);
  }


  public void update()
  {
    this.sim = ((AbstractTunnelTable)parent.getModel()).getSim();
  }

  public void update(EditorChangedEvent e)
  {
    this.sim = ((AbstractTunnelTable)parent.getModel()).getSim();
  }

  public void actionPerformed(ActionEvent e)
  {
    ArrayList<EventProxy> possibleEvents = sim.getValidTransitions();
    if (possibleEvents.size() == 0)
      System.err.println("ERROR: BLOCKING: There are no possible transitions");
    else if (possibleEvents.size() == 1)
    {
      try {
        sim.singleStepMutable(possibleEvents.get(0));
      } catch (UncontrollableException exception) {
        // TODO Auto-generated catch block
        System.err.println(exception.toString());
      }
    }
    else
    {
      try {
        sim.singleStepMutable(findOptions(possibleEvents));
      } catch (UncontrollableException exception) {
        // TODO Auto-generated catch block
        System.err.println(exception.toString());
      }
    }
    ((AbstractTunnelTable)parent.getModel()).updateSim(sim);

  }

  private EventProxy findOptions (ArrayList<EventProxy> possibleEvents)
  {
    do
    {
      Object[] possibilities = possibleEvents.toArray();
      EventProxy event = (EventProxy)JOptionPane.showInputDialog(
                          parent,
                          "There are multiple events possible. Which one do you wish to fire?",
                          "Multiple Options available",
                          JOptionPane.QUESTION_MESSAGE,
                          null, // The supremica icon goes here
                          possibilities,
                          possibilities[0]);

      //If a string was returned, say so.
      if ((event != null)) {
          for (EventProxy findEvent : possibleEvents)
          {
            if (findEvent == event)
              return event;
          }
      }
      JOptionPane.showMessageDialog(parent, "ERROR: The choice you made was not a valid choice, please try again"
          , "Illegal Choice", JOptionPane.ERROR_MESSAGE); // ,supremicaIcon
    }
    while (true);

  }

  Simulation sim;
  JTable parent;

}
