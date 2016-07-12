package net.sourceforge.waters.gui.flexfact;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.SwingUtilities;

import net.sourceforge.waters.gui.simulator.Simulation;
import net.sourceforge.waters.gui.simulator.SimulatorPanel;
import net.sourceforge.waters.gui.simulator.SimulatorStep;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.properties.Config;

public class AutoStep implements Runnable
{

  int n;
  SimulatorStep c;
  SimulatorPanel panel;

  public AutoStep(final SimulatorPanel _panel){
   panel = _panel;
  }

  @Override
  public void run()
  {
        // Get simulation and events
        Simulation sim = panel.getSimulation();
        List<SimulatorStep> possibleEvents = sim.getEnabledSteps();

        // Get controllable events from the possible events list
        final List<SimulatorStep> possibleControllableEvents = new ArrayList<SimulatorStep>();
        for(final SimulatorStep s : possibleEvents){
          if(s.getEvent().getKind() == EventKind.CONTROLLABLE)
            possibleControllableEvents.add(s);
        }

        // While there is at least one controllable event to go to
        while(!possibleControllableEvents.isEmpty())
        {
          n = possibleEvents.size();

          final Random rand = new Random();

          //Get random controllable event. If list is size of 1, get the first element.
          c = possibleControllableEvents.get(possibleControllableEvents.size() == 1 ? 0 : rand.nextInt(possibleControllableEvents.size() - 1));

          if(Config.INCLUDE_FLEXFACT.isTrue()){
            SwingUtilities.invokeLater(new Runnable() {
              @Override
              public void run()
              {
                if(panel.getSimulation().getEnabledSteps().size() == n && panel.getSimulation().getEnabledSteps().contains(c)){
                  panel.getSimulation().step(c);
                }
              }
            });
          }

            sim = panel.getSimulation();
            possibleEvents = sim.getEnabledSteps();
            possibleControllableEvents.clear();
            for(final SimulatorStep s : possibleEvents){
              if(s.getEvent().getKind() == EventKind.CONTROLLABLE)
                possibleControllableEvents.add(s);
            }

        }

  }

}
