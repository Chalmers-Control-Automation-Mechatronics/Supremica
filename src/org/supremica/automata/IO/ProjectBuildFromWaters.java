//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata.IO;

import gnu.trove.set.hash.THashSet;

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.PropertySuppressionKindTranslator;
import net.sourceforge.waters.model.analysis.SynthesisKindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.DefaultProjectFactory;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.automata.ProjectFactory;
import org.supremica.automata.State;
import org.supremica.properties.Config;


/**
 * A converter that translates the WATERS {@link ProductDESProxy} interfaces
 * into Supremica's {@link Project} classes.
 *
 * @author Knut &Aring;kesson, Robi Malik
 */
public class ProjectBuildFromWaters
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a WATERS-to-Supremica converter using a default project
   * factory.
   * @param manager
   *          The document manager used when compiling Waters modules. To
   *          convert modules, this argument must be provided and reference a
   *          document manager that is properly initialised to load modules.
   *          To support proper caching, the document manager should be shared
   *          throughout the application.
   */
  public ProjectBuildFromWaters(final DocumentManager manager)
  {
    this(manager, new DefaultProjectFactory());
  }

  /**
   * Creates a WATERS-to-Supremica converter.
   * @param manager
   *          The document manager used when compiling Waters modules. To
   *          convert modules, this argument must be provided and reference a
   *          document manager that is properly initialised to load modules.
   *          To support proper caching, the document manager should be shared
   *          throughout the application.
   * @param factory
   *          The project factory used to create the Supremica automata.
   */
  public ProjectBuildFromWaters(final DocumentManager manager,
                                final ProjectFactory factory)
  {
    mDocumentManager = manager;
    mProjectFactory = factory;
    mWarnings = new LinkedList<String>();
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets whether this project builder also converts property automata
   * found in the Waters input. If <CODE>true</CODE>, properties are
   * converted, otherwise if <CODE>false</CODE> (the default), properties
   * are suppressed. This option is implemented by setting a
   * {@link KindTranslator}.
   * @see #setKindTranslator(KindTranslator)
   */
  public void setIncludesProperties(final boolean include)
  {
    mKindTranslator = include ?
      IdenticalKindTranslator.getInstance() :
      PropertySuppressionKindTranslator.getInstance();
  }

  /**
   * Sets a kind translator to convert component and event kinds.
   * A kind translator can mask component and event types, e.g., redefining
   * supervisors as specifications, and suppress the generation of
   * events or components by assigning a <CODE>null</CODE> type.
   * @see KindTranslator
   * @see EventKind
   * @see ComponentKind
   */
  public void setKindTranslator(final KindTranslator translator)
  {
    mKindTranslator = translator;
  }

  /**
   * Returns the kind translator used to convert component and event kinds.
   * @see #setKindTranslator(KindTranslator)
   */
  public KindTranslator getKindTranslator()
  {
    return mKindTranslator;
  }

  /**
   * Sets the proposition that defines whether states are accepting.
   * While Waters supports multiple propositions, Supremica only allows
   * states to be accepting or not. If a default marking is configured
   * with this methods, states marked by this proposition will be
   * marked as accepting in the generated Supremica {@link Automata}.
   * Otherwise (or if the configured default marking is <CODE>null</CODE>,
   * which is the default), a proposition with the default name
   * {@link EventDeclProxy#DEFAULT_MARKING_NAME} will be used. In the latter
   * case, if the model does not contain a proposition with the default name,
   * all states in the Supremica {@link Automata} will be accepting.
   * @param  marking  The marking proposition to be used, or <CODE>null</CODE>
   *                  to look for the default marking name.
   */
  public void setConfiguredDefaultMarking(final EventProxy marking)
  {
    mConfiguredDefaultMarking = marking;
  }

  /**
   * Returns the proposition that defines whether states are accepting.
   * @see #setConfiguredDefaultMarking(EventProxy)
   */
  public EventProxy getConfiguredDefaultMarking()
  {
    return mConfiguredDefaultMarking;
  }

  /**
   * <P>Sets how uncontrollable events that appear only in the specification
   * are converted.</P>
   *
   * <P>Supremica treats controllable events that appear in the specification
   * but not in the plant differently from Waters. Waters assumes such events
   * to be always enabled by the plant, so any specification that disables
   * them in some state will be uncontrollable. Supremica assumes such events
   * to be enabled in the plant if and only if they are enabled in the
   * specification.</P>
   *
   * <P>If this option is enabled, the generated {@link Automata} object will
   * include an additional one-state plant component with selfloops for all
   * uncontrollable event that appears in the specification but not in the
   * plant. This ensures that Supremica's algorithms produce the same result
   * as Waters. If the option is disabled (the default), the Waters model is
   * converted without additions, thus resulting in Supremoica's
   * controllability semantics.</P>
   */
  public void setEnsuringUncontrollablesInPlant(final boolean ensure)
  {
    mEnsuresUncontrollablesInPlant = ensure;
  }

  /**
   * Returns how uncontrollable events that appear only in the specification
   * are converted.
   * @see #setEnsuringUncontrollablesInPlant(boolean)
   */
  public boolean isEnsuringUncontrollablesInPlant()
  {
    return mEnsuresUncontrollablesInPlant;
  }


  //#########################################################################
  //# Simple Access
  public List<String> getWarnings()
  {
    return Collections.unmodifiableList(mWarnings);
  }


  //#########################################################################
  //# Invocation
  /**
   * Converts a WATERS module to a Supremica project. This method uses a
   * compiler to translate and instantiate the module, and then converts the
   * resulting product DES.
   *
   * @param module
   *          The WATERS module to be converted.
   * @return The module in Supremica form.
   * @throws EvalException
   *           to indicate that compilation of the module has failed.
   */
  public Project build(final ModuleProxy module) throws EvalException
  {
    if (module == null) {
      throw new NullPointerException("NULL module passed to ProjectBuildFromWaters.build()!");
    }
    final ProductDESProxyFactory factory =
      ProductDESElementFactory.getInstance();
    final boolean optimize = Config.OPTIMIZING_COMPILER.isTrue();
    final boolean expand = Config.EXPAND_EXTENDED_AUTOMATA.isTrue();
    final boolean normalize = Config.NORMALIZING_COMPILER.isTrue();
    final boolean autVars = Config.AUTOMATON_VARIABLES_COMPILER.isTrue();
    final ModuleCompiler compiler =
      new ModuleCompiler(mDocumentManager, factory, module);
    compiler.setOptimizationEnabled(optimize);
    compiler.setExpandingEFATransitions(expand);
    compiler.setNormalizationEnabled(normalize);
    compiler.setAutomatonVariablesEnabled(autVars);
    if (mKindTranslator instanceof PropertySuppressionKindTranslator ||
        mKindTranslator instanceof SynthesisKindTranslator) {
      final Collection<String> empty = Collections.emptyList();
      compiler.setEnabledPropertyNames(empty);
    }
    final ProductDESProxy des = compiler.compile();
    return build(des);
  }

  /**
   * Converts a WATERS product DES to a Supremica project.
   *
   * @param des
   *          The WATERS product DES to be converted.
   * @return The product DES in Supremica form.
   * @throws EvalException
   *           to indicate that the model could not be converted due to WATERS
   *           features not supported by Supremica.
   */
  public Project build(final ProductDESProxy des) throws EvalException
  {
    mWarnings.clear();
    try {
      final Collection<EventProxy> events = des.getEvents();
      if (mEnsuresUncontrollablesInPlant) {
        final int numEvents = events.size();
        mPlantUncontrollableEvents = new ArrayList<>(numEvents);
        mSpecUncontrollableEvents = new THashSet<>(numEvents);
      }
      findMarkingEvents(events);
      final Project currProject = mProjectFactory.getProject();
      currProject.setName(des.getName());
      currProject.setComment(des.getComment());
      for (final AutomatonProxy aut : des.getAutomata()) {
        if (mKindTranslator.getComponentKind(aut) != null) {
          final Automaton supaut = buildWithMarkings(aut);
          currProject.addAutomaton(supaut);
        }
      }
      final Automaton extraAut = buildPlantWithExtraUncontrollables();
      if (extraAut != null) {
        currProject.addAutomaton(extraAut);
      }
      return currProject;
    } finally {
      mUsedDefaultMarking = mUsedForbiddenMarking = null;
      mPlantUncontrollableEvents = mSpecUncontrollableEvents = null;
    }
  }

  /**
   * Converts a WATERS automaton to a Supremica automaton.
   *
   * @param aut
   *          The WATERS automaton to be converted.
   * @return The automaton in Supremica form.
   * @throws EvalException
   *           to indicate that the model could not be converted due to WATERS
   *           features not supported by Supremica.
   */
  public Automaton build(final AutomatonProxy aut) throws EvalException
  {
    try {
      findMarkingEvents(aut.getEvents());
      return buildWithMarkings(aut);
    } finally {
      mUsedDefaultMarking = mUsedForbiddenMarking = null;
    }
  }

  public Automaton buildWithMarkings(final AutomatonProxy aut)
    throws EvalException
  {
    final Automaton supaut = new Automaton(aut.getName());
    supaut.setCorrespondingAutomatonProxy(aut);

    //System.err.println("Automaton: " + aut.getName());
    final ComponentKind compKind = mKindTranslator.getComponentKind(aut);
    supaut.setType(AutomatonType.toType(compKind));

    // Create the alphabet
    boolean marking = false;
    final Alphabet currSupremicaAlphabet = supaut.getAlphabet();
    for (final EventProxy currWatersEvent : aut.getEvents()) {
      final EventKind eventKind =
        mKindTranslator.getEventKind(currWatersEvent);
      if (eventKind == null) {
        // skip
      } else if (eventKind == EventKind.PROPOSITION) {
        marking |= (currWatersEvent == mUsedDefaultMarking);
      } else {
        recordUncontrollableEvent(currWatersEvent, compKind);
        final LabeledEvent currSupremicaEvent =
          new LabeledEvent(currWatersEvent);
        currSupremicaAlphabet.addEvent(currSupremicaEvent);
      }
    }

    // Create states
    for (final StateProxy currWatersState : aut.getStates()) {
      final State currSupremicaState = new State(currWatersState.getName());
      // Set attributes
      // Initial?
      currSupremicaState.setInitial(currWatersState.isInitial());
      // Find marked status (only one type of marking here!!!)
      for (final EventProxy event : currWatersState.getPropositions()) {
        if (event == mUsedDefaultMarking) {
          currSupremicaState.setAccepting(true);
        } else if (event == mUsedForbiddenMarking) {
          currSupremicaState.setForbidden(true);
        }
      }
      // If the marking proposition is not in alphabet: mark all states!
      if (!marking) {
        currSupremicaState.setAccepting(true);
      }
      // Add to automaton
      supaut.addState(currSupremicaState);
    }

    // Create transitions
    for (final TransitionProxy currWatersTransition : aut.getTransitions()) {
      final StateProxy watersSourceState = currWatersTransition.getSource();
      final StateProxy watersTargetState = currWatersTransition.getTarget();
      final EventProxy watersEvent = currWatersTransition.getEvent();
      final State supremicaSourceState =
        supaut.getStateWithName(watersSourceState.getName());
      final State supremicaTargetState =
        supaut.getStateWithName(watersTargetState.getName());
      final LabeledEvent supremicaEvent =
        currSupremicaAlphabet.getEvent(watersEvent.getName());
      final Arc currSupremicaArc =
        new Arc(supremicaSourceState, supremicaTargetState, supremicaEvent);
      supaut.addArc(currSupremicaArc);
    }

    addCostToStates(supaut);
    addProbabilityToTransitions(supaut);

    return supaut;
  }

  /**
   * Converts a collection of WATERS events to a Supremica alphabet.
   *
   * @param events
   *          The WATERS events to be converted.
   * @return An alphabet containing Supremica event labels corresponding to
   *         all the given events, except for the propositions.
   */
  public Alphabet buildAlphabet(final Collection<? extends EventProxy> events)
  {
    final Alphabet alphabet = new Alphabet();
    for (final EventProxy event : events) {
      final EventKind kind = mKindTranslator.getEventKind(event);
      if (kind != EventKind.PROPOSITION) {
        final LabeledEvent label = new LabeledEvent(event);
        alphabet.addEvent(label);
      }
    }
    return alphabet;
  }

  /**
   * Goes through the states of the supplied automaton and adds costs if the
   * code name, "cost", is found in the WATERS product DES.
   *
   * @param aut
   *          The automaton that may need addition of cost to its states
   */
  private void addCostToStates(final Automaton aut) throws EvalException
  {
    for (final Iterator<State> stateIt = aut.iterator(); stateIt.hasNext();) {
      final State state = stateIt.next();
      final String stateName = state.getName();
      if (stateName.contains("cost") && stateName.contains("=")) {
        final int pivotIndex = stateName.indexOf("cost");
        String prefixStr = stateName.substring(0, pivotIndex);
        String suffixStr = stateName.substring(pivotIndex);
        double costValue = -1;

        // Find the first numerical value, following the 'cost'-keyword (that is our state cost)
        try {
          final StreamTokenizer tokenizer =
            new StreamTokenizer(new StringReader(suffixStr));
          tokenizer.parseNumbers();

          int type = tokenizer.nextToken();
          while (type != StreamTokenizer.TT_EOF) {
            if (type == StreamTokenizer.TT_NUMBER) {
              costValue = tokenizer.nval;
            }

            type = tokenizer.nextToken();
          }
        } catch (final Exception e) {
          e.printStackTrace();
        }

        if (costValue == -1) {
          throw new EvalException("The cost, defined in state '" + stateName
                                  + "', could not be parsed");
        } else {
          // Remove comma before the 'cost'-keyword
          if (prefixStr.trim().endsWith(",")) {
            prefixStr = prefixStr.substring(0, prefixStr.lastIndexOf(","));
          }

          // Find the actual cost string (note that integer cost values must be re-casted into int.
          String costStr = "cost=" + costValue;
          if (!suffixStr.startsWith(costStr)) {
            costStr = "cost=" + (int) costValue;
          }

          // Remove the cost string from the suffix and construct the state name
          suffixStr = suffixStr.substring(costStr.length());
          state.setName(prefixStr + suffixStr);
          // Set the state cost
          state.setCost(costValue);
        }
      }
    }
  }

  private void addProbabilityToTransitions(final Automaton aut)
  {
    final ArrayList<Arc> arcsToBeRemoved = new ArrayList<Arc>();
    final ArrayList<Arc> arcsToBeAdded = new ArrayList<Arc>();

    for (final Iterator<Arc> arcIt = aut.arcIterator(); arcIt.hasNext();) {
      final Arc arc = arcIt.next();
      String label = arc.getLabel();
      if (label.contains("prob_")) {
        Double percentage = null;
        try {
          percentage = new Double(label
            .substring(label.lastIndexOf("prob_") + 5).trim());
        } catch (final NumberFormatException e) {
          logger.error("Parsing of transition named " + label + " failed.");
        }

        if (percentage != null) {
          label = label.substring(0, label.indexOf("prob_")).trim();
          if (label.endsWith("_")) {
            label = label.substring(0, label.length() - 1);
          }

          final LabeledEvent newEvent = new LabeledEvent(label);
          final Arc newArc = new Arc(arc.getSource(), arc.getTarget(),
                                     newEvent, percentage / 100);
          if (!aut.getAlphabet().contains(newEvent)) {
            aut.getAlphabet().addEvent(newEvent);
          }

          arcsToBeAdded.add(newArc);
          arcsToBeRemoved.add(arc);
        }
      }
    }

    for (int i = 0; i < arcsToBeRemoved.size(); i++) {
      aut.removeArc(arcsToBeRemoved.get(i));
    }
    for (int i = 0; i < arcsToBeAdded.size(); i++) {
      aut.addArc(arcsToBeAdded.get(i));
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void findMarkingEvents(final Collection<EventProxy> events)
  {
    if (mConfiguredDefaultMarking != null) {
      mUsedDefaultMarking = mConfiguredDefaultMarking;
    }
    StringBuilder warning = null;
    for (final EventProxy event : events) {
      final EventKind kind = mKindTranslator.getEventKind(event);
      if (kind == EventKind.PROPOSITION) {
        final String name = event.getName();
        if (name.equals(EventDeclProxy.DEFAULT_MARKING_NAME)) {
          if (mConfiguredDefaultMarking == null) {
            mUsedDefaultMarking = event;
          }
        } else if (name.equals(EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
          mUsedForbiddenMarking = event;
        } else if (mConfiguredDefaultMarking == null) {
          if (warning == null) {
            warning = new StringBuilder
              ("Multiple propositions are not supported by Supremica. " +
               "Ignoring ");
          } else {
            warning.append(", ");
          }
          warning.append(name);
        }
      }
    }
    if (warning != null) {
      warning.append('.');
      mWarnings.add(warning.toString());
    }
  }

  private void recordUncontrollableEvent(final EventProxy event,
                                         final ComponentKind compKind)
  {
    if (mPlantUncontrollableEvents != null &&
        mKindTranslator.getEventKind(event) == EventKind.UNCONTROLLABLE) {
      if (compKind == ComponentKind.PLANT) {
        mPlantUncontrollableEvents.add(event);
      } else {
        mSpecUncontrollableEvents.add(event);
      }
    }
  }

  private Automaton buildPlantWithExtraUncontrollables()
    throws EvalException
  {
    if (mPlantUncontrollableEvents == null ||
        mSpecUncontrollableEvents.isEmpty()) {
      return null;
    }
    final List<EventProxy> events = new ArrayList<>(mSpecUncontrollableEvents);
    events.removeAll(mPlantUncontrollableEvents);
    if (events.isEmpty()) {
      return null;
    }
    Collections.sort(events);
    final Automaton supAut = new Automaton(EXTRA_UNCONTROLLABLES);
    supAut.setType(AutomatonType.PLANT);
    final State state = new State(STATE0);
    state.setInitial(true);
    state.setAccepting(true);
    supAut.addState(state);
    final Alphabet alphabet = supAut.getAlphabet();
    for (final EventProxy watersEvent : events) {
      final LabeledEvent supEvent = new LabeledEvent(watersEvent);
      alphabet.addEvent(supEvent);
      final Arc arc = new Arc(state, state, supEvent);
      supAut.addArc(arc);
    }

    return supAut;
  }


  //#########################################################################
  //# Data Members
  private final ProjectFactory mProjectFactory;
  private final DocumentManager mDocumentManager;
  private final List<String> mWarnings;

  private KindTranslator mKindTranslator =
    PropertySuppressionKindTranslator.getInstance();
  private EventProxy mConfiguredDefaultMarking;
  private boolean mEnsuresUncontrollablesInPlant = false;

  private EventProxy mUsedDefaultMarking;
  private EventProxy mUsedForbiddenMarking;
  private Collection<EventProxy> mPlantUncontrollableEvents;
  private Collection<EventProxy> mSpecUncontrollableEvents;


  //#########################################################################
  //# Class Constants
  private static final Logger logger =
    LogManager.getLogger(ProjectBuildFromWaters.class);
  private static final String EXTRA_UNCONTROLLABLES = ":extra-uncontrollables";
  private static final String STATE0 = "e0";

}