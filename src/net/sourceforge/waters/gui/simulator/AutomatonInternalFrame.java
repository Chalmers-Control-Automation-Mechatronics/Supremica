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
  public AutomatonInternalFrame(final AutomatonProxy aut,
                                final GraphSubject graph,
                                final AutomatonDesktopPane parent,
                                final ModuleContainer container,
                                final Simulation sim)
    throws GeometryAbsentException
  {
    super(aut.getName(), true, true, false, true);
    mAutomaton = aut;
    mDisplayPane = new AutomatonDisplayPane(aut, graph, container, sim);
    setContentPane(mDisplayPane);
    mDisplayPane.repaint();
    addMouseListener(new InternalFrameMouseAdapter(this));
    setVisible(true);
    pack();
    addComponentListener();
  }

  private void addComponentListener()
  {
    this.addComponentListener(new PreserveAspectComponentListener(this, mDisplayPane));
  }

  //##########################################################################
  //# Data Members
  private final AutomatonProxy mAutomaton;
  private final AutomatonDisplayPane mDisplayPane;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
