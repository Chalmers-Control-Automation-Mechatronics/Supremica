package net.sourceforge.waters.gui.simulator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.PropositionIcon;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.gui.ide.ModuleContainer;


public class Simulation implements ModelObserver, Observer
{

  //#########################################################################
  //# Constructors
  public Simulation(final ModuleContainer container)
  {
    mSimulationObservers = new ArrayList<SimulationObserver>();
    mAllAutomatons = new HashMap<AutomatonProxy, StateProxy>();
    mEnabledEvents = new ArrayList<EventProxy>();
    mInvalidEvents = new HashMap<EventProxy, ArrayList<AutomatonProxy>> ();
    mModuleContainer = container;
    mPreviousEvents = new ArrayList<EventProxy>();
    mEnabledLastStep = new ArrayList<AutomatonProxy>();
    mPreviousAutomatonStates = new ArrayList<HashMap<AutomatonProxy, StateProxy>>();
    mPreviousEnabledLastStep = new ArrayList<ArrayList<AutomatonProxy>>();
    mContainer = container;
    final ModuleSubject module = container.getModule();
    module.addModelObserver(this);
    container.attach(this);
    final ProductDESProxy des = container.getCompiledDES();
    setCompiledDES(des);
  }


  //#########################################################################
  //# Accessor Functions
  @SuppressWarnings("unchecked")
  public ArrayList<EventProxy> getValidTransitions()
  {
    return (ArrayList<EventProxy>)mEnabledEvents.clone();
  }

  @SuppressWarnings("unchecked")
  public HashMap<EventProxy, ArrayList<AutomatonProxy>> getInvalidEvents()
  {
    return (HashMap<EventProxy,ArrayList<AutomatonProxy>>) mInvalidEvents.clone();
  }

  @SuppressWarnings("unchecked")
  public HashMap<AutomatonProxy, StateProxy> getCurrentStates()
  {
    return (HashMap<AutomatonProxy,StateProxy>) mAllAutomatons.clone();
  }

  public List<EventProxy> getAllEvents()
  {
    if (mCompiledDES == null) {
      return Collections.emptyList();
    } else {
      final Collection<EventProxy> events = mCompiledDES.getEvents();
      final ArrayList<EventProxy> output = new ArrayList<EventProxy>();
      for (final EventProxy event : events)
        if (event.getKind() != EventKind.PROPOSITION)
          output.add(event);
      Collections.sort(output);
      return output;
    }
  }

  public String getBlockingTextual()
  {
    String output = "";
    output += "The automaton which are blocking the states are:\r\n";
    for (final EventProxy event : mInvalidEvents.keySet())
    {
      output += event.getName() + ":[";
      for (final AutomatonProxy proxy : mInvalidEvents.get(event))
      {
        output += proxy.getName() + ",";
      }
      output += "]\r\n";
    }
    return output;
  }

  public String getBlockingTextual(final EventProxy event)
  {
    String output = "";
    if (mInvalidEvents.containsKey(event))
    {
      output += "The automatons which are blocking the event " +  event.getName() + " are: [";
      for (final AutomatonProxy proxy : mInvalidEvents.get(event))
      {
        output += proxy.getName() + ",";
      }
      output += "]\r\n";
    }
    else
    {
      output += "The event " + event.getName() + " isn't blocked, and can be fired";
    }
    return output;
  }

