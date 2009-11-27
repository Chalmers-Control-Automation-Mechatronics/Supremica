package net.sourceforge.waters.gui.simulator;

import javax.swing.JInternalFrame;
import net.sourceforge.waters.model.des.AutomatonProxy;

import org.supremica.gui.ide.ModuleContainer;

public class AutomatonInternalFrame extends JInternalFrame
{
  //#################################################################################
  //# Constructors
  public AutomatonInternalFrame(final AutomatonProxy automaton, final AutomatonDesktopPane parent, final ModuleContainer container, final Simulation mSim)
  {
    super(automaton.getName(), true, true, false, true);
    mDesktopParent = parent;
    mAutomaton = automaton;
    setVisible(true);
    final AutomatonDisplayPane displayPane = new AutomatonDisplayPane(automaton, container, mSim);
    this.getContentPane().add(displayPane);
    this.addMouseListener(new InternalFrameMouseAdapter(this));
    this.pack();
  }

  //#################################################################################
  //# Class JInternalFrame
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
