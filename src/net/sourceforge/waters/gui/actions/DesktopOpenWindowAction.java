package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sourceforge.waters.model.des.AutomatonProxy;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;

public class DesktopOpenWindowAction extends WatersDesktopAction
{

  protected DesktopOpenWindowAction(final IDE ide, final AutomatonProxy autoToOpen)
  {
    super(ide);
    mAutomaton = autoToOpen;
    putValue(Action.NAME, "Open Automaton");
    putValue(Action.SHORT_DESCRIPTION, "Open this Automaton window");
    setEnabled(true);
  }

  public void actionPerformed(final ActionEvent e)
  {
    final ModuleContainer mContainer = (ModuleContainer)getIDE().getActiveDocumentContainer();
    getDesktop().addAutomaton(mAutomaton.getName(), mContainer, mContainer.getSimulatorPanel().getSimulation(), 2);
  }

  private final AutomatonProxy mAutomaton;

  private static final long serialVersionUID = -1644229513613033199L;
}