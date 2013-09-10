//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   AbstractEFAEventEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import net.sourceforge.waters.analysis.efa.base.AbstractEFATransitionLabelEncoding;


/**
 * @author Robi Malik, Sahar Mohajerani
 */

public class UnifiedEFAEventEncoding
  extends AbstractEFATransitionLabelEncoding<AbstractEFAEvent>
{

  //#########################################################################
  //# Constructors
  public UnifiedEFAEventEncoding()
  {
    this(DEFAULT_SIZE);
  }

  public UnifiedEFAEventEncoding(final int size)
  {
    super(size);
    final SilentEFAEvent tau = new SilentEFAEvent(null);
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

  public AbstractEFAEvent getUpdate(final int code)
  {
    return super.getTransitionLabel(code);
  }

  public int createEventId(final AbstractEFAEvent event)
  {
    return super.createTransitionLabelId(event);
  }


  //#########################################################################
  //# Class Constants
  public static final int OMEGA = 0;

}

