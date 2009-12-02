package net.sourceforge.waters.gui.simulator;

import java.util.ArrayList;

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
    observers = new ArrayList<InternalFrameObserver>();
  }


  //##########################################################################
  //# Class JInternalFrame
  public void dispose()
  {
    fireFrameClosedEvent();
    super.dispose();
  }

  private void addComponentListener()
  {
    this.addComponentListener(new PreserveAspectComponentListener(this, mDisplayPane));
  }

  //##########################################################################
  //# Dealing with attached InternalFrameObservers

  public void attach (final InternalFrameObserver observer)
  {
    if (!observers.contains(observer))
      observers.add(observer);
  }

  public void detach (final InternalFrameObserver observer)
  {
    observers.remove(observer);
  }

  public void fireFrameClosedEvent()
  {
    final ArrayList<InternalFrameObserver> temp =
      new ArrayList<InternalFrameObserver>(observers);
    for (final InternalFrameObserver observer : temp)
    {
      observer.onFrameEvent(new InternalFrameEvent(mAutomaton.getName(), this, false));
    }
  }

  //##########################################################################
  //# Data Members
  private final AutomatonProxy mAutomaton;
  private final AutomatonDisplayPane mDisplayPane;
  private final ArrayList<InternalFrameObserver> observers;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
