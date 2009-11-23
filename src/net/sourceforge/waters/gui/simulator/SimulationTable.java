package net.sourceforge.waters.gui.simulator;

import javax.swing.table.AbstractTableModel;

public abstract class SimulationTable extends AbstractTableModel implements SimulationObserver
{
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public SimulationTable(final Simulation sim)
  {
    this.mSim = sim;
    sim.attach(this);
  }

  public abstract Simulation getSim();

  public Simulation mSim;
}
