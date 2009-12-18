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
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.MutableTreeNode;
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

public class TraceJTree extends JTree implements InternalFrameObserver, ComponentListener
{
  public TraceJTree(final Simulation sim, final AutomatonDesktopPane desktop, final ModuleContainer container)
  {
    super();
    this.setCellRenderer(new TraceTreeCellRenderer());
    mSim = sim;
    mDesktop = desktop;
    mPane = null;
    desktop.attach(this);
    automatonAreOpen = new ArrayList<String>();
    mContainer = container;
    expandedNodes = new ArrayList<String>();
    final TraceMutableTreeNode root = new TraceMutableTreeNode(sim, this);
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
    this.addMouseListener(new MouseAdapter(){
      public void mouseClicked(final MouseEvent e)
      {
        if (e.getClickCount() == 2)
        {
          final TreePath path = TraceJTree.this.getClosestPathForLocation((int)e.getPoint().getX(), (int)e.getPoint().getY());
          final MutableTreeNode node = (MutableTreeNode)path.getLastPathComponent();
          if (node == null)
            return; // Nothing is selected
          if (node.getClass() == EventBranchNode.class)
          {
            final EventBranchNode eventNode = (EventBranchNode)node;
            final int targetTime = eventNode.getTime();
            int currentTime = sim.getCurrentTime();
            while (currentTime != targetTime)
            {
              if (targetTime < currentTime)
              {
                sim.stepBack();
                currentTime--;
              }
              else if (targetTime > currentTime)
              {
                sim.replayStep();
                currentTime++;
              }
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
    this.addTreeWillExpandListener(new TreeWillExpandListener(){

      public void treeWillCollapse(final TreeExpansionEvent event)
          throws ExpandVetoException
      {
        if (event.getPath().getLastPathComponent().getClass() == EventBranchNode.class)
        {
          expandedNodes.remove(((EventBranchNode)event.getPath().getLastPathComponent()).getEvent().getName());
        }
      }

      public void treeWillExpand(final TreeExpansionEvent event)
          throws ExpandVetoException
      {
        if (event.getPath().getLastPathComponent().getClass() == EventBranchNode.class)
        {
          for (final String name : expandedNodes)
          {
            if (((EventBranchNode)event.getPath().getLastPathComponent()).getEvent().getName().compareTo(name) == 0)
              return;
          }
          expandedNodes.add(((EventBranchNode)event.getPath().getLastPathComponent()).getEvent().getName());
          ((EventBranchNode)event.getPath().getLastPathComponent()).addAutomata(mSim,
              mSim.getAutomatonHistory().get((((EventBranchNode)event.getPath().getLastPathComponent()).getTime())));
        }
      }
    });
  }

  public void addScrollPane(final JScrollPane scroll)
  {
    mPane = scroll;
    mPane.addComponentListener(this);
  }

  // ############################################################################
  // # Simple Access

  public void forceRecalculation()
  {
    final TraceMutableTreeNode node = new TraceMutableTreeNode(mSim, this);
    this.setModel(new DefaultTreeModel(node, false));
    for (int looper = 0; looper < expandedNodes.size(); looper++)
    {
      final String name = expandedNodes.get(looper);
      for (int nodeIndex = 0; nodeIndex < node.getChildCount(); nodeIndex++)
      {
        if (((EventBranchNode)node.getChildAt(nodeIndex)).getEvent().getName().compareTo(name) == 0)
        {
          ((EventBranchNode)node.getChildAt(nodeIndex)).addAutomata(mSim,
              mSim.getAutomatonHistory().get(((EventBranchNode)node.getChildAt(nodeIndex)).getTime()));
          this.expandPath(new TreePath(((EventBranchNode)node.getChildAt(nodeIndex)).getPath()));
        }
      }
    }
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

  //########################################################################
  // # Interface ComponentListener
  public void componentHidden(final ComponentEvent e)
  {
    //Do nothing
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

  // ############################################################################
  // # Inner Classes

  private class TraceTreeCellRenderer
  implements TreeCellRenderer
  {
    //#######################################################################
    //# Constructor
    private TraceTreeCellRenderer()
    {
    }

    //#######################################################################
    //# Interface javax.swing.tree.TreeCellRenderer
    public Component getTreeCellRendererComponent
    (final JTree tree, final Object value, final boolean sel,
        final boolean expanded, final boolean leaf,
        final int row, final boolean hasFocus)
     {
      panel = new JPanel();
      if (sel)
        panel.setBackground(EditorColor.BACKGROUND_FOCUSSED);
      else
        panel.setBackground(EditorColor.BACKGROUNDCOLOR);
       if (value.getClass() == EventBranchNode.class)
       {
         final GridBagLayout layout = new GridBagLayout();
         panel.setLayout(layout);
         final EventBranchNode eventNode = (EventBranchNode)value;
         left = new JLabel(String.valueOf(eventNode.getTime() + 1));
         final EventProxy event = eventNode.getEvent();
         right = new JLabel(event.getName());
         if (event.getKind() == EventKind.CONTROLLABLE)
           right.setIcon(IconLoader.ICON_CONTROLLABLE);
         else
           right.setIcon(IconLoader.ICON_UNCONTROLLABLE);
         if (eventNode.getTime() == TraceJTree.this.mSim.getCurrentTime())
         {
           right.setFont(right.getFont().deriveFont(Font.BOLD));
           left.setFont(left.getFont().deriveFont(Font.BOLD));
         }
         else
         {
           right.setFont(right.getFont().deriveFont(Font.PLAIN));
           left.setFont(left.getFont().deriveFont(Font.PLAIN));
         }
         final int width = mPane.getWidth();
         final int rightWidth = (width * eventColumnWidth[1] - noduleWidth * eventColumnWidth[1]) / (sum(eventColumnWidth));
         final int leftWidth = (width * eventColumnWidth[0] - noduleWidth * eventColumnWidth[0]) / (sum(eventColumnWidth));
         left.setPreferredSize(new Dimension(leftWidth, rowHeight));
         right.setPreferredSize(new Dimension(rightWidth, rowHeight));
         layout.columnWidths = new int[]{leftWidth, rightWidth};
         System.out.println("DEBUG: Widths: " + leftWidth + "/" + rightWidth);
         layout.rowHeights = new int[]{rowHeight};
         panel.add(left);
         panel.add(right);
         return panel;
       }
       else if (value.getClass() == AutomatonLeafNode.class)
       {
         final AutomatonLeafNode autoNode = (AutomatonLeafNode) value;
         final AutomatonProxy autoProxy = autoNode.getAutomata();
         final GridBagLayout layout = new GridBagLayout();
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
         currentState = autoNode.getOverloadedState();
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
    private JLabel left;
    private JLabel right;
    private JLabel center;
    private JPanel panel;

    // ###########################################################################
    // # Class Constants
    private static final long serialVersionUID = 6788022446662090661L;
  }

  private final AutomatonDesktopPane mDesktop;
  private final Simulation mSim;
  private final ModuleContainer mContainer;
  private final ArrayList<String> automatonAreOpen;
  private JScrollPane mPane;
  private final ArrayList<String> expandedNodes;

  private static final long serialVersionUID = -4373175227919642063L;
  private static final int[] automataColumnWidth = {110, 20, 60};
  private static final int[] eventColumnWidth = {20, 180};
  private static int totalEventWidth;
  private static final int noduleWidth = 30;
  private static final int rowHeight = 20;

}

// This is the 'improved' code for this class. Unfortunatly, it doesn't work, and is not likely to work, as each event object can
// be represented multiple times in this tree.

/*package net.sourceforge.waters.gui.simulator;

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

public class TraceJTree extends JTree implements InternalFrameObserver, ComponentListener, SimulationObserver
{
  public TraceJTree(final Simulation sim, final AutomatonDesktopPane desktop, final ModuleContainer container)
  {
    super();
    this.setCellRenderer(new TraceTreeCellRenderer());
    mSim = sim;
    mSim.attach(this);
    mDesktop = desktop;
    mPane = null;
    desktop.attach(this);
    automatonAreOpen = new ArrayList<String>();
    mContainer = container;
    expandedNodes = new ArrayList<String>();
    this.setModel(new TraceTreeModel(mSim));
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
    this.addMouseListener(new MouseAdapter(){
      public void mouseClicked(final MouseEvent e)
      {
        if (e.getClickCount() == 2)
        {
          final TreePath path = TraceJTree.this.getClosestPathForLocation((int)e.getPoint().getX(), (int)e.getPoint().getY());
          final Object node = path.getLastPathComponent();
          if (node == null)
            return; // Nothing is selected
          if (EventProxy.class.isInstance(node))
          {
            final EventProxy eventNode = (EventProxy) node;
            final int targetTime = getTime(eventNode, TraceJTree.this.getRowForLocation((int)e.getPoint().getX(), (int)e.getPoint().getY()));
            int currentTime = sim.getCurrentTime();
            while (currentTime != targetTime)
            {
              if (targetTime < currentTime)
              {
                sim.stepBack();
                currentTime--;
              }
              else if (targetTime > currentTime)
              {
                sim.replayStep();
                currentTime++;
              }
            }
          }
          else if (StateProxy.class.isInstance(node))
          {
            final AutomatonProxy toAdd = findAutomaton((StateProxy)node);
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

  private AutomatonProxy findAutomaton(final StateProxy state)
  {
    for (int looper = 0; looper < mSim.getEventHistory().size(); looper++)
    {
      final AutomatonProxy found = findAutomaton(state, looper);
      if (found != null)
        return found;
    }
    return null;
  }
  private AutomatonProxy findAutomaton(final StateProxy state, final int time)
  {
    for (final AutomatonProxy auto : mSim.getAutomatonHistory().get(time).keySet())
    {
      if (mSim.getAutomatonHistory().get(time).get(auto) == state)
        return auto;
    }
    return null;
  }

  public void addScrollPane(final JScrollPane scroll)
  {
    mPane = scroll;
    mPane.addComponentListener(this);
  }

  // ############################################################################
  // # Simple Access

  public void forceRecalculation()
  {
    this.setModel(new TraceTreeModel(mSim));
    System.out.println("DEBUG: Reached.");
    for (int looper = 0; looper < expandedNodes.size(); looper++)
    {
      final String name = expandedNodes.get(looper);
      for (int nodeIndex = 0; nodeIndex < this.getModel().getChildCount(mSim); nodeIndex++)
      {
        if (((EventProxy)this.getModel().getChild(mSim, nodeIndex)).getName().compareTo(name) == 0)
        {
          this.expandPath(new TreePath(new Object[]{mSim, this.getModel().getChild(mSim, nodeIndex)}));
        }
      }
    }
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

  //########################################################################
  // # Interface ComponentListener
  public void componentHidden(final ComponentEvent e)
  {
    //Do nothing
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

  // ############################################################################
  // # Interface SimulationObserver

  public void simulationChanged(final SimulationChangeEvent event)
  {
    forceRecalculation();
  }

  // ############################################################################
  // # Auxillary Methods

  private int getTime(final EventProxy event, final int row)
  {
    int scannerRow = -1;
    int time = -1;
    for (int previousEvents = 0; previousEvents < this.getModel().getChildCount(mSim); previousEvents++)
    {
      final EventProxy scannerEvent = (EventProxy)this.getModel().getChild(mSim, previousEvents);
      scannerRow++;
      time++;
      if (scannerRow == row)
        return time;
      if (expandedNodes.contains(scannerEvent))
        scannerRow += this.getModel().getChildCount(scannerEvent);
    }
    return -1;
  }

  // ############################################################################
  // # Inner Classes

  private class TraceTreeCellRenderer
  implements TreeCellRenderer
  {
    //#######################################################################
    //# Constructor
    private TraceTreeCellRenderer()
    {
    }

    //#######################################################################
    //# Interface javax.swing.tree.TreeCellRenderer
    public Component getTreeCellRendererComponent
    (final JTree tree, final Object value, final boolean sel,
        final boolean expanded, final boolean leaf,
        final int row, final boolean hasFocus)
     {
      panel = new JPanel();
      if (sel)
        panel.setBackground(EditorColor.BACKGROUND_FOCUSSED);
      else
        panel.setBackground(EditorColor.BACKGROUNDCOLOR);
       if (EventProxy.class.isInstance(value))
       {
         final GridBagLayout layout = new GridBagLayout();
         panel.setLayout(layout);
         final EventProxy event = (EventProxy) value;
         final int time = getTime(event, row);
         left = new JLabel(String.valueOf(time + 1));
         right = new JLabel(event.getName());
         if (event.getKind() == EventKind.CONTROLLABLE)
           right.setIcon(IconLoader.ICON_CONTROLLABLE);
         else
           right.setIcon(IconLoader.ICON_UNCONTROLLABLE);
         if (time == TraceJTree.this.mSim.getCurrentTime())
         {
           right.setFont(right.getFont().deriveFont(Font.BOLD));
           left.setFont(left.getFont().deriveFont(Font.BOLD));
         }
         else
         {
           right.setFont(right.getFont().deriveFont(Font.PLAIN));
           left.setFont(left.getFont().deriveFont(Font.PLAIN));
         }
         final int width = mPane.getWidth();
         final int rightWidth = (width * eventColumnWidth[1] - noduleWidth * eventColumnWidth[1]) / (sum(eventColumnWidth));
         final int leftWidth = (width * eventColumnWidth[0] - noduleWidth * eventColumnWidth[0]) / (sum(eventColumnWidth));
         left.setPreferredSize(new Dimension(leftWidth, rowHeight));
         right.setPreferredSize(new Dimension(rightWidth, rowHeight));
         layout.columnWidths = new int[]{leftWidth, rightWidth};
         System.out.println("DEBUG: Widths: " + leftWidth + "/" + rightWidth);
         layout.rowHeights = new int[]{rowHeight};
         panel.add(left);
         panel.add(right);
         return panel;
       }
       else if (StateProxy.class.isInstance(value))
       {
         final StateProxy state = (StateProxy)value;
         final AutomatonProxy autoProxy = findAutomaton(state);
         final GridBagLayout layout = new GridBagLayout();
         panel.setLayout(layout);
         left = new JLabel(autoProxy.getName());
         if (mContainer.getSourceInfoMap().get(autoProxy).getSourceObject().getClass() == VariableComponentSubject.class)
           left.setIcon(IconLoader.ICON_VARIABLE);
         else
           left.setIcon(ModuleContext.getComponentKindIcon(autoProxy.getKind()));
         center = new JLabel();
         center.setIcon(IconLoader.ICON_TICK); // TODO: Find the event parent of this automaton
         StateProxy currentState;
         currentState = state;
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
    private JLabel left;
    private JLabel right;
    private JLabel center;
    private JPanel panel;

    // ###########################################################################
    // # Class Constants
    private static final long serialVersionUID = 6788022446662090661L;
  }

  private final AutomatonDesktopPane mDesktop;
  private final Simulation mSim;
  private final ModuleContainer mContainer;
  private final ArrayList<String> automatonAreOpen;
  private JScrollPane mPane;
  private final ArrayList<String> expandedNodes;

  private static final long serialVersionUID = -4373175227919642063L;
  private static final int[] automataColumnWidth = {110, 20, 60};
  private static final int[] eventColumnWidth = {20, 180};
  private static int totalEventWidth;
  private static final int noduleWidth = 30;
  private static final int rowHeight = 20;

}*/