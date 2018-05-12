//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.model.analysis.des;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.AbstractAbortable;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.DefaultAnalysisResult;
import net.sourceforge.waters.model.analysis.InvalidModelException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * An abstract base class that can be used for all model analyser
 * implementations. This class simply provides access to model and
 * factory members needed by virtually any model analyser implementation.
 *
 * @author Robi Malik
 */

public abstract class AbstractModelAnalyzer
  extends AbstractAbortable
  implements ModelAnalyzer
{

  //#########################################################################
  //# Constructors
  public AbstractModelAnalyzer(final ProductDESProxyFactory factory,
                               final KindTranslator translator)
  {
    this((ProductDESProxy) null, factory, translator);
  }

  public AbstractModelAnalyzer(final ProductDESProxy model,
                               final ProductDESProxyFactory factory,
                               final KindTranslator translator)
  {
    mFactory = factory;
    mKindTranslator = translator;
    mDetailedOutputEnabled = true;
    mModel = model;
    mNodeLimit = Integer.MAX_VALUE;
    mTransitionLimit = Integer.MAX_VALUE;
  }

  public AbstractModelAnalyzer(final AutomatonProxy aut,
                               final ProductDESProxyFactory factory,
                               final KindTranslator translator)
  {
    this(AutomatonTools.createProductDESProxy(aut, factory),
         factory, translator);
  }


  //#########################################################################
  //# Simple Access Methods
  @Override
  public ProductDESProxyFactory getFactory()
  {
    return mFactory;
  }

  @Override
  public ProductDESProxy getModel()
  {
    return mModel;
  }

  @Override
  public void setModel(final ProductDESProxy model)
  {
    mModel = model;
    clearAnalysisResult();
  }

  @Override
  public void setModel(final AutomatonProxy aut)
  {
    final ProductDESProxy des =
      AutomatonTools.createProductDESProxy(aut, mFactory);
    setModel(des);
  }

  @Override
  public void setKindTranslator(final KindTranslator translator)
  {
    if (mKindTranslator != translator) {
      mKindTranslator = translator;
      clearAnalysisResult();
    }
  }

  @Override
  public KindTranslator getKindTranslator()
  {
    return mKindTranslator;
  }

  @Override
  public void setDetailedOutputEnabled(final boolean enable)
  {
    mDetailedOutputEnabled = enable;
  }

  @Override
  public boolean isDetailedOutputEnabled()
  {
    return mDetailedOutputEnabled;
  }

  @Override
  public void setNodeLimit(final int limit)
  {
    mNodeLimit = limit;
  }

  @Override
  public int getNodeLimit()
  {
    return mNodeLimit;
  }

  @Override
  public void setTransitionLimit(final int limit)
  {
    mTransitionLimit = limit;
  }

  @Override
  public int getTransitionLimit()
  {
    return mTransitionLimit;
  }

  @Override
  public AnalysisResult getAnalysisResult()
  {
    return mAnalysisResult;
  }

  @Override
  public void clearAnalysisResult()
  {
    mAnalysisResult = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Initialises the model analyser for a new run.
   * This method should be called by all subclasses at the beginning of
   * each {@link ModelAnalyzer#run() run()}. If overridden, the overriding
   * method should call the superclass methods first.
   */
  protected void setUp()
    throws AnalysisException
  {
    mStartTime = System.currentTimeMillis();
    mAnalysisResult = createAnalysisResult();
    checkAbort();
  }

  /**
   * Resets the model analyser at the end of a run.
   * This method should be called by all subclasses upon completion of
   * each {@link ModelAnalyzer#run() run()}, even if an exception is
   * thrown. If overridden, the overriding method should call the superclass
   * methods last.
   */
  protected void tearDown()
  {
    addStatistics();
  }

  /**
   * Attempts to retrieve the single input automaton from the input model.
   * This method checks whether the input model contains exactly one
   * automaton, and if this is the case, returns it.
   * @throws InvalidModelException if the input model does not contain
   *         exactly one automaton.
   */
  protected AutomatonProxy getInputAutomaton()
    throws InvalidModelException
  {
    final ProductDESProxy des = getModel();
    final Collection<AutomatonProxy> automata = des.getAutomata();
    if (automata.size() == 1) {
      return automata.iterator().next();
    } else {
      throw new InvalidModelException
        ("The input product DES '" + des.getName() +
         "' does not contain exactly one automaton, which is required for " +
         ProxyTools.getShortClassName(this) + "!");
    }
  }

  /**
   * Returns whether the given event is a proper event under the current
   * {@link KindTranslator}. A proper event is controllable or uncontrollable,
   * not a proposition.
   * @see #getKindTranslator()
   * @see EventKind
   */
  protected boolean isProperEvent(final EventProxy event)
  {
    final KindTranslator translator = getKindTranslator();
    final EventKind kind = translator.getEventKind(event);
    return kind == EventKind.CONTROLLABLE || kind == EventKind.UNCONTROLLABLE;
  }

  /**
   * Returns whether the given automaton should be included in the
   * analysis. This default implementation returns <CODE>true</CODE> if
   * the given automaton is a plant or specification under the current
   * {@link KindTranslator}.
   * @see #getKindTranslator()
   * @see ComponentKind
   */
  protected boolean isProperAutomaton(final AutomatonProxy aut)
  {
    final KindTranslator translator = getKindTranslator();
    final ComponentKind kind = translator.getComponentKind(aut);
    return kind == ComponentKind.PLANT || kind == ComponentKind.SPEC;
  }

  /**
   * Creates an analysis result object to store the model analyser's results.
   * This method is called is in {@link #setUp()} to ensure that the
   * analysis result is available throughout the analysis run. It is
   * overridden by subclasses the require a more specific analysis result
   * type.
   */
  public AnalysisResult createAnalysisResult()
  {
    return new DefaultAnalysisResult(this);
  }

  /**
   * Replaces the present result record by the given new result record.
   */
  protected void setAnalysisResult(final AnalysisResult result)
  {
    mAnalysisResult = result;
  }

  /**
   * Stores the given Boolean value on the analysis result and marks the run
   * as completed.
   * @return The given Boolean value.
   */
  protected boolean setBooleanResult(final boolean value)
  {
    mAnalysisResult.setSatisfied(value);
    addStatistics();
    return value;
  }

  /**
   * Stores the given exception on the analysis result and marks the run
   * as completed.
   * @return The given exception.
   */
  protected AnalysisException setExceptionResult
    (final AnalysisException exception)
  {
    if (mAnalysisResult != null) {
      mAnalysisResult.setException(exception);
      addStatistics();
    }
    return exception;
  }


  /**
   * Stores any available statistics on this analyser's last run in the
   * analysis result. This default implementation only records the runtime
   * and memory usage. To provide more statistics, it should be overridden
   * by subclasses, who should always call the superclass method first.
   */
  protected void addStatistics()
  {
    if (mAnalysisResult != null) {
      final long current = System.currentTimeMillis();
      mAnalysisResult.setRuntime(current - mStartTime);
      final long usage = DefaultAnalysisResult.getCurrentMemoryUsage();
      mAnalysisResult.updatePeakMemoryUsage(usage);
    }
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;
  private ProductDESProxy mModel;
  private AnalysisResult mAnalysisResult;
  private KindTranslator mKindTranslator;

  private boolean mDetailedOutputEnabled;
  private int mNodeLimit;
  private int mTransitionLimit;
  private long mStartTime;

}
