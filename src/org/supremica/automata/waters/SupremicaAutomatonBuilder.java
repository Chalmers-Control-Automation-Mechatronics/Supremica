package org.supremica.automata.waters;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionMap;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.AutomatonBuilder;
import net.sourceforge.waters.model.analysis.des.AutomatonResult;
import net.sourceforge.waters.model.analysis.kindtranslator.IdenticalKindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.IO.AutomataToWaters;
import org.supremica.automata.algorithms.EquivalenceRelation;
import org.supremica.automata.algorithms.minimization.AutomatonMinimizer;
import org.supremica.automata.algorithms.minimization.MinimizationOptions;
import org.supremica.properties.Config;

public class SupremicaAutomatonBuilder extends SupremicaModelAnalyzer
  implements AutomatonBuilder
{

  public SupremicaAutomatonBuilder(final ProductDESProxyFactory factory,
                                   final EquivalenceRelation relation)
  {
    super(null, factory, IdenticalKindTranslator.getInstance(), false);

    mMinimizationOptions = new MinimizationOptions();
    mMinimizationOptions.setMinimizationType(relation);
    mFactory = factory;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.options.Configurable
  @Override
  public List<Option<?>> getOptions(final OptionMap db)
  {
    final List<Option<?>> options = new LinkedList<>();
    final EquivalenceRelation rel = mMinimizationOptions.getMinimizationType();
    if (rel != EquivalenceRelation.LANGUAGEEQUIVALENCE) {
      db.append(options, SupremicaAutomatonBuilder.
                OPTION_SupremicaAutomatonBuilder_AlsoTransitions);
    }
    if (rel != EquivalenceRelation.CONFLICTEQUIVALENCE) {
      db.append(options, SupremicaAutomatonBuilder.
                OPTION_SupremicaAutomatonBuilder_IgnoreMarking);
    }
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(SupremicaAutomatonBuilder.
                            OPTION_SupremicaAutomatonBuilder_AlsoTransitions)) {
      final BooleanOption propOption = (BooleanOption) option;
      mMinimizationOptions.setAlsoTransitions(propOption.getValue());
    } else if (option.hasID(SupremicaAutomatonBuilder.
                            OPTION_SupremicaAutomatonBuilder_IgnoreMarking)) {
      final BooleanOption propOption = (BooleanOption) option;
      mMinimizationOptions.setIgnoreMarking(propOption.getValue());
    } else {
      //TODO?
    }
  }


  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
  }


  @Override
  public boolean run() throws AnalysisException
  {
    try {
      final Map<EventProxy, EventProxy> forwardsMap = createForwardsMapping();
      setEventMap(forwardsMap);
      setUp();

      final Automata automata = getSupremicaAutomata();
      final Automaton aut = automata.getAutomatonAt(0);

      final Map<String, EventProxy> backwardsMap = createBackwardsMapping(forwardsMap);

      final AutomatonMinimizer minimizer = new AutomatonMinimizer(aut);

      try {
        final Automaton newAut = minimizer
          .getMinimizedAutomaton(mMinimizationOptions);
        newAut.setName(mOutputName);
        final AutomataToWaters importer =
          new AutomataToWaters(mFactory, backwardsMap);
        mResult = importer.convertAutomaton(newAut);
      }
      catch (final Exception e) {
        //TODO
        e.printStackTrace();
      }

    }
    catch(final AnalysisException e) {
      //TODO
      e.printStackTrace();
    }
    finally {
      tearDown();
    }

    return false;
  }

  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }

  @Override
  public void setSynchronisingOnUnobservableEvents(final boolean sync)
  {
    mSynchronisingOnUnobservableEvents = sync;
  }

  @Override
  public boolean isSynchronisingOnUnobservableEvents()
  {
    return mSynchronisingOnUnobservableEvents;
  }







  @Override
  public void setOutputName(final String name)
  {
    mOutputName = name;
  }

  @Override
  public String getOutputName()
  {
    return mOutputName;
  }

  @Override
  public AutomatonProxy getComputedProxy()
  {
    //TODO Throw error if null?
    return mResult;
  }

  @Override
  public void setOutputKind(final ComponentKind kind)
  {
    mComponentKind = kind;
  }

  @Override
  public ComponentKind getOutputKind()
  {
    return mComponentKind;
  }

  @Override
  public AutomatonProxy getComputedAutomaton()
  {
    return getComputedProxy();
  }

  @Override
  public AutomatonResult getAnalysisResult()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Map<EventProxy, EventProxy> createForwardsMapping() {
    final AutomatonProxy aut = getModel()
      .getAutomata()
      .iterator()
      .next();

    final Map<EventProxy, EventProxy> eventMap = new HashMap<>();
    if (aut instanceof TRAutomatonProxy) {

      final Map<String, EventProxy> clashMap = new HashMap<>();
      final String[] reservedNames = new String[] { TAU, TAU_C, TAU_U };

      final TRAutomatonProxy trAut = (TRAutomatonProxy) aut;
      final EventEncoding enc = trAut.getEventEncoding();
      for (final EventProxy event : aut.getEvents()) {

        final int code = enc.getEventCode(event);
        if (code == EventEncoding.TAU) {
          clashMap.put(VALUE+TAU, event);
        }
        else {
          final byte status = enc.getProperEventStatus(code);
          if (EventStatus.isLocalEvent(status)) {
            if (EventStatus.isControllableEvent(status)) {
              clashMap.put(VALUE+TAU_C, event);
            }
            else {
              clashMap.put(VALUE+TAU_U, event);
            }
          }
        }

        for (final String s : reservedNames) {
          if (event.getName().equals(s)) clashMap.put(NAME+s, event);
        }

      }

      for (final String name : reservedNames) {
        //An event which must be renamed to a reserved name
        final EventProxy ev = clashMap.get(VALUE+name);
        //An event which must be renamed from a reserved name
        final EventProxy en = clashMap.get(NAME+name);
        if (ev == en) continue;
        if (ev != null) {
          addReplacementEventToMap(eventMap, ev, name);
        }
        if (en != null) {
          final String newName = findUnusedEventName(TEMP+name, aut);
          addReplacementEventToMap(eventMap, en, newName);
        }
      }

    }
    return eventMap;
  }

  public Map<String, EventProxy> createBackwardsMapping(final Map<EventProxy, EventProxy> forwardsMap) {
    final Map<String, EventProxy> backwardsMap = new HashMap<String, EventProxy>();
    for (final EventProxy event : getModel().getEvents()) {
      final String mappedName = forwardsMap.getOrDefault(event, event).getName();
      backwardsMap.put(mappedName, event);
    }
    return backwardsMap;
  }

  private String findUnusedEventName(String name, final AutomatonProxy aut) {
    for (;;) {
      boolean found = false;
      for (final EventProxy event : aut.getEvents()) {
        if (event.getName().equals(name)) {
          found = true;
          break;
        }
      }
      if (!found) return name;
      name = "_" + name;
    }
  }

  private void addReplacementEventToMap(final Map<EventProxy, EventProxy> eventMap,
                                        final EventProxy currentEvent,
                                        final String newName) {
    final EventProxy event = mFactory.createEventProxy(newName,
                                                 currentEvent.getKind(),
                                                 false);
    eventMap.put(currentEvent, event);
  }

  private final ProductDESProxyFactory mFactory;

  private final MinimizationOptions mMinimizationOptions;

  private boolean mSynchronisingOnUnobservableEvents;
  private String mOutputName;
  private ComponentKind mComponentKind;

  private AutomatonProxy mResult;

  private static final String TAU =
    Config.MINIMIZATION_SILENT_EVENT_NAME.get();
  private static final String TAU_C =
    Config.MINIMIZATION_SILENT_CONTROLLABLE_EVENT_NAME.get();
  private static final String TAU_U =
    Config.MINIMIZATION_SILENT_UNCONTROLLABLE_EVENT_NAME.get();
  private static final String VALUE = "Value: ";
  private static final String NAME = "Name: ";
  private static final String TEMP = "temp_name:";


  public static final String OPTION_SupremicaAutomatonBuilder_AlsoTransitions =
    "SupremicaAutomatonBuilder.AlsoTransitions";
  public static final String OPTION_SupremicaAutomatonBuilder_IgnoreMarking =
    "SupremicaAutomatonBuilder.IgnoreMarking";

}
