package org.supremica.gui.ide;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import net.sourceforge.waters.model.module.ModuleProxy;


public class AbstractTunnelTable extends AbstractTableModel
{

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private static DocumentContainer data;

  public AbstractTunnelTable(DocumentContainer data)
  {
    this.data= data;
  }

  public int getColumnCount()
  {
    return 10;
  }

  public int getRowCount()
  {
    return 10;
  }

  public Object getValueAt(int row, int col)
  {
    if (col == 0) {
      Class interfaceClass = data.getDocument().getProxyInterface();
      if (interfaceClass == ModuleProxy.class){
        ModuleProxy castData = ((ModuleProxy)data.getDocument());
        // return the name of the automaton at row's offset
        throw new UnsupportedOperationException();
      }
    }
    throw new UnsupportedOperationException();
  }
}
