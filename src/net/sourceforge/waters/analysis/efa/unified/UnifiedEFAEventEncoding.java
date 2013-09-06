//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
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
  UnifiedEFAEventEncoding()
  {
    this(DEFAULT_SIZE);
  }

  UnifiedEFAEventEncoding(final int size)
  {
    super(size);
    final ConstraintList empty = new ConstraintList();
    final UnifiedEFAEvent tau = new UnifiedEFAEvent(null, empty);
    createEventId(tau);
  }

  UnifiedEFAEventEncoding(final UnifiedEFAEventEncoding encoding)
  {
    super(encoding);
  }

  public int getEventId(final UnifiedEFAEvent update)
  {
    return super.getTransitionLabelId(update);
  }

  public UnifiedEFAEvent getUpdate(final int event)
  {
    return super.getTransitionLabel(event);
  }

  public int createEventId(final UnifiedEFAEvent update)
  {
    return super.createTransitionLabelId(update);
  }

  //#########################################################################
  //# Class Constants
  public static final int OMEGA = 0;

}

