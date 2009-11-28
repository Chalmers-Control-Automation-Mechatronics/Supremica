package net.sourceforge.waters.gui.simulator;

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
  }

  //#########################################################################
  //# Mutator Methods
  public void addAutomaton(final AutomatonProxy automaton,
      final ModuleContainer container, final Simulation sim, final int clicks)
  {
    if (!openAutomaton.containsKey(automaton)) {
      if (clicks == 2) {
        final Map<Proxy,SourceInfo> infomap = container.getSourceInfoMap();
        final Proxy source = infomap.get(automaton).getSourceObject();
        if (source instanceof SimpleComponentSubject) {
          final SimpleComponentSubject comp = (SimpleComponentSubject) source;
          final GraphSubject graph = comp.getGraph();
          try {
            final AutomatonInternalFrame newFrame = new AutomatonInternalFrame
              (automaton, graph, this, container, sim);
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

  public void removeAutomaton(final AutomatonProxy automaton)
  {
    if (openAutomaton.containsKey(automaton))
      openAutomaton.remove(automaton);
  }

  public void onReOpen(final ModuleContainer container, final Simulation mSim)
  {
    for (final AutomatonProxy proxy : oldOpen)
    {
      addAutomaton(proxy, container, mSim, 2);
    }
  }

  private void selectAutomaton(final int clicks, final AutomatonProxy automaton)
  {
    if (clicks == 1)
    {
      openAutomaton.get(automaton).setFocusable(true);
    }
    else if (clicks == 2)
    {
      openAutomaton.get(automaton).setFocusable(true);
      openAutomaton.get(automaton).moveToFront();
    }
  }

  //#########################################################################
  //# Interface SimulationObserver
  public void simulationChanged(final SimulationChangeEvent event)
  {
    oldOpen = new ArrayList<AutomatonProxy>();
    if (event.getKind() == SimulationChangeEvent.MODEL_CHANGED)
    {
      for (final AutomatonProxy automaton : openAutomaton.keySet())
      {
        oldOpen.add(automaton);
        openAutomaton.get(automaton).dispose();
        removeAutomaton(automaton);
      }
    }
  }

  //#########################################################################
  //# Data Members
  HashMap<AutomatonProxy, AutomatonInternalFrame> openAutomaton = new HashMap<AutomatonProxy, AutomatonInternalFrame>();
  ArrayList<AutomatonProxy> oldOpen = new ArrayList<AutomatonProxy>();

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -5528014241244952875L;


}
