package org.supremica.gui.ide;

import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.ImageIcon;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;


public class AbstractTunnelTable extends AbstractTableModel
{

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private static ModuleContainer data;
  private static Object[][] rawData;

  public AbstractTunnelTable(ModuleContainer data)
  {
    this.data= data;
    Class interfaceClass = data.getDocument().getProxyInterface();
    Object[][] rawData = null;
    if (interfaceClass == ModuleProxy.class){
      ModuleProxy castData = (ModuleProxy)data.getDocument();
      rawData = getRawData(castData);
    }
    Object[] titles = new Object[]{"Name", "Active", "State Type", "State Name"};
    this.rawData = rawData;
  }

  public int getColumnCount()
  {
    return 4;
  }

  public int getRowCount()
  {
    Class interfaceClass = data.getDocument().getProxyInterface();
    if (interfaceClass == ModuleProxy.class){
      ModuleProxy castData = ((ModuleProxy)data.getDocument());
      List<Proxy> componentList = castData.getComponentList();
      int count = 0;
      for (Proxy automaton : componentList)
      {
        if (automaton.getClass() == SimpleComponentSubject.class){
          count++;
        }
      }
      return count;
    }
    return 0;
  }

  private Object[][] getRawData(ModuleProxy data)
  {
    Object[][] output = new Object[getRowCount()][getColumnCount()];
    SimpleComponentSubject current = null;
    ImageIcon error = new ImageIcon("images/icons/waters/ForbiddenState16.gif");
    for (int looper = 0; looper < output.length; looper++)
    {
      current = getNextProxy(current, data.getComponentList());
      output[looper][0] = current.getName();
      output[looper][1] = error;
      output[looper][2] = error;
      output[looper][3] = error;
    }
    return output;
  }

  private SimpleComponentSubject getNextProxy(SimpleComponentSubject oldProxy, List<Proxy> componentList)
  {
    int oldIndex = componentList.indexOf(oldProxy);
    SimpleComponentProxy first = null;
    if (oldIndex != -1)
    {
      System.out.println("DEBUG: Successfully Located");
      for (int looper = oldIndex + 1; looper < componentList.size(); looper++)
      {
        if (componentList.get(looper).getProxyInterface() == SimpleComponentProxy.class)
        {
          return (SimpleComponentSubject)componentList.get(looper);
        }
      }
      return null;
    }
    else
    {
      for (int looper = 0; looper < componentList.size(); looper++)
      {
        if (componentList.get(looper).getProxyInterface() == SimpleComponentProxy.class)
        {
          return (SimpleComponentSubject)componentList.get(looper);
        }
      }
      return null;
    }
  }

  public Object getValueAt(int row, int col)
  {
    return rawData[row][col];
  }
}
