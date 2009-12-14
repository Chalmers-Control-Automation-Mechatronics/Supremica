package net.sourceforge.waters.gui.simulator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.sourceforge.waters.gui.IconLoader;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.subject.module.VariableComponentSubject;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.gui.ide.ModuleContainer;

public class EventJTree extends JTree implements InternalFrameObserver
{

  public EventJTree(final Simulation sim, final AutomatonDesktopPane desktop, final ModuleContainer container)
  {
    super();
    this.setCellRenderer(new EventTreeCellRenderer());
    mSim = sim;
    mDesktop = desktop;
    mPane = null;
    desktop.attach(this);
    automatonAreOpen = new ArrayList<String>();
    mContainer = container;
    mSortingMethods = new ArrayList<Pair<Boolean, Integer>>();
    selectedEvents = new ArrayList<EventProxy>();
    final EventMutableTreeNode root = new EventMutableTreeNode(sim, this, mSortingMethods);
    this.setModel(new DefaultTreeModel(root, false));
    this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    setRootVisible(false);
    setShowsRootHandles(true);
    setAutoscrolls(true);
    setToggleClickCount(0);
    totalEventWidth = 0;
    for (final Integer intVal : eventColumnWidth)
    {
      totalEventWidth += intVal;
    }
    // Expand all foreach-component entries.

    this.addMouseListener(new MouseAdapter(){
      public void mouseClicked(final MouseEvent e)
      {
        if (e.getClickCount() == 2)
        {
          final TreePath path = EventJTree.this.getClosestPathForLocation((int)e.getPoint().getX(), (int)e.getPoint().getY());
          final MutableTreeNode node = (MutableTreeNode)path.getLastPathComponent();
          if (node == null)
            return; // Nothing is selected
          if (node.getClass() == EventBranchNode.class)
          {
            try {
              if (sim.getValidTransitions().contains(((EventBranchNode)node).getEvent()))
                sim.step(((EventBranchNode)node).getEvent());
              else
                System.out.println("ERROR: That event is blocked");
            } catch (final UncontrollableException exception) {
              System.out.println("ERROR: Uncontrollable Event detected: " + exception.getMessage() + ". No event has been fired.");
            } catch (final IllegalArgumentException exception) {
              System.out.println(exception.getMessage() + ". No event has been fired");
            }
          }
          else if (node.getClass() == AutomatonLeafNode.class)
          {
            final AutomatonProxy toAdd = ((AutomatonLeafNode)node).getAutomata();
            mDesktop.addAutomaton(toAdd.getName(), mSim.getContainer(), mSim, 2);
          }
        }
      }
    });
  }

  public void addScrollPane(final JScrollPane scroll)
  {
    mPane = scroll;
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
  }

  public void addSelectedEvent(final EventProxy event)
  {
    if (!selectedEvents.contains(event))
      selectedEvents.add(event);
    repaint();
  }

  public void removeSelectedEvent(final EventProxy event)
  {
    if (selectedEvents.contains(event))
      selectedEvents.remove(event);
    repaint();
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
  // # Auxillary Classes

  public void forceRecalculation()
  {
    final EventMutableTreeNode node = new EventMutableTreeNode(mSim, this, mSortingMethods);
    this.setModel(new DefaultTreeModel(node, false));
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
      //final JPanel output = (JPanel) ((EventTreeCellRenderer)super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)).getComponent(0);
      panel  = new JPanel();

      if (value.getClass() == EventBranchNode.class)
      {
        final GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        final EventBranchNode eventNode = (EventBranchNode)value;
        final EventProxy event = eventNode.getEvent();
        left = new JLabel(event.getName());
        if (event.getKind() == EventKind.CONTROLLABLE)
          left.setIcon(IconLoader.ICON_CONTROLLABLE);
        else
          left.setIcon(IconLoader.ICON_UNCONTROLLABLE);
        if (selectedEvents.contains(event))
          left.setFont(left.getFont().deriveFont(Font.BOLD));
        else
          left.setFont(left.getFont().deriveFont(Font.PLAIN));
        right = new JLabel();
        if (mSim.getValidTransitions().contains(event))
          right.setIcon(IconLoader.ICON_TICK);
        else
          right.setIcon(IconLoader.ICON_CROSS);
        final int width = mPane.getWidth();
        final int rightWidth = (width - noduleWidth) / (1 + (eventColumnWidth[0] / eventColumnWidth[1]));
        final int leftWidth = (eventColumnWidth[0] / eventColumnWidth[1]) * rightWidth;
        layout.columnWidths = new int[]{leftWidth, rightWidth};
        System.out.println("DEBUG: Width: " + mPane.getWidth()
            + " Left width: " + leftWidth
            + " Right width: " + rightWidth);
        left.setPreferredSize(new Dimension(leftWidth, rowHeight));
        right.setPreferredSize(new Dimension(rightWidth, rowHeight));
        panel.add(left);
        panel.add(right);
        return panel;
      }
      else if (value.getClass() == AutomatonLeafNode.class)
      {
        final AutomatonLeafNode autoNode = (AutomatonLeafNode) value;
        final AutomatonProxy autoProxy = autoNode.getAutomata();
        final GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = automataColumnWidth;
        panel.setLayout(layout);
        left = new JLabel(autoProxy.getName());
        if (mContainer.getSourceInfoMap().get(autoProxy).getSourceObject().getClass() == VariableComponentSubject.class)
          left.setIcon(IconLoader.ICON_VARIABLE);
        else
          left.setIcon(ModuleContext.getComponentKindIcon(autoProxy.getKind()));
        center = new JLabel();
        if (mSim.getBlocking(((EventBranchNode)autoNode.getParent()).getEvent()).contains(autoProxy))
          center.setIcon(IconLoader.ICON_CROSS);
        else
          center.setIcon(IconLoader.ICON_TICK);
        StateProxy currentState;
        currentState = mSim.getCurrentStates().get(autoProxy);
        right = new JLabel(currentState.getName());
        right.setIcon(mSim.getMarkingIcon(currentState, autoProxy));
        left.setPreferredSize(new Dimension(automataColumnWidth[0], rowHeight));
        center.setPreferredSize(new Dimension(automataColumnWidth[1], rowHeight));
        right.setPreferredSize(new Dimension(automataColumnWidth[2], rowHeight));
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
  private final ArrayList<EventProxy> selectedEvents;
  private JScrollPane mPane;

  private static final long serialVersionUID = -4373175227919642063L;
  private static final int[] automataColumnWidth = {110, 20, 60};
  private static int totalEventWidth;
  private static final int[] eventColumnWidth = {180, 20};
  private static final int noduleWidth = 20;
  public static final int rowHeight = 20;

}
