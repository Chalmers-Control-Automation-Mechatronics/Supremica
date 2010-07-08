package net.sourceforge.waters.gui.simulator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.IconLoader;
import net.sourceforge.waters.model.base.Pair;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.gui.ide.ModuleContainer;

public class EventJTree
  extends JTree
  implements InternalFrameObserver, SimulationObserver, ComponentListener
{

  //#########################################################################
  //# Constructor
  public EventJTree(final Simulation sim,
                    final AutomatonDesktopPane desktop)
  {
    setCellRenderer(new EventTreeCellRenderer());
    mSim = sim;
    mDesktop = desktop;
    mPane = null;
    desktop.attach(this);
    sim.attach(this);
    automatonAreOpen = new ArrayList<String>();
    mSortingMethods = new ArrayList<Pair<Boolean, Integer>>();
    expandedNodes = new ArrayList<String>();
    setModel(new EventTreeModel(mSim, mSortingMethods));
    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    setRootVisible(false);
    setShowsRootHandles(true);
    setAutoscrolls(true);
    setToggleClickCount(0);
    final ModuleContainer container = sim.getModuleContainer();
    factory = new EventTreePopupFactory(container.getIDE().getPopupActionManager(), mDesktop);
    addMouseListener(new MouseListener() {
      public void mouseClicked(final MouseEvent e)
      {
        if (e.getClickCount() == 2) {
          final TreePath path = getClosestPathForLocation((int)e.getPoint().getX(), (int)e.getPoint().getY());
          final Object node = path.getLastPathComponent();
          if (node == null)
            return; // Nothing is selected
          if (node instanceof EventProxy) {
            final EventProxy event = (EventProxy) node;
            mSim.step(event);
          } else if (node instanceof AutomatonProxy) {
            final AutomatonProxy aut = (AutomatonProxy) node;
            mDesktop.addAutomaton(aut.getName(), container, mSim, 2);
          }
        }
      }

      public void mousePressed(final MouseEvent e)
      {
        if (EventJTree.this.getPathForLocation(e.getX(), e.getY()) != null)
        {
          factory.maybeShowPopup(EventJTree.this, e, (Proxy)EventJTree.this.getPathForLocation(e.getX(), e.getY()).getLastPathComponent());
        }
      }

      public void mouseEntered(final MouseEvent e)
      {
        // Do nothing
      }

      public void mouseExited(final MouseEvent e)
      {
        // Do nothing
      }

      public void mouseReleased(final MouseEvent e)
      {
        // Do nothing
      }
    });
    this.addMouseMotionListener(new MouseMotionListener(){
      public void mouseMoved(final MouseEvent e)
      {
        final TreePath path = EventJTree.this.getClosestPathForLocation(e.getX(), e.getY());
        final Object comp = path.getLastPathComponent();
        final Proxy proxy = (Proxy) comp;
        final ToolTipVisitor visitor = mSim.getToolTipVisitor();
        final String tooltip = visitor.getToolTip(proxy, true);
        setToolTipText(tooltip);
      }

      public void mouseDragged(final MouseEvent e)
      {
        // Do nothing
      }
    });
    this.addTreeWillExpandListener(new TreeWillExpandListener(){

      public void treeWillCollapse(final TreeExpansionEvent event)
          throws ExpandVetoException
      {
        if (EventProxy.class.isInstance(event.getPath().getLastPathComponent()))
        {
          expandedNodes.remove(((EventProxy)event.getPath().getLastPathComponent()).getName());
        }
      }

      public void treeWillExpand(final TreeExpansionEvent event)
          throws ExpandVetoException
      {
        if (EventProxy.class.isInstance(event.getPath().getLastPathComponent()))
        {
          for (final String name : expandedNodes)
          {
            if (((EventProxy)event.getPath().getLastPathComponent()).getName().compareTo(name) == 0)
              return;
          }
          expandedNodes.add(((EventProxy)event.getPath().getLastPathComponent()).getName());
        }
      }
    });
  }

  //########################################################################
  //# Access
  public void addPane(final JScrollPane pane)
  {
    mPane = pane;
    mPane.addComponentListener(this);
  }

  public void sortBy(final int index)
  {
    int oldIndex = -1;
    boolean oldAscending = false;
    for (int looper = 0; looper < mSortingMethods.size(); looper++)
    {
      if (mSortingMethods.get(looper).getSecond() == index)
      {
        oldIndex = looper;
        oldAscending = mSortingMethods.get(looper).getFirst();
      }
    }
    if (oldIndex != -1)
    {
      if (oldIndex != 0)
      {
        final Pair<Boolean, Integer> oldMethod = mSortingMethods.get(oldIndex);
        mSortingMethods.remove(oldIndex);
        mSortingMethods.add(0, oldMethod);
      }
      else
      {
        final Pair<Boolean, Integer> newMethod = new Pair<Boolean, Integer>(!oldAscending, index);
        mSortingMethods.remove(oldIndex);
        mSortingMethods.add(0, newMethod);
      }
    }
    else
    {
      final Pair<Boolean, Integer> newMethod = new Pair<Boolean, Integer>(true, index);
      mSortingMethods.add(0, newMethod);
    }
    forceRecalculation();
  }

  // ##################################################################
  // # Interface SimulationObserver

  public void forceRecalculation()
  {
    //final long time = System.currentTimeMillis();
    final EventTreeModel model = new EventTreeModel(mSim, mSortingMethods);
    this.setModel(model);
    for (int looper = 0; looper < expandedNodes.size(); looper++)
    {
      final String name = expandedNodes.get(looper);
      for (int nodeIndex = 0; nodeIndex < mSim.getOrderedEvents().size(); nodeIndex++)
      {
        if (mSim.getOrderedEvents().get(nodeIndex).getName().compareTo(name) == 0)
        {
          this.expandPath(new TreePath(new Object[]{mSim, mSim.getOrderedEvents().get(nodeIndex)}));
        }
      }
    }
  }

  public void simulationChanged(final SimulationChangeEvent event)
  {
    // Any event can change the order, so ALWAYS redraw the entire graph
    forceRecalculation();
  }

  // #################################################################
  // # Interface ComponentListener

  public void componentHidden(final ComponentEvent e)
  {
    // Do nothing
  }

  public void componentMoved(final ComponentEvent e)
  {
    // Do nothing
  }

  public void componentResized(final ComponentEvent e)
  {
    forceRecalculation();
  }

  public void componentShown(final ComponentEvent e)
  {
    forceRecalculation();
  }

  //##################################################################
  // # Interface InternalFrameObserver

  public void onFrameEvent(final InternalFrameEvent event)
  {
    if (event.isOpeningEvent())
    {
      if (!automatonAreOpen.contains(event.getName()))
      {
        automatonAreOpen.add(event.getName());
      }
    }
    else
    {
      if (automatonAreOpen.contains(event.getName()))
      {
        automatonAreOpen.remove(event.getName());
      }
    }
    repaint();
  }


  //#########################################################################
  //# Inner Class EventTreeCellRenderer
  private class EventTreeCellRenderer
    implements TreeCellRenderer
  {

    //#######################################################################
    //# Constructor
    private EventTreeCellRenderer()
    {
      mEventPanel = new JPanel();
      final FlowLayout layout = new FlowLayout(FlowLayout.LEADING, 0, 0);
      mEventPanel.setLayout(layout);
      mEventNameLabel = new JLabel();
      mEventNameLabel.setFont(mEventNameLabel.getFont().deriveFont(Font.PLAIN));
      mEventStatusLabel = new JLabel();
      mEventPanel.add(mEventNameLabel);
      mEventPanel.add(mEventStatusLabel);
      mAutomatonPanel = new JPanel();
      mAutomatonPanel.setLayout(layout);
      mAutomatonNameLabel = new JLabel();
      mAutomatonIconLabel = new JLabel();
      mAutomatonStatusLabel = new JLabel();
      mAutomatonPanel.add(mAutomatonNameLabel);
      mAutomatonPanel.add(mAutomatonIconLabel);
      mAutomatonPanel.add(mAutomatonStatusLabel);
    }

    //#######################################################################
    //# Interface javax.swing.tree.TreeCellRenderer
    public Component getTreeCellRendererComponent
      (final JTree tree, final Object value, final boolean sel,
       final boolean expanded, final boolean leaf,
       final int row, final boolean hasFocus)
    {
       if (value instanceof EventProxy) {
         if (sel)
           mEventPanel.setBackground(EditorColor.BACKGROUND_FOCUSSED);
         else
           mEventPanel.setBackground(EditorColor.BACKGROUNDCOLOR);
        final EventProxy event = (EventProxy)value;
        if (event.getKind() == EventKind.CONTROLLABLE)
          mEventNameLabel.setIcon(IconLoader.ICON_CONTROLLABLE);
        else
          mEventNameLabel.setIcon(IconLoader.ICON_UNCONTROLLABLE);
        mEventNameLabel.setText(event.getName());
        final Icon eventActivityIcon = mSim.getEventActivityIcon(event);
        mEventStatusLabel.setIcon(eventActivityIcon);
        final int width = mPane.getViewport().getWidth();
        final int rightWidth = eventColumnWidth[1];
        final int leftWidth = width - rightWidth - noduleWidth;
        mEventPanel.setPreferredSize(new Dimension(width, rowHeight));
        mEventNameLabel.setPreferredSize(new Dimension(leftWidth, rowHeight));
        mEventStatusLabel.setPreferredSize(new Dimension(rightWidth, rowHeight));
        return mEventPanel;
      } else if (value instanceof AutomatonProxy) {
        if (sel) {
          mAutomatonPanel.setBackground(EditorColor.BACKGROUND_FOCUSSED);
        } else {
          mAutomatonPanel.setBackground(EditorColor.BACKGROUNDCOLOR);
        }
        final AutomatonProxy aut = (AutomatonProxy) value;
        mAutomatonNameLabel.setText(aut.getName());
        final Icon autIcon = AutomatonLeafNode.getAutomatonIcon(mSim, aut);
        mAutomatonNameLabel.setIcon(autIcon);
        if (mSim.getAutomatonStatus(aut) == AutomatonStatus.DISABLED) {
          mAutomatonIconLabel.setIcon(null);
          mAutomatonStatusLabel.setText("");
          mAutomatonStatusLabel.setIcon(null);
        } else {
          final EventProxy event = getParentEvent(row);
          final EventStatus status = mSim.getEventStatus(event, aut);
          final Icon statusIcon = status.getIcon();
          mAutomatonIconLabel.setIcon(statusIcon);
          final StateProxy currentState = mSim.getCurrentState(aut);
          mAutomatonStatusLabel.setText(currentState.getName());
          mAutomatonStatusLabel.setIcon(mSim.getMarkingIcon(currentState, aut, false));
        }
        final int width = mPane.getWidth();
        final int rightWidth = (width * automataColumnWidth[2] - 2 * noduleWidth * automataColumnWidth[2]) / (sum(automataColumnWidth));
        final int centerWidth = (width * automataColumnWidth[1] - 2 * noduleWidth * automataColumnWidth[1]) / (sum(automataColumnWidth));
        final int leftWidth = (width * automataColumnWidth[0] - 2 * noduleWidth * automataColumnWidth[0]) / (sum(automataColumnWidth));
        mAutomatonNameLabel.setPreferredSize(new Dimension(leftWidth, rowHeight));
        mAutomatonIconLabel.setPreferredSize(new Dimension(centerWidth, rowHeight));
        mAutomatonStatusLabel.setPreferredSize(new Dimension(rightWidth, rowHeight));
        if (automatonAreOpen.contains(aut.getName())) {
          mAutomatonNameLabel.setFont(mAutomatonNameLabel.getFont().deriveFont(Font.BOLD));
          mAutomatonStatusLabel.setFont(mAutomatonStatusLabel.getFont().deriveFont(Font.BOLD));
        } else {
          mAutomatonNameLabel.setFont(mAutomatonNameLabel.getFont().deriveFont(Font.PLAIN));
          mAutomatonStatusLabel.setFont(mAutomatonStatusLabel.getFont().deriveFont(Font.PLAIN));
        }
        return mAutomatonPanel;
      } else {
        return new JPanel();
      }
    }

    private int sum(final int[] a)
    {
      int o = 0;
      for (int i = 0; i < a.length; i++) {
        o += a[i];
      }
      return o;
    }

    private EventProxy getParentEvent(int row)
    {
      if (row >= getRowCount()) {
        row = getRowCount() - 1;
      }
      final TreePath path = getPathForRow(row);
      return (EventProxy) path.getPathComponent(1);
    }

    //#######################################################################
    //# Data Members
    private final JPanel mEventPanel;
    private final JLabel mEventNameLabel;
    private final JLabel mEventStatusLabel;
    private final JPanel mAutomatonPanel;
    private final JLabel mAutomatonNameLabel;
    private final JLabel mAutomatonIconLabel;
    private final JLabel mAutomatonStatusLabel;

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 6788022446662090661L;
  }


  //#########################################################################
  //# Data Members
  private final AutomatonDesktopPane mDesktop;
  private final Simulation mSim;
  private final ArrayList<String> automatonAreOpen;
  private final ArrayList<Pair<Boolean, Integer>> mSortingMethods;
  private final ArrayList<String> expandedNodes;
  private JScrollPane mPane;
  private final EventTreePopupFactory factory;


  //#########################################################################
  //# Class Constants
  static final int rowHeight = 20;

  private static final long serialVersionUID = -4373175227919642063L;
  private static final int[] automataColumnWidth = {110, 20, 60};
  private static final int[] eventColumnWidth = {200, 20};
  private static final int noduleWidth = 30;

}