  public Icon getMarking(final StateProxy state, final AutomatonProxy automaton)
  {
    if (!hasPropositions(automaton))
      return PropositionIcon.getDefaultMarkedIcon();
    final Collection<EventProxy> props = state.getPropositions();
    if (props.isEmpty()) {
      return PropositionIcon.getUnmarkedIcon();
    } else {
      final Map<Proxy,SourceInfo> infomap = mModuleContainer.getSourceInfoMap();
      final int size = props.size();
      final Set<Color> colorset = new HashSet<Color>(size);
      final List<Color> colorlist = new ArrayList<Color>(size);
      boolean forbidden = false;
      for (final EventProxy prop : props) {
        final SourceInfo info = infomap.get(prop);
        final EventDeclProxy decl = (EventDeclProxy) info.getSourceObject();
        final ColorGeometryProxy geo = decl.getColorGeometry();
        if (geo != null) {
          for (final Color color : geo.getColorSet()) {
            if (colorset.add(color)) {
              colorlist.add(color);
            }
          }
        } else if (decl.getName().equals
            (EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
          forbidden = true;
        } else {
          if (colorset.add(EditorColor.DEFAULTMARKINGCOLOR)) {
            colorlist.add(EditorColor.DEFAULTMARKINGCOLOR);
          }
        }
      }
      final PropositionIcon.ColorInfo colorinfo =
        new PropositionIcon.ColorInfo(colorlist, forbidden);
      return colorinfo.getIcon();
    }
  }

  public boolean changedLastStep(final AutomatonProxy automaton)
  {
    return mEnabledLastStep.contains(automaton);
  }

  public ModuleContainer getContainer()
  {
    return mContainer;
  }

  @SuppressWarnings("unchecked")
  public ArrayList<EventProxy> getEventHistory()
  {
    return (ArrayList<EventProxy>) mPreviousEvents.clone();
  }

  public ArrayList<AutomatonProxy> getAutomata()
  {
    final ArrayList<AutomatonProxy> output = new ArrayList<AutomatonProxy>();
    for (final AutomatonProxy automaton : mAllAutomatons.keySet())
      output.add(automaton);
    Collections.sort(output);
    return output;
  }

  ProductDESProxy getCompiledDES()
  {
    return mCompiledDES;
  }


  //#########################################################################
  //# Mutator Methods
  public void setState(final AutomatonProxy automaton, final StateProxy state)
  {
    if (!automaton.getStates().contains(state))
    {
      throw new IllegalArgumentException("ERROR: This state does not belong to this automaton");
    }
    if (mAllAutomatons.containsKey(automaton))
      mAllAutomatons.put(automaton, state);
    else
      throw new IllegalArgumentException("ERROR: This automaton is not in this program");
  }

  @SuppressWarnings("unchecked")
  public void singleStepMutable(final EventProxy event) throws UncontrollableException
  {
    if (event == null)
    {
      return;
    }
    if (testForControlability() != null)
    {
      final Pair<EventProxy, AutomatonProxy> invalidEvent = testForControlability();
      throw new UncontrollableException("ERROR: The event " + invalidEvent.getFirst().getName()
          + " is not controllable, inside the automaton " + invalidEvent.getSecond().getName()
          + " Current state is: " + mAllAutomatons.get(invalidEvent.getSecond()).getName());
    }
    if (isInInvalidEvent(event))
    {
      String errorMessage = "ERROR: The event " + event.getName() +
        " cannot be compiled as the following automata are blocking it:";
      for (final AutomatonProxy automata : mInvalidEvents.get(event))
        errorMessage += "\r\n" + automata.getName();
      throw new IllegalArgumentException(errorMessage);
    }
    else if (!isInValidEvent(event))
    {
      final String errorMessage = "ERROR: The event " + event.getName() +
        " cannot be completed as it is not inside any automata";
      throw new IllegalArgumentException(errorMessage);
    }
    else
    {
      mPreviousEvents.add(event);
      mPreviousAutomatonStates.add((HashMap<AutomatonProxy,StateProxy>) mAllAutomatons.clone());
      mPreviousEnabledLastStep.add(mEnabledLastStep);
      mEnabledLastStep = new ArrayList<AutomatonProxy>();
      for (final AutomatonProxy automata : mAllAutomatons.keySet())
      {
        for (final TransitionProxy trans : automata.getTransitions())
        {
          if (trans.getEvent() == event)
          {
            if (trans.getSource() == mAllAutomatons.get(automata))
              mAllAutomatons.put(automata, trans.getTarget());
            mEnabledLastStep.add(automata);
          }
        }
      }
    }
    findEventClassification();
    final SimulationChangeEvent simEvent = new SimulationChangeEvent
      (this, SimulationChangeEvent.STATE_CHANGED);
    fireSimulationChangeEvent(simEvent);
    System.out.println("Event successfully completed:" + event);
  }

  public void reverseSingleStep()
  {
    if (mPreviousEvents.size() == 0)
      throw new IllegalArgumentException("No events have been performed on this simulation yet");
    else
    {
      final EventProxy event = mPreviousEvents.get(mPreviousEvents.size() - 1);
      mPreviousEvents.remove(mPreviousEvents.size() - 1);
      mAllAutomatons = mPreviousAutomatonStates.get(mPreviousAutomatonStates.size() - 1);
      mPreviousAutomatonStates.remove(mPreviousAutomatonStates.size() - 1);
      mEnabledLastStep = mPreviousEnabledLastStep.get(mPreviousEnabledLastStep.size() - 1);
      mPreviousEnabledLastStep.remove(mPreviousEnabledLastStep.size() - 1);
      findEventClassification();
      final SimulationChangeEvent simEvent = new SimulationChangeEvent
        (this, SimulationChangeEvent.STATE_CHANGED);
      fireSimulationChangeEvent(simEvent);
      System.out.println("Event successfully completed:" + event);
    }
  }


  //#########################################################################
  //# Interface Object
  public boolean Equals(final Object e)
  {
    if (e.getClass() != Simulation.class) return false;
    final Simulation comparer = (Simulation)e;
    final HashMap<AutomatonProxy,StateProxy> comparerStates = comparer.getCurrentStates();
    for (final AutomatonProxy comparerAuto : comparerStates.keySet())
    {
      boolean found = false;
      for (final AutomatonProxy thisAuto : mAllAutomatons.keySet())
      {
        if ((comparerAuto == thisAuto) && (comparerStates.get(comparerAuto) == mAllAutomatons.get(thisAuto)))
          found = true;
      }
      if (found == false)
        return false;
    }
    return true;
  }

  public String toString()
  {
    String output = "";
    for (final AutomatonProxy auto : mAllAutomatons.keySet()){
      output += auto.getName() + " -> " + mAllAutomatons.get(auto) + "||";
    }
    return output;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  // This event is received when the user has made changes to the
  // model in the editor. The handler invalidates the current
  // compiled DES; it will be recompiled later when the simulator
  // tab is activated.
  public void modelChanged(final ModelChangeEvent event)
  {
    if (event.getKind() != ModelChangeEvent.GEOMETRY_CHANGED) {
      setCompiledDES(null);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.Observer
  // This event is received when a new tab has been activated in a
  // module container, after recompiling the module. If the activated
  // tab was the simulator, it is now safe to get an updated compiled
  // DES from the module container. After storing the new simulation
  // state in the simulator, all registered views are notified to
  // update themselves.
  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.MAINPANEL_SWITCH &&
        mModuleContainer.isSimulatorActive()) {
      final ProductDESProxy newdes = mModuleContainer.getCompiledDES();
      setCompiledDES(newdes);
    }
  }


  //#########################################################################
  //# Auxiliary Functions
  private Pair<EventProxy, AutomatonProxy> testForControlability()
  {
    for (final EventProxy event : mInvalidEvents.keySet())
    {
      if (event.getKind() == EventKind.UNCONTROLLABLE)
      {
        boolean blockingPlant = false;
        Pair<EventProxy, AutomatonProxy> blockingSpec = null;
        for (final AutomatonProxy automata : mInvalidEvents.get(event))
        {
          if (automata.getKind() == ComponentKind.SPEC)
          {
            blockingSpec =  new Pair<EventProxy, AutomatonProxy> (event, automata);
          }
          else if (automata.getKind() == ComponentKind.PLANT)
          {
            blockingPlant = true;
          }
        }
        if (!blockingPlant && blockingSpec != null)
          return blockingSpec;
      }
    }
    return null;
  }

  private boolean isInValidEvent (final EventProxy event)
  {
    for (final EventProxy validEvent : mEnabledEvents)
    {
      if (event == validEvent)
        return true;
    }
    return false;
  }

  private boolean isInInvalidEvent(final EventProxy event)
  {
    for (final EventProxy invalidEvent : mInvalidEvents.keySet())
    {
      if (invalidEvent == event)
        return true;
    }
    return false;
  }

  private void findEventClassification()
  {
    mEnabledEvents = new ArrayList<EventProxy>();
    mInvalidEvents = new HashMap<EventProxy, ArrayList<AutomatonProxy>> ();
    for (final AutomatonProxy automaton : mAllAutomatons.keySet())
    {
      for (final EventProxy event : automaton.getEvents())
      {
        boolean eventIsValidForAutomata = false;
        for (final TransitionProxy transition : automaton.getTransitions())
        {
          if (transition.getSource() == mAllAutomatons.get(automaton) && transition.getEvent() == event)
          {
            eventIsValidForAutomata = true;
            if (!isInInvalidEvent(event))
            {
              if (!isInValidEvent(event))
              {
                mEnabledEvents.add(event);
              }
            }
          }
        }
        if (!eventIsValidForAutomata)
        {
          addNewInvalidEvent(event, automaton);
        }
      }
    }
  }

  private void addNewInvalidEvent(final EventProxy event, final AutomatonProxy automaton)
  {
    if (isInInvalidEvent(event))
    {
      final ArrayList<AutomatonProxy> got = mInvalidEvents.get(event);
      got.add(automaton);
      mInvalidEvents.put(event, got);
    }
    else if (isInValidEvent(event))
    {
      mEnabledEvents.remove(event);
      final ArrayList<AutomatonProxy> failAutomaton = new ArrayList<AutomatonProxy>();
      failAutomaton.add(automaton);
      mInvalidEvents.put(event, failAutomaton);
    }
    else
    {
      final ArrayList<AutomatonProxy> failAutomaton = new ArrayList<AutomatonProxy>();
      failAutomaton.add(automaton);
      mInvalidEvents.put(event, failAutomaton);
    }
  }

  private boolean hasPropositions(final AutomatonProxy automaton)
  {
    for (final EventProxy event : automaton.getEvents())
    {
      if (event.getKind() == EventKind.PROPOSITION)
        return true;
    }
    return false;
  }

  /**
   * Stores a new compiled DES. Recomputes all data associated with it
   * and notifies registered views of the change.
   */
  private void setCompiledDES(final ProductDESProxy des)
  {
    if (des != mCompiledDES) {
      mCompiledDES = des;
      resetSimulation();
      final SimulationChangeEvent event = new SimulationChangeEvent
          (this, SimulationChangeEvent.MODEL_CHANGED);
      fireSimulationChangeEvent(event);
    }
  }

  public void attach(final SimulationObserver observer)
  {
    if (!mSimulationObservers.contains(observer)) {
      mSimulationObservers.add(observer);
    }
  }

  public void detach(final SimulationObserver observer)
  {
    mSimulationObservers.remove(observer);
  }

  private void fireSimulationChangeEvent(final SimulationChangeEvent event)
  {
    final ArrayList<SimulationObserver> temp =
      new ArrayList<SimulationObserver>(mSimulationObservers);
    for (final SimulationObserver observer : temp) {
      observer.simulationChanged(event);
    }
  }

  public void resetSimulation()
  {
    mCompiledDES = mModuleContainer.getCompiledDES();
    mAllAutomatons = new HashMap<AutomatonProxy, StateProxy>();
    mEnabledEvents = new ArrayList<EventProxy>();
    mInvalidEvents = new HashMap<EventProxy, ArrayList<AutomatonProxy>> ();
    if (mCompiledDES != null)
    {
      for (final AutomatonProxy automaton : mCompiledDES.getAutomata())
        for (final StateProxy state : automaton.getStates())
          if (state.isInitial())
            mAllAutomatons.put(automaton, state);
      findEventClassification();
    }
    mPreviousEvents = new ArrayList<EventProxy>();
    mEnabledLastStep = new ArrayList<AutomatonProxy>();
  }

  //#########################################################################
  //# Data Members
  // The Map object is the current state of the key
  private HashMap<AutomatonProxy,StateProxy> mAllAutomatons;
  private ArrayList<EventProxy> mEnabledEvents;
  private HashMap<EventProxy, ArrayList<AutomatonProxy>> mInvalidEvents; //The Map object is the list of all the Automatons which are blocking the key
  private ArrayList<EventProxy> mPreviousEvents;
  private final ArrayList<HashMap<AutomatonProxy, StateProxy>> mPreviousAutomatonStates;
  private final ArrayList<ArrayList<AutomatonProxy>> mPreviousEnabledLastStep;
  private final ModuleContainer mModuleContainer;
  private ArrayList<AutomatonProxy> mEnabledLastStep;
  private final ArrayList<SimulationObserver> mSimulationObservers;
  private ProductDESProxy mCompiledDES;
  private final ModuleContainer mContainer;
}
