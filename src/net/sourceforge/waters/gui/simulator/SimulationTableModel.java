package net.sourceforge.waters.gui.simulator;

import javax.swing.table.AbstractTableModel;

import org.supremica.gui.ide.ModuleContainer;


abstract class SimulationTableModel
  extends AbstractTableModel
  implements SimulationObserver
{
  // #########################################################################
  // # Constructors
  SimulationTableModel(final Simulation sim)
  {
    mSimulation = sim;
    sim.attach(this);
  }

  // #########################################################################
  // # Simple Access
  Simulation getSimulation()
  {
    return mSimulation;
  }

  ModuleContainer getModuleContainer()
  {
    if (mSimulation == null) {
      return null;
    } else {
      return mSimulation.getContainer();
    }
  }

  // #########################################################################
  // # Data Members
  private final Simulation mSimulation;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
