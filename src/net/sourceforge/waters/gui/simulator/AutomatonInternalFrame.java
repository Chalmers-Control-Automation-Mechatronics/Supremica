package net.sourceforge.waters.gui.simulator;

import java.awt.Dimension;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;

import net.sourceforge.waters.model.des.AutomatonProxy;

import org.supremica.gui.ide.ModuleContainer;

public class AutomatonInternalFrame extends JInternalFrame
{
  public AutomatonInternalFrame(final AutomatonProxy automaton, final AutomatonDesktopPane parent, final ModuleContainer container)
  {
    super(automaton.getName(), true, true, false, true);
    mDesktopParent = parent;
    mAutomaton = automaton;
    setVisible(true);
    setSize(new Dimension(300,100));
    setLocation(100, 100);
    moveToFront();
    final JScrollPane scrollPane = new JScrollPane(new AutomatonDisplayPane(automaton, container));
    this.getContentPane().add(scrollPane);
  }

  public void dispose()
  {
    super.dispose();
    mDesktopParent.removeAutomaton(mAutomaton);
  }


  //#########################################################################
  //# Data Members
  private final AutomatonProxy mAutomaton;
  private final AutomatonDesktopPane mDesktopParent;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
