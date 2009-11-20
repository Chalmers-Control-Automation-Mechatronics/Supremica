package net.sourceforge.waters.gui.simulator;

import java.util.Set;

import javax.swing.table.AbstractTableModel;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;

import org.supremica.gui.ide.ModuleContainer;


public class EventTableModel extends SimulationTable implements Observer
{

  public EventTableModel(final ModuleContainer container)
  {
    mCompiledDES = null;
    mRawData = getRawData();
    mModuleContainer = container;
    mModuleContainer.attach(this);
    mSim = new Simulation(container);
  }

  public int getColumnCount()
  {
    return 2;
  }

  public int getRowCount()
  {
    if (mCompiledDES == null) {
      return 0;
    } else {
      return mCompiledDES.getEvents().size();
    }
  }

  public Object getValueAt(int rowIndex, int columnIndex)
  {
    return mRawData[rowIndex][columnIndex];
  }

  private Object[][] getRawData()
  {
    if (mCompiledDES != null && mModuleContainer != null)
    {
      final Object[][] output = new Object[getRowCount()][getColumnCount()];
      final Set<EventProxy> allEvents = mCompiledDES.getEvents();
      int looper = 0;
      for (final EventProxy event : allEvents) {
        output[looper][0] = event.getName();
        if (mSim.getEventHistory().size() != 0)
          output[looper][1] = mSim.getEventHistory().get(mSim.getEventHistory().size() - 1) == event;
        else
          output[looper][1] = "false";
        looper++;
      }
      return output;
    }
    return new Object[0][2];
  }


  public void update(EditorChangedEvent e)
  {
    update();
  }

  public void updateSim(Simulation sim)
  {
    mSim = sim;
    mRawData = getRawData();
    fireTableDataChanged();
  }

  public void update()
  {
    mCompiledDES = mModuleContainer.getCompiledDES();
    mSim = new Simulation(mModuleContainer);
    mRawData = getRawData();
    fireTableDataChanged();
  }

  //#################################################################################
  //# Data Members

  private ProductDESProxy mCompiledDES;
  private Object[][] mRawData;
  private final ModuleContainer mModuleContainer;
  private Simulation mSim;
  public Simulation getSim()
  {
    return mSim;
  }

}
