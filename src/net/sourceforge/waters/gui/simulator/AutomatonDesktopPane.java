package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;

import javax.swing.JDesktopPane;
import net.sourceforge.waters.model.des.AutomatonProxy;

import org.supremica.gui.ide.ModuleContainer;

public class AutomatonDesktopPane extends JDesktopPane
{

  public void addAutomaton(final AutomatonProxy automaton, final ModuleContainer container)
  {
    if (!allAutomaton.contains(automaton))
    {
      allAutomaton.add(automaton);
      add(new AutomatonInternalFrame(automaton, this, container));
    }
  }

  public void removeAutomaton(final AutomatonProxy automaton)
  {
    if (allAutomaton.contains(automaton))
      allAutomaton.remove(automaton);
  }

  ArrayList<AutomatonProxy> allAutomaton = new ArrayList<AutomatonProxy>();
  /**
   *
   */
  private static final long serialVersionUID = -5528014241244952875L;


}
