package net.sourceforge.waters.gui.simulator;

import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import net.sourceforge.waters.gui.PopupFactory;
import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.EventProxy;

class EventTreePopupFactory extends PopupFactory
{

  //#########################################################################
  //# Constructor
  EventTreePopupFactory(final WatersPopupActionManager master)
  {
    super(master);
    mSelectedEvent = null;
  }


  //#########################################################################
  //# Menu Items
  public void maybeShowPopup(final Component invoker,
                             final MouseEvent event,
                             final Proxy proxy)
  {
    if (proxy instanceof EventProxy) {
      mSelectedEvent = (EventProxy) proxy;
      super.maybeShowPopup(invoker, event, proxy);
    }
  }

  protected void addDefaultMenuItems()
  {
    // Do nothing
  }

  protected void addItemSpecificMenuItems(final Proxy proxy)
  {
    // Do nothing
  }

  protected void addCommonMenuItems()
  {
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    final IDEAction fireEvent = master.getEventExecuteAction(mSelectedEvent);
    popup.add(fireEvent);
  }


  //#########################################################################
  //# Data Members
  private EventProxy mSelectedEvent;

}
