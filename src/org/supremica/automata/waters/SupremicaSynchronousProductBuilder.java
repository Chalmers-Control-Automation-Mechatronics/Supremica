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

import net.sourceforge.waters.analysis.options.BoolParameter;
import net.sourceforge.waters.analysis.options.ComponentKindParameter;
import net.sourceforge.waters.analysis.options.EventParameter;
import net.sourceforge.waters.analysis.options.Parameter;
import net.sourceforge.waters.analysis.options.ParameterIDs;
import net.sourceforge.waters.analysis.options.StringParameter;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;
import net.sourceforge.waters.model.analysis.des.DefaultSynchronousProductResult;
import net.sourceforge.waters.model.analysis.des.SynchronousProductBuilder;
import net.sourceforge.waters.model.analysis.des.SynchronousProductResult;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.IO.AutomataToWaters;
import org.supremica.automata.algorithms.AutomataSynchronizer;
import org.supremica.automata.algorithms.AutomataSynchronizerHelperStatistics;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.properties.Config;


/**
 * <P>A wrapper to invoke Supremica's synchronous product algorithm ({@link
 * AutomataSynchronizer}) through the {@link SynchronousProductBuilder}
 * interface of Waters.</P>
 *
 * <P>The SupremicaSynchronousProductBuilder converts its input to an
 * {@link Automata} object, launches a {@link AutomataSynchronizer}, and
 * converts the computed synchronous product {@link Automaton} to an
 * {@link AutomatonProxy} object.</P>
 *
 * @author Robi Malik
 */

