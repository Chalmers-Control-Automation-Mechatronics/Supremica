package net.sourceforge.waters.gui.simulator;

import java.util.List;

import javax.swing.Icon;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.des.EventProxy;
import org.supremica.gui.ide.ModuleContainer;


public class EventTableModel
  extends SimulationTable
{

  //##########################################################################
  //# Constructors
  public EventTableModel(final ModuleContainer container, final Simulation sim)
  {
    super(sim);
    mModuleContainer = container;
    getRawData();
  }

  //##########################################################################
  //# Interface SimulationObserver
  public void simulationChanged(final SimulationChangeEvent event)
  {
    getRawData();
    fireTableDataChanged();
  }

  //##########################################################################
  //# Class TableModel
  public int getColumnCount()
  {
    return 3;
  }

  public int getRowCount()
  {
    return getSim().getAllEvents().size();
  }

  public Object getValueAt(final int rowIndex, final int columnIndex)
  {
    return mRawData[rowIndex][columnIndex];
  }

  public Class<?> getColumnClass(final int column)
  {
    switch (column) {
    case 0:
      return Icon.class;
    case 1:
      return String.class;
    case 2:
      return Icon.class;
    default:
      throw new ArrayIndexOutOfBoundsException
        ("Bad column number for markings table model!");
    }
  }

  public String getColumnName(final int rowIndex)
  {
    switch (rowIndex)
    {
    case 0:
      return "";
    case 1:
      return "Event";
    case 2:
      return "Enabled";
    default:
      return "Invalid";
    }
  }

  private void getRawData()
  {
    if (getSim() != null && mModuleContainer != null)
    {
      final Object[][] output = new Object[getRowCount()][getColumnCount()];
      final List<EventProxy> allEvents = getSim().getAllEvents();
      int looper = 0;
      for (final EventProxy event : allEvents) {
        output[looper][0] = ModuleContext.getEventKindIcon(event.getKind());
        output[looper][1] = event.getName();
        output[looper][2] = ModuleContext.getBooleanIcon(getSim().getValidTransitions().contains(event));
        looper++;
      }
      mRawData = output;
    }
    else
      mRawData = new Object[0][0];
  }

  //##########################################################################
  //# Data Members
  private Object[][] mRawData;
  private final ModuleContainer mModuleContainer;


  //#################################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
