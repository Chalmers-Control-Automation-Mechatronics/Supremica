package net.sourceforge.waters.gui.simulator;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

import net.sourceforge.waters.model.des.AutomatonProxy;

public class AutomatonMouseListener extends MouseAdapter
{
  //#################################################################################
  //# Constructors
  public AutomatonMouseListener(final Simulation sim, final JTable table, final AutomatonDesktopPane desktop)
  {
    this.mSim = sim;
    this.parent = table;
    this.output = desktop;
  }

  //#################################################################################
  //# Class MouseAdapter
  public void mouseClicked(final MouseEvent e){

    final int row = parent.rowAtPoint(e.getPoint());
    if (row != -1)
    {
      final AutomatonProxy toAdd = ((AbstractTunnelTable)parent.getModel()).getAutomaton(row, mSim);
      output.addAutomaton(toAdd.getName(), mSim.getContainer(), mSim, e.getClickCount());
    }
  }

  //#################################################################################
  //# Data Members

  private final Simulation mSim;
  private final JTable parent;
  private final AutomatonDesktopPane output;
}
