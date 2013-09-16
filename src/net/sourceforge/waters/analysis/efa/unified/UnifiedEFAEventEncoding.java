//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   AbstractEFAEventEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import java.util.List;

import net.sourceforge.waters.analysis.efa.base.AbstractEFATransitionLabelEncoding;


/**
 * @author Robi Malik, Sahar Mohajerani
 */

public class UnifiedEFAEventEncoding
  extends AbstractEFATransitionLabelEncoding<AbstractEFAEvent>
{

  //#########################################################################
  //# Constructors
  public UnifiedEFAEventEncoding(final String name)
  {
    this(name, DEFAULT_SIZE);
  }

  public UnifiedEFAEventEncoding(final String name, final int size)
  {
    super(size);
    final SilentEFAEvent tau = new SilentEFAEvent(name);
    createEventId(tau);
  }

  public UnifiedEFAEventEncoding(final UnifiedEFAEventEncoding encoding)
  {
    super(encoding);
  }


  //#########################################################################
  //# Simple Access
  public int getEventId(final AbstractEFAEvent event)
  {
    return super.getTransitionLabelId(event);
  }

  public AbstractEFAEvent getEvent(final int code)
  {
    return getTransitionLabel(code);
  }

  public AbstractEFAEvent getUpdate(final int code)
  {
    return super.getTransitionLabel(code);
  }

  public int createEventId(final AbstractEFAEvent event)
  {
    return super.createTransitionLabelId(event);
  }

  /**
   * Retrieves the list of all events in this encoding, except the
   * silent (tau) event.
   * @return An unmodifiable list backed by the encoding.
   */
  public List<AbstractEFAEvent> getEventsExceptTau()
  {
    return getTransitionLabelsExceptTau();
  }

  /**
   * Retrieves the list of all events in this encoding, including the
   * silent (tau) event.
   * @return An unmodifiable list backed by the encoding.
   */
  public List<AbstractEFAEvent> getEventsIncludingTau()
  {
    return getTransitionLabelsIncludingTau();
  }


  //#########################################################################
  //# Class Constants
  public static final int OMEGA = 0;

}

