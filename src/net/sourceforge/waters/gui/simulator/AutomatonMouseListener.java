package net.sourceforge.waters.gui.simulator;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

import net.sourceforge.waters.model.des.AutomatonProxy;

public class AutomatonMouseListener extends MouseAdapter implements SimulationObserver
{
  public AutomatonMouseListener(final Simulation sim, final JTable table, final AutomatonDesktopPane desktop)
  {
    this.mSim = sim;
    this.parent = table;
    this.output = desktop;
    mSim.attach(this);
  }

  public void mouseClicked(final MouseEvent e){
    if (e.getClickCount() == 2)
    {
      final int row = parent.rowAtPoint(e.getPoint());
      if (row != -1)
      {
        final AutomatonProxy toAdd = mSim.getAutomata().get(row);
        output.addAutomaton(toAdd, mSim.getContainer());
        System.out.println("DEBUG: Reached " + row);
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
  private final AutomatonDesktopPane output;
}
