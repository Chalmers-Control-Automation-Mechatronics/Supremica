package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sourceforge.waters.model.des.AutomatonProxy;

import org.supremica.gui.ide.IDE;

public class DesktopCloseWindowAction extends WatersDesktopAction
{

  protected DesktopCloseWindowAction(final IDE ide, final AutomatonProxy autoToClose)
  {
    super(ide);
    mAutomaton = autoToClose;
    putValue(Action.NAME, "Close");
    putValue(Action.SHORT_DESCRIPTION, "Close the window");
    setEnabled(true);
  }

  public void actionPerformed(final ActionEvent e)
  {
    getDesktop().closeAutomaton(mAutomaton.getName());
  }

  private final AutomatonProxy mAutomaton;

  private static final long serialVersionUID = -1644229513613033199L;
}
