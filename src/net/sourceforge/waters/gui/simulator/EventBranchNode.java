package net.sourceforge.waters.gui.simulator;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.waters.model.des.EventProxy;

public class EventBranchNode extends DefaultMutableTreeNode
{
  public EventBranchNode(final EventProxy event, final int currentTime)
  {
    super(event.getName(), true);
    mEvent = event;
    mTime = currentTime;

  }

  public EventProxy getEvent()
  {
    return mEvent;
  }
  public int getTime()
  {
    return mTime;
  }
  public String getData(final int indents)
  {
    String output = getIndents(indents) + this.toString();
    for (int childLoop = 0; childLoop < this.getChildCount(); childLoop++)
    {
      if (this.getChildAt(childLoop).getClass() == EventBranchNode.class)
      {
        final EventBranchNode node = (EventBranchNode)this.getChildAt(childLoop);
        output += "\r\n" + getIndents(indents) + node.getData(indents + 1);
      }
      else if (this.getChildAt(childLoop).getClass() == AutomatonLeafNode.class)
      {
        final AutomatonLeafNode node = (AutomatonLeafNode)this.getChildAt(childLoop);
        output += "\r\n" + getIndents(indents) + node.getData(indents + 1);
      }
    }
    return output;
  }
  private String getIndents(final int indents)
  {
    String output = "";
    for (int looper = 0; looper < indents; looper++)
      output += "-";
    return output;
  }

  private final EventProxy mEvent;
  private final int mTime;

  static ImageIcon enabledEventControllableIcon;
  static ImageIcon disabledEventControllableIcon;
  static ImageIcon enabledEventUncontrollableIcon;
  static ImageIcon disabledEventUncontrollableIcon;

  private static final long serialVersionUID = 1581075011997555080L;


}
