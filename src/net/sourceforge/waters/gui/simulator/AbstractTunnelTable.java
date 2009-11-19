package net.sourceforge.waters.gui.simulator;

import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

import org.supremica.gui.ide.ModuleContainer;


public class AbstractTunnelTable extends AbstractTableModel implements Observer
{

  //#########################################################################
  //# Constructor
  public AbstractTunnelTable(final ModuleContainer container)
  {
    mCompiledDES = null;
    mRawData = getRawData();
    mModule = container;
    mModule.attach(this);
    mSim = new Simulation(container);
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
    if (mCompiledDES == null && mModule != null)
      mCompiledDES = mModule.getCompiledDES();
    if (mCompiledDES != null)
      return mCompiledDES.getAutomata().size();
    System.out.println("DEBUG: DES has not been successfully compiled");
    return 0;
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

  //#########################################################################
  //# Interface Observer

  public void update()
  {
    mCompiledDES = mModule.getCompiledDES();
    mSim = new Simulation(mModule);
    mRawData = getRawData();
    fireTableDataChanged();
  }

  public void updateSim(Simulation sim)
  {
    mSim = sim;
    mRawData = getRawData();
    fireTableDataChanged();
  }

  public void update(EditorChangedEvent e)
  {
    update();
  }


  //#########################################################################
  //# Auxiliary Methods

  private Object[][] getRawData()
  {
    if (mSim != null && mModule != null)
    {
      final Object[][] output = new Object[getRowCount()][getColumnCount()];
      final Set<AutomatonProxy> automata = mSim.getCurrentStates().keySet();
      int looper = 0;
      for (final AutomatonProxy aut : automata) {
        output[looper][0] = aut.getName();
        output[looper][1] = "X";
        StateProxy currentState = mSim.getCurrentStates().get(aut);
        SourceInfo info = mModule.getSourceInfoMap().get(currentState);
        SimpleNodeProxy node = (SimpleNodeProxy)info.getSourceObject();
        output[looper][2] = mModule.getModuleContext().getIcon(node);
        output[looper][3] = mSim.getCurrentStates().get(aut).getName();
        looper++;
      }
      return output;
    }
    else
      return new Object[0][0];
  }


  //#########################################################################
  //# Data Members
  //private final ModuleContainer mModuleContainer;
  private ProductDESProxy mCompiledDES;
  private Object[][] mRawData;
  private final ModuleContainer mModule;
  private Simulation mSim;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;



}
