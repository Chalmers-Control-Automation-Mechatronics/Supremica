package net.sourceforge.waters.gui.simulator;

import java.util.Set;

import javax.swing.ImageIcon;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;

import org.supremica.gui.ide.ModuleContainer;


public class AbstractTunnelTable extends SimulationTable implements Observer, ModelObserver
{

  //#########################################################################
  //# Constructor
  public AbstractTunnelTable(final ModuleContainer container, final Simulation sim)
  {
    super(sim);
    mCompiledDES = null;
    getRawData();
    mModuleContainer = container;
    mModuleContainer.attach(this);
  }

  public Simulation getSim()
  {
    return mSim;
  }


  //#########################################################################
  //# Interface javax.swing.table.TableModel
  public int getColumnCount()
  {
    return 4;
  }

  public int getRowCount()
  {
    if (mCompiledDES == null) {
      return 0;
    } else {
      return mCompiledDES.getAutomata().size();
    }
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
      return "Automata";
    case 1:
      return "Active";
    case 2:
      return "Colour";
    case 3:
      return "State";
     default:
       return "Invalid";
    }
  }

  //#########################################################################
  //# Interface Observer

  public void update()
  {
    if (mCompiledDES == null)
    {
      mCompiledDES = mModuleContainer.getCompiledDES();
      mSim.resetSimulation();
      getRawData();
      fireTableDataChanged();
    }
  }

  public void update(final EditorChangedEvent e)
  {
    if (e.getKind() == EditorChangedEvent.Kind.MAINPANEL_SWITCH)
      update();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void getRawData()
  {
    if (mSim != null && mModuleContainer != null)
    {
      final Object[][] output = new Object[getRowCount()][getColumnCount()];
      final Set<AutomatonProxy> automata = mSim.getCurrentStates().keySet();
      int looper = 0;
      for (final AutomatonProxy aut : automata) {
        output[looper][0] = aut.getName();
        output[looper][1] = mSim.changedLastStep(aut);
        final StateProxy currentState = mSim.getCurrentStates().get(aut);
        output[looper][2] = mSim.getMarking(currentState, aut);
        output[looper][3] = mSim.getCurrentStates().get(aut).getName();
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
  private ProductDESProxy mCompiledDES;
  private Object[][] mRawData;
  private final ModuleContainer mModuleContainer;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;


  public void modelChanged(final ModelChangeEvent event)
  {
    if (event.getKind() != ModelChangeEvent.GEOMETRY_CHANGED)
    {
      mCompiledDES = null;
      getRawData();
    }
  }

  public void simulationChanged(final SimulationChangeEvent event)
  {
    getRawData();
    fireTableDataChanged();
  }
}
