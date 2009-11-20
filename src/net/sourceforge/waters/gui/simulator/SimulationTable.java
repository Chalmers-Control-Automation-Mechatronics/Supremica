package net.sourceforge.waters.gui.simulator;

import javax.swing.table.AbstractTableModel;

public abstract class SimulationTable extends AbstractTableModel
{
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public abstract Simulation getSim();

  public abstract void updateSim(Simulation sim);
}
