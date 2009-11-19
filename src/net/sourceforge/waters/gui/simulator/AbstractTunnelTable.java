package net.sourceforge.waters.gui.simulator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.PropositionIcon;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;

import org.supremica.gui.ide.ModuleContainer;


public class AbstractTunnelTable extends AbstractTableModel implements Observer
{

  //#########################################################################
  //# Constructor
  public AbstractTunnelTable(final ModuleContainer container)
  {
    mCompiledDES = null;
    mRawData = getRawData();
    mModuleContainer = container;
    mModuleContainer.attach(this);
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
    if (mCompiledDES == null) {
      return 0;
    } else {
      return mCompiledDES.getAutomata().size();
    }
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
    mCompiledDES = mModuleContainer.getCompiledDES();
    mSim = new Simulation(mModuleContainer);
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
    if (mSim != null && mModuleContainer != null)
    {
      final Object[][] output = new Object[getRowCount()][getColumnCount()];
      final Set<AutomatonProxy> automata = mSim.getCurrentStates().keySet();
      int looper = 0;
      for (final AutomatonProxy aut : automata) {
        output[looper][0] = aut.getName();
        output[looper][1] = "X";
        StateProxy currentState = mSim.getCurrentStates().get(aut);
        output[looper][2] = getStateIcon(currentState);
        output[looper][3] = mSim.getCurrentStates().get(aut).getName();
        looper++;
      }
      return output;
    }
    else
      return new Object[0][0];
  }

  private Icon getStateIcon(final StateProxy state)
  {
    final Collection<EventProxy> props = state.getPropositions();
    if (props.isEmpty()) {
      return PropositionIcon.getUnmarkedIcon();
    } else {
      final Map<Proxy,SourceInfo> infomap = mModuleContainer.getSourceInfoMap();
      final int size = props.size();
      final Set<Color> colorset = new HashSet<Color>(size);
      final List<Color> colorlist = new ArrayList<Color>(size);
      boolean forbidden = false;
      for (final EventProxy prop : props) {
        final SourceInfo info = infomap.get(prop);
        final EventDeclProxy decl = (EventDeclProxy) info.getSourceObject();
        final ColorGeometryProxy geo = decl.getColorGeometry();
        if (geo != null) {
          for (final Color color : geo.getColorSet()) {
            if (colorset.add(color)) {
              colorlist.add(color);
            }
          }
        } else if (decl.getName().equals
            (EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
          forbidden = true;
        } else {
          if (colorset.add(EditorColor.DEFAULTMARKINGCOLOR)) {
            colorlist.add(EditorColor.DEFAULTMARKINGCOLOR);
          }
        }
      }
      final PropositionIcon.ColorInfo colorinfo =
        new PropositionIcon.ColorInfo(colorlist, forbidden);
      return colorinfo.getIcon();
    }
  }


  //#########################################################################
  //# Data Members
  //private final ModuleContainer mModuleContainer;
  private ProductDESProxy mCompiledDES;
  private Object[][] mRawData;
  private final ModuleContainer mModuleContainer;
  private Simulation mSim;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;



}
