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
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
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
  }

  public PreselectingMethod getPreselectingMethod()
  {
    return mCompositionalSynthesizer.getPreselectingMethod();
  }

  public void setSelectionHeuristic(final SelectionHeuristicCreator selecting)
  {
    mCompositionalSynthesizer.setSelectionHeuristic(selecting);
  }

  public SelectionHeuristic<Candidate> getSelectionHeuristic()
  {
    return mCompositionalSynthesizer.getSelectionHeuristic();
  }

  public void setInternalStateLimit(final int internalStateLimit)
  {
    mCompositionalSynthesizer.setInternalStateLimit(internalStateLimit);
  }

  public int getInternalStateLimit()
  {
    return mCompositionalSynthesizer.getInternalStateLimit();
  }

  public void setInternalTransitionLimit(final int internalTransitionLimit)
  {
    mCompositionalSynthesizer.setInternalTransitionLimit(internalTransitionLimit);
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
    final KindTranslator translator =
      ConflictKindTranslator.getInstanceControllable();
    mCompositionalSynthesizer.setKindTranslator(translator);
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
        final ProductDESProxy modularResult =
          mModularSynthesizer.getComputedProductDES();
        if (!modularResult.getEvents().contains(mUsedMarking)) {
          break;
        }
        mCompositionalSynthesizer.setModel(modularResult);
        if (!mCompositionalSynthesizer.run()) {
          return setBooleanResult(false);
        }
        final CompositionalAutomataSynthesisResult compositionalResult =
          mCompositionalSynthesizer.getAnalysisResult();
        final Collection<AutomatonProxy> computedAutomata =
          compositionalResult.getComputedAutomata();
        supervisors.addAll(computedAutomata);
        final Collection<AutomatonProxy> specToBe =
          new ArrayList<>(computedAutomata.size());
        for (final AutomatonProxy aut : computedAutomata) {
          for (final EventProxy event : compositionalResult.getDisabledEvents(aut)) {
            if (translator.getEventKind(event) == EventKind.UNCONTROLLABLE) {
              specToBe.add(aut);
              break;
            }
          }
        }
        if (specToBe.isEmpty()) {
          break;
        }
        final Collection<AutomatonProxy> modularAutomata =
          modularResult.getAutomata();
        final Collection<AutomatonProxy> combinedAutomata =
          new ArrayList<>(modularAutomata.size() + computedAutomata.size());
        combinedAutomata.addAll(modularAutomata);
        combinedAutomata.addAll(computedAutomata);
        final ProductDESProxy modularDES = AutomatonTools.createProductDESProxy
          (model.getName(), combinedAutomata, getFactory());
        mModularSynthesizer.setModel(modularDES);
        final ModularKindTranslator modularTranslator =
          new ModularKindTranslator(specToBe);
        mModularSynthesizer.setKindTranslator(modularTranslator);
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
  //# Inner Class ModularKindTranslator
  private class ModularKindTranslator implements KindTranslator
  {
    //#######################################################################
    //# Constructors
    ModularKindTranslator(final Collection<AutomatonProxy> specToBe)
    {
      mSpecificationToBe = specToBe;
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

}