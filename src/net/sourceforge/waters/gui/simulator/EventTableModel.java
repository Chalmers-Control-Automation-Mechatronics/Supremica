package net.sourceforge.waters.gui.simulator;

import java.awt.event.ItemListener;
import java.util.ArrayList;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;

import org.supremica.gui.ide.ModuleContainer;


public class EventTableModel extends SimulationTable implements Observer, ModelObserver
{

  public EventTableModel(final ModuleContainer container, final Simulation sim)
  {
    super(sim);
    mCompiledDES = null;
    getRawData();
    mModuleContainer = container;
    mModuleContainer.attach(this);
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

  public Object getValueAt(final int rowIndex, final int columnIndex)
  {
    return mRawData[rowIndex][columnIndex];
  }

  public String getColumnName(final int rowIndex)
  {
    switch (rowIndex)
    {
    case 0:
      return "Event";
    case 1:
      return "Active";
    default:
      return "Invalid";
    }
  }

  private void getRawData()
  {
    update();
    if (mSim != null && mModuleContainer != null)
    {
      final Object[][] output = new Object[getRowCount()][getColumnCount()];
      final ArrayList<EventProxy> allEvents = mSim.getAllEvents();
      int looper = 0;
      for (final EventProxy event : allEvents) {
        output[looper][0] = event.getName();
        if (mSim.getEventHistory().size() != 0)
          output[looper][1] = mSim.getEventHistory().get(mSim.getEventHistory().size() - 1) == event;
        else
          output[looper][1] = "false";
        looper++;
      }
      mRawData = output;
    }
    else
      mRawData = new Object[0][0];
  }


  public void update(final EditorChangedEvent e)
  {
    if (e.getKind() == EditorChangedEvent.Kind.MAINPANEL_SWITCH)
      update();
  }

  public void update()
  {
    if (mCompiledDES == null && mModuleContainer != null)
    {
      mCompiledDES = mModuleContainer.getCompiledDES();
      mSim.resetSimulation();
      getRawData();
      fireTableDataChanged();
    }
  }

  //##########################################################################
  //# Data Members
  private ProductDESProxy mCompiledDES;
  private Object[][] mRawData;
  private final ModuleContainer mModuleContainer;

  //#########################################################################
  //# Class SimulationTable

  public Simulation getSim()
  {
    return mSim;
  }


  //##########################################################################
  //# Interface net.sourceforge.waters.subject.base.ModelObserver
  public void modelChanged(final ModelChangeEvent event)
  {
    if (event.getKind() != ModelChangeEvent.GEOMETRY_CHANGED) {
      mCompiledDES = null;
      getRawData();
    }
  }


  //#################################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
  private final ArrayList<ItemListener> allListeners = new ArrayList<ItemListener>();
  private final int rowSelected = -1;

  public void simulationChanged(final SimulationChangeEvent event)
  {
    getRawData();
    fireTableDataChanged();
  }

}
