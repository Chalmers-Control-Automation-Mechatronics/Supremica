package net.sourceforge.waters.gui.simulator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.xsd.des.Automaton;

import org.supremica.gui.Supremica;
import org.supremica.gui.ide.ModuleContainer;
import org.xml.sax.SAXException;


public class AbstractTunnelTable extends AbstractTableModel
{

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private final ModuleContainer data;
  private final Object[][] rawData;

  public AbstractTunnelTable(ModuleContainer data)
  {
    this.data= data;
    Object[][] rawData = null;
    ProductDESProxy convertedData = null;
    try {
      convertedData = compile(data);
    } catch (WatersUnmarshalException exception) {
      // TODO Auto-generated catch block
      exception.printStackTrace();
    } catch (EvalException exception) {
      // TODO Auto-generated catch block
      exception.printStackTrace();
    } catch (IOException exception) {
      // TODO Auto-generated catch block
      exception.printStackTrace();
    } catch (JAXBException exception) {
      // TODO Auto-generated catch block
      exception.printStackTrace();
    } catch (SAXException exception) {
      // TODO Auto-generated catch block
      exception.printStackTrace();
    }
    if (convertedData != null)
    {
      rawData = getRawData(convertedData);
      this.rawData = rawData;
    }
    else
    {
      this.rawData = null;
    }
  }

  public int getColumnCount()
  {
    return 4;
  }

  public int getRowCount()
  {
    try {
      return compile(data).getAutomata().size();
    } catch (WatersUnmarshalException exception) {
      // TODO Auto-generated catch block
      exception.printStackTrace();
    } catch (EvalException exception) {
      // TODO Auto-generated catch block
      exception.printStackTrace();
    } catch (IOException exception) {
      // TODO Auto-generated catch block
      exception.printStackTrace();
    } catch (JAXBException exception) {
      // TODO Auto-generated catch block
      exception.printStackTrace();
    } catch (SAXException exception) {
      // TODO Auto-generated catch block
      exception.printStackTrace();
    }
    return -1;
  }

  private ProductDESProxy compile (ModuleContainer data) throws WatersUnmarshalException, IOException, JAXBException, SAXException, EvalException
  {
    final DocumentManager manager = data.getIDE().getDocumentManager();
    ProductDESElementFactory elementFactory = new ProductDESElementFactory();
    ModuleCompiler compiler = new ModuleCompiler(manager, elementFactory, data.getModule());
    return compiler.compile();
  }

  private Object[][] getRawData(ProductDESProxy data)
  {
    Object[][] output = new Object[getRowCount()][getColumnCount()];
    Set<AutomatonProxy> graph = data.getAutomata();
    int looper = 0;
    for (AutomatonProxy automata : graph)
    {
      output[looper][0] = automata.getName();
      output[looper][1] = ModuleContext.getComponentKindIcon(automata.getKind());
      output[looper][2] = ModuleContext.getComponentKindIcon(automata.getKind());
      output[looper][3] = ModuleContext.getComponentKindIcon(automata.getKind());
      looper++;
    }
    return output;
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

  private SimpleComponentSubject getNextProxy(SimpleComponentSubject oldProxy, List<Proxy> componentList)
  {
    int oldIndex = componentList.indexOf(oldProxy);
    SimpleComponentProxy first = null;
    if (oldIndex != -1)
    {
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
