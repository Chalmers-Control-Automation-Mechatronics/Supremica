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

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.SupervisorReductionFactory;
import net.sourceforge.waters.analysis.compositional.AbstractCompositionalModelAnalyzer.PreselectingMethod;
import net.sourceforge.waters.analysis.compositional.AbstractCompositionalModelAnalyzer.PreselectingMethodFactory;
import net.sourceforge.waters.analysis.modular.ModularControllabilitySynthesizer;
import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractSupervisorSynthesizer;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.analysis.des.SynchronousProductResult;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * <P>The modular and compositional synthesis algorithm.</P>
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public class ModularAndCompositionalSynthesizer
  extends AbstractSupervisorSynthesizer
{

  //#########################################################################
  //# Constructors
  public ModularAndCompositionalSynthesizer(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public ModularAndCompositionalSynthesizer(final ProductDESProxy model,
                                            final ProductDESProxyFactory factory)
  {
    this(model, factory, IdenticalKindTranslator.getInstance());
  }

  public ModularAndCompositionalSynthesizer(final ProductDESProxy model,
                                            final ProductDESProxyFactory factory,
                                            final KindTranslator translator)
  {
    super(model, factory, translator);
    mModularSynthesizer = new ModularControllabilitySynthesizer(factory);
    mCompositionalSynthesizer = new CompositionalAutomataSynthesizer(factory);
    mCompositionalSynthesizer.setAbstractionProcedureCreator
      (AutomataSynthesisAbstractionProcedureFactory.OE);
    mCompositionalConflictChecker = new CompositionalConflictChecker(factory);
    mCompositionalConflictChecker.setAbstractionProcedureCreator
      (ConflictAbstractionProcedureFactory.NBA);
  }


  //#########################################################################
  //# Configuration
  public CompositionalSelectionHeuristicFactory getSelectionHeuristicFactory()
  {
    return mCompositionalSynthesizer.getSelectionHeuristicFactory();
  }

  public PreselectingMethodFactory getPreselectingMethodFactory()
  {
    return mCompositionalSynthesizer.getPreselectingMethodFactory();
  }

  public void setPreselectingMethod(final PreselectingMethod preselecting)
  {
    mCompositionalSynthesizer.setPreselectingMethod(preselecting);
    mCompositionalConflictChecker.setPreselectingMethod(preselecting);
  }

  public PreselectingMethod getPreselectingMethod()
  {
    return mCompositionalSynthesizer.getPreselectingMethod();
  }

  public void setSelectionHeuristic(final SelectionHeuristicCreator selecting)
  {
    mCompositionalSynthesizer.setSelectionHeuristic(selecting);
    mCompositionalConflictChecker.setSelectionHeuristic(selecting);
  }

  public SelectionHeuristic<Candidate> getSelectionHeuristic()
  {
    return mCompositionalSynthesizer.getSelectionHeuristic();
  }

  public void setInternalStateLimit(final int internalStateLimit)
  {
    mCompositionalSynthesizer.setInternalStateLimit(internalStateLimit);
    mCompositionalConflictChecker.setInternalStateLimit(internalStateLimit);
  }

  public int getInternalStateLimit()
  {
    return mCompositionalSynthesizer.getInternalStateLimit();
  }

  public void setInternalTransitionLimit(final int internalTransitionLimit)
  {
    mCompositionalSynthesizer.setInternalTransitionLimit(internalTransitionLimit);
    mCompositionalConflictChecker.setInternalTransitionLimit(internalTransitionLimit);
  }

  public int getInternalTransitionLimit()
  {
    return mCompositionalSynthesizer.getInternalTransitionLimit();
  }

  public void setMonolithicStateLimit(final int finalStateLimit)
  {
    setNodeLimit(finalStateLimit);
  }

  public int getMonolithicStateLimit()
  {
    return getNodeLimit();
  }

  public void setMonolithicTransitionLimit(final int finalTransitionLimit)
  {
    setTransitionLimit(finalTransitionLimit);
  }

  public int getMonolithicTransitionLimit()
  {
    return getTransitionLimit();
  }

  public void setRemovesUnnecessarySupervisors(final boolean removes)
  {
    mModularSynthesizer.setRemovingUnnecessarySupervisors(removes);
  }

  public boolean getRemovesUnnecessarySupervisors()
  {
    return mModularSynthesizer.isRemovingUnnecessarySupervisors();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mModularSynthesizer != null) {
      mModularSynthesizer.requestAbort();
    }
    if (mCompositionalSynthesizer != null) {
      mCompositionalSynthesizer.requestAbort();
    }
    if (mCompositionalConflictChecker != null) {
      mCompositionalConflictChecker.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    if (mModularSynthesizer != null) {
      mModularSynthesizer.resetAbort();
    }
    if (mCompositionalSynthesizer != null) {
      mCompositionalSynthesizer.resetAbort();
    }
    if (mCompositionalConflictChecker != null) {
      mCompositionalConflictChecker.resetAbort();
    }
  }


  //#########################################################################
  //# Overrides for Base Class
  //# net.sourceforge.waters.model.analysis.AbstractModelAnalyser
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    final KindTranslator translator =
      ConflictKindTranslator.getInstanceControllable();
    mCompositionalSynthesizer.setKindTranslator(translator);
    mCompositionalConflictChecker.setKindTranslator(translator);
    final EventProxy marking = getUsedDefaultMarking();
    mCompositionalSynthesizer.setConfiguredDefaultMarking(marking);
    mCompositionalConflictChecker.setConfiguredDefaultMarking(marking);
    mModularSynthesizer.setConfiguredDefaultMarking(marking);
    final int stateLimit = getNodeLimit();
    mCompositionalSynthesizer.setMonolithicStateLimit(stateLimit);
    mCompositionalConflictChecker.setMonolithicStateLimit(stateLimit);
    mModularSynthesizer.setNodeLimit(stateLimit);
    final int transLimit = getTransitionLimit();
    mCompositionalSynthesizer.setMonolithicTransitionLimit(transLimit);
    mCompositionalConflictChecker.setMonolithicTransitionLimit(transLimit);
    mModularSynthesizer.setTransitionLimit(transLimit);
    final String prefix = getOutputName();
    mCompositionalSynthesizer.setOutputName(prefix);
    mModularSynthesizer.setOutputName(prefix);
    final SupervisorReductionFactory factory = getSupervisorReductionFactory();
    mCompositionalSynthesizer.setSupervisorReductionFactory(factory);
    mModularSynthesizer.setSupervisorReductionFactory(factory);
    final boolean locEnabled = isSupervisorLocalizationEnabled();
    mCompositionalSynthesizer.setSupervisorLocalizationEnabled(locEnabled);
    mModularSynthesizer.setSupervisorLocalizationEnabled(locEnabled);
    final boolean nd = supportsNondeterminism();
    mCompositionalSynthesizer.setNondeterminismEnabled(nd);
    mModularSynthesizer.setNondeterminismEnabled(nd);
    mModularSynthesizer.setIncludesAllAutomata(true);
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
  }

  @Override
  public CompositionalAutomataSynthesisResult createAnalysisResult()
  {
    final CompositionalAutomataSynthesisResult result =
      new CompositionalAutomataSynthesisResult(this, isDetailedOutputEnabled());
    final AbstractionProcedureCreator creator =
      mCompositionalSynthesizer.getAbstractionProcedureCreator();
    final AutomataSynthesisAbstractionProcedure proc =
      (AutomataSynthesisAbstractionProcedure)
      creator.createAbstractionProcedure(mCompositionalSynthesizer);
    proc.storeStatistics(result);
    final MonolithicSynchronousProductBuilder synch =
      mCompositionalSynthesizer.getSynchronousProductBuilder();
    final SynchronousProductResult synchResult = synch.createAnalysisResult();
    result.addSynchronousProductAnalysisResult(synchResult);
    return result;
  }

  @Override
  public CompositionalAutomataSynthesisResult getAnalysisResult()
  {
    return (CompositionalAutomataSynthesisResult) super.getAnalysisResult();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      final ProductDESProxy model = getModel();
      final KindTranslator translator = getKindTranslator();
      final int numAutomata = model.getAutomata().size();
      final Collection<AutomatonProxy> automata = new ArrayList<>(numAutomata);
      for (final AutomatonProxy aut : model.getAutomata()) {
        switch (translator.getComponentKind(aut)) {
        case PLANT:
        case SPEC:
          automata.add(aut);
          break;
        default:
          break;
        }
      }
      final ProductDESProxyFactory factory = getFactory();
      final ProductDESProxy sanitizedModel =
        AutomatonTools.createProductDESProxy(model.getName(), automata, factory);
      mModularSynthesizer.setModel(sanitizedModel);
      mModularSynthesizer.setKindTranslator(translator);
      if (!mModularSynthesizer.run()) {
        return setBooleanResult(false);
      }
      final CompositionalAutomataSynthesisResult results = getAnalysisResult();
      collectSupervisors(mModularSynthesizer);
      final EventProxy marking = getUsedDefaultMarking();
      final String prefix = getSupervisorNamePrefix();
      boolean maybeBlocking = false;
      int supCount = 1;
      do {
        mCompositionalSynthesizer.setOutputName(prefix + supCount + ":");
        final ProductDESProxy modularResult =
          mModularSynthesizer.getComputedProductDES();
        if (!modularResult.getEvents().contains(marking)) {
          break;
        }
        mCompositionalConflictChecker.setModel(modularResult);
        if (mCompositionalConflictChecker.run()) {
          break;
        }
        mCompositionalSynthesizer.setModel(modularResult);
        if (!mCompositionalSynthesizer.run()) {
          return setBooleanResult(false);
        }
        final CompositionalAutomataSynthesisResult compositionalResult =
          mCompositionalSynthesizer.getAnalysisResult();
        results.merge(compositionalResult);
        final Collection<AutomatonProxy> compSupervisors =
          compositionalResult.getComputedAutomata();
        final Collection<AutomatonProxy> modularAutomata =
          modularResult.getAutomata();
        final Collection<AutomatonProxy> combinedAutomata =
          new ArrayList<>(modularAutomata.size() + compSupervisors.size());
        combinedAutomata.addAll(modularAutomata);
        combinedAutomata.addAll(compSupervisors);
        final ProductDESProxy modularDES = AutomatonTools.createProductDESProxy
          (model.getName(), combinedAutomata, factory);
        mModularSynthesizer.setModel(modularDES);
        final ModularKindTranslator modularTranslator =
          new ModularKindTranslator(compSupervisors);
        mModularSynthesizer.setKindTranslator(modularTranslator);
        if (!mModularSynthesizer.run()) {
          return setBooleanResult(false);
        }
        collectSupervisors(mModularSynthesizer);
        maybeBlocking = !mModularSynthesizer.getDisabledEvents().isEmpty();
        supCount++;
      } while (maybeBlocking);
      results.setSatisfied(true);
      return true;
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } catch (final OutOfMemoryError error) {
      tearDown();
      final Logger logger = LogManager.getLogger();
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


  //#########################################################################
  //# Auxiliary Methods
  private void collectSupervisors(final SupervisorSynthesizer synthesizer)
  {
    final CompositionalAutomataSynthesisResult results = getAnalysisResult();
    final Collection<AutomatonProxy> supressedAutomata = new THashSet<>();
    supressedAutomata.addAll(getModel().getAutomata());
    supressedAutomata.addAll(results.getComputedAutomata());
    for (final AutomatonProxy aut : synthesizer.getComputedProductDES().getAutomata()) {
      if (!supressedAutomata.contains(aut)) {
        results.addBackRenamedSupervisor(aut);
      }
    }
  }


  //#########################################################################
  //# Inner Class ModularKindTranslator
  private class ModularKindTranslator implements KindTranslator
  {
    //#######################################################################
    //# Constructors
    ModularKindTranslator(final Collection<AutomatonProxy> specToBe)
    {
      mSpecificationToBe = new THashSet<>(specToBe);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.KindTranslator
    @Override
    public EventKind getEventKind(final EventProxy event)
    {
      final KindTranslator translator = getKindTranslator();
      return translator.getEventKind(event);
    }

    @Override
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      if (mSpecificationToBe.contains(aut)) {
        return ComponentKind.SPEC;
      } else {
        return ComponentKind.PLANT;
      }
    }

    //#######################################################################
    //# Data Members
    private final Set<AutomatonProxy> mSpecificationToBe;
  }


  //#########################################################################
  //# Data Members
  // Permanent tools
  private final ModularControllabilitySynthesizer mModularSynthesizer;
  private final CompositionalAutomataSynthesizer mCompositionalSynthesizer;
  private final CompositionalConflictChecker mCompositionalConflictChecker;

}
