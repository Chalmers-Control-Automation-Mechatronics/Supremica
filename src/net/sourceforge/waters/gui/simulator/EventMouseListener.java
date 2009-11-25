package net.sourceforge.waters.gui.simulator;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

import net.sourceforge.waters.model.des.EventProxy;

public class EventMouseListener extends MouseAdapter implements SimulationObserver
{
  public EventMouseListener(final Simulation sim, final JTable table)
  {
    this.mSim = sim;
    this.parent = table;
    mSim.attach(this);
  }

  public void mouseClicked(final MouseEvent e){
      if (e.getClickCount() == 2)
      {
        for (int row = 0; row < parent.getRowCount(); row++)
        {
          for (int column = 0; column < parent.getColumnCount(); column++)
          {
            if (parent.getCellRect(row, column, true).contains(e.getPoint()))
            {
              final EventProxy event = mSim.getAllEvents().get(row);
              if (mSim.getValidTransitions().contains(event))
              {
                try {
                  mSim.singleStepMutable(event);
                } catch (final UncontrollableException exception) {
                  System.out.println(exception.getMessage());
                }
              }
              else
                System.out.println(mSim.getBlockingTextual(event));
            }
          }
        }
      }
    }

  //#######################################################################################
  //# Interface Simulation Observer

  public void simulationChanged(final SimulationChangeEvent event)
  {
    // TODO Auto-generated method stub
  }

  private final Simulation mSim;
  private final JTable parent;
}
