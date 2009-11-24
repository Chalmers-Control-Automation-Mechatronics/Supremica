package net.sourceforge.waters.gui.simulator;

import java.awt.Dimension;

import javax.swing.JInternalFrame;

import net.sourceforge.waters.model.des.AutomatonProxy;

public class AutomatonInternalFrame extends JInternalFrame
{
  public AutomatonInternalFrame(final AutomatonProxy automaton, final AutomatonDesktopPane parent)
  {
    super(automaton.getName(), true, true, false, true);
    desktopParent = parent;
    this.automaton = automaton;
    setVisible(true);
    setSize(new Dimension(300,100));
    setLocation(100, 100);
    this.add(new AutomatonDisplayPane(automaton));
  }

  public void dispose()
  {
    super.dispose();
    desktopParent.removeAutomaton(automaton);
  }

  AutomatonProxy automaton;
  AutomatonDesktopPane desktopParent;
}
