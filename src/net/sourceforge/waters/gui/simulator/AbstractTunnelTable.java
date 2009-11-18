package net.sourceforge.waters.gui.simulator;

import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.supremica.gui.ide.ModuleContainer;


public class AbstractTunnelTable extends AbstractTableModel implements Observer
{

  //#########################################################################
  //# Constructor
  public AbstractTunnelTable(final ModuleContainer container)
  {
    mCompiledDES = container.getCompiledDES();
    if (mCompiledDES != null)
      System.out.println("DEBUG: SUCCESS! The DES is now non-null");
    mRawData = getRawData();
    mModule = container;
    mModule.attach(this);
    //mSim = new Simulation(container);
  }


  //#########################################################################
  //# Interface javax.swing.table.TableModel
  public int getColumnCount()
  {
    return 4;
  }

  public int getRowCount()
  {
    //return mCompiledDES.getAutomata().size();
    return 1; // Temporary placeholder until the compiling is fixed.
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

  public void update(EditorChangedEvent e)
  {
    mCompiledDES = mModule.getCompiledDES();
    mRawData = getRawData();
    //mSim = new Simulation(mModule);
  }


  //#########################################################################
  //# Auxiliary Methods

  private Object[][] getRawData()
  {
    /*final Object[][] output = new Object[getRowCount()][getColumnCount()];
    final Set<AutomatonProxy> automata = mSim.getCurrentStates().keySet();
    int looper = 0;
    for (final AutomatonProxy aut : automata) {
      output[looper][0] = aut.getName();
      output[looper][1] = "X";
      output[looper][2] = mModule.getModuleContext().getIcon(mSim.getCurrentStates().get(aut));
      output[looper][3] = mSim.getCurrentStates().get(aut).getName();
      looper++;
    }
    return output;*/
    return new Object[][]{{"A", "B", "C", "D"}}; // Temporary placeholder until the compiler works
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
