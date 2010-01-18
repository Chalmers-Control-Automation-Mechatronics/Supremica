package net.sourceforge.waters.gui.simulator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;
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
    expandedNodes = new ArrayList<Integer>();
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
          if (EventBranchNode.class.isInstance(node))
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
          else if (AutomatonLeafNode.class.isInstance(node))
          {
            final AutomatonProxy toAdd = ((AutomatonLeafNode)node).getAutomata();
            mDesktop.addAutomaton(toAdd.getName(), mSim.getContainer(), mSim, 2);
          }
          else if (InitialState.class.isInstance(node))
          {
            int currentTime = sim.getCurrentTime();
            while (currentTime != -1)
            {
              sim.stepBack();
              currentTime--;
            }
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
          expandedNodes.remove((Integer)(((EventBranchNode)event.getPath().getLastPathComponent()).getTime()));
        }
        else if (InitialState.class.isInstance(event.getPath().getLastPathComponent()))
        {
          expandedNodes.remove(Integer.decode("-1"));
        }
      }

      public void treeWillExpand(final TreeExpansionEvent event)
          throws ExpandVetoException
      {
        if (EventBranchNode.class.isInstance(event.getPath().getLastPathComponent()))
        {
          final int expansionIndex = ((EventBranchNode)event.getPath().getLastPathComponent()).getTime();
          for (final Integer index : expandedNodes)
          {
            if (index == expansionIndex)
              return;
          }
          expandedNodes.add(expansionIndex);
          ((EventBranchNode)event.getPath().getLastPathComponent()).addAutomata(mSim, mSim.getAutomatonHistory().get(expansionIndex));
        }
        else if (InitialState.class.isInstance(event.getPath().getLastPathComponent()))
        {
          ((InitialState)event.getPath().getLastPathComponent()).expand(mSim);
          expandedNodes.add(-1);
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
      final int expandedIndex = expandedNodes.get(looper);
      boolean located = false;
      for (int nodeIndex = 0; nodeIndex < node.getChildCount(); nodeIndex++)
      {
        if (EventBranchNode.class.isInstance(node.getChildAt(nodeIndex)))
        {
          if (((EventBranchNode)node.getChildAt(nodeIndex)).getTime() == expandedIndex)
          {
            ((EventBranchNode)node.getChildAt(nodeIndex)).addAutomata(mSim,
                mSim.getAutomatonHistory().get(((EventBranchNode)node.getChildAt(nodeIndex)).getTime()));
            this.expandPath(new TreePath(((EventBranchNode)node.getChildAt(nodeIndex)).getPath()));
            located = true;
          }
        }
        else if (InitialState.class.isInstance(node.getChildAt(nodeIndex)))
        {
          if (expandedIndex == -1)
          {
            ((InitialState)node.getChildAt(nodeIndex)).expand(mSim);
            this.expandPath(new TreePath(((InitialState)node.getChildAt(nodeIndex)).getPath()));
            located = true;
          }
        }
      }
      if (!located)
      {
        expandedNodes.remove(looper);
        looper--;
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
      mEventPanel = new JPanel();
      final FlowLayout layout = new FlowLayout(FlowLayout.LEADING, 0, 0);
      mEventPanel.setLayout(layout);
      mEventNameLabel = new JLabel();
      mEventNameLabel.setFont(mEventNameLabel.getFont().deriveFont(Font.PLAIN));
      mEventPanel.add(mEventNameLabel);
      mAutomataPanel = new JPanel();
      mAutomataPanel.setLayout(layout);
      mAutomataNameLabel = new JLabel();
      mAutomataIconLabel = new JLabel();
      mAutomataStatusLabel = new JLabel();
      mAutomataPanel.add(mAutomataNameLabel);
      mAutomataPanel.add(mAutomataIconLabel);
      mAutomataPanel.add(mAutomataStatusLabel);
    }

    //#######################################################################
    //# Interface javax.swing.tree.TreeCellRenderer
    public Component getTreeCellRendererComponent
    (final JTree tree, final Object value, final boolean sel,
        final boolean expanded, final boolean leaf,
        final int row, final boolean hasFocus)
     {
       if (EventBranchNode.class.isInstance(value))
       {
         if (sel)
           mEventPanel.setBackground(EditorColor.BACKGROUND_FOCUSSED);
         else
           mEventPanel.setBackground(EditorColor.BACKGROUNDCOLOR);
         final EventBranchNode eventNode = (EventBranchNode)value;
         final EventProxy event = eventNode.getEvent();
         mEventNameLabel.setText(String.valueOf(eventNode.getTime() + 1) + ". " + event.getName());
         if (event.getKind() == EventKind.CONTROLLABLE)
           mEventNameLabel.setIcon(IconLoader.ICON_CONTROLLABLE);
         else
           mEventNameLabel.setIcon(IconLoader.ICON_UNCONTROLLABLE);
         if (eventNode.getTime() == TraceJTree.this.mSim.getCurrentTime())
         {
           mEventNameLabel.setFont(mEventNameLabel.getFont().deriveFont(Font.BOLD));
         }
         else
         {
           mEventNameLabel.setFont(mEventNameLabel.getFont().deriveFont(Font.PLAIN));
         }
         return mEventPanel;
       }
       else if (AutomatonLeafNode.class.isInstance(value))
       {
         if (sel)
           mAutomataPanel.setBackground(EditorColor.BACKGROUND_FOCUSSED);
         else
           mAutomataPanel.setBackground(EditorColor.BACKGROUNDCOLOR);
         final AutomatonLeafNode autoNode = (AutomatonLeafNode) value;
         final AutomatonProxy autoProxy = autoNode.getAutomata();
         mAutomataNameLabel.setText(autoProxy.getName());
         if (mContainer.getSourceInfoMap().get(autoProxy).getSourceObject().getClass() == VariableComponentSubject.class)
           mAutomataNameLabel.setIcon(IconLoader.ICON_VARIABLE);
         else
           mAutomataNameLabel.setIcon(ModuleContext.getComponentKindIcon(autoProxy.getKind()));
         if (autoNode.getBlocking())
           mAutomataIconLabel.setIcon(IconLoader.ICON_WARNING);
         else
           mAutomataIconLabel.setIcon(new ImageIcon());
         StateProxy currentState;
         currentState = autoNode.getOverloadedState();
         mAutomataStatusLabel.setText(currentState.getName());
         mAutomataStatusLabel.setIcon(mSim.getMarkingIcon(currentState, autoProxy));
         final int width = mPane.getWidth();
         final int rightWidth = (width * automataColumnWidth[2] - 2 * noduleWidth * automataColumnWidth[2]) / (sum(automataColumnWidth));
         final int centerWidth = (width * automataColumnWidth[1] - 2 * noduleWidth * automataColumnWidth[1]) / (sum(automataColumnWidth));
         final int leftWidth = (width * automataColumnWidth[0] - 2 * noduleWidth * automataColumnWidth[0]) / (sum(automataColumnWidth));
         mAutomataNameLabel.setPreferredSize(new Dimension(leftWidth, rowHeight));
         mAutomataIconLabel.setPreferredSize(new Dimension(centerWidth, rowHeight));
         mAutomataStatusLabel.setPreferredSize(new Dimension(rightWidth, rowHeight));
         if (automatonAreOpen.contains(autoProxy.getName()))
         {
           mAutomataNameLabel.setFont(mAutomataNameLabel.getFont().deriveFont(Font.BOLD));
           mAutomataStatusLabel.setFont(mAutomataStatusLabel.getFont().deriveFont(Font.BOLD));
         }
         else
         {
           mAutomataNameLabel.setFont(mAutomataNameLabel.getFont().deriveFont(Font.PLAIN));
           mAutomataStatusLabel.setFont(mAutomataStatusLabel.getFont().deriveFont(Font.PLAIN));
         }
         return mAutomataPanel;
       }
       else if (InitialState.class.isInstance(value))
       {
         if (sel)
           mEventPanel.setBackground(EditorColor.BACKGROUND_FOCUSSED);
         else
           mEventPanel.setBackground(EditorColor.BACKGROUNDCOLOR);
         mEventNameLabel.setText("Initial State");
         if (TraceJTree.this.mSim.getCurrentTime() == -1)
         {
           mEventNameLabel.setFont(mEventNameLabel.getFont().deriveFont(Font.BOLD));
         }
         else
         {
           mEventNameLabel.setFont(mEventNameLabel.getFont().deriveFont(Font.PLAIN));
         }
         mEventNameLabel.setIcon(new ImageIcon());
         return mEventPanel;
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
    private final JPanel mEventPanel;
    private final JLabel mEventNameLabel;
    private final JPanel mAutomataPanel;
    private final JLabel mAutomataNameLabel;
    private final JLabel mAutomataIconLabel;
    private final JLabel mAutomataStatusLabel;

    // ###########################################################################
    // # Class Constants
    private static final long serialVersionUID = 6788022446662090661L;
  }

  private final AutomatonDesktopPane mDesktop;
  private final Simulation mSim;
  private final ModuleContainer mContainer;
  private final ArrayList<String> automatonAreOpen;
  private JScrollPane mPane;
  private final ArrayList<Integer> expandedNodes;

  private static final long serialVersionUID = -4373175227919642063L;
  private static final int[] automataColumnWidth = {110, 20, 60};
  private static final int[] eventColumnWidth = {20, 180};
  private static int totalEventWidth;
  private static final int noduleWidth = 30;
  private static final int rowHeight = 20;

}