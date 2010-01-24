package net.sourceforge.waters.gui.simulator;

import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import net.sourceforge.waters.gui.PopupFactory;
import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;

public class TraceTreePopupFactory extends PopupFactory
{

  //#########################################################################
  //# Constructor
  TraceTreePopupFactory(final WatersPopupActionManager master, final AutomatonDesktopPane desktop)
  {
    super(master);
    mDesktop = desktop;
    mTime = -2;
  }


  //#########################################################################
  //# Menu Items
  public void maybeShowPopup(final Component invoker,
                             final MouseEvent event,
                             final Proxy proxy,
                             final int time)
  {
    mTime = time;
    maybeShowPopup(invoker, event, proxy);
  }

  public void maybeShowPopup(final Component invoker,
                             final MouseEvent event,
                             final Proxy proxy)
  {
    if (mTime == -2 && !(proxy instanceof AutomatonProxy))
      throw new UnsupportedOperationException("Do not call maybeShowPopup(Component, MouseEvent, EventProxy) directly, instead, call maybeShowPopup(Component, MouseEvent, EventProxy, int)");
    if (proxy instanceof AutomatonProxy) {
      mSelectedAutomata = (AutomatonProxy)proxy;
      super.maybeShowPopup(invoker, event, proxy);
    }
    else
      super.maybeShowPopup(invoker, event, proxy);
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
    if (mTime != -2)
    {
      final IDEAction fireEvent = master.getTraceTravelAction(mTime);
      popup.add(fireEvent);
    }
    else if (mSelectedAutomata != null)
    {

      final IDEAction closeAll = master.getDesktopCloseAllAction();
      popup.add(closeAll);
      final IDEAction showAll = master.getDesktopShowAllAction();
      popup.add(showAll);
      final IDEAction cascade = master.getDesktopCascadeAction();
      popup.add(cascade);
      if (mDesktop.automatonIsOpen(mSelectedAutomata))
      {
        final IDEAction closeOther = master.getDesktopCloseOtherAction(mSelectedAutomata);
        popup.add(closeOther);
        final IDEAction close = master.getDesktopCloseWindowAction(mSelectedAutomata);
        popup.add(close);
      }
      else
      {
        final IDEAction openOther = master.getDesktopOpenOtherAction(mSelectedAutomata);
        popup.add(openOther);
        final IDEAction open = master.getDesktopOpenWindowAction(mSelectedAutomata);
        popup.add(open);
      }
    }
    mTime = -2;
    mSelectedAutomata = null;
  }


  //#########################################################################
  //# Data Members
  private AutomatonProxy mSelectedAutomata;
  private final AutomatonDesktopPane mDesktop;
  private int mTime;

}