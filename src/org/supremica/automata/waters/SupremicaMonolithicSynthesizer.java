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
 *  suffered by Licensee from the use of this oftware.
 *
 *  Supremica is owned and represented by KA.
 */

package org.supremica.automata.waters;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.SupervisorReductionFactory;
import net.sourceforge.waters.model.options.BooleanOption;
import net.sourceforge.waters.model.options.LeafOptionPage;
import net.sourceforge.waters.model.options.Option;
import net.sourceforge.waters.model.options.PositiveIntOption;
import net.sourceforge.waters.model.options.PropositionOption;
import net.sourceforge.waters.model.options.StringOption;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.DefaultProductDESResult;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.analysis.kindtranslator.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.SynthesisKindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.StateSet;
import org.supremica.automata.IO.AutomataToWaters;
import org.supremica.automata.algorithms.AutomataSynchronizerHelperStatistics;
import org.supremica.automata.algorithms.MonolithicAutomataSynthesizer;
import org.supremica.automata.algorithms.MonolithicReturnValue;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.automata.algorithms.SynthesisType;
import org.supremica.automata.algorithms.SynthesizerOptions;


/**
 * <P>A wrapper to invoke Supremica's monolithic synthesis algorithm ({@link
 * MonolithicAutomataSynthesizer}) through the {@link SupervisorSynthesizer}
 * interface of Waters.</P>
 *
 * <P>The SupremicaMonolithicSynthesizer converts its input to an
 * {@link Automata} object, launches a {@link SupervisorSynthesizer}, and
 * converts the synthesised supervisor back to an {@link AutomatonProxy}
 * object.</P>
 *
 * @author Robi Malik
 */

