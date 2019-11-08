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

import java.util.List;

import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionMap;
import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.InvalidModelException;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.EventDeclProxy;

import org.supremica.automata.Automata;
import org.supremica.automata.IO.ProjectBuildFromWaters;


/**
 * An abstract base to support the invocation of Supremica's algorithms
 * through the {@link ModelAnalyzer} interface of Waters.
 * A SupremicaModelAnalyzer acts as a wrapper that converts its input
 * to a {@link Automata} object that can be analysed by Supremica
 * algorithms.
 *
 * @author Robi Malik
 */

public abstract class SupremicaModelAnalyzer
  extends AbstractModelAnalyzer
{

  //#########################################################################
  //# Constructor
  public SupremicaModelAnalyzer(final ProductDESProxy model,
                                final ProductDESProxyFactory factory,
                                final KindTranslator translator,
                                final boolean ensuringUncontrollablesInPlant)
  {
    super(model, factory, translator);
    mProjectBuilder.setKindTranslator(translator);
    mProjectBuilder.setEnsuringUncontrollablesInPlant(ensuringUncontrollablesInPlant);
  }


  //#########################################################################
  //# Specific Configuration
  /**
   * <P>Sets the <I>marking proposition</I> to be used for nonblocking
   * verification and synthesis.</P>
   * <P>A marking proposition of&nbsp;<CODE>null</CODE> may be specified to
   * request the default marking {@link EventDeclProxy#DEFAULT_MARKING_NAME}
   * of the model to be used. If then the model does not have such a marking,
   * all states are considered as marked.</P>
   * @param  marking  The marking proposition to be used,
   *                  or <CODE>null</CODE> to request the default marking.
   */
  public void setConfiguredDefaultMarking(final EventProxy marking)
  {
    mProjectBuilder.setConfiguredDefaultMarking(marking);
  }

  /**
   * Gets the <I>marking proposition</I> to be used for nonblocking
   * verification and synthesis.
   * @see #setConfiguredDefaultMarking(EventProxy)
   */
  public EventProxy getConfiguredDefaultMarking()
  {
    return mProjectBuilder.getConfiguredDefaultMarking();
  }

  /**
   * <P>Sets how uncontrollable events that appear only in the specification
   * are converted to Supremica.</P>
   *
   * <P>Supremica treats controllable events that appear in the specification
   * but not in the plant differently from Waters. Waters assumes such events
   * to be always enabled by the plant, so any specification that disables
   * them in some state will be uncontrollable. Supremica assumes such events
   * to be enabled in the plant if and only if they are enabled in the
   * specification.</P>
   *
   * <P>If this option is enabled (the default), the generated Supremica
   * {@link Automata} will include an additional one-state plant component
   * with selfloops for all uncontrollable event that appears in the
   * specification but not in the plant. This ensures that Supremica's
   * algorithms produce the same result as Waters. If the option is disabled,
   * no changes to the model are made, thus resulting in Supremoica's
   * controllability semantics.</P>
   */
  public void setEnsuringUncontrollablesInPlant(final boolean ensure)
  {
    mProjectBuilder.setEnsuringUncontrollablesInPlant(ensure);
  }

  /**
   * Returns how uncontrollable events that appear only in the specification
   * are converted to Supremica.
   * @see #setEnsuringUncontrollablesInPlant(boolean)
   */
  public boolean isEnsuringUncontrollablesInPlant()
  {
    return mProjectBuilder.isEnsuringUncontrollablesInPlant();
  }

  /**
   * Sets whether synchronisation is also performed on unobservable events.
   * If set, unobservable events are treated as ordinary events in
   * synchronous composition, otherwise they are considered as local events
   * that are executed by each component separately.
   */
  public abstract void setSynchronisingOnUnobservableEvents(final boolean sync);

  /**
   * Returns whether synchronisation is also performed on unobservable events.
   * @see #setSynchronisingOnUnobservableEvents(boolean)
   */
  public abstract boolean isSynchronisingOnUnobservableEvents();


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.ModelAnalyzer
  @Override
  public List<Option<?>> getOptions(final OptionMap db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.append(options, SupremicaModelAnalyzerFactory.
              OPTION_SupremicaSynchronousProductBuilder_SynchronisingOnUnobservableEvents);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(SupremicaModelAnalyzerFactory.
                     OPTION_SupremicaSynchronousProductBuilder_SynchronisingOnUnobservableEvents)) {
      final BooleanOption boolOption = (BooleanOption) option;
      setSynchronisingOnUnobservableEvents(boolOption.getBooleanValue());
    } else {
      super.setOption(option);
    }
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzer
  @Override
  public void setKindTranslator(final KindTranslator translator)
  {
    super.setKindTranslator(translator);
    mProjectBuilder.setKindTranslator(translator);
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    createSupremicaAutomata();
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mSupremicaAutomata = null;
    mSupremicaTask = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    if (mSupremicaTask != null) {
      mSupremicaTask.requestAbort();
    }
  }

  @Override
  public void resetAbort()
  {
    super.requestAbort();
    if (mSupremicaTask != null) {
      mSupremicaTask.resetAbort();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  protected Automata createSupremicaAutomata()
    throws InvalidModelException
  {
    if (mSupremicaAutomata == null) {
      if (!supportsNondeterminism()) {
        final ProductDESProxy des = getModel();
        for (final AutomatonProxy aut : des.getAutomata()) {
          AutomatonTools.checkDeterministic(aut);
        }
      }
      try {
        final ProductDESProxy des = getModel();
        mSupremicaAutomata = mProjectBuilder.build(des);
      } catch (final EvalException exception) {
        throw new InvalidModelException(exception);
      }
    }
    return mSupremicaAutomata;
  }

  protected Automata getSupremicaAutomata()
  {
    return mSupremicaAutomata;
  }

  protected void setSupremicaTask(final Abortable task)
  {
    mSupremicaTask = task;
  }


  //#########################################################################
  //# Data Members
  private final ProjectBuildFromWaters mProjectBuilder =
    new ProjectBuildFromWaters(null);

  private Automata mSupremicaAutomata = null;
  private Abortable mSupremicaTask = null;

}
