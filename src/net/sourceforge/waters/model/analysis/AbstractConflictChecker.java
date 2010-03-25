//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractConflictChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

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
      super(model, factory, ConflictKindTranslator.getInstance());
      mMarking = marking;
      mPreconditionMarking = preMarking;
      mUsedMarking = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  public void setModel(final ProductDESProxy model)
  {
    super.setModel(model);
    mUsedMarking = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ConflictChecker
  public void setMarkingProposition(final EventProxy marking)
  {
    mMarking = marking;
    mUsedMarking = null;
  }

  public EventProxy getMarkingProposition()
  {
    return mMarking;
  }
  public void setGeneralisedPrecondition(final EventProxy marking){
    mPreconditionMarking = marking;
  }
  public EventProxy getGeneralisedPrecondition(){
    return mPreconditionMarking;
  }
  public ConflictTraceProxy getCounterExample()
  {
    return (ConflictTraceProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Gets the marking proposition to be used.
   * This method returns the marking proposition specified by the {@link
   * #setMarkingProposition(EventProxy) setMarkingProposition()} method, if
   * non-null, or the default marking proposition of the input model.
   * @throws IllegalArgumentException to indicate that the a
   *         <CODE>null</CODE> marking was specified, but input model does
   *         not contain any proposition with the default marking name.
   */
  protected EventProxy getUsedMarkingProposition()
    throws EventNotFoundException
  {
    if (mUsedMarking == null) {
      if (mMarking == null) {
        final ProductDESProxy model = getModel();
        mUsedMarking = getMarkingProposition(model);
      } else {
        mUsedMarking = mMarking;
      }
    }
    return mUsedMarking;
  }

  /**
   * Searches the given model for a proposition event with the default
   * marking name and returns this event.
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


  //#########################################################################
  //# Data Members
  private EventProxy mMarking;
  private EventProxy mUsedMarking;
  private EventProxy mPreconditionMarking;
}
