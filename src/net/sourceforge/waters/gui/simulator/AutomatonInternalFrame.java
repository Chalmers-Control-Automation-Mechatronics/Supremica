package net.sourceforge.waters.gui.simulator;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
    mDisplayPane = new AutomatonDisplayPane(aut, graph, container, sim);
    setContentPane(mDisplayPane);
    mDisplayPane.repaint();
    addMouseListener(new InternalFrameMouseAdapter());
    setVisible(true);
    pack();
    addComponentListener();
  }

  private void addComponentListener()
  {
    aspectComponentListener = new PreserveAspectComponentListener(this, mDisplayPane);
    this.addComponentListener(aspectComponentListener);
  }

  //##########################################################################
  //# Inner class
  private class InternalFrameMouseAdapter extends MouseAdapter
  {

    //#################################################################################
    //# Class MouseAdapter
    public void mouseReleased(final MouseEvent e){
      aspectComponentListener.setBounds(new Rectangle(getBounds()));
    }

  }

  //##########################################################################
  //# Data Members
  private final AutomatonDisplayPane mDisplayPane;
  private PreserveAspectComponentListener aspectComponentListener;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
