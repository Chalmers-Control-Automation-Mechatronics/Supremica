package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;

import javax.swing.ImageIcon;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;
import org.supremica.gui.ide.ModuleContainer;


public class AbstractTunnelTable
  extends SimulationTable
  implements SimulationObserver
{

  //#########################################################################
  //# Constructor
  public AbstractTunnelTable(final ModuleContainer container, final Simulation sim)
  {
    super(sim);
    getRawData();
    mModuleContainer = container;
  }


  //#########################################################################
  //# Interface javax.swing.table.TableModel
  public int getColumnCount()
  {
    return 4;
  }

  public int getRowCount()
  {
    return getSim().getAutomata().size();
  }

  public Class<?> getColumnClass(final int column)
  {
    switch (column) {
    case 0:
      return String.class;
    case 1:
      return String.class;
    case 2:
      return ImageIcon.class;
    case 3:
      return String.class;
    default:
      throw new ArrayIndexOutOfBoundsException
        ("Bad column number for markings table model!");
    }
  }

  public Object getValueAt(final int row, final int col)
  {
    return mRawData[row][col];
  }

  public String getColumnName(final int columnVal)
  {
    switch (columnVal)
    {
    case 0:
      return "Automaton";
    case 1:
      return "Active";
    case 2:
      return "Marking";
    case 3:
      return "State";
    default:
      return "Invalid";
    }
  }

  //##########################################################################
  //# Interface net.sourceforge.waters.gui.simulator.SimulationObserver
  public void simulationChanged(final SimulationChangeEvent event)
  {
    getRawData();
    fireTableDataChanged();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void getRawData()
  {
    if (getSim() != null && mModuleContainer != null)
    {
      final Object[][] output = new Object[getRowCount()][getColumnCount()];
      final ArrayList<AutomatonProxy> automata = getSim().getAutomata();
      int looper = 0;
      for (final AutomatonProxy aut : automata) {
        output[looper][0] = aut.getName();
        output[looper][1] = getSim().changedLastStep(aut);
        final StateProxy currentState = getSim().getCurrentStates().get(aut);
        output[looper][2] = getSim().getMarking(currentState, aut);
        output[looper][3] = getSim().getCurrentStates().get(aut).getName();
        looper++;
      }
      mRawData = output;
    }
    else
      mRawData = new Object[0][0];
  }


  //#########################################################################
  //# Data Members
  //private final ModuleContainer mModuleContainer;
  private Object[][] mRawData;
  private final ModuleContainer mModuleContainer;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
