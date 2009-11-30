package net.sourceforge.waters.gui.simulator;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
  public AutomatonDesktopPane(final ModuleContainer container,
                              final Simulation sim)
  {
    onReOpen(container, sim);
    sim.attach(this);
  }


  //#########################################################################
  //# Access Methods
  public boolean automatonIsOpen(final AutomatonProxy automaton)
  {
    return openAutomaton.containsKey(automaton);
  }


  //#########################################################################
  //# Mutator Methods
  public void addAutomaton(final AutomatonProxy aut,
      final ModuleContainer container, final Simulation sim, final int clicks)
  {
    if (!openAutomaton.containsKey(aut)) {
      if (clicks == 2) {
        final Map<Proxy,SourceInfo> infomap = container.getSourceInfoMap();
        final Proxy source = infomap.get(aut).getSourceObject(); // Reaches here on successful run
        if (source instanceof SimpleComponentSubject) {
          final SimpleComponentSubject comp = (SimpleComponentSubject) source;
          final GraphSubject graph = comp.getGraph();
          try {
            final AutomatonInternalFrame newFrame = new AutomatonInternalFrame
              (aut, graph, this, container, sim);
            newFrame.setLocation(findCoords(newFrame.getSize()));
            add(newFrame);
            newFrame.moveToFront();
            openAutomaton.put(aut, newFrame);
          } catch (final GeometryAbsentException exception) {
            final IDE ide = container.getIDE();
            final String msg = exception.getMessage();
            ide.error(msg);
          }
        }
      }
    } else {
      selectAutomaton(clicks, aut);
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

  public void removeAutomaton(final AutomatonProxy aut)
  {
    openAutomaton.remove(aut);
  }

  public void onReOpen(final ModuleContainer container, final Simulation sim)
  {
    for (final String name : oldOpen.keySet()) {
      final AutomatonProxy aut = sim.getAutomatonFromName(name);
      addAutomaton(aut, container, sim, 2);
    }
  }

  private void selectAutomaton(final int clicks, final AutomatonProxy aut)
  {
    try {
      final AutomatonInternalFrame frame = openAutomaton.get(aut);
      switch (clicks) {
      case 1:
        frame.setSelected(true);
        break;
      case 2:
        frame.setSelected(true);
        frame.moveToFront();
        break;
      default:
        break;
      }
    } catch (final PropertyVetoException exception) {
      // Can't select frame---too bad ...
    }
  }


  //#########################################################################
  //# Interface SimulationObserver
  public void simulationChanged(final SimulationChangeEvent event)
  {
    oldOpen.clear();
    if (event.getKind() == SimulationChangeEvent.MODEL_CHANGED) {
      final List<Map.Entry<AutomatonProxy,AutomatonInternalFrame>> entries =
        new ArrayList<Map.Entry<AutomatonProxy,AutomatonInternalFrame>>
          (openAutomaton.entrySet());
      for (final Map.Entry<AutomatonProxy,AutomatonInternalFrame> entry :
           entries) {
        final AutomatonProxy aut = entry.getKey();
        final String name = aut.getName();
        final AutomatonInternalFrame frame = entry.getValue();
        final Rectangle bounds = frame.getBounds();
        oldOpen.put(name, bounds);
        frame.dispose();
      }
      openAutomaton.clear();
    }
  }

  //#########################################################################
  //# Data Members
  private final HashMap<AutomatonProxy,AutomatonInternalFrame> openAutomaton =
    new HashMap<AutomatonProxy,AutomatonInternalFrame>();
  private final HashMap<String, Rectangle> oldOpen =
    new HashMap<String, Rectangle>();

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -5528014241244952875L;


}
