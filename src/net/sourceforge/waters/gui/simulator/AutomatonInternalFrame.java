package net.sourceforge.waters.gui.simulator;

import java.awt.geom.Rectangle2D;

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
    mDesktopParent = parent;
    mAutomaton = aut;
    mDisplayPane = new AutomatonDisplayPane(aut, graph, container, sim);
    setContentPane(mDisplayPane);
    addMouseListener(new InternalFrameMouseAdapter(this));
    setVisible(true);
    pack();
  }


  //##########################################################################
  //# Class JInternalFrame
  public void dispose()
  {
    mDisplayPane.close();
    mDesktopParent.removeAutomaton(mAutomaton);
    super.dispose();
  }

  public void reshape(final int x, final int y, final int wantedWidth, final int wantedHeight)
  {
    final Rectangle2D preferredSize = mDisplayPane.getMinimumBoundingRectangle();
    final int preferredWidth = (int) preferredSize.getWidth();
    final int preferredHeight = (int) preferredSize.getHeight();
    final int finalWidth = ((wantedWidth + wantedHeight) * preferredWidth) / (preferredHeight + preferredWidth);
    final int finalHeight = (preferredHeight * finalWidth) / preferredWidth;
    super.reshape(x, y, finalWidth, finalHeight);
    super.repaint();
  }


  //##########################################################################
  //# Data Members
  private final AutomatonProxy mAutomaton;
  private final AutomatonDisplayPane mDisplayPane;
  private final AutomatonDesktopPane mDesktopParent;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
