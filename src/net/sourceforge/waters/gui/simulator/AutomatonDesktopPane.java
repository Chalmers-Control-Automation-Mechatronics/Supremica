package net.sourceforge.waters.gui.simulator;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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

import net.sourceforge.waters.gui.EditorColor;
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


public class AutomatonDesktopPane
  extends JDesktopPane
  implements SimulationObserver, Observer
{

  //#########################################################################
  //# Constructor
  public AutomatonDesktopPane(final ModuleContainer container,
                              final Simulation sim)
  {
    mSim = sim;
    mContainer = container;
    observers = new HashSet<InternalFrameObserver>();
    factory = new DesktopPanePopupFactory(container.getIDE().getPopupActionManager());
    setBackground(EditorColor.BACKGROUNDCOLOR);
    sim.attach(this);
    container.attach(this);
    this.addMouseListener(new MouseListener(){

      public void mouseClicked(final MouseEvent e)
      {
        // Do nothing
      }

      public void mouseEntered(final MouseEvent e)
      {
        // Do nothing
      }

      public void mouseExited(final MouseEvent e)
      {
        // Do nothing
      }

      public void mousePressed(final MouseEvent e)
      {
        factory.maybeShowPopup(AutomatonDesktopPane.this, e, null);
      }

      public void mouseReleased(final MouseEvent e)
      {
        // Do nothing
      }

    });
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
    /*String DEBUG = "DEBUG: AutomatonDesktopPane [92]: Automaton Current State list is {";
    for (final AutomatonProxy auto : sim.getCurrentStates().keySet())
    {
      DEBUG += auto.getName() + " -> " + sim.getCurrentStates().get(auto).getName() + ",";
    }
    DEBUG += "}";
    System.out.println(DEBUG);
    */
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
            final ArrayList<Rectangle> otherScreens = new ArrayList<Rectangle>();
            for (final AutomatonInternalFrame automaton : openAutomaton.values())
              otherScreens.add(automaton.getBounds());
            newFrame.setLocation(findLocation(otherScreens, newFrame.getSize()));
            add(newFrame);
            newFrame.moveToFront();
            openAutomaton.put(aut, newFrame);
            fireFrameOpenedEvent(aut, newFrame);
            newFrame.setVisible(true);
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

  private Set<String> copySet(final Set<String> toCopy)
  {
    final Set<String> output = new HashSet<String>();
    for (final String copy : toCopy)
    {
      output.add(copy);
    }
    return output;
  }

  private Point findLocation(final ArrayList<Rectangle> bannedLocations, final Dimension newSize)
  {
    final ArrayList<Rectangle> bannedRegions = new ArrayList<Rectangle>();
    final ArrayList<Rectangle> otherScreens = bannedLocations;
    for (int y = 0; y < this.getHeight() - newSize.getHeight(); y++)
    {
      for (int x = 0; x < this.getWidth() - newSize.getWidth(); x++)
      {
        boolean failed = false;
        final Rectangle2D thisFrame = new Rectangle(x, y, (int)newSize.getWidth(), (int)newSize.getHeight());
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
    for (int coords = 0; coords < Math.min(this.getHeight(), this.getWidth()); coords += 30)
    {
      boolean fail = false;
      for (final Rectangle rect : otherScreens)
      {
        if (Math.abs(rect.getX() - coords) < SCREENS_TOO_CLOSE && Math.abs(rect.getY() - coords) < SCREENS_TOO_CLOSE)
          fail = true;
      }
      if (!fail)
        return new Point(coords, coords);
    }
    return new Point(0,0);
  }

  //#########################################################################
  //# Event Access Methods

  public void closeAutomaton(final String aut)
  {
    openAutomaton.get(aut).dispose();
  }

  public void closeAllAutomaton()
  {
    final Set<String> copySet = copySet(openAutomaton.keySet());
    for (final String string : copySet)
      openAutomaton.get(string).dispose();
  }

  public void closeOtherAutomaton(final String aut)
  {
    final Set<String> copySet = copySet(openAutomaton.keySet());
    for (final String string : copySet)
      if (string.compareTo(aut) != 0)
        openAutomaton.get(string).dispose();
  }

  public void openOtherAutomaton(final String name)
  {
    final List<AutomatonProxy> otherAutomata = mSim.getAutomata();
    for (final AutomatonProxy auto : otherAutomata)
      if (name.compareTo(auto.getName()) != 0)
        addAutomaton(auto.getName(), mContainer, mSim, 2);
  }

  public void showAllAutomata()
  {
    for (final AutomatonProxy automata : mSim.getAutomata())
    {
      addAutomaton(automata.getName(), mContainer, mSim, 2);
    }
  }

  public void cascade()
  {
    final ArrayList<Rectangle> previousLocations = new ArrayList<Rectangle>();
    for (final AutomatonInternalFrame frame : openAutomaton.values())
    {
      final Point newPoint = findLocation(previousLocations, frame.getSize());
      final Rectangle newRect = new Rectangle(newPoint.x, newPoint.y, (int)frame.getSize().getWidth(), (int)frame.getSize().getHeight());
      frame.setLocation(newPoint);
      frame.toFront();
      previousLocations.add(newRect);
    }
  }

  public void execute (final String aut)
  {
    if (openAutomaton.get(aut) != null)
    {
      openAutomaton.get(aut).execute();
    }
  }

  public void resizeAutomaton(final String name)
  {
    final AutomatonInternalFrame thisAutomaton = openAutomaton.get(name);
    if (thisAutomaton != null && canResize(name))
    {
      thisAutomaton.resize();
    }
  }

  public void resizeOther(final String name)
  {
    if (canResizeOther(name))
    {
      for (final AutomatonInternalFrame frame : openAutomaton.values())
      {
        if (frame.getTitle() != name && canResize(frame.getTitle()))
        {
          frame.resize();
        }
      }
    }
  }

  public void resizeAllAutomaton()
  {
    if (canResizeAll())
    {
      for (final AutomatonInternalFrame frame : openAutomaton.values())
      {
        if (canResize(frame.getTitle()))
        {
          frame.resize();
        }
      }
    }
  }

  public boolean canResize(final String name)
  {
    final AutomatonInternalFrame thisAutomaton = openAutomaton.get(name);
    if (thisAutomaton != null)
      return thisAutomaton.canResize();
    else
      return false;
  }

  public boolean canResizeAll()
  {
    for (final AutomatonInternalFrame frame : openAutomaton.values())
    {
      if (canResize(frame.getTitle()))
      {
        return true;
      }
    }
    return false;
  }

  public boolean canResizeOther(final String name)
  {
    for (final AutomatonInternalFrame frame : openAutomaton.values())
    {
      if (frame.getTitle() != name && canResize(frame.getTitle()))
      {
        return true;
      }
    }
    return false;
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
      while (entries.size() != 0)
      {
        int highestZValue = Integer.MIN_VALUE;
        Map.Entry<String,AutomatonInternalFrame> lowestFrame = null;
        for (final Map.Entry<String,AutomatonInternalFrame> entry :
             entries) {
          final AutomatonInternalFrame frame = entry.getValue();
          if (this.getComponentZOrder(frame) > highestZValue)
          {
            highestZValue = this.getComponentZOrder(frame);
            lowestFrame = entry;
          }
        }
        final String name = lowestFrame.getKey();
        final AutomatonInternalFrame frame = lowestFrame.getValue();
        final Rectangle bounds = frame.getBounds();
        System.out.println("DEBUG: AutomatonDesktopPane [413]: " + frame.getTitle() + " has bounds " + frame.getBounds());
        oldOpen.put(name, bounds);
        frame.dispose();
        entries.remove(lowestFrame);
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
  private final DesktopPanePopupFactory factory;

  //#########################################################################
  //# Class Constants

  private static final double SCREENS_TOO_CLOSE = 3;
  private static final long serialVersionUID = -5528014241244952875L;
}
