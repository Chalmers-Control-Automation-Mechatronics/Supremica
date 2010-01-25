package net.sourceforge.waters.gui.simulator;

import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import net.sourceforge.waters.gui.PopupFactory;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;

public class AutomatonTablePopupFactory extends PopupFactory
{

  //#########################################################################
  //# Constructor
  AutomatonTablePopupFactory(final WatersPopupActionManager master, final AutomatonDesktopPane desktop)
  {
    super(master);
    mSelectedAutomaton = null;
    mDesktop = desktop;
  }

  //#########################################################################
  //# Menu Items
  public void maybeShowPopup(final Component invoker,
                             final MouseEvent event,
                             final Proxy proxy)
  {
    if (proxy instanceof AutomatonProxy) {
      mSelectedAutomaton = (AutomatonProxy)proxy;
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
    if (mSelectedAutomaton != null)
    {
      AutomatonPopupFactory.setPopup(popup, master, mDesktop, mSelectedAutomaton);
    }
  }


  //#########################################################################
  //# Data Members

  private AutomatonProxy mSelectedAutomaton;
  private final AutomatonDesktopPane mDesktop;
}