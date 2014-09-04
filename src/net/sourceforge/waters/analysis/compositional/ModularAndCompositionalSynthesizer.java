//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   ModularAndCompositionalSynthesizer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.ArrayList;
import java.util.Collection;

import net.sourceforge.waters.analysis.modular.ModularControllabilitySynthesizer;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
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
    mModularSynthesizer.setConfiguredDefaultMarking(marking);
    mCompositionalSynthesizer.setConfiguredDefaultMarking(marking);
  }

  @Override
  public EventProxy getConfiguredDefaultMarking()
  {
    return mModularSynthesizer.getConfiguredDefaultMarking();
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
    final KindTranslator translator = ConflictKindTranslator.getInstanceControllable();
    mCompositionalSynthesizer.setKindTranslator(translator);
    mCompositionalSynthesizer.setAbstractionProcedureCreator
    (AutomataSynthesisAbstractionProcedureFactory.OE);
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
      boolean maybeBlocking = false;
      do {
        final ProductDESProxy modularResults =
          mModularSynthesizer.getComputedProductDES();
        mCompositionalSynthesizer.setModel(modularResults);
        if (!mCompositionalSynthesizer.run()) {
          return setBooleanResult(false);
        }
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
        maybeBlocking = !mModularSynthesizer.getDisabledEvents().isEmpty();
      } while (maybeBlocking);
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

  // Permanent tools
  private final ModularControllabilitySynthesizer mModularSynthesizer;
  private final CompositionalAutomataSynthesizer mCompositionalSynthesizer;

  // Algorithm variables


  //#########################################################################
  //# Class Constants

}