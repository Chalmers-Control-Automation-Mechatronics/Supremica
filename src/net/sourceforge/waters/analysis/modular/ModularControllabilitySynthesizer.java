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
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractProductDESBuilder;
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
 * <P>This implementation also attempts to ensure nonblocking, but only
 * locally for each subsystems considered. No attempt is made to ensure
 * global nonblocking, and the resultant supervised behaviour may indeed
 * be blocking.</P>
 *
 * <P><STRONG>Reference:</STRONG> K.&nbsp;&Aring;kesson, H.&nbsp;Flordal, and
 * M.&nbsp;Fabian. Exploiting Modularity for Synthesis and Verification of
 * Supervisors. Proc. 15th IFAC World Congress on Automatic Control,
 * Barcelona, 2002.</P>
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
    super(factory);
    mMonolithicSynthesizer = new MonolithicSynthesizer(factory);
  }

  public ModularControllabilitySynthesizer(final ProductDESProxy model,
                                           final ProductDESProxyFactory factory)
  {
    super(model, factory);
    mMonolithicSynthesizer = new MonolithicSynthesizer(factory);
  }

  public ModularControllabilitySynthesizer(final ProductDESProxy model,
                                           final ProductDESProxyFactory factory,
                                           final KindTranslator translator)
  {
    super(model, factory, translator);
    mMonolithicSynthesizer = new MonolithicSynthesizer(factory);
  }


  //#########################################################################
  //# Configuration
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


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mMonolithicSynthesizer != null) {
      mMonolithicSynthesizer.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mMonolithicSynthesizer != null) {
      mMonolithicSynthesizer.resetAbort();
    }
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
  //# Overrides for Base Class
  //# net.sourceforge.waters.model.analysis.AbstractModelAnalyser
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    final KindTranslator translator = getKindTranslator();
    mMonolithicSynthesizer.setKindTranslator(translator);
    mMonolithicSynthesizer.setNodeLimit(getNodeLimit());
    mMonolithicSynthesizer.setTransitionLimit(getTransitionLimit());
    mMonolithicSynthesizer.setNonblockingSupported(false);
    final ProductDESProxy model = getModel();
    final int numEvents = model.getEvents().size();
    mUncontrollableEventMap = new HashMap<>(numEvents);
    for (final AutomatonProxy aut : model.getAutomata()) {
      if (translator.getComponentKind(aut) == ComponentKind.PLANT) {
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
      final ProductDESProxy model = getModel();
      final int numAutomata = model.getAutomata().size();
      final Collection<AutomatonProxy> supervisors =
        new ArrayList<>(numAutomata);
      final KindTranslator translator = getKindTranslator();
      for (final AutomatonProxy aut : model.getAutomata()) {
        if (translator.getComponentKind(aut) == ComponentKind.SPEC) {
          checkAbort();
          final Collection<EventProxy> uncontrollable =
            getUncontrollableEvents(aut);
          if (uncontrollable.isEmpty()) {
            continue;
          }
          final Collection<AutomatonProxy> plants =
            new THashSet<>(model.getAutomata().size());
          for (final EventProxy event : uncontrollable) {
            final Collection<AutomatonProxy> automata =
              mUncontrollableEventMap.get(event);
            if (automata != null) {
              plants.addAll(automata);
            }
          }
          mMonolithicSynthesizer.setOutputName("sup:" + aut.getName());
          boolean moreEvents = false;
          do {
            final ProductDESProxy des = createProductDES(aut, plants);
            mMonolithicSynthesizer.setModel(des);
            extendUncontrollableEvents(uncontrollable, plants);
            final MonolithicKindTranslator monolithicTranslator =
              new MonolithicKindTranslator(uncontrollable);
            mMonolithicSynthesizer.setKindTranslator(monolithicTranslator);
            if (!mMonolithicSynthesizer.run()) {
              return setBooleanResult(false);
            }
            moreEvents = false;
            final Collection<EventProxy> disabledEvents = mMonolithicSynthesizer.getDisabledEvents();
            for (final EventProxy event : disabledEvents) {
              if (translator.getEventKind(event) == EventKind.UNCONTROLLABLE) {
                final Collection<AutomatonProxy> automata =
                  mUncontrollableEventMap.get(event);
                uncontrollable.add(event);
                if (automata != null) {
                  moreEvents |= plants.addAll(automata);
                }
              }
            }
          } while (moreEvents);
          supervisors.addAll
            (mMonolithicSynthesizer.getAnalysisResult().getComputedAutomata());
          final Collection<EventProxy> disabledEvents = mMonolithicSynthesizer.getDisabledEvents();
          mDisabledEvents.addAll(disabledEvents);
        }
      }
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
                                             getFactory());
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
    if (mIncludesAllAutomata) {
      mUsedAutomata.addAll(list);
    }
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
        final Collection<AutomatonProxy> automata = mUncontrollableEventMap.get(event);
        if (automata == null || plant.containsAll(automata)) {
          uncontrollable.add(event);
        }
      }
    }
  }

  public Collection<EventProxy> getDisabledEvents()
  {
    return mDisabledEvents;
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
  //# Inner Class
  private class MonolithicKindTranslator implements KindTranslator
  {
    MonolithicKindTranslator (final Collection<EventProxy> localUncont) {
      mLocalUncontrollable = localUncont;
    }

    @Override
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      final KindTranslator translator = getKindTranslator();
      return translator.getComponentKind(aut);
    }

    @Override
    public EventKind getEventKind(final EventProxy event)
    {
      final KindTranslator translator = getKindTranslator();
      final EventKind kind = translator.getEventKind(event);
      if (kind == EventKind.UNCONTROLLABLE ) {
        if (mLocalUncontrollable.contains(event)) {
          return EventKind.UNCONTROLLABLE;
        } else {
          return EventKind.CONTROLLABLE;
        }
      } else {
        return kind;
      }
    }

    private final Collection<EventProxy> mLocalUncontrollable;
  }

  //#########################################################################
  //# Data Members
  // Configuration options
  private boolean mIncludesAllAutomata = false;

  // Permanent tools
  private final MonolithicSynthesizer mMonolithicSynthesizer;

  // Algorithm variables
  private Map<EventProxy,Collection<AutomatonProxy>> mUncontrollableEventMap;
  private Collection<AutomatonProxy> mUsedAutomata;
  private Collection<EventProxy> mDisabledEvents;


  //#########################################################################
  //# Class Constants

}