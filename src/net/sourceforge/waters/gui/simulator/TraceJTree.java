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
import javax.swing.tree.DefaultTreeModel;
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
         final int rightWidth = (width - noduleWidth) / (1 + (eventColumnWidth[0] / eventColumnWidth[1]));
         final int leftWidth = (eventColumnWidth[0] / eventColumnWidth[1]) * rightWidth;
         left.setPreferredSize(new Dimension(leftWidth, rowHeight));
         right.setPreferredSize(new Dimension(rightWidth, rowHeight));
         layout.columnWidths = new int[]{leftWidth, rightWidth};
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
         currentState = autoNode.getOverloadedState();
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

  private static final long serialVersionUID = -4373175227919642063L;
  private static final int[] automataColumnWidth = {110, 20, 60};
  private static final int[] eventColumnWidth = {20, 180};
  private static int totalEventWidth;
  private static final int noduleWidth = 30;
  private static final int rowHeight = 20;

}