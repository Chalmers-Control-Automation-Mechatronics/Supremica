package net.sourceforge.waters.gui.simulator;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.model.des.AutomatonProxy;

public class EventJTree extends JTree
{
  public EventJTree(final Simulation sim, final AutomatonDesktopPane desktop)
  {
    super();
    this.setCellRenderer(new EventTreeCellRenderer());
    mSim = sim;
    mDesktop = desktop;
    final EventMutableTreeNode root = new EventMutableTreeNode(sim, this);
    this.setModel(new EventTreeModel(root, sim));
    this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    setRootVisible(false);
    setShowsRootHandles(true);
    //setAutoscrolls(true);
    setToggleClickCount(0);
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
      }
    });
  }



  public void expandPath(final TreePath e)
  {
    super.expandPath(e);
    System.out.println("DEBUG: Expanded with object : " + e);
  }

  public void expandRow(final int row)
  {
    super.expandRow(row);
    System.out.println("DEBUG: Expanded row : " + row);
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
      setBackgroundSelectionColor(EditorColor.BACKGROUNDCOLOR);
      setBorderSelectionColor(EditorColor.BACKGROUNDCOLOR);
      super.getTreeCellRendererComponent
        (tree, value, sel, expanded, leaf, row, hasFocus);
      System.out.println("DEBUG: Value class is " + value.getClass());
      /*final String text = mPrinter.toString(proxy);
      setText(text);
      final Icon icon = mModuleContext.getIcon(proxy);
      setIcon(icon);
      final String tooltip = mModuleContext.getToolTipText(proxy);
      setToolTipText(tooltip);*/
      return this;
    }

    // ###########################################################################
    // # Class Constants
    private static final long serialVersionUID = 6788022446662090661L;
  }

  private final AutomatonDesktopPane mDesktop;
  private final Simulation mSim;

  private static final long serialVersionUID = -4373175227919642063L;

}
