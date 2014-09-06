//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ModularControllabilitySynthesizer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.monolithic.MonolithicSynthesizer;
import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractProductDESBuilder;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.apache.log4j.Logger;


/**
 * <P>The modular synthesis algorithm.</P>
 *
 * <P>This synthesis algorithm produces a least restrictive supervisor
 * that ensures controllability, but not necessarily nonblocking.
 * For each specification, it takes into account all plants containing
 * the controllable events disabled by the specification and possibly
 * further plants as needed, to produce a supervisor component ensuring
 * controllability of that specification.</P>
 *
 * <P>Optionally, this implementation also attempts to ensure nonblocking,
 * but only locally for each subsystems considered. No attempt is made to
 * ensure global nonblocking, and the resultant supervised behaviour may
 * indeed be blocking.</P>
 *
 * <P><I>Reference:</I><BR>
 * K.&nbsp;&Aring;kesson, H.&nbsp;Flordal, and M.&nbsp;Fabian.
 * Exploiting Modularity for Synthesis and Verification of Supervisors.
 * Proc. 15th IFAC World Congress on Automatic Control, Barcelona, 2002.</P>
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public class ModularControllabilitySynthesizer
  extends AbstractProductDESBuilder
  implements SupervisorSynthesizer
{

  //#########################################################################
  //# Constructors
  public ModularControllabilitySynthesizer(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public ModularControllabilitySynthesizer(final ProductDESProxy model,
                                           final ProductDESProxyFactory factory)
  {
    this(model, factory, IdenticalKindTranslator.getInstance());
  }

  public ModularControllabilitySynthesizer(final ProductDESProxy model,
                                           final ProductDESProxyFactory factory,
                                           final KindTranslator translator)
  {
    super(model, factory, translator);
    final SafetyVerifier inner = new NativeControllabilityChecker(factory);
    mModularControllabilityChecker =
      new ModularControllabilityChecker(factory, inner);
    mModularControllabilityChecker.setCollectsFailedSpecs(true);
    mMonolithicSynthesizer = new MonolithicSynthesizer(factory);
    mMonolithicSynthesizer.setNonblockingSupported(false);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets whether synthesis includes local nonblocking checks.
   * If set, the algorithm will attempt to synthesise controllable
   * and nonblocking supervisors each time a subsystem is sent for
   * monolithic synthesis. While this setting may help to remove some
   * blocking states, it does not ensure that the result is a globally
   * nonblocking supervisor.
   */
  public void setLocalNonblockingSupported(final boolean support)
  {
    mMonolithicSynthesizer.setNonblockingSupported(support);
  }

  /**
   * Returns whether synthesis includes local nonblocking checks.
   * @see #setLocalNonblockingSupported(boolean) setLocalNonblockingSupported()
   */
  public boolean getLocalNonblockingSupported()
  {
    return mMonolithicSynthesizer.getNonblockingSupported();
  }

  /**
   * Sets whether the synthesised supervisor should include automata from
   * the original model that have not been used in synthesis.
   * If <CODE>true</CODE>, the synthesis result will also include
   * specifications that are already controllable and plants that were
   * not needed for any synthesis; otherwise only computed supervisors
   * will be included.
   */
  public void setIncludesAllAutomata(final boolean include)
  {
    mIncludesAllAutomata = include;
  }

  /**
   * Returns whether the synthesised supervisor includes automata from
   * the original model that have not been used in synthesis.
   * @see #setIncludesAllAutomata(boolean) setIncludesAllAutomata()
   */
  public boolean getIncludesAllAutomata()
  {
    return mIncludesAllAutomata;
  }

  public Collection<EventProxy> getDisabledEvents()
  {
    return mDisabledEvents;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SupervisorSynthesizer
  @Override
  public void setConfiguredDefaultMarking(final EventProxy marking)
  {
    mMonolithicSynthesizer.setConfiguredDefaultMarking(marking);
  }

  @Override
  public EventProxy getConfiguredDefaultMarking()
  {
    return mMonolithicSynthesizer.getConfiguredDefaultMarking();
  }

  @Override
  public void setSupervisorReductionEnabled(final boolean enable)
  {
    mMonolithicSynthesizer.setSupervisorReductionEnabled(enable);
  }

  @Override
  public boolean getSupervisorReductionEnabled()
  {
    return mMonolithicSynthesizer.getSupervisorReductionEnabled();
  }

  @Override
  public void setSupervisorLocalizationEnabled(final boolean enable)
  {
    mMonolithicSynthesizer.setSupervisorLocalizationEnabled(enable);
  }

  @Override
  public boolean getSupervisorLocalizationEnabled()
  {
    return mMonolithicSynthesizer.getSupervisorLocalizationEnabled();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mModularControllabilityChecker != null) {
      mModularControllabilityChecker.requestAbort();
    }
    if (mMonolithicSynthesizer != null) {
      mMonolithicSynthesizer.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mModularControllabilityChecker != null) {
      mModularControllabilityChecker.resetAbort();
    }
    if (mMonolithicSynthesizer != null) {
      mMonolithicSynthesizer.resetAbort();
    }
  }


  //#########################################################################
  //# Overrides for Base Class
  //# net.sourceforge.waters.model.analysis.AbstractModelAnalyser
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    final KindTranslator translator = getKindTranslator();
    mModularControllabilityChecker.setKindTranslator(translator);
    mModularControllabilityChecker.setNodeLimit(getNodeLimit());
    mModularControllabilityChecker.setTransitionLimit(getTransitionLimit());
    mMonolithicSynthesizer.setKindTranslator(translator);
    mMonolithicSynthesizer.setNodeLimit(getNodeLimit());
    mMonolithicSynthesizer.setTransitionLimit(getTransitionLimit());
    final ProductDESProxy model = getModel();
    final int numEvents = model.getEvents().size();
    mUncontrollableEventMap = new HashMap<>(numEvents);
    if (mIncludesAllAutomata) {
      mUsedAutomata = new THashSet<>(numEvents);
    }
    mDisabledEvents = new THashSet<>();
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mUncontrollableEventMap = null;
    mUsedAutomata = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();

      // 1. Find uncontrollable specifications
      final ProductDESProxyFactory factory = getFactory();
      final KindTranslator translator = getKindTranslator();
      mModularControllabilityChecker.setKindTranslator(translator);
      final ProductDESProxy model = getModel();
      mModularControllabilityChecker.setModel(model);
      if (mModularControllabilityChecker.run()) {
        // The system is already controllable!
        if (mIncludesAllAutomata) {
          return setProxyResult(model);
        } else {
          final Collection<AutomatonProxy> empty = Collections.emptyList();
          final ProductDESProxy emptyDES =
            AutomatonTools.createProductDESProxy("empty", empty, factory);
          return setProxyResult(emptyDES);
        }
      }

      // 2. Establish map from uncontrollable events to plants
      final Collection<AutomatonProxy> uncontrollableSpecs =
        mModularControllabilityChecker.getAnalysisResult().getFailedSpecs();
      final Collection<AutomatonProxy> uncontrollableSpecSet =
        new THashSet<>(uncontrollableSpecs);
      for (final AutomatonProxy aut : model.getAutomata()) {
        if (!uncontrollableSpecSet.contains(aut)) {
          for (final EventProxy event : aut.getEvents()) {
            if (translator.getEventKind(event) == EventKind.UNCONTROLLABLE) {
              Collection<AutomatonProxy> automata =
                mUncontrollableEventMap.get(event);
              if (automata == null) {
                automata = new ArrayList<>();
                mUncontrollableEventMap.put(event, automata);
              }
              automata.add(aut);
            }
          }
        }
      }

      // 3. Perform synthesis for uncontrollable specifications
      final int numAutomata = model.getAutomata().size();
      final Collection<AutomatonProxy> supervisors =
        new ArrayList<>(numAutomata);
      for (final AutomatonProxy spec : uncontrollableSpecs) {
        checkAbort();
        final Collection<EventProxy> specUncontrollableEvents =
          getUncontrollableEvents(spec);
        final Collection<EventProxy> uncontrollableEvents =
          new THashSet<>(specUncontrollableEvents);
        final Collection<AutomatonProxy> plants =
          new THashSet<>(model.getAutomata().size());
        for (final EventProxy event : specUncontrollableEvents) {
          final Collection<AutomatonProxy> automata =
            mUncontrollableEventMap.get(event);
          if (automata != null) {
            plants.addAll(automata);
          }
        }
        mMonolithicSynthesizer.setOutputName("sup:" + spec.getName());
        boolean moreEvents = false;
        do {
          checkAbort();
          final ProductDESProxy des = createProductDES(spec, plants);
          mMonolithicSynthesizer.setModel(des);
          extendUncontrollableEvents(uncontrollableEvents, plants);
          final MonolithicKindTranslator monolithicTranslator =
            new MonolithicKindTranslator(spec, uncontrollableEvents);
          mMonolithicSynthesizer.setKindTranslator(monolithicTranslator);
          if (!mMonolithicSynthesizer.run()) {
            return setBooleanResult(false);
          }
          moreEvents = false;
          final Collection<EventProxy> disabledEvents =
            mMonolithicSynthesizer.getDisabledEvents();
          for (final EventProxy event : disabledEvents) {
            if (translator.getEventKind(event) == EventKind.UNCONTROLLABLE) {
              final Collection<AutomatonProxy> automata =
                mUncontrollableEventMap.get(event);
              uncontrollableEvents.add(event);
              if (automata != null) {
                moreEvents |= plants.addAll(automata);
              }
            }
          }
        } while (moreEvents);
        final Collection<EventProxy> disabledEvents =
          mMonolithicSynthesizer.getDisabledEvents();
        final Collection<? extends AutomatonProxy> localSups =
          mMonolithicSynthesizer.getAnalysisResult().getComputedAutomata();
        supervisors.addAll(localSups);
        mDisabledEvents.addAll(disabledEvents);
        if (mIncludesAllAutomata) {
          mUsedAutomata.add(spec);
          mUsedAutomata.addAll(plants);
        }
      }

      // 4. Create product DES containing supervisors
      if (mIncludesAllAutomata) {
        for (final AutomatonProxy aut : model.getAutomata()) {
          switch (translator.getComponentKind(aut)) {
          case PLANT:
          case SPEC:
            if (!mUsedAutomata.contains(aut)) {
              supervisors.add(aut);
            }
            break;
          default:
            break;
          }
        }
      }
      final ProductDESProxy des =
        AutomatonTools.createProductDESProxy("supervisor", supervisors,
                                             factory);
      return setProxyResult(des);
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
      tearDown();
      final Logger logger = getLogger();
      logger.debug("<out of memory>");
      final OverflowException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } catch (final StackOverflowError error) {
      final OverflowException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }
  }

  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }


  //#########################################################################
  //# Auxiliary Methods
  private Collection<EventProxy> getUncontrollableEvents(final AutomatonProxy aut)
  {
    final Collection<EventProxy> result = new ArrayList<>();
    final KindTranslator translator = getKindTranslator();
    for (final EventProxy event : aut.getEvents()) {
      if (translator.getEventKind(event) == EventKind.UNCONTROLLABLE) {
        result.add(event);
      }
    }
    return result;
  }

  private ProductDESProxy createProductDES(final AutomatonProxy spec,
                                           final Collection<AutomatonProxy> plants)
  {
    final int numAutomata = plants.size() + 1;
    final List<AutomatonProxy> list = new ArrayList<>(numAutomata);
    list.add(spec);
    list.addAll(plants);
    Collections.sort(list);
    final ProductDESProxy des =
      AutomatonTools.createProductDESProxy("subsystem:" + spec.getName(),
                                           list, getFactory());
    logSubsystem(des);
    return des;
  }

  private void extendUncontrollableEvents(final Collection<EventProxy> uncontrollable,
                                          final Collection<AutomatonProxy> plant)
  {
    final KindTranslator translator = getKindTranslator();
    final ProductDESProxy model = getModel();
    for (final EventProxy event : model.getEvents()) {
      if (translator.getEventKind(event) == EventKind.UNCONTROLLABLE){
        final Collection<AutomatonProxy> automata =
          mUncontrollableEventMap.get(event);
        if (automata == null || plant.containsAll(automata)) {
          uncontrollable.add(event);
        }
      }
    }
  }


  //#########################################################################
  //# Debugging
  private void logSubsystem(final ProductDESProxy des)
  {
    final Logger logger = getLogger();
    if (logger.isDebugEnabled()) {
      final KindTranslator translator = getKindTranslator();
      final StringBuilder builder =
        new StringBuilder("Attempting monolithic synthesis for: ");
      final ComponentKind[] kinds = {ComponentKind.SPEC, ComponentKind.PLANT};
      String sep = "";
      for (final ComponentKind kind : kinds) {
        for (final AutomatonProxy aut : des.getAutomata()) {
          if (translator.getComponentKind(aut) == kind) {
            builder.append(sep);
            builder.append(aut.getName());
            sep = ", ";
          }
        }
        sep = "; ";
      }
      logger.debug(builder);
    }
  }


  //#########################################################################
  //# Inner Class MonolithicKindTranslator
  private class MonolithicKindTranslator implements KindTranslator
  {
    //#######################################################################
    //# Constructor
    private MonolithicKindTranslator
      (final AutomatonProxy spec, final Collection<EventProxy> localUncont)
    {
      mSpec = spec;
      mLocalUncontrollables = localUncont;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.KindTranslator
    @Override
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      if (aut == mSpec) {
        return ComponentKind.SPEC;
      } else {
        return ComponentKind.PLANT;
      }
    }

    @Override
    public EventKind getEventKind(final EventProxy event)
    {
      final KindTranslator translator = getKindTranslator();
      final EventKind kind = translator.getEventKind(event);
      if (kind == EventKind.UNCONTROLLABLE ) {
        if (mLocalUncontrollables.contains(event)) {
          return EventKind.UNCONTROLLABLE;
        } else {
          return EventKind.CONTROLLABLE;
        }
      } else {
        return kind;
      }
    }

    //#######################################################################
    //# Data Members
    private final AutomatonProxy mSpec;
    private final Collection<EventProxy> mLocalUncontrollables;
  }


  //#########################################################################
  //# Data Members
  // Configuration options
  private boolean mIncludesAllAutomata = false;

  // Additional results
  private Collection<EventProxy> mDisabledEvents;

  // Permanent tools
  private final ModularControllabilityChecker mModularControllabilityChecker;
  private final MonolithicSynthesizer mMonolithicSynthesizer;

  // Algorithm variables
  private Map<EventProxy,Collection<AutomatonProxy>> mUncontrollableEventMap;
  private Collection<AutomatonProxy> mUsedAutomata;

}