package net.sourceforge.waters.gui.simulator;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.waters.model.des.EventProxy;

public class EventLeafNode extends DefaultMutableTreeNode
{
  public EventLeafNode(final EventProxy event)
  {
    super(event.getName(), false);
    mEvent = event;
  }

  public EventProxy getEvent()
  {
    return mEvent;
  }

  private final EventProxy mEvent;

  private static final long serialVersionUID = 2563835721533387968L;
}
