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

}

