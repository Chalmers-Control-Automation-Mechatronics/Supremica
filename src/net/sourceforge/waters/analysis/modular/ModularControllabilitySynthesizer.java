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
import net.sourceforge.waters.model.analysis.ProxyResult;
import net.sourceforge.waters.model.analysis.des.AbstractProductDESBuilder;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
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
 * The modular synthesis algorithm.
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public class ModularControllabilitySynthesizer extends AbstractProductDESBuilder
  implements SupervisorSynthesizer
{

  //#########################################################################
  //# Constructors
  public ModularControllabilitySynthesizer(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public ModularControllabilitySynthesizer(final ProductDESProxy model,
                               final ProductDESProxyFactory factory)
  {
    super(model, factory);
  }

  public ModularControllabilitySynthesizer(final ProductDESProxy model,
                               final ProductDESProxyFactory factory,
                               final KindTranslator translator)
  {
    super(model, factory, translator);
  }


  //#########################################################################
  //# Configuration
  public void setSupervisorReductionEnabled(final boolean enable)
  {
    mSupervisorReductionEnabled = enable;
  }

  public boolean getSupervisorReductionEnabled()
  {
    return mSupervisorReductionEnabled;
  }

  public void setSupervisorLocalizationEnabled(final boolean enable)
  {
    mSupervisorLocalizationEnabled = enable;
  }

  public boolean getSupervisorLocalizationEnabled()
  {
    return mSupervisorLocalizationEnabled;
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

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SynchronousProductBuilder
  @Override
  public void setConfiguredDefaultMarking(final EventProxy marking)
  {
    mConfiguredMarking = marking;
  }

  @Override
  public EventProxy getConfiguredDefaultMarking()
  {
    return mConfiguredMarking;
  }


  //#########################################################################
  //# Overrides for Base Class
  //# net.sourceforge.waters.model.analysis.AbstractModelAnalyser
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    final KindTranslator translator = getKindTranslator();
    final ProductDESProxy model = getModel();
    mUncontrollableEventMap = new HashMap<>(model.getEvents().size());
    for (final AutomatonProxy aut : model.getAutomata()) {
      if (translator.getComponentKind(aut) == ComponentKind.PLANT) {
        for (final EventProxy event : aut.getEvents()) {
          if (translator.getEventKind(event) == EventKind.UNCONTROLLABLE) {
            Collection<AutomatonProxy> automata = mUncontrollableEventMap.get(event);
            if (automata == null) {
              automata = new ArrayList<>();
              mUncontrollableEventMap.put(event, automata);
            }
            automata.add(aut);
          }
        }
      }
    }
    mMonolithicSynthesizer = new MonolithicSynthesizer(getFactory());
    mMonolithicSynthesizer.setKindTranslator(translator);
    mMonolithicSynthesizer.setConfiguredDefaultMarking(mConfiguredMarking);
    mMonolithicSynthesizer.setSupervisorLocalizationEnabled(mSupervisorLocalizationEnabled);
    mMonolithicSynthesizer.setSupervisorReductionEnabled(mSupervisorReductionEnabled);
    mUsedAutomata = new THashSet<>();
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final ProxyResult<ProductDESProxy> result = getAnalysisResult();
    result.setNumberOfAutomata(mNumAutomata);
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mMonolithicSynthesizer = null;
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
      final Collection<AutomatonProxy> supervisors = new ArrayList<>();

      final ProductDESProxy model = getModel();
      final KindTranslator translator = getKindTranslator();
      final ProductDESResult result = getAnalysisResult();
      for (final AutomatonProxy aut : model.getAutomata()) {
        if (translator.getComponentKind(aut) == ComponentKind.SPEC) {
          final Collection<EventProxy> uncontrollable = getUncontrollableEvents(aut);
          if (uncontrollable.isEmpty()) {
            continue;
          }
          final Collection<AutomatonProxy> plants =
            new THashSet<>(model.getAutomata().size());
          for (final EventProxy event : uncontrollable) {
            plants.addAll(mUncontrollableEventMap.get(event));
          }
          ProductDESProxy des = createProductDES(aut, plants);
          mMonolithicSynthesizer.setModel(des);
          mMonolithicSynthesizer.setNonFailingUncontrollableEvents(uncontrollable);
          while (!mMonolithicSynthesizer.run()) {
            final EventProxy failedEvent = mMonolithicSynthesizer.getFailedUncontrollableEvent();
            if (failedEvent == null) {
              result.setSatisfied(false);
              return false;
            }
            plants.addAll(mUncontrollableEventMap.get(failedEvent));
            uncontrollable.add(failedEvent);
            mMonolithicSynthesizer.setNonFailingUncontrollableEvents(uncontrollable);
            des = createProductDES(aut, plants);
            mMonolithicSynthesizer.setModel(des);
          }
          supervisors.addAll(mMonolithicSynthesizer.getAnalysisResult().getComputedAutomata());
        }
      }
      for (final AutomatonProxy aut : model.getAutomata()) {
        if (!mUsedAutomata.contains(aut)) {
          supervisors.add(aut);
        }
      }
      final ProductDESProxy des = AutomatonTools.createProductDESProxy("supervisor",
                                                                 supervisors,
                                                                 getFactory());
      result.setComputedProductDES(des);
      return true;
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
    final List<AutomatonProxy> list = new ArrayList<>();
    list.add(spec);
    list.addAll(plants);
    Collections.sort(list);
    mUsedAutomata.addAll(list);
    return AutomatonTools.createProductDESProxy("subsystem:" + spec.getName(),
                                                list, getFactory());
  }

  //#########################################################################
  //# Data Members
  private EventProxy mConfiguredMarking;
  private boolean mSupervisorReductionEnabled = false;
  private boolean mSupervisorLocalizationEnabled = false;

  //# Variables used for encoding/decoding

  private int mNumAutomata;

  private MonolithicSynthesizer mMonolithicSynthesizer;
  private Map<EventProxy, Collection<AutomatonProxy>> mUncontrollableEventMap;
  private Collection<AutomatonProxy> mUsedAutomata;


  //#########################################################################
  //# Class Constants

}