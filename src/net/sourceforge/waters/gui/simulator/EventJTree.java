package net.sourceforge.waters.gui.simulator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
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

public class EventJTree extends JTree
{


  public EventJTree(final Simulation sim, final AutomatonDesktopPane desktop, final ModuleContainer container)
  {
    super();
    this.setCellRenderer(new EventTreeCellRenderer());
    mSim = sim;
    mDesktop = desktop;
    mContainer = container;
    final EventMutableTreeNode root = new EventMutableTreeNode(sim, this);
    this.setModel(new EventTreeModel(root, sim));
    this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    setRootVisible(false);
    setShowsRootHandles(true);
    //setAutoscrolls(true);
    setToggleClickCount(0);
    this.setLargeModel(false);
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

  private class EventTreeCellRenderer
  extends DefaultTreeCellRenderer
  {
    //#######################################################################
    //# Constructor
    private EventTreeCellRenderer()
    {
      setTextSelectionColor(EditorColor.TEXTCOLOR);
    }

    //#######################################################################
    //# Interface javax.swing.tree.TreeCellRenderer
    public Component getTreeCellRendererComponent
      (final JTree tree, final Object value, final boolean sel,
       final boolean expanded, final boolean leaf,
       final int row, final boolean hasFocus)
    {
      if (value.getClass() == EventBranchNode.class)
      {
        final JPanel output = new JPanel();
        output.setBackground(EditorColor.BACKGROUNDCOLOR);
        final EventBranchNode eventNode = (EventBranchNode)value;
        final EventProxy event = eventNode.getEvent();
        final JLabel left = new JLabel(event.getName());
        if (event.getKind() == EventKind.CONTROLLABLE)
          left.setIcon(IconLoader.ICON_CONTROLLABLE);
        else
          left.setIcon(IconLoader.ICON_UNCONTROLLABLE);
        final JLabel right = new JLabel();
        if (mSim.getValidTransitions().contains(event))
          right.setIcon(IconLoader.ICON_TICK);
        else
          right.setIcon(IconLoader.ICON_CROSS);
        left.setPreferredSize(new Dimension(eventColumnWidth[0], rowHeight));
        right.setPreferredSize(new Dimension(eventColumnWidth[1], rowHeight));
        output.add(left, BorderLayout.WEST);
        output.add(right, BorderLayout.EAST);
        return output;
      }
      else if (value.getClass() == AutomatonLeafNode.class)
      {
        final AutomatonLeafNode autoNode = (AutomatonLeafNode) value;
        final AutomatonProxy autoProxy = autoNode.getAutomata();
        final JPanel output = new JPanel();
        output.setBackground(EditorColor.BACKGROUNDCOLOR);
        final JLabel left = new JLabel(autoProxy.getName());
        if (mContainer.getSourceInfoMap().get(autoProxy).getSourceObject().getClass() == VariableComponentSubject.class)
          left.setIcon(IconLoader.ICON_VARIABLE);
        else
          left.setIcon(ModuleContext.getComponentKindIcon(autoProxy.getKind()));
        final JLabel center = new JLabel();
        if (mSim.getBlocking(((EventBranchNode)autoNode.getParent()).getEvent()).contains(autoProxy))
          center.setIcon(IconLoader.ICON_CROSS);
        else
          center.setIcon(IconLoader.ICON_TICK);
        final StateProxy currentState = mSim.getCurrentStates().get(autoProxy);
        final JLabel right = new JLabel(currentState.getName());
        right.setIcon(mSim.getMarkingIcon(currentState, autoProxy));
        left.setPreferredSize(new Dimension(automataColumnWidth[0], rowHeight));
        center.setPreferredSize(new Dimension(automataColumnWidth[1], rowHeight));
        right.setPreferredSize(new Dimension(automataColumnWidth[2], rowHeight));
        output.add(left, BorderLayout.WEST);
        output.add(center, BorderLayout.CENTER);
        output.add(right, BorderLayout.EAST);
        return output;
      }
      else
      {
        super.getTreeCellRendererComponent
          (tree, value, sel, expanded, leaf, row, hasFocus);
        return this;
      }
    }

    // ###########################################################################
    // # Class Constants
    private static final long serialVersionUID = 6788022446662090661L;
  }

  private final AutomatonDesktopPane mDesktop;
  private final Simulation mSim;
  private final ModuleContainer mContainer;

  private static final long serialVersionUID = -4373175227919642063L;
  private static final int[] automataColumnWidth = {110, 20, 60};
  private static final int[] eventColumnWidth = {180, 20};
  private static final int rowHeight = 20;

}
