package net.sourceforge.waters.gui.simulator;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import net.sourceforge.waters.model.des.AutomatonProxy;

public class EventJTree extends JTree
{
  public EventJTree(final Simulation sim, final AutomatonDesktopPane desktop)
  {
    super();
    mSim = sim;
    mDesktop = desktop;
    this.setModel(new EventTreeModel(new EventMutableTreeNode(sim, this), sim));
    this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    this.addTreeSelectionListener(new TreeSelectionListener()
    {
      public void valueChanged(final TreeSelectionEvent e)
      {
        System.out.println("Value has been changed");
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode)EventJTree.this.getLastSelectedPathComponent();
        if (node == null)
          return; // Nothing is selected
        if (node.getClass() == EventBranchNode.class)
        {
          try {
            sim.step(((EventBranchNode)node).getEvent());
          } catch (final UncontrollableException exception) {
            exception.printStackTrace();
          } catch (final IllegalArgumentException exception) {
            exception.printStackTrace();
          }
        }
        else if (node.getClass() == AutomatonLeafNode.class)
        {
          final AutomatonProxy toAdd = ((AutomatonLeafNode)node).getAutomata();
          mDesktop.addAutomaton(toAdd.getName(), mSim.getContainer(), mSim, 2);
        }
      }
    });
  }

  private final AutomatonDesktopPane mDesktop;
  private final Simulation mSim;

  private static final long serialVersionUID = -4373175227919642063L;

}
