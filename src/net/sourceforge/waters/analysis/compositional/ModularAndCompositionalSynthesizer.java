//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   ModularAndCompositionalSynthesizer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.compositional.AbstractCompositionalModelAnalyzer.PreselectingMethod;
import net.sourceforge.waters.analysis.compositional.AbstractCompositionalModelAnalyzer.PreselectingMethodFactory;
import net.sourceforge.waters.analysis.modular.ModularControllabilitySynthesizer;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.AbstractProductDESBuilder;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
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
 * <P>The modular and compositional synthesis algorithm.</P>
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public class ModularAndCompositionalSynthesizer
  extends AbstractProductDESBuilder
  implements SupervisorSynthesizer
{

  //#########################################################################
  //# Constructors
  public ModularAndCompositionalSynthesizer(final ProductDESProxyFactory factory)
  {
    super(factory);
    mModularSynthesizer = new ModularControllabilitySynthesizer(factory);
    mCompositionalSynthesizer = new CompositionalAutomataSynthesizer(factory);
  }

  public ModularAndCompositionalSynthesizer(final ProductDESProxy model,
                                           final ProductDESProxyFactory factory)
  {
    super(model, factory);
    mModularSynthesizer = new ModularControllabilitySynthesizer(factory);
    mCompositionalSynthesizer = new CompositionalAutomataSynthesizer(factory);
  }

  public ModularAndCompositionalSynthesizer(final ProductDESProxy model,
                                           final ProductDESProxyFactory factory,
                                           final KindTranslator translator)
  {
    super(model, factory, translator);
    mModularSynthesizer = new ModularControllabilitySynthesizer(factory);
    mCompositionalSynthesizer = new CompositionalAutomataSynthesizer(factory);
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

  public void setInternalStateLimit(final int internalStateLimit)
  {
    mCompositionalSynthesizer.setInternalStateLimit(internalStateLimit);
  }

  public void setInternalTransitionLimit(final int internalTransitionLimit)
  {
    mCompositionalSynthesizer.setInternalTransitionLimit(internalTransitionLimit);
  }

  public void setMonolithicStateLimit(final int finalStateLimit)
  {
    setNodeLimit(finalStateLimit);
  }

  public void setMonolithicTransitionLimit(final int finalTransitionLimit)
  {
    setTransitionLimit(finalTransitionLimit);
  }

  public void setPreselectingMethod(final PreselectingMethod preselecting)
  {
    mCompositionalSynthesizer.setPreselectingMethod(preselecting);
  }

  public void setSelectionHeuristic(final SelectionHeuristicCreator selecting)
  {
    mCompositionalSynthesizer.setSelectionHeuristic(selecting);
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
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SupervisorSynthesizer
  @Override
  public void setConfiguredDefaultMarking(final EventProxy marking)
  {
    mConfiguredMarking = marking;
    mUsedMarking = null;
  }

  @Override
  public EventProxy getConfiguredDefaultMarking()
  {
    return mConfiguredMarking;
  }

  @Override
  public void setSupervisorReductionEnabled(final boolean enable)
  {
    mModularSynthesizer.setSupervisorReductionEnabled(enable);
    mCompositionalSynthesizer.setSupervisorReductionEnabled(enable);
  }

  @Override
  public boolean getSupervisorReductionEnabled()
  {
    return mModularSynthesizer.getSupervisorReductionEnabled();
  }

  @Override
  public void setSupervisorLocalizationEnabled(final boolean enable)
  {
    mModularSynthesizer.setSupervisorLocalizationEnabled(enable);
    mCompositionalSynthesizer.setSupervisorLocalizationEnabled(enable);
  }

  @Override
  public boolean getSupervisorLocalizationEnabled()
  {
    return mModularSynthesizer.getSupervisorLocalizationEnabled();
  }


  //#########################################################################
  //# Overrides for Base Class
  //# net.sourceforge.waters.model.analysis.AbstractModelAnalyser
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    getUsedDefaultMarking();
    final KindTranslator translator = ConflictKindTranslator.getInstanceControllable();
    mCompositionalSynthesizer.setKindTranslator(translator);
    mCompositionalSynthesizer.setAbstractionProcedureCreator
      (AutomataSynthesisAbstractionProcedureFactory.OE);
    mCompositionalSynthesizer.setNodeLimit(getNodeLimit());
    mCompositionalSynthesizer.setTransitionLimit(getTransitionLimit());
    mCompositionalSynthesizer.setConfiguredDefaultMarking(mUsedMarking);
    mModularSynthesizer.setIncludesAllAutomata(true);
    mModularSynthesizer.setNodeLimit(getNodeLimit());
    mModularSynthesizer.setTransitionLimit(getTransitionLimit());
    mModularSynthesizer.setConfiguredDefaultMarking(mUsedMarking);
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
    mUsedMarking = null;
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
      mModularSynthesizer.setModel(model);
      mModularSynthesizer.setKindTranslator(translator);
      if (!mModularSynthesizer.run()) {
        return setBooleanResult(false);
      }
      final Collection<AutomatonProxy> supervisors = new THashSet<>();
      collectSupervisors(supervisors, mModularSynthesizer);
      boolean maybeBlocking = false;
      int supCount = 1;
      do {
        mCompositionalSynthesizer.setSupervisorNamePrefix("sup" + supCount + ":");
        final ProductDESProxy modularResults =
          mModularSynthesizer.getComputedProductDES();
        if (!modularResults.getEvents().contains(mUsedMarking)) {
          break;
        }
        mCompositionalSynthesizer.setModel(modularResults);
        if (!mCompositionalSynthesizer.run()) {
          return setBooleanResult(false);
        }
        supervisors.addAll(mCompositionalSynthesizer.getComputedProductDES().getAutomata());
        final Collection<AutomatonProxy> specToBe = new ArrayList<>();
        final CompositionalAutomataSynthesisResult compositionalResults =
          mCompositionalSynthesizer.getAnalysisResult();
        for (final AutomatonProxy aut : compositionalResults.getComputedAutomata()){
          for (final EventProxy event : compositionalResults.getDisabledEvents(aut)) {
            if (translator.getEventKind(event) == EventKind.UNCONTROLLABLE) {
              specToBe.add(aut);
              break;
            }
          }
        }
        if (specToBe.isEmpty()) {
          break;
        }
        final Collection<AutomatonProxy> combinedAutomata = new ArrayList<>();
        final Collection<AutomatonProxy> modularAutomata = modularResults.getAutomata();
        combinedAutomata.addAll(modularAutomata);
        combinedAutomata.addAll(compositionalResults.getComputedAutomata());
        final ProductDESProxy modularDES = AutomatonTools.createProductDESProxy
          (model.getName(), combinedAutomata, getFactory());
        final ModularKindTranslator modularTranslator = new ModularKindTranslator(specToBe);
        mModularSynthesizer.setKindTranslator(modularTranslator);
        mModularSynthesizer.setModel(modularDES);
        if (!mModularSynthesizer.run()) {
          return setBooleanResult(false);
        }
        collectSupervisors(supervisors, mModularSynthesizer);
        maybeBlocking = !mModularSynthesizer.getDisabledEvents().isEmpty();
        supCount++;
      } while (maybeBlocking);
      final List<AutomatonProxy> supervisorsList = new ArrayList<>(supervisors);
      Collections.sort(supervisorsList);
      final ProductDESProxy supervisorsDES =
        AutomatonTools.createProductDESProxy("supervisor", supervisorsList,
                                             getFactory());
      return setProxyResult(supervisorsDES);
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
  /**
   * Gets the marking proposition to be used.
   * This method returns the marking proposition specified by the {@link
   * #setConfiguredDefaultMarking(EventProxy) setMarkingProposition()} method,
   * if non-null, or the default marking proposition of the input model.
   * @throws EventNotFoundException to indicate that the a
   *         <CODE>null</CODE> marking was specified, but input model does
   *         not contain any proposition with the default marking name.
   */
  protected EventProxy getUsedDefaultMarking()
    throws EventNotFoundException
  {
    if (mUsedMarking == null) {
      if (mConfiguredMarking == null) {
        final ProductDESProxy model = getModel();
        mUsedMarking = AbstractConflictChecker.getMarkingProposition(model);
      } else {
        mUsedMarking = mConfiguredMarking;
      }
    }
    return mUsedMarking;
  }

  private void collectSupervisors(final Collection<AutomatonProxy> supervisors,
                                  final SupervisorSynthesizer synthesizer)
  {
    final Collection<AutomatonProxy> model = getModel().getAutomata();
    for (final AutomatonProxy aut : synthesizer.getComputedProductDES().getAutomata()) {
      if (!model.contains(aut)) {
        supervisors.add(aut);
      }
    }
  }


  //#########################################################################
  //# Debugging
  //#########################################################################
  //# Inner Class
  private class ModularKindTranslator implements KindTranslator
  {
    ModularKindTranslator (final Collection<AutomatonProxy> specToBe) {
      mSpecificationToBe = specToBe;
    }

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

    private final Collection<AutomatonProxy> mSpecificationToBe;
  }


  //#########################################################################
  //# Data Members
  // Configuration options
  private EventProxy mConfiguredMarking;
  private EventProxy mUsedMarking;

  // Permanent tools
  private final ModularControllabilitySynthesizer mModularSynthesizer;
  private final CompositionalAutomataSynthesizer mCompositionalSynthesizer;

  // Algorithm variables


  //#########################################################################
  //# Class Constants

}