public class SupremicaSynchronousProductBuilder
  extends SupremicaModelAnalyzer
  implements SynchronousProductBuilder
{

  //#########################################################################
  //# Constructors
  public SupremicaSynchronousProductBuilder(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public SupremicaSynchronousProductBuilder(final ProductDESProxy model,
                                            final ProductDESProxyFactory factory)
  {
    super(model, factory, IdenticalKindTranslator.getInstance(), false);
    setDetailedOutputEnabled(mSynchronizationOptions.buildAutomaton());
    updateEnsuringUncontrollablesInPlant();
  }


  //#########################################################################
  //# Specific Configuration
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
   * Returns how state names in the synchronous product are generated.
   * @see #setUsingShortStateNames(boolean)
   */
  public boolean isUsingShortStateNames()
  {
    return mSynchronizationOptions.useShortStateNames();
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
   * Sets whether uncontrollable states are to be marked as forbidden
   * in the synchronous product.
   */
  public void setMarkingUncontrollableStatesAsForbidden(final boolean forbid)
  {
    mSynchronizationOptions.setForbidUncontrollableStates(forbid);
    updateEnsuringUncontrollablesInPlant();
  }

  /**
   * Returns whether uncontrollable states are to be marked as forbidden
   * in the synchronous product.
   * @see #setMarkingUncontrollableStatesAsForbidden(boolean)
   */
  public boolean isMarkingUncontrollableStatesAsForbidden()
  {
    return mSynchronizationOptions.forbidUncontrollableStates();
  }

  /**
   * Sets whether forbidden states encountered in the synchronous
   * product are explored further.
   */
  public void setExpandingForbiddenStates(final boolean expand)
  {
    mSynchronizationOptions.setExpandForbiddenStates(expand);
  }

  /**
   * Returns whether forbidden states encountered in the synchronous
   * product are explored further.
   * @see #setExpandingForbiddenStates(boolean)
   */
  public boolean isExpandingForbiddenStates()
  {
    return mSynchronizationOptions.expandForbiddenStates();
  }

  /**
   * Sets whether disabled transitions are to be included in the synchronous
   * product. If enabled, the synchronous product includes transitions to a
   * 'dump' state for all transitions in a plant that are disabled by a
   * specification
   */
  public void setRememberingDisabledEvents(final boolean remember)
  {
    mSynchronizationOptions.setRememberDisabledEvents(remember);
    updateEnsuringUncontrollablesInPlant();
  }

  /**
   * Returns whether disabled transitions are to be included in the synchronous
   * product.
   * @see #setRememberingDisabledEvents(boolean)
   */
  public boolean isRememberingDisabledEvents()
  {
    return mSynchronizationOptions.rememberDisabledEvents();
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

  @Override
  public void setEnsuringUncontrollablesInPlant(final boolean ensure)
  {
    mUncontrollablesInPlantRequested = ensure;
    updateEnsuringUncontrollablesInPlant();
  }

  @Override
  public boolean isEnsuringUncontrollablesInPlant()
  {
    return mUncontrollablesInPlantRequested;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.SynchronousProductBuilder
  @Override
  public void setOutputKind(final ComponentKind kind)
  {
    mOutputKind = kind;
  }

  @Override
  public ComponentKind getOutputKind()
  {
    return mOutputKind;
  }

  @Override
  public void setRemovingSelfloops(final boolean removing)
  {
    mRemovingSelfloops = removing;
  }

  @Override
  public boolean isRemovingSelfloops()
  {
    return mRemovingSelfloops;
  }

  @Override
  public void setPropositions(final Collection<EventProxy> props)
  {
    switch (props.size()) {
    case 0:
      setConfiguredDefaultMarking(null);
      break;
    case 1:
      setConfiguredDefaultMarking(props.iterator().next());
      break;
    default:
      throw new UnsupportedOperationException
        ("Supremica does not support more than one proposition!");
    }
  }

  @Override
  public Collection<EventProxy> getPropositions()
  {
    final EventProxy marking = getConfiguredDefaultMarking();
    if (marking == null) {
      return Collections.emptyList();
    } else {
      return Collections.singletonList(marking);
    }
  }

  @Override
  public void addMask(final Collection<EventProxy> hidden,
                      final EventProxy replacement)
    throws OverflowException
  {
    throw new UnsupportedOperationException
      ("Supremica synchronous product does not support event masking!");
  }

  @Override
  public void clearMask()
  {
  }

  @Override
  public SynchronousProductResult getAnalysisResult()
  {
    return (SynchronousProductResult) super.getAnalysisResult();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.AutomatonBuilder
  @Override
  public AutomatonProxy getComputedAutomaton()
  {
    final SynchronousProductResult result = getAnalysisResult();
    return result.getComputedAutomaton();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.ModelBuilder<AutomatonProxy>
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
    return getComputedAutomaton();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.ModelAnalyzer
  @Override
  public void setDetailedOutputEnabled(final boolean enable)
  {
    super.setDetailedOutputEnabled(enable);
    mSynchronizationOptions.setBuildAutomaton(enable);
  }

  @Override
  public List<Parameter> getParameters()
  {
    final List<Parameter> list = super.getParameters();
    for (final Parameter param : list) {
      if (param.getID() == ParameterIDs.ModelAnalyzer_DetailedOutputEnabled_ID) {
        param.setName("Build automaton model");
        param.setDescription("Disable this to suppress the creation of a " +
                             "synchronous product automaton, and only run " +
                             "for statistics.");
        break;
      }
    }
    list.add(new EventParameter
      (ParameterIDs.SupervisorSynthesizer_ConfiguredDefaultMarking)
      {
        @Override
        public void commitValue()
        {
          setConfiguredDefaultMarking(getValue());
        }
      });
    list.add(new StringParameter
      (ParameterIDs.ModelBuilder_OutputName)
      {
        @Override
        public void commitValue()
        {
          setOutputName(getValue());
        }
      });
    list.add(new ComponentKindParameter
      (ParameterIDs.AutomatonBuilder_OutputKind)
      {
        @Override
        public void commitValue()
        {
          setOutputKind(getValue());
        }
      });
    list.add(new BoolParameter
      (ParameterIDs.SupremicaSynchronousProductBuilder_ShortStateNames)
      {
        @Override
        public void commitValue()
        {
          setUsingShortStateNames(getValue());
        }
      });
    list.add(new StringParameter
      (ParameterIDs.SupremicaSynchronousProductBuilder_StateNameSeparator,
       getStateNameSeparator())
      {
        @Override
        public void commitValue()
        {
          setStateNameSeparator(getValue());
        }
      });
    list.add(new BoolParameter
      (ParameterIDs.SynchronousProductBuilder_RemovingSelfloops)
      {
        @Override
        public void commitValue()
        {
          setRemovingSelfloops(getValue());
        }
      });
    list.add(new BoolParameter
      (ParameterIDs.SupremicaSynchronousProductBuilder_MarkingUncontrollableStatesAsForbidden)
      {
        @Override
        public void commitValue()
        {
          setMarkingUncontrollableStatesAsForbidden(getValue());
        }
      });
    list.add(new BoolParameter
      (ParameterIDs.SupremicaSynchronousProductBuilder_ExpandingForbiddenStates)
      {
        @Override
        public void commitValue()
        {
          setExpandingForbiddenStates(getValue());
        }
      });
    list.add(new BoolParameter
      (ParameterIDs.SupremicaSynchronousProductBuilder_RememberingDisabledEvents)
      {
        @Override
        public void commitValue()
        {
          setRememberingDisabledEvents(getValue());
        }
      });
    list.add(new BoolParameter
      (ParameterIDs.SupremicaModelAnalyzer_EnsuringUncontrollablesInPlant)
      {
        @Override
        public void commitValue()
        {
          setEnsuringUncontrollablesInPlant(getValue());
        }
      });
    return list;
  }

  @Override
  public SynchronousProductResult createAnalysisResult()
  {
    return new DefaultSynchronousProductResult(this);
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzer
  /**
   * Returns whether or not this model analyser supports nondeterministic
   * automata.
   * @return <CODE>true</CODE> as Supremica's synchronous product supports
   *         nondeterminism.
   */
  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }


  //#########################################################################
  //# Invocation
  @Override
  public boolean run() throws AnalysisException
  {
    try {
      setUp();
      final Automata automata = getSupremicaAutomata();

      final AutomataSynchronizer synchronizer =
        new AutomataSynchronizer(automata, mSynchronizationOptions,
                                 Config.SYNTHESIS_SUP_AS_PLANT.get());
      setSupremicaTask(synchronizer);
      synchronizer.execute();

      final AutomataSynchronizerHelperStatistics stats =
        synchronizer.getHelper().getHelperData();
      final long numStates = stats.getNumberOfReachableStates();
      final long numTrans = stats.getNumberOfExaminedTransitions();
      final SynchronousProductResult result = getAnalysisResult();
      result.setNumberOfStates(numStates);
      result.setNumberOfTransitions(numTrans);
      if (numStates > getNodeLimit()) {
        throw new OverflowException(getNodeLimit());
      } else if (numTrans > getTransitionLimit()) {
        throw new OverflowException(OverflowKind.TRANSITION,
                                    getTransitionLimit());
      }

      if (isDetailedOutputEnabled()) {
        checkAbort();
        final ProductDESProxyFactory factory = getFactory();
        final ProductDESProxy model = getModel();
        final EventProxy defaultMarking = getConfiguredDefaultMarking();
        final AutomataToWaters importer =
          new AutomataToWaters(factory, model, defaultMarking);
        importer.setSuppressingRedundantSelfloops(mRemovingSelfloops);
        final Automaton aut = synchronizer.getAutomaton();
        aut.setName(mOutputName);
        if (mOutputKind != null) {
          switch (mOutputKind) {
          case PLANT:
            aut.setType(AutomatonType.PLANT);
            break;
          case SPEC:
            aut.setType(AutomatonType.SPECIFICATION);
            break;
          case SUPERVISOR:
            aut.setType(AutomatonType.SUPERVISOR);
            break;
          case PROPERTY:
            aut.setType(AutomatonType.PROPERTY);
            break;
          default:
            break;
          }
        }
        final AutomatonProxy sync = importer.convertAutomaton(aut);
        result.setComputedAutomaton(sync);
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


  //#########################################################################
  //# Auxiliary Methods
  private void updateEnsuringUncontrollablesInPlant()
  {
    final boolean ensure = mUncontrollablesInPlantRequested &&
      (isMarkingUncontrollableStatesAsForbidden() ||
       isRememberingDisabledEvents());
    super.setEnsuringUncontrollablesInPlant(ensure);
  }


  //#########################################################################
  //# Data Members
  private String mOutputName = "sync";
  private ComponentKind mOutputKind = null;
  private boolean mRemovingSelfloops = false;
  private boolean mUncontrollablesInPlantRequested = true;
  private final SynchronizationOptions mSynchronizationOptions =
    SynchronizationOptions.getDefaultSynchronizationOptions();

}
