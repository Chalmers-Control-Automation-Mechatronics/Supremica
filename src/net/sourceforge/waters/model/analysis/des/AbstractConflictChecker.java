//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * An abstract base class that can be used for all conflict checker
 * implementations. In addition to the model and factory members inherited
 * from {@link AbstractModelVerifier}, this class provides some support to
 * get and set the default marking, and to return an error trace of the
 * appropriate kind.
 *
 * @author Robi Malik
 */

public abstract class AbstractConflictChecker
  extends AbstractModelVerifier
  implements ConflictChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new conflict checker without a model or marking
   * proposition.
   */
  public AbstractConflictChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  /**
   * Creates a new conflict checker to check whether the given model
   * nonconflicting with respect to the default marking proposition.
   * @param  model      The model to be checked by this conflict checker.
   * @param  factory    Factory used for trace construction.
   */
  public AbstractConflictChecker(final ProductDESProxy model,
                                 final ProductDESProxyFactory factory)
  {
    this(model, null, factory);
  }

  /**
   * Creates a new conflict checker to check a particular model.
   * @param  model      The model to be checked by this conflict checker.
   * @param  marking    The proposition event that defines which states
   *                    are marked. Every state has a list of propositions
   *                    attached to it; the conflict checker considers only
   *                    those states as marked that are labelled by
   *                    <CODE>marking</CODE>, i.e., their list of
   *                    propositions must contain this event (exactly the
   *                    same object).
   * @param  factory    Factory used for trace construction.
   */
  public AbstractConflictChecker(final ProductDESProxy model,
                                 final EventProxy marking,
                                 final ProductDESProxyFactory factory)
  {
    this(model, marking, null, factory);
  }

    /**
     * Creates a new conflict checker to check a particular model.
     * @param  model      The model to be checked by this conflict checker.
     * @param  marking    The proposition event that defines which states
     *                    are marked. Every state has a list of propositions
     *                    attached to it; the conflict checker considers only
     *                    those states as marked that are labelled by
     *                    <CODE>marking</CODE>, i.e., their list of
     *                    propositions must contain this event (exactly the
     *                    same object).
     * @param preMarking  The proposition event that defines which states have
     *                    alpha (precondition) markings for a generalised
     *                    nonblocking check.
     * @param  factory    Factory used for trace construction.
     */
    public AbstractConflictChecker(final ProductDESProxy model,
                                   final EventProxy marking,
                                   final EventProxy preMarking,
                                   final ProductDESProxyFactory factory)
    {
      super(model, factory, ConflictKindTranslator.getInstanceUncontrollable());
      mConfiguredMarking = marking;
      mPreconditionMarking = preMarking;
      mUsedMarking = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public void setModel(final ProductDESProxy model)
  {
    super.setModel(model);
    mUsedMarking = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ConflictChecker
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
  public void setConfiguredPreconditionMarking(final EventProxy marking){
    mPreconditionMarking = marking;
  }

  @Override
  public EventProxy getConfiguredPreconditionMarking(){
    return mPreconditionMarking;
  }

  @Override
  public ConflictTraceProxy getCounterExample()
  {
    return (ConflictTraceProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.ModelAnalyser
  @Override
  protected void tearDown()
  {
    mUsedMarking = null;
    super.tearDown();
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
        mUsedMarking = getMarkingProposition(model);
      } else {
        mUsedMarking = mConfiguredMarking;
      }
    }
    return mUsedMarking;
  }

  /**
   * Searches the given model for a proposition event with the default
   * marking name {@link EventDeclProxy#DEFAULT_MARKING_NAME} and returns
   * this event.
   * @throws EventNotFoundException to indicate that the given model
   *         does not contain any proposition with the default marking
   *         name.
   */
  public static EventProxy getMarkingProposition(final ProductDESProxy model)
    throws EventNotFoundException
  {
    return getMarkingProposition(model, EventDeclProxy.DEFAULT_MARKING_NAME);
  }

  /**
   * Creates a precondition marking for the given model. This method creates
   * a proposition event not present in the model, with a new name similar to
   * the default name {@link EventDeclProxy#DEFAULT_PRECONDITION_NAME}.
   */
  public static EventProxy createNewPreconditionMarking
    (final ProductDESProxy model, final ProductDESProxyFactory factory)
  {
    final Collection<EventProxy> events = model.getEvents();
    int index = 0;
    String name;
    boolean found;
    do {
      name = EventDeclProxy.DEFAULT_PRECONDITION_NAME;
      if (index > 0) {
        name += ":" + index;
      }
      index++;
      found = false;
      for (final EventProxy event : events) {
        if (event.getName().equals(name)) {
          found = true;
          break;
        }
      }
    } while (found);
    return factory.createEventProxy(name, EventKind.PROPOSITION);
  }


  /**
   * Searches the given model for a proposition event with the given
   * name and returns this event.
   * @throws EventNotFoundException to indicate that the given model
   *         does not contain any proposition with the default marking
   *         name.
   */
  public static EventProxy getMarkingProposition
    (final ProductDESProxy model, final String name)
    throws EventNotFoundException
  {
    for (final EventProxy event : model.getEvents()) {
      if (event.getKind() == EventKind.PROPOSITION &&
          event.getName().equals(name)) {
        return event;
      }
    }
    throw new EventNotFoundException(model, name, EventKind.PROPOSITION, false);
  }


  /**
   * Gets a name that can be used for a counterexample for the current model.
   */
  protected String getTraceName()
  {
    final ProductDESProxy model = getModel();
    return getTraceName(model);
  }

  /**
   * Gets a name that can be used for a counterexample for the given model.
   */
  public static String getTraceName(final ProductDESProxy model)
  {
    final String modelname = model.getName();
    return modelname + "-conflicting";
  }


  //#########################################################################
  //# Data Members
  private EventProxy mConfiguredMarking;
  private EventProxy mUsedMarking;
  private EventProxy mPreconditionMarking;
}
