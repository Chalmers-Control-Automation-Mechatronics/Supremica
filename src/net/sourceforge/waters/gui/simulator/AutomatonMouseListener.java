package net.sourceforge.waters.gui.simulator;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

import net.sourceforge.waters.model.des.AutomatonProxy;

public class AutomatonMouseListener extends MouseAdapter implements SimulationObserver
{
  //#################################################################################
  //# Constructors
  public AutomatonMouseListener(final Simulation sim, final JTable table, final AutomatonDesktopPane desktop)
  {
    this.mSim = sim;
    this.parent = table;
    this.output = desktop;
    mSim.attach(this);
  }

  //#################################################################################
  //# Class MouseAdapter
  public void mouseClicked(final MouseEvent e){

    final int row = parent.rowAtPoint(e.getPoint());
    if (row != -1)
    {
      final AutomatonProxy toAdd = mSim.getAutomata().get(row);
      output.addAutomaton(toAdd, mSim.getContainer(), mSim, e.getClickCount());
      // parent.clearSelection();
    }
  }

  //#######################################################################################
  //# Interface Simulation Observer

  public void simulationChanged(final SimulationChangeEvent event)
  {
    // If needed
  }

  //#################################################################################
  //# Data Members

  private final Simulation mSim;
  private final JTable parent;
  private final AutomatonDesktopPane output;
}
