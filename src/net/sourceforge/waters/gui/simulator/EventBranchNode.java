package net.sourceforge.waters.gui.simulator;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.waters.model.des.EventProxy;

public class EventBranchNode extends DefaultMutableTreeNode
{
  public EventBranchNode(final EventProxy event)
  {
    super(event.getName(), true);
    mEvent = event;
  }

  public EventProxy getEvent()
  {
    return mEvent;
  }

  private final EventProxy mEvent;

  static ImageIcon enabledEventControllableIcon;
  static ImageIcon disabledEventControllableIcon;
  static ImageIcon enabledEventUncontrollableIcon;
  static ImageIcon disabledEventUncontrollableIcon;

  private static final long serialVersionUID = 1581075011997555080L;
}
