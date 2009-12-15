package net.sourceforge.waters.gui.simulator;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JDesktopPane;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;

public class AutomatonDesktopPane extends JDesktopPane implements SimulationObserver, Observer
{
  //#########################################################################
  //# Constructor
  public AutomatonDesktopPane(final ModuleContainer container,
                              final Simulation sim)
  {
    sim.attach(this);
    container.attach(this);
    mSim = sim;
    mContainer = container;
    observers = new HashSet<InternalFrameObserver>();
  }


  //#########################################################################
  //# Simple Access

  public boolean automatonIsOpen(final AutomatonProxy automaton)
  {
    return openAutomaton.containsKey(automaton.getName());
  }

  public void addAutomaton(final String aut,
      final ModuleContainer container, final Simulation sim, final int clicks)
  {
    if (aut == null)
      return;
    if (!openAutomaton.containsKey(aut)) {
      if (clicks == 2) {
        final Map<Proxy,SourceInfo> infomap = container.getSourceInfoMap();
        final AutomatonProxy realAuto = sim.getAutomatonFromName(aut);
        if (realAuto == null)
          return;
        final Proxy source = infomap.get(realAuto).getSourceObject();
        if (source instanceof SimpleComponentSubject) {
          final SimpleComponentSubject comp = (SimpleComponentSubject) source;
          final GraphSubject graph = comp.getGraph();
          try {
            final AutomatonInternalFrame newFrame = new AutomatonInternalFrame
              (realAuto, graph, this, container, sim);
            newFrame.setLocation(findCoords(newFrame.getSize()));
            add(newFrame);
            newFrame.moveToFront();
            openAutomaton.put(aut, newFrame);
            fireFrameOpenedEvent(aut, newFrame);
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

  public void removeAutomaton(final String aut)
  {
    fireFrameClosedEvent(aut, openAutomaton.get(aut));
    openAutomaton.remove(aut);
  }

  public void onReOpen(final ModuleContainer container, final Simulation sim)
  {
    for (final String name : oldOpen.keySet()) {
      addAutomaton(name, container, sim, 2);
      if (openAutomaton.get(name) != null)
      {
        openAutomaton.get(name).setPreferredSize(new Dimension((int)oldOpen.get(name).getWidth(), (int)oldOpen.get(name).getHeight()));
        openAutomaton.get(name).setLocation((int)oldOpen.get(name).getX(), (int)oldOpen.get(name).getY());
      }
    }
  }

  private void selectAutomaton(final int clicks, final String aut)
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
  //# Dealing with attached InternalFrameObservers

  public void attach (final InternalFrameObserver observer)
  {
    observers.add(observer);
  }

  public void detach (final InternalFrameObserver observer)
  {
    observers.remove(observer);
  }

  private void fireFrameOpenedEvent(final String mAutomaton, final AutomatonInternalFrame opening)
  {
    final Set<InternalFrameObserver> temp =
      new HashSet<InternalFrameObserver>(observers);
    for (final InternalFrameObserver observer : temp)
    {
      observer.onFrameEvent(new InternalFrameEvent(mAutomaton, opening, true));
    }
  }

  private void fireFrameClosedEvent(final String mAutomaton, final AutomatonInternalFrame closing)
  {
    final Set<InternalFrameObserver> temp =
      new HashSet<InternalFrameObserver>(observers);
    for (final InternalFrameObserver observer : temp)
    {
      observer.onFrameEvent(new InternalFrameEvent(mAutomaton, closing, false));
    }
  }


  //#########################################################################
  //# Interface SimulationObserver
  public void simulationChanged(final SimulationChangeEvent event)
  {
    if (event.getKind() == SimulationChangeEvent.MODEL_CHANGED) {
      if (openAutomaton.keySet().size() == 0)
        return;
      oldOpen.clear();
      final List<Map.Entry<String,AutomatonInternalFrame>> entries =
        new ArrayList<Map.Entry<String,AutomatonInternalFrame>>
          (openAutomaton.entrySet());
      for (final Map.Entry<String,AutomatonInternalFrame> entry :
           entries) {
        final String name = entry.getKey();
        final AutomatonInternalFrame frame = entry.getValue();
        final Rectangle bounds = frame.getBounds();
        oldOpen.put(name, bounds);
        frame.dispose();
      }
      openAutomaton.clear();
    }
  }

  //#########################################################################
  //# Interface ModelObserver


  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.MAINPANEL_SWITCH)
    onReOpen(mContainer, mSim);
  }

  //#########################################################################
  //# Data Members

  private final HashMap<String,AutomatonInternalFrame> openAutomaton =
    new HashMap<String,AutomatonInternalFrame>();
  private final HashMap<String, Rectangle> oldOpen =
    new HashMap<String, Rectangle>();
  private final Simulation mSim;
  private final ModuleContainer mContainer;
  private final Set<InternalFrameObserver> observers;

  //#########################################################################
  //# Class Constants

  private static final long serialVersionUID = -5528014241244952875L;

}
