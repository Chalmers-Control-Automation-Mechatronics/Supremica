//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

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
import java.util.Collection;
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
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.compiler.efsm.EFSMCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    mObservers = new HashSet<InternalFrameObserver>();
    mPopupFactory = new SimulatorPopupFactory(sim);
    order = new ArrayList<String>();
    setBackground(EditorColor.BACKGROUNDCOLOR);
    container.attach(this);
    sim.attach(this);
    addMouseListener(new MouseListener(){
      @Override
      public void mouseClicked(final MouseEvent e)
      {
        // Do nothing
      }

      @Override
      public void mouseEntered(final MouseEvent e)
      {
        // Do nothing
      }

      @Override
      public void mouseExited(final MouseEvent e)
      {
        // Do nothing
      }

      @Override
      public void mousePressed(final MouseEvent event)
      {
        mPopupFactory.maybeShowPopup(AutomatonDesktopPane.this, event, null);
      }

      @Override
      public void mouseReleased(final MouseEvent event)
      {
        mPopupFactory.maybeShowPopup(AutomatonDesktopPane.this, event, null);
      }
    });
  }


  //#########################################################################
  //# Simple Access
  public boolean automatonIsOpen(final AutomatonProxy automaton)
  {
    return mOpenAutomata.containsKey(automaton.getName());
  }

  public void addAutomaton(final AutomatonProxy aut,
                           final ModuleContainer container,
                           final Simulation sim,
                           final int clicks)
  {
    if (aut != null) {
      final Map<String,String> attribs = aut.getAttributes();
      if (!attribs.containsKey(EFSMCompiler.ATTRIB_PLANT)) {
        final String name = aut.getName();
        addAutomaton(name, container, sim, clicks);
      }
    }
  }

  public void addAutomaton(final String name,
                           final ModuleContainer container,
                           final Simulation sim,
                           final int clicks)
  {
    if (name == null) {
      return;
    } else if (!mOpenAutomata.containsKey(name)) {
      if (clicks == 2) {
        final AutomatonProxy realAuto = sim.getAutomatonFromName(name);
        if (realAuto == null) {
          return;
        }
        final Map<Object,SourceInfo> infoMap = container.getSourceInfoMap();
        final SourceInfo info = infoMap.get(realAuto);
        final Proxy source = info.getSourceObject();
        if (source instanceof SimpleComponentSubject) {
          final SimpleComponentSubject comp = (SimpleComponentSubject) source;
          final GraphSubject graph = comp.getGraph();
          final BindingContext bindings = info.getBindingContext();
          try {
            final AutomatonInternalFrame newFrame = new AutomatonInternalFrame
              (graph, realAuto, bindings, container, sim, this);
            final ArrayList<Rectangle> otherScreens = new ArrayList<Rectangle>();
            for (final AutomatonInternalFrame automaton : mOpenAutomata.values())
              otherScreens.add(automaton.getBounds());
            newFrame.setLocation(findLocation(otherScreens, newFrame.getSize()));
            add(newFrame);
            newFrame.moveToFront();
            mOpenAutomata.put(name, newFrame);
            fireFrameOpenedEvent(name, newFrame);
            newFrame.setVisible(true);
          } catch (final GeometryAbsentException exception) {
            final Logger logger = LogManager.getLogger();
            final String msg = exception.getMessage(comp);
            logger.error(msg);
          }
        }
      }
    } else {
      selectAutomaton(clicks, name);
    }
  }

  public void removeAutomaton(final String name)
  {
    final AutomatonInternalFrame frame = mOpenAutomata.remove(name);
    fireFrameClosedEvent(name, frame);
  }

  public void onReOpen()
  {
    if (mHasBeenEdited) {
      for (final String name : order) {
        addAutomaton(name, mContainer, mSim, 2);
        final AutomatonInternalFrame frame = mOpenAutomata.get(name);
        if (frame != null) {
          frame.setPreferredSize(new Dimension((int)oldOpen.get(name).getWidth(), (int)oldOpen.get(name).getHeight()));
          frame.setLocation((int)oldOpen.get(name).getX(), (int)oldOpen.get(name).getY());
        }
      }
    }
    mHasBeenEdited = false;
  }

  private void selectAutomaton(final int clicks, final String aut)
  {
    try {
      final AutomatonInternalFrame frame = mOpenAutomata.get(aut);
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
  public int getNumberOfOpenAutomata()
  {
    return mOpenAutomata.size();
  }

  public void closeAutomaton(final String name)
  {
    mOpenAutomata.get(name).dispose();
  }

  public void closeAllAutomata()
  {
    final Collection<AutomatonInternalFrame> victims =
      new ArrayList<AutomatonInternalFrame>(mOpenAutomata.values());
    for (final AutomatonInternalFrame frame : victims) {
      frame.dispose();
    }
  }

  public void closeOtherAutomata(final String name)
  {
    final Collection<AutomatonInternalFrame> victims =
      new ArrayList<AutomatonInternalFrame>(mOpenAutomata.values());
    for (final AutomatonInternalFrame frame : victims) {
      final String fname = frame.getTitle();
      if (!name.equals(fname)) {
        frame.dispose();
      }
    }
  }

  public void showAllAutomata()
  {
    for (final AutomatonProxy aut : mSim.getOrderedAutomata()) {
      addAutomaton(aut, mContainer, mSim, 2);
    }
  }

  public void cascade()
  {
    final ArrayList<Rectangle> previousLocations = new ArrayList<Rectangle>();
    for (final AutomatonInternalFrame frame : mOpenAutomata.values())
    {
      final Point newPoint = findLocation(previousLocations, frame.getSize());
      final Rectangle newRect = new Rectangle(newPoint.x, newPoint.y, (int)frame.getSize().getWidth(), (int)frame.getSize().getHeight());
      frame.setLocation(newPoint);
      frame.toFront();
      previousLocations.add(newRect);
    }
  }

  public void execute(final String aut, final Proxy proxyToFire)
  {
    if (mOpenAutomata.get(aut) != null)
    {
      mOpenAutomata.get(aut).execute(proxyToFire);
    }
  }

  public void resizeAutomaton(final String name)
  {
    final AutomatonInternalFrame thisAutomaton = mOpenAutomata.get(name);
    if (thisAutomaton != null && canResize(name))
    {
      thisAutomaton.resize();
    }
  }

  public void resizeOther(final String name)
  {
    if (canResizeOther(name))
    {
      for (final AutomatonInternalFrame frame : mOpenAutomata.values())
      {
        if (frame.getTitle() != name && canResize(frame.getTitle()))
        {
          frame.resize();
        }
      }
    }
  }

  public void resizeAllAutomata()
  {
    if (canResizeAll())
    {
      for (final AutomatonInternalFrame frame : mOpenAutomata.values())
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
    final AutomatonInternalFrame thisAutomaton = mOpenAutomata.get(name);
    if (thisAutomaton != null)
      return thisAutomaton.canResize();
    else
      return false;
  }

  public boolean canResizeAll()
  {
    for (final AutomatonInternalFrame frame : mOpenAutomata.values())
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
    for (final AutomatonInternalFrame frame : mOpenAutomata.values())
    {
      if (frame.getTitle() != name && canResize(frame.getTitle()))
      {
        return true;
      }
    }
    return false;
  }

  public boolean canOpenOther(final String name)
  {
    if (mOpenAutomata.size() > 1)
      return true;
    else if (mOpenAutomata.size() == 0)
      return false;
    else if (mOpenAutomata.containsKey(name))
      return false;
    else
      return true;
  }


  //#########################################################################
  //# Dealing with attached InternalFrameObservers
  public void attach(final InternalFrameObserver observer)
  {
    mObservers.add(observer);
  }

  public void detach(final InternalFrameObserver observer)
  {
    mObservers.remove(observer);
  }

  private void fireFrameOpenedEvent(final String mAutomaton,
                                    final AutomatonInternalFrame opening)
  {
    final Set<InternalFrameObserver> temp =
      new HashSet<InternalFrameObserver>(mObservers);
    final InternalFrameEvent event =
      new InternalFrameEvent(mAutomaton, opening, true);
    for (final InternalFrameObserver observer : temp) {
      observer.onFrameEvent(event);
    }
  }

  private void fireFrameClosedEvent(final String mAutomaton,
                                    final AutomatonInternalFrame closing)
  {
    final Set<InternalFrameObserver> temp =
      new HashSet<InternalFrameObserver>(mObservers);
    final InternalFrameEvent event =
      new InternalFrameEvent(mAutomaton, closing, false);
    for (final InternalFrameObserver observer : temp) {
      observer.onFrameEvent(event);
    }
  }

  //#########################################################################
  //# Interface SimulationObserver
  @Override
  public void simulationChanged(final SimulationChangeEvent event)
  {
    if (event.getKind() == SimulationChangeEvent.MODEL_CHANGED) {
      if (!mHasBeenEdited) {
        mHasBeenEdited = true;
        oldOpen.clear();
        order.clear();
        if (mOpenAutomata.isEmpty()) {
          return;
        }
        // TODO This is done better by sorting the frames by z-order.
        final List<Map.Entry<String,AutomatonInternalFrame>> entries =
          new ArrayList<Map.Entry<String,AutomatonInternalFrame>>
            (mOpenAutomata.entrySet());
        while (entries.size() != 0) {
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
          oldOpen.put(name, bounds);
          order.add(name);
          frame.dispose();
          entries.remove(lowestFrame);
        }
      }
    }
  }

  //#########################################################################
  //# Interface ModelObserver
  @Override
  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.MAINPANEL_SWITCH &&
        mContainer.getActivePanel() instanceof SimulatorPanel &&
        mSim.getCompiledDES() != null) {
      onReOpen();
    }
  }


  //#########################################################################
  //# Data Members
  private final HashMap<String,AutomatonInternalFrame> mOpenAutomata =
    new HashMap<String,AutomatonInternalFrame>();
  private final HashMap<String, Rectangle> oldOpen =
    new HashMap<String, Rectangle>();
  private final ArrayList<String> order;
  private final Simulation mSim;
  private final ModuleContainer mContainer;
  private final Set<InternalFrameObserver> mObservers;
  private final SimulatorPopupFactory mPopupFactory;
  private boolean mHasBeenEdited;


  //#########################################################################
  //# Class Constants
  private static final double SCREENS_TOO_CLOSE = 3;
  private static final long serialVersionUID = -5528014241244952875L;

}
