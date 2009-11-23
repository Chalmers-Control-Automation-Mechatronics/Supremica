package net.sourceforge.waters.gui.simulator;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

public class EventMouseListener extends MouseAdapter implements SimulationObserver
{
  public EventMouseListener(final Simulation sim, final JTable table)
  {
    this.mSim = sim;
    this.parent = table;
  }

  public void mouseClicked(final MouseEvent e){
      for (int row = 0; row < parent.getRowCount(); row++)
      {
        for (int column = 0; column < parent.getColumnCount(); column++)
        {
          if (parent.getCellRect(row, column, true).contains(e.getPoint()))
            System.out.println("TEMPORARY: Event selected: Event offset: " + row);
            // Code designed to deal with what happens when a row on an event table occurs, happens here.
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