public class SupremicaMonolithicSynthesizer
  extends SupremicaModelAnalyzer
  implements SupervisorSynthesizer
{

  //#########################################################################
  //# Constructors
  public SupremicaMonolithicSynthesizer(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public SupremicaMonolithicSynthesizer(final ProductDESProxy model,
                                        final ProductDESProxyFactory factory)
  {
    super(model, factory,
          SynthesisKindTranslator.getInstanceWithControllability(), true);
  }


  //#########################################################################
  //# Specific Configuration
  /**
   * Sets whether controllability is considered during synthesis.
   * If enabled (the default), synthesis must ensure a supervisor that
   * is controllable based on the event and component types defined in
   * the model.
   */
  public void setControllableSynthesis(final boolean controllable)
  {
    final SynthesisType type = mSynthesizerOptions.getSynthesisType();
    if (controllable) {
      if (type == null) {
        mSynthesizerOptions.setSynthesisType(SynthesisType.CONTROLLABLE);
      } else if (type == SynthesisType.NONBLOCKING) {
        mSynthesizerOptions.setSynthesisType(SynthesisType.NONBLOCKING_CONTROLLABLE);
      }
    } else {
      if (type == SynthesisType.CONTROLLABLE) {
        mSynthesizerOptions.setSynthesisType(null);
      } else if (type == SynthesisType.NONBLOCKING_CONTROLLABLE) {
        mSynthesizerOptions.setSynthesisType(SynthesisType.NONBLOCKING);
      }
    }
  }

  /**
   * Returns whether controllability is considered during synthesis.
   * @see #setControllableSynthesis(boolean)
   */
  public boolean isControllableSynthesis()
  {
    final SynthesisType type = mSynthesizerOptions.getSynthesisType();
    if (type == null) {
      return false;
    } else {
      switch (type) {
      case CONTROLLABLE:
      case NONBLOCKING_CONTROLLABLE:
        return true;
      case NONBLOCKING:
        return false;
      default:
        throw new IllegalStateException("Unknown synthesis type '" +
                                        mSynthesizerOptions.getSynthesisType() +
                                        "'!");
      }
    }
  }

  /**
   * Sets how state names in the synchronous product are generated.
   * If disabled (the default), the synchronous product has long names
   * for state tuples that include the names of all component states.
   * If enabled, the synchronous product uses simple numbered states,
   * <CODE>q0</CODE>, <CODE>q1</CODE>, etc.
   */
  public void setUsingShortStateNames(final boolean shortNames)
  {
    mSynchronizationOptions.setUseShortStateNames(shortNames);
  }

  /**
   * Sets a string to separate state tuple components when using long
   * state names.
   */
  public void setStateNameSeparator(final String sep)
  {
    mSynchronizationOptions.setStateNameSeparator(sep);
  }

  /**
   * Returns the string to separate state tuple components when using long
   * state names.
   * @see #setStateNameSeparator(String)
   */
  public String getStateNameSeparator()
  {
    return mSynchronizationOptions.getStateNameSeparator();
  }

  /**
   * Returns how state names in the synchronous product are generated.
   * @see #setUsingShortStateNames(boolean)
   */
  public boolean isUsingShortStateNames()
  {
    return mSynchronizationOptions.useShortStateNames();
  }

  /**
   * Sets whether unreachable states are removed in synthesis.
   * If enabled (the default), states that become unreachable during
   * synthesis are removed from the supervisor.
   */
  public void setPurging(final boolean purging)
  {
    mSynthesizerOptions.setPurge(purging);
  }

  /**
   * Returns whether unreachable states are removed in synthesis.
   * @see #setPurging(boolean)
   */
  public boolean isPurging()
  {
    return mSynthesizerOptions.doPurge();
  }


  //#########################################################################
  //# Overrides for
  //# org.supremica.automata.waters.SupremicaModelAnalyzer
  @Override
  public void setSynchronisingOnUnobservableEvents(final boolean sync)
  {
    mSynchronizationOptions.setUnobsEventsSynch(sync);
  }

  @Override
  public boolean isSynchronisingOnUnobservableEvents()
  {
    return mSynchronizationOptions.getUnobsEventsSynch();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.SupervisorSynthesizer
  @Override
  public void setNonblockingSynthesis(final boolean nonblocking)
  {
    final SynthesisType type = mSynthesizerOptions.getSynthesisType();
    if (nonblocking) {
      if (type == null) {
        mSynthesizerOptions.setSynthesisType(SynthesisType.NONBLOCKING);
      } else if (type == SynthesisType.CONTROLLABLE) {
        mSynthesizerOptions.setSynthesisType(SynthesisType.NONBLOCKING_CONTROLLABLE);
      }
    } else {
      if (type == SynthesisType.NONBLOCKING) {
        mSynthesizerOptions.setSynthesisType(null);
      } else if (type == SynthesisType.NONBLOCKING_CONTROLLABLE) {
        mSynthesizerOptions.setSynthesisType(SynthesisType.CONTROLLABLE);
      }
    }
  }

  @Override
  public boolean isNonblockingSynthesis()
  {
    final SynthesisType type = mSynthesizerOptions.getSynthesisType();
    if (type == null) {
      return false;
    } else {
      switch (type) {
      case CONTROLLABLE:
        return false;
      case NONBLOCKING:
      case NONBLOCKING_CONTROLLABLE:
        return true;
      default:
        throw new IllegalStateException("Unknown synthesis type '" +
                                        mSynthesizerOptions.getSynthesisType() +
                                        "'!");
      }
    }
  }

  @Override
  public void setNondeterminismEnabled(final boolean enable)
  {
  }

  @Override
  public void setSupervisorReductionFactory(final SupervisorReductionFactory factory)
  {
    if (factory == null ||
        factory == SupremicaSupervisorReductionFactory.OFF) {
      mSynthesizerOptions.setReduceSupervisors(false);
    } else if (factory == SupremicaSupervisorReductionFactory.DET_MINSTATE) {
      mSynthesizerOptions.setReduceSupervisors(true);
    } else {
      throw new IllegalArgumentException("Unknown supervisor reduction type '" +
                                         factory + "'!");
    }
  }

  @Override
  public SupervisorReductionFactory getSupervisorReductionFactory()
  {
    if (mSynthesizerOptions.getReduceSupervisors()) {
      return SupremicaSupervisorReductionFactory.DET_MINSTATE;
    } else {
      return SupremicaSupervisorReductionFactory.OFF;
    }
  }

  @Override
  public void setSupervisorLocalizationEnabled(final boolean enable)
  {
  }

  @Override
  public boolean isSupervisorLocalizationEnabled()
  {
    return false;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.ProductDESBuilder
  @Override
  public ProductDESResult getAnalysisResult()
  {
    return (ProductDESResult) super.getAnalysisResult();
  }

  @Override
  public ProductDESProxy getComputedProductDES()
  {
    final ProductDESResult result = getAnalysisResult();
    return result.getComputedProductDES();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.ModelBuilder<ProductDESProxy>
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
  public ProductDESProxy getComputedProxy()
  {
    return getComputedProductDES();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.ModelAnalyzer
  @Override
  public List<Option<?>> getOptions(final LeafOptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.prepend(options, AbstractModelAnalyzerFactory.
               OPTION_SupervisorSynthesizer_ConfiguredDefaultMarking);
    db.prepend(options, AbstractModelAnalyzerFactory.
               OPTION_SupervisorSynthesizer_NonblockingSynthesis);
    db.prepend(options, AbstractModelAnalyzerFactory.
               OPTION_SupervisorSynthesizer_ControllableSynthesis);
    db.prepend(options, AbstractModelAnalyzerFactory.
               OPTION_SynchronousProductBuilder_DetailedOutputEnabled);
    db.append(options, AbstractModelAnalyzerFactory.
              OPTION_SynchronousProductBuilder_OutputName);
    db.append(options, SupremicaModelAnalyzerFactory.
              OPTION_SupremicaSynchronousProductBuilder_StateNameSeparator);
    db.append(options, SupremicaModelAnalyzerFactory.
              OPTION_SupremicaSynchronousProductBuilder_ShortStateNames);
    db.append(options, SupremicaModelAnalyzerFactory.
              OPTION_SupremicaSupervisorSynthesizer_Purging);
    db.append(options, SupremicaModelAnalyzerFactory.
              OPTION_SupremicaSynchronousProductBuilder_EnsuringUncontrollablesInPlant);
    db.append(options, SupremicaModelAnalyzerFactory.
              OPTION_SupremicaSupervisorSynthesizer_SupervisorReduction);
    db.append(options, AbstractModelAnalyzerFactory.
              OPTION_ModelAnalyzer_FinalStateLimit);
    db.append(options, AbstractModelAnalyzerFactory.
              OPTION_ModelAnalyzer_FinalTransitionLimit);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(AbstractModelAnalyzerFactory.
                     OPTION_SupervisorSynthesizer_ConfiguredDefaultMarking)) {
      final PropositionOption propOption = (PropositionOption) option;
      setConfiguredDefaultMarking(propOption.getValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_SupervisorSynthesizer_NonblockingSynthesis)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setNonblockingSynthesis(boolOption.getBooleanValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_SupervisorSynthesizer_ControllableSynthesis)) {
      final BooleanOption boolOption = (BooleanOption) option;
      final KindTranslator translator = boolOption.getBooleanValue() ?
        IdenticalKindTranslator.getInstance() :
        ConflictKindTranslator.getInstanceControllable();
      setKindTranslator(translator);
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_SynchronousProductBuilder_DetailedOutputEnabled)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setDetailedOutputEnabled(boolOption.getBooleanValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_SynchronousProductBuilder_OutputName)) {
      final StringOption stringOption = (StringOption) option;
      setOutputName(stringOption.getValue());
    } else if (option.hasID(SupremicaModelAnalyzerFactory.
                            OPTION_SupremicaSynchronousProductBuilder_StateNameSeparator)) {
      final StringOption stringOption = (StringOption) option;
      setStateNameSeparator(stringOption.getValue());
    } else if (option.hasID(SupremicaModelAnalyzerFactory.
                            OPTION_SupremicaSynchronousProductBuilder_ShortStateNames)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setUsingShortStateNames(boolOption.getBooleanValue());
    } else if (option.hasID(SupremicaModelAnalyzerFactory.
                            OPTION_SupremicaSupervisorSynthesizer_Purging)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setPurging(boolOption.getBooleanValue());
    } else if (option.hasID(SupremicaModelAnalyzerFactory.
                            OPTION_SupremicaSynchronousProductBuilder_EnsuringUncontrollablesInPlant)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setEnsuringUncontrollablesInPlant(boolOption.getBooleanValue());
    } else if (option.hasID(SupremicaModelAnalyzerFactory.
                            OPTION_SupremicaSupervisorSynthesizer_SupervisorReduction)) {
      final BooleanOption boolOption = (BooleanOption) option;
      mSynthesizerOptions.setReduceSupervisors(boolOption.getBooleanValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_ModelAnalyzer_FinalStateLimit)) {
      final PositiveIntOption intOption = (PositiveIntOption) option;
      setNodeLimit(intOption.getIntValue());
    } else if (option.hasID(AbstractModelAnalyzerFactory.
                            OPTION_ModelAnalyzer_FinalTransitionLimit)) {
      final PositiveIntOption intOption = (PositiveIntOption) option;
      setTransitionLimit(intOption.getIntValue());
    } else {
      super.setOption(option);
    }
  }

  @Override
  public ProductDESResult createAnalysisResult()
  {
    return new DefaultProductDESResult(this);
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzer
  /**
   * Returns whether or not this model analyser supports nondeterministic
   * automata.
   * @return <CODE>false</CODE> as Supremica's synthesis does not
   *         support nondeterminism.
   */
  @Override
  public boolean supportsNondeterminism()
  {
    return false;
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    mHelperStatistics = new AutomataSynchronizerHelperStatistics();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mHelperStatistics = null;
  }

  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      if (mSynthesizerOptions.getSynthesisType() == null) {
        return setBooleanResult(true);
      }

      final Automata automata = getSupremicaAutomata();
      final MonolithicAutomataSynthesizer synthesizer =
        new MonolithicAutomataSynthesizer();
      setSupremicaTask(synthesizer);
      final MonolithicReturnValue supResult =
        synthesizer.synthesizeSupervisor(automata, mSynthesizerOptions,
                                         mSynchronizationOptions,
                                         null, mHelperStatistics, false);
      final Automaton aut = supResult.automaton;

      final StateSet states = aut.getStateSet();
      if (!states.hasInitialState()) {
        return setBooleanResult(false);
      } else if (states.size() > getNodeLimit()) {
        throw new OverflowException(getNodeLimit());
      } else if (mHelperStatistics.getNumberOfExaminedTransitions() >
                 getTransitionLimit()) {
        throw new OverflowException(OverflowKind.TRANSITION,
                                    getTransitionLimit());
      }

      if (isDetailedOutputEnabled()) {
        checkAbort();
        aut.setName(mOutputName);
        final ProductDESProxyFactory factory = getFactory();
        final ProductDESProxy model = getModel();
        final EventProxy defaultMarking = getConfiguredDefaultMarking();
        final AutomataToWaters importer =
          new AutomataToWaters(factory, model, defaultMarking);
        importer.setSuppressingRedundantSelfloops(true);
        final AutomatonProxy sup = importer.convertAutomaton(aut);
        final Collection<AutomatonProxy> sups = Collections.singletonList(sup);
        final ProductDESProxy supDES =
          AutomatonTools.createProductDESProxy(mOutputName, sups, factory);
        final ProductDESResult watersResult = getAnalysisResult();
        watersResult.setComputedProductDES(supDES);
      }
      return setBooleanResult(true);
    } catch (final OutOfMemoryError error) {
      System.gc();
      final AnalysisException exception = new OverflowException(error);
      throw setExceptionResult(exception);
    } catch (final AnalysisException exception) {
      throw setExceptionResult(exception);
    } finally {
      tearDown();
    }
  }

  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final ProductDESResult result = getAnalysisResult();
    if (mHelperStatistics != null) {
      result.setNumberOfStates(mHelperStatistics.getNumberOfReachableStates());
      result.setNumberOfTransitions(mHelperStatistics.getNumberOfExaminedTransitions());
    }
  }


  //#########################################################################
  //# Data Members
  private String mOutputName = "monolithic_supervisor";
  private final SynthesizerOptions mSynthesizerOptions =
    SynthesizerOptions.getDefaultMonolithicCNBSynthesizerOptions();
  private final SynchronizationOptions mSynchronizationOptions =
    SynchronizationOptions.getDefaultSynthesisOptions();

  private AutomataSynchronizerHelperStatistics mHelperStatistics = null;

}
