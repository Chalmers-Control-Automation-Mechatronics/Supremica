package net.sourceforge.waters.gui.simulator;

import javax.swing.JPopupMenu;

import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.model.des.AutomatonProxy;

public class AutomatonPopupFactory
{
  static void setPopup(final JPopupMenu popup, final WatersPopupActionManager master, final AutomatonDesktopPane desktopPane, final AutomatonProxy aut)
  {
    final IDEAction closeAll = master.getDesktopCloseAllAction();
    popup.add(closeAll);
    final IDEAction showAll = master.getDesktopShowAllAction();
    popup.add(showAll);
    final IDEAction cascade = master.getDesktopCascadeAction();
    popup.add(cascade);
    popup.addSeparator();
    if (aut != null) {
      final IDEAction closeOther = master.getDesktopCloseOtherAction(aut);
      popup.add(closeOther);
      final IDEAction close = master.getDesktopCloseWindowAction(aut);
      popup.add(close);
      final IDEAction edit = master.getDesktopEditAction(aut);
      popup.add(edit);
      if (desktopPane.canResize(aut.getName()))
      {
        final IDEAction resize = master.getResizeAction(aut);
        popup.add(resize);
      }
    }
  }
}
