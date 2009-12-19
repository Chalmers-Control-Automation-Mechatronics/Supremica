package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTable;

import net.sourceforge.waters.gui.IconLoader;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.subject.module.VariableComponentSubject;

import org.supremica.gui.ide.ModuleContainer;


public class AbstractTunnelTable extends SimulationTable implements
    SimulationObserver, InternalFrameObserver
{



  // #########################################################################
  // # Constructor
  public AbstractTunnelTable(final ModuleContainer container,
      final Simulation sim, final AutomatonDesktopPane desktop)
  {
    super(sim);
    getRawData();
    mModuleContainer = container;
    mParent = null;
    desktop.attach(this);
    mComparator = new AutomatonTableComparator<Object>();
    observers = new HashSet<TableOrderObserver>();
  }

  // #########################################################################
  // # Simple Access

  public void attachTable(final JTable table)
  {
    mParent = table;
  }

  /**
   * Gets the automaton represented by the indexth row of the sorted table
   * @param index The index of the row of the table
   * @return The automaton represented at that index
   */
  public AutomatonProxy getAutomaton(final int index, final Simulation mSimulation)
  {
    final String finder = (String)mRawData.get(index).get(1);
    return mSimulation.getAutomatonFromName(finder);
  }

  public AutomatonTableComparator<Object> getComparitor()
  {
    return mComparator;
  }

  //#########################################################################
  // # Processing TableOrderObservers

  @SuppressWarnings("unchecked")
  public void tableOrderChanged()
  {
    final HashSet<TableOrderObserver> clone = (HashSet<TableOrderObserver>)observers.clone();
    for (final TableOrderObserver observer : clone)
      observer.processTableReorder(new TableOrderChangedEvent(this));
    getRawData();
  }

  public void attach (final TableOrderObserver observer)
  {
    observers.add(observer);
  }
  public void detach (final TableOrderObserver observer)
  {
    observers.remove(observer);
  }

  // #########################################################################
  // # Interface javax.swing.table.TableModel
  public int getColumnCount()
  {
    return 5;
  }

  public int getRowCount()
  {
    return getSim().getAutomata().size();
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
    getRawData();
    fireTableDataChanged();
  }

  //###########################################################################
  //# Interface InternalFrameObserver
  public void onFrameEvent(final InternalFrameEvent event)
  {
    mParent.repaint();
  }

  // #########################################################################
  // # Auxiliary Methods
  private void getRawData()
  {
    if (getSim() != null && mModuleContainer != null) {
      final List<List<Object>> output = new ArrayList<List<Object>>();
      final ArrayList<AutomatonProxy> automata = getSim().getAutomata();
      for (final AutomatonProxy aut : automata) {
        final List<Object> row = new ArrayList<Object>();
        if (mModuleContainer.getSourceInfoMap().get(aut).getSourceObject().getClass() == VariableComponentSubject.class)
          row.add(IconLoader.ICON_VARIABLE);
        else
          row.add(ModuleContext.getComponentKindIcon(aut.getKind()));
        row.add(aut.getName());
        row.add(ModuleContext.getBooleanIcon(getSim().changedLastStep(aut)));
        final StateProxy currentState = getSim().getCurrentStates().get(aut);
        row.add(getSim().getMarkingIcon(currentState, aut));
        row.add(getSim().getCurrentStates().get(aut).getName());
        output.add(row);
      }
      Collections.sort(output, mComparator);
      mRawData = output;
    } else
      mRawData = new ArrayList<List<Object>>();
  }

  // #########################################################################
  // # Data Members
  // private final ModuleContainer mModuleContainer;
  private List<List<Object>> mRawData;
  private final ModuleContainer mModuleContainer;
  private JTable mParent;
  private final AutomatonTableComparator<Object> mComparator;
  private final HashSet<TableOrderObserver> observers;

  // #########################################################################
  // # Class Constants
  private static final long serialVersionUID = 1L;
}
