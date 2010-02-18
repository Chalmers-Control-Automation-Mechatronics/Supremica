package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;

import org.supremica.gui.ide.IDE;

public class DesktopExecuteAction extends WatersDesktopAction
{
  protected DesktopExecuteAction(final IDE ide, final AutomatonProxy autoToClose, final Proxy proxyToFire)
  {
    super(ide);
    mAutomaton = autoToClose;
    mProxyToFire = proxyToFire;
    putValue(Action.NAME, "Execute Transition");
    putValue(Action.SHORT_DESCRIPTION, "Execute this transition");
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