package net.sourceforge.waters.gui.simulator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
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
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.subject.module.VariableComponentSubject;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.gui.ide.ModuleContainer;

public class EventJTree extends JTree implements InternalFrameObserver, SimulationObserver, ComponentListener
{

  public EventJTree(final Simulation sim, final AutomatonDesktopPane desktop, final ModuleContainer container)
  {
    super();
    this.setCellRenderer(new EventTreeCellRenderer());
    mSim = sim;
    mDesktop = desktop;
    mPane = null;
    desktop.attach(this);
    sim.attach(this);
    automatonAreOpen = new ArrayList<String>();
    mContainer = container;
    mSortingMethods = new ArrayList<Pair<Boolean, Integer>>();
    expandedNodes = new ArrayList<String>();
    this.setModel(new EventTreeModel(mSim));
    this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    setRootVisible(false);
    setShowsRootHandles(true);
    setAutoscrolls(true);
    setToggleClickCount(0);
    // Expand all foreach-component entries.

    this.addMouseListener(new MouseAdapter(){
      public void mouseClicked(final MouseEvent e)
      {
        if (e.getClickCount() == 2)
        {
          final TreePath path = EventJTree.this.getClosestPathForLocation((int)e.getPoint().getX(), (int)e.getPoint().getY());
          final Object node = path.getLastPathComponent();
          if (node == null)
            return; // Nothing is selected
          if (EventProxy.class.isInstance(node))
          {
            try {
              if (sim.getValidTransitions().contains((EventProxy)node))
                sim.step((EventProxy)node);
              else
                System.out.println("ERROR: That event is blocked");
            } catch (final UncontrollableException exception) {
              System.out.println("ERROR: Uncontrollable Event detected: " + exception.getMessage() + ". No event has been fired.");
            } catch (final IllegalArgumentException exception) {
              System.out.println(exception.getMessage() + ". No event has been fired");
            }
          }
          else if (AutomatonProxy.class.isInstance(node))
          {
            final AutomatonProxy toAdd = (AutomatonProxy)node;
            mDesktop.addAutomaton(toAdd.getName(), mSim.getContainer(), mSim, 2);
          }
        }
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

  public void addPane(final JScrollPane pane)
  {
    mPane = pane;
  }

  // ##################################################################
  // # Simple Access

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
    this.setModel(new EventTreeModel(mSim));
    for (int looper = 0; looper < expandedNodes.size(); looper++)
    {
      final String name = expandedNodes.get(looper);
      for (int nodeIndex = 0; nodeIndex < mSim.getAllEvents().size(); nodeIndex++)
      {
        if (mSim.getAllEvents().get(nodeIndex).getName().compareTo(name) == 0)
        {
          this.expandPath(new TreePath(new Object[]{mSim, mSim.getAllEvents().get(nodeIndex)}));
        }
      }
    }
  }

  public void simulationChanged(final SimulationChangeEvent event)
  {
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

  // ########################################################################
  // # Inner Classes

  private class EventTreeCellRenderer
  implements TreeCellRenderer
  {
    //#######################################################################
    //# Constructor
    private EventTreeCellRenderer()
    {
    }

    //#######################################################################
    //# Interface javax.swing.tree.TreeCellRenderer
    public Component getTreeCellRendererComponent
      (final JTree tree, final Object value, final boolean sel,
       final boolean expanded, final boolean leaf,
       final int row, final boolean hasFocus)
    {
      panel  = new JPanel();
      if (sel)
        panel.setBackground(EditorColor.BACKGROUND_FOCUSSED);
      else
        panel.setBackground(EditorColor.BACKGROUNDCOLOR);
      if (EventProxy.class.isInstance(value))
      {
        final GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        final EventProxy event = (EventProxy)value;
        left = new JLabel(event.getName());
        if (event.getKind() == EventKind.CONTROLLABLE)
          left.setIcon(IconLoader.ICON_CONTROLLABLE);
        else
          left.setIcon(IconLoader.ICON_UNCONTROLLABLE);
        right = new JLabel();
        if (mSim.getValidTransitions().contains(event))
          right.setIcon(IconLoader.ICON_TICK);
        else
          right.setIcon(IconLoader.ICON_CROSS);
        final int width = mPane.getWidth();
        final int rightWidth = (width * eventColumnWidth[1] - noduleWidth * eventColumnWidth[1]) / (sum(eventColumnWidth));
        final int leftWidth = (width * eventColumnWidth[0] - noduleWidth * eventColumnWidth[0]) / (sum(eventColumnWidth));
        left.setPreferredSize(new Dimension(leftWidth, rowHeight));
        right.setPreferredSize(new Dimension(rightWidth, rowHeight));
        panel.add(left);
        panel.add(right);
        return panel;
      }
      else if (AutomatonProxy.class.isInstance(value))
      {
        final AutomatonProxy autoProxy = (AutomatonProxy)value;
        final GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        left = new JLabel(autoProxy.getName());
        if (mContainer.getSourceInfoMap().get(autoProxy).getSourceObject().getClass() == VariableComponentSubject.class)
          left.setIcon(IconLoader.ICON_VARIABLE);
        else
          left.setIcon(ModuleContext.getComponentKindIcon(autoProxy.getKind()));
        center = new JLabel();
        //if (mSim.getBlocking(((EventBranchNode)autoNode.getParent()).getEvent()).contains(autoProxy))
        //  center.setIcon(IconLoader.ICON_CROSS);
        //else
        center.setIcon(IconLoader.ICON_TICK); // TODO: Determine what event is this automatons parent
        StateProxy currentState;
        currentState = mSim.getCurrentStates().get(autoProxy);
        right = new JLabel(currentState.getName());
        right.setIcon(mSim.getMarkingIcon(currentState, autoProxy));
        final int width = mPane.getWidth();
        final int rightWidth = (width * automataColumnWidth[2] - 2 * noduleWidth * automataColumnWidth[2]) / (sum(automataColumnWidth));
        final int centerWidth = (width * automataColumnWidth[1] - 2 * noduleWidth * automataColumnWidth[1]) / (sum(automataColumnWidth));
        final int leftWidth = (width * automataColumnWidth[0] - 2 * noduleWidth * automataColumnWidth[0]) / (sum(automataColumnWidth));
        left.setPreferredSize(new Dimension(leftWidth, rowHeight));
        center.setPreferredSize(new Dimension(centerWidth, rowHeight));
        right.setPreferredSize(new Dimension(rightWidth, rowHeight));
        layout.columnWidths = new int[]{leftWidth, centerWidth, rightWidth};
        if (automatonAreOpen.contains(autoProxy.getName()))
        {
          left.setFont(left.getFont().deriveFont(Font.BOLD));
          right.setFont(right.getFont().deriveFont(Font.BOLD));
        }
        else
        {
          left.setFont(left.getFont().deriveFont(Font.PLAIN));
          right.setFont(right.getFont().deriveFont(Font.PLAIN));
        }
        panel.add(left);
        panel.add(center);
        panel.add(right);
        return panel;
      }
      else
      {
        return new JPanel();
      }
    }

    private int sum (final int[] a)
    {
      int o = 0;
      for (int i = 0; i < a.length; i++)
        o+=a[i];
      return o;
    }

    // ###########################################################################
    // # Data Members

    private JPanel panel;
    private JLabel left;
    private JLabel right;
    private JLabel center;

    // ###########################################################################
    // # Class Constants
    private static final long serialVersionUID = 6788022446662090661L;
  }

  private final AutomatonDesktopPane mDesktop;
  private final Simulation mSim;
  private final ModuleContainer mContainer;
  private final ArrayList<String> automatonAreOpen;
  private final ArrayList<Pair<Boolean, Integer>> mSortingMethods;
  private final ArrayList<String> expandedNodes;
  private JScrollPane mPane;

  private static final long serialVersionUID = -4373175227919642063L;
  private static final int[] automataColumnWidth = {110, 20, 60};
  private static final int[] eventColumnWidth = {180, 20};
  private static final int noduleWidth = 30;
  public static final int rowHeight = 20;

}
