package net.sourceforge.waters.gui.simulator;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDesktopPane;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;

public class AutomatonDesktopPane extends JDesktopPane implements SimulationObserver
{

  //#########################################################################
  //# Constructor

  public AutomatonDesktopPane(final ModuleContainer container, final Simulation mSim)
  {
    super();
    onReOpen(container, mSim);
    mSim.attach(this);
  }

  //#########################################################################
  //# Accessor Methods

  public boolean automatonIsOpen(final AutomatonProxy automaton)
  {
    return openAutomaton.containsKey(automaton);
  }
  //#########################################################################
  //# Mutator Methods
  public void addAutomaton(final String automaton,
      final ModuleContainer container, final Simulation sim, final int clicks)
  {
    if (!openAutomaton.containsKey(automaton)) {
      if (clicks == 2) {
        final Map<Proxy,SourceInfo> infomap = container.getSourceInfoMap();
        final Proxy source = infomap.get(sim.getAutomatonFromName(automaton)).getSourceObject(); // Reaches here on successful run
        if (source instanceof SimpleComponentSubject) {
          final SimpleComponentSubject comp = (SimpleComponentSubject) source;
          final GraphSubject graph = comp.getGraph();
          try {
            final AutomatonInternalFrame newFrame = new AutomatonInternalFrame
              (automaton, graph, this, container, sim);
            newFrame.setLocation(findCoords(newFrame.getSize()));
            add(newFrame);
            newFrame.moveToFront();
            openAutomaton.put(automaton, newFrame);
          } catch (final GeometryAbsentException exception) {
            final IDE ide = container.getIDE();
            final String msg = exception.getMessage();
            ide.error(msg);
          }
        }
      }
    } else {
      selectAutomaton(clicks, automaton);
    }
  }

  private Point findCoords(final Dimension size)
  {
    final ArrayList<Rectangle> bannedRegions = new ArrayList<Rectangle>();
    final ArrayList<Rectangle> otherScreens = new ArrayList<Rectangle>();
    for (final AutomatonInternalFrame automaton : openAutomaton.values())
      otherScreens.add(automaton.getBounds());
    for (int y = 0; y < this.getHeight() - size.getHeight(); y++)
    {
      for (int x = 0; x < this.getWidth() - size.getWidth(); x++)
      {
        boolean failed = false;
        final Rectangle2D thisFrame = new Rectangle(x, y, (int)size.getWidth(), (int)size.getHeight());
        final Area thisArea = new Area(thisFrame);
        for (final Rectangle screen : otherScreens)
        {
          if (thisArea.intersects(screen))
          {
            final Rectangle newBanned = new Rectangle
            (x, y, (int) (screen.getWidth() + (screen.getX() - thisArea.getBounds().getX()))
                , (int) (screen.getHeight() + (screen.getY() - thisArea.getBounds().getY())));
            bannedRegions.add(newBanned);
          }
        }
        for (final Rectangle banned : bannedRegions)
        {
          if (thisArea.intersects(banned))
          {
            x = (int) (banned.getX() + banned.getWidth());
            failed = true;
          }
        }
        if (!failed)
          return new Point(x, y);
      }
    }
    return new Point(0,0);
  }

  public void removeAutomaton(final String automaton)
  {
    if (openAutomaton.containsKey(automaton))
    {
      openAutomaton.remove(automaton);
    }
  }

  public void onReOpen(final ModuleContainer container, final Simulation mSim)
  {
    for (final String proxy : oldOpen.keySet())
    {
      addAutomaton(proxy, container, mSim, 2);
    }
  }

  private void selectAutomaton(final int clicks, final String automaton)
  {
    if (clicks == 1)
    {
      try {
        openAutomaton.get(automaton).setSelected(true);
      } catch (final PropertyVetoException exception) {
        // TODO Auto-generated catch block
        exception.printStackTrace();
      }
    }
    else if (clicks == 2)
    {
      try {
        openAutomaton.get(automaton).setSelected(true);
      } catch (final PropertyVetoException exception) {
        // TODO Auto-generated catch block
        exception.printStackTrace();
      }
      openAutomaton.get(automaton).moveToFront();
    }
  }

  //#########################################################################
  //# Interface SimulationObserver
  public void simulationChanged(final SimulationChangeEvent event)
  {
    oldOpen = new HashMap<String, Rectangle>();
    if (event.getKind() == SimulationChangeEvent.MODEL_CHANGED)
    {
      for (final String automaton : openAutomaton.keySet())
      {
        oldOpen.put(automaton, openAutomaton.get(automaton).getBounds());
        openAutomaton.get(automaton).dispose();
        removeAutomaton(automaton);
      }
    }
  }

  //#########################################################################
  //# Data Members
  HashMap<String, AutomatonInternalFrame> openAutomaton = new HashMap<String, AutomatonInternalFrame>();
  HashMap<String, Rectangle> oldOpen = new HashMap<String, Rectangle>();

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -5528014241244952875L;


}
