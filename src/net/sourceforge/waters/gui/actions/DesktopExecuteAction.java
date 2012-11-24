package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sourceforge.waters.gui.util.IconLoader;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;

import org.supremica.gui.ide.IDE;

public class DesktopExecuteAction extends WatersDesktopAction
{
  protected DesktopExecuteAction(final IDE ide,
                                 final AutomatonProxy aut,
                                 final Proxy proxyToFire)
  {
    super(ide);
    mAutomaton = aut;
    mProxyToFire = proxyToFire;
    String name = null;
    if (proxyToFire instanceof IdentifierProxy) {
      name = proxyToFire.toString();
      if (name.length() > 32) {
        name = null;
      }
    }
    if (name != null) {
      putValue(Action.NAME, "Execute Transition " + name);
    } else {
      putValue(Action.NAME, "Execute Transition");
    }
    putValue(Action.SHORT_DESCRIPTION, "Execute this transition");
    putValue(Action.SMALL_ICON, IconLoader.ICON_SIMULATOR_STEP);
    setEnabled(true);
  }

  public void actionPerformed(final ActionEvent e)
  {
    getDesktop().execute(mAutomaton.getName(), mProxyToFire);
  }

  private final AutomatonProxy mAutomaton;
  private final Proxy mProxyToFire;
  private static final long serialVersionUID = -1644229513613033199L;
}