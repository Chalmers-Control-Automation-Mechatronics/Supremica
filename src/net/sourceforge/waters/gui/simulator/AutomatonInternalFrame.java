package net.sourceforge.waters.gui.simulator;

import javax.swing.JInternalFrame;

import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.subject.module.GraphSubject;

import org.supremica.gui.ide.ModuleContainer;

public class AutomatonInternalFrame extends JInternalFrame
{
  //#################################################################################
  //# Constructors
  public AutomatonInternalFrame(final String automaton,
                                final GraphSubject graph,
                                final AutomatonDesktopPane parent,
                                final ModuleContainer container,
                                final Simulation sim)
    throws GeometryAbsentException
  {
    super(automaton, true, true, false, true);
    mDesktopParent = parent;
    mAutomaton = sim.getAutomatonFromName(automaton);
    mDisplayPane = new AutomatonDisplayPane(automaton, graph, container, sim);
    setContentPane(mDisplayPane);
    addMouseListener(new InternalFrameMouseAdapter(this));
    setVisible(true);
    pack();
  }


  //#################################################################################
  //# Class JInternalFrame
  public void dispose()
  {
    mDisplayPane.close();
    mDesktopParent.removeAutomaton(mAutomaton.getName());
    super.dispose();
  }


  //#########################################################################
  //# Data Members
  private final AutomatonProxy mAutomaton;
  private final AutomatonDisplayPane mDisplayPane;
  private final AutomatonDesktopPane mDesktopParent;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
