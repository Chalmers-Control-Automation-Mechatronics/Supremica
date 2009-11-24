package net.sourceforge.waters.gui.simulator;

import javax.swing.table.AbstractTableModel;

public abstract class SimulationTable extends AbstractTableModel implements SimulationObserver
{

  public SimulationTable(final Simulation sim)
  {
    this.mSim = sim;
    sim.attach(this);
  }

  public Simulation getSim()
  {
    return mSim;
  }

  private final Simulation mSim;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
