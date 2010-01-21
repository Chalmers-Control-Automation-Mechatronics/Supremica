package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sourceforge.waters.gui.IconLoader;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.subject.module.VariableComponentSubject;

import org.supremica.gui.ide.ModuleContainer;


class AutomataTableModel
  extends SimulationTableModel
  implements SimulationObserver, InternalFrameObserver
{

  // #########################################################################
  // # Constructor
  AutomataTableModel(final Simulation sim, final AutomatonDesktopPane desktop)
  {
    super(sim);
    getRawData();
    desktop.attach(this);
    mComparator = new AutomatonTableComparator();
  }


  // #########################################################################
  // # Simple Access
  /**
   * Gets the automaton represented by the index-th row of the sorted table.
   * @param  index   The index of the row of the table.
   * @return The automaton represented at that index.
   */
  AutomatonProxy getAutomaton(final int index)
  {
    final Simulation sim = getSimulation();
    final String finder = (String)mRawData.get(index).get(1);
    return sim.getAutomatonFromName(finder);
  }

  int getIndex(final AutomatonProxy aut)
  {
    for (int looper = 0; looper < this.getRowCount(); looper++)
    {
      if (((String)mRawData.get(looper).get(1)).compareTo(aut.getName()) == 0)
        return looper;
    }
    return -1;
  }

  AutomatonTableComparator getComparator()
  {
    return mComparator;
  }


  // #########################################################################
  // # Interface javax.swing.table.TableModel
  public int getColumnCount()
  {
    return 5;
  }

  public int getRowCount()
  {
    return getSimulation().getAutomata().size();
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
    case 3:
      return ImageIcon.class;
    case 4:
      return String.class;
    default:
      throw new ArrayIndexOutOfBoundsException(
          "Bad column number for markings table model!");
    }
  }

  public Object getValueAt(final int row, final int col)
  {
    return mRawData.get(row).get(col);
  }

  public String getColumnName(final int columnVal)
  {
    switch (columnVal) {
    case 0:
      return "";
    case 1:
      return "Automaton";
    case 2:
      return "Act";
    case 3:
      return "Mrk";
    case 4:
      return "State";
    default:
      return "Invalid";
    }
  }

  // ##########################################################################
  // # Interface net.sourceforge.waters.gui.simulator.SimulationObserver
  public void simulationChanged(final SimulationChangeEvent event)
  {
    if (event.getKind() == SimulationChangeEvent.MODEL_CHANGED)
    {
      getRawData();
      fireTableDataChanged();
    }
    else
      updateRawData();
  }


  //##########################################################################
  //# Interface InternalFrameObserver
  public void onFrameEvent(final InternalFrameEvent event)
  {
    // This should identify the cells that have changed and then fire
    // a more appropriate change event.
    updateRawData();
  }


  //##########################################################################
  //# Sorting
  void addSortingMethod(final int column)
  {
    mComparator.addNewSortingMethod(column);
    getRawData();
    fireTableDataChanged();
  }


  // #########################################################################
  // # Auxiliary Methods
  private void getRawData()
  {
    final ModuleContainer container = getModuleContainer();
    if (container != null) {
      final List<List<Object>> output = new ArrayList<List<Object>>();
      final ArrayList<AutomatonProxy> automata = getSimulation().getAutomata();
      for (final AutomatonProxy aut : automata) {
        final List<Object> row = new ArrayList<Object>();
        if (container.getSourceInfoMap().get(aut).getSourceObject().getClass() == VariableComponentSubject.class)
          row.add(IconLoader.ICON_VARIABLE);
        else
          row.add(ModuleContext.getComponentKindIcon(aut.getKind()));
        row.add(aut.getName());
        row.add(getSimulation().getAutomatonActivityIcon(aut));
        final StateProxy currentState = getSimulation().getCurrentStates().get(aut);
        row.add(getSimulation().getMarkingIcon(currentState, aut, false));
        row.add(getSimulation().getCurrentStates().get(aut).getName());
        output.add(row);
      }
      Collections.sort(output, mComparator);
      mRawData = output;
    } else
      mRawData = new ArrayList<List<Object>>();
  }

  private void updateRawData()
  {
    if (mRawData.size() == 0)
    {
      getRawData();
      return;
    }
    for (final AutomatonProxy aut : getSimulation().getAutomata())
    {
      if (getSimulation().changedLastStep(aut) || getSimulation().changedSecondLastStep(aut)
          || getSimulation().changedNextStep(aut) || getSimulation().getEventHistory().size() == 0
          || getSimulation().isNonControllableAtTime(getSimulation().getCurrentTime()).contains(aut)
          || getSimulation().isNonControllableAtTime(getSimulation().getCurrentTime() - 1).contains(aut)
          || getSimulation().isNonControllableAtTime(getSimulation().getCurrentTime() + 1).contains(aut))
      {
        final int indexToChange = getIndex(aut);
        final List<Object> row = new ArrayList<Object>();
        if (getModuleContainer().getSourceInfoMap().get(aut).getSourceObject().getClass() == VariableComponentSubject.class)
          row.add(IconLoader.ICON_VARIABLE);
        else
          row.add(ModuleContext.getComponentKindIcon(aut.getKind()));
        row.add(aut.getName());
        row.add(getSimulation().getAutomatonActivityIcon(aut));
        final StateProxy currentState = getSimulation().getCurrentStates().get(aut);
        row.add(getSimulation().getMarkingIcon(currentState, aut, false));
        row.add(getSimulation().getCurrentStates().get(aut).getName());
        mRawData.set(indexToChange, row);
        this.fireTableRowsUpdated(indexToChange, indexToChange);
      }
    }
  }

  // #########################################################################
  // # Data Members
  private List<List<Object>> mRawData;
  private final AutomatonTableComparator mComparator;


  // #########################################################################
  // # Class Constants
  private static final long serialVersionUID = 1L;
}
