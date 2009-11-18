package net.sourceforge.waters.gui.simulator;

import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.supremica.gui.ide.ModuleContainer;


public class AbstractTunnelTable extends AbstractTableModel
{

  //#########################################################################
  //# Constructor
  public AbstractTunnelTable(final ModuleContainer container)
  {
    //mModuleContainer = container;
    try {
      mCompiledDES = compile(container);
    } catch (final EvalException exception) {
      throw new WatersRuntimeException(exception);
      // Exception should be caught and message displayed using JOptionPane.
      // If exception occurs when compiling, the simulator tab cannot be
      // activated.
    }
    mRawData = getRawData(mCompiledDES);
  }


  //#########################################################################
  //# Interface javax.swing.table.TableModel
  public int getColumnCount()
  {
    return 4;
  }

  public int getRowCount()
  {
    return mCompiledDES.getAutomata().size();
  }

  public Class<?> getColumnClass(final int column)
  {
    switch (column) {
    case 0:
      return String.class;
    case 1:
      return ImageIcon.class;
    case 2:
      return ImageIcon.class;
    case 3:
      return ImageIcon.class;
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
  //# Auxiliary Methods
  private ProductDESProxy compile(ModuleContainer container)
    throws EvalException
  {
    final DocumentManager manager = container.getIDE().getDocumentManager();
    final ProductDESProxyFactory factory =
      ProductDESElementFactory.getInstance();
    final ModuleCompiler compiler =
      new ModuleCompiler(manager, factory, container.getModule());
    return compiler.compile();
  }

  private Object[][] getRawData(final ProductDESProxy des)
  {
    final Object[][] output = new Object[getRowCount()][getColumnCount()];
    final Set<AutomatonProxy> automata = des.getAutomata();
    int looper = 0;
    for (final AutomatonProxy aut : automata) {
      output[looper][0] = aut.getName();
      output[looper][1] = ModuleContext.getComponentKindIcon(aut.getKind());
      output[looper][2] = ModuleContext.getComponentKindIcon(aut.getKind());
      output[looper][3] = ModuleContext.getComponentKindIcon(aut.getKind());
      looper++;
    }
    return output;
  }


  //#########################################################################
  //# Data Members
  //private final ModuleContainer mModuleContainer;
  private final ProductDESProxy mCompiledDES;
  private final Object[][] mRawData;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
