package net.sourceforge.waters.gui.simulator;

import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import net.sourceforge.waters.gui.PopupFactory;
import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;

class EventTreePopupFactory extends PopupFactory
{

  //#########################################################################
  //# Constructor
  EventTreePopupFactory(final WatersPopupActionManager master, final AutomatonDesktopPane desktop)
  {
    super(master);
    mSelectedEvent = null;
    mDesktop = desktop;
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
    else if (proxy instanceof AutomatonProxy) {
      mSelectedAutomata = (AutomatonProxy)proxy;
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
    if (mSelectedEvent != null)
    {
      final IDEAction fireEvent = master.getEventExecuteAction(mSelectedEvent);
      popup.add(fireEvent);
    }
    if (mSelectedAutomata != null)
    {
      AutomatonPopupFactory.setPopup(popup, master, mDesktop, mSelectedAutomata);
    }
    mSelectedEvent = null;
    mSelectedAutomata = null;
  }

  //#########################################################################
  //# Data Members
  private EventProxy mSelectedEvent;
  private AutomatonProxy mSelectedAutomata;
  private final AutomatonDesktopPane mDesktop;
}
