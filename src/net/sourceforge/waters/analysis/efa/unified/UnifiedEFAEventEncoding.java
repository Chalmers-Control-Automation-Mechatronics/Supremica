//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   UnifiedEFAEventEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import net.sourceforge.waters.analysis.efa.base.AbstractEFATransitionLabelEncoding;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;


/**
 * @author Robi Malik, Sahar Mohajerani
 */

public class UnifiedEFAEventEncoding
  extends AbstractEFATransitionLabelEncoding<UnifiedEFAEvent>
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
    final ConstraintList empty = new ConstraintList();
    final UnifiedEFAEvent tau = new UnifiedEFAEvent(null, empty);
    createEventId(tau);
  }

  public UnifiedEFAEventEncoding(final UnifiedEFAEventEncoding encoding)
  {
    super(encoding);
  }


  //#########################################################################
  //# Simple Access
  public int getEventId(final UnifiedEFAEvent event)
  {
    return super.getTransitionLabelId(event);
  }

  public UnifiedEFAEvent getUpdate(final int code)
  {
    return super.getTransitionLabel(code);
  }

  public int createEventId(final UnifiedEFAEvent event)
  {
    return super.createTransitionLabelId(event);
  }


  //#########################################################################
  //# Class Constants
  public static final int OMEGA = 0;

}

