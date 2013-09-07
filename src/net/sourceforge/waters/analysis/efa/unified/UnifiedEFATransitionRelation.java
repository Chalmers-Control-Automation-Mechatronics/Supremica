//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   UnifiedEFATransitionRelation
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import java.util.List;

import net.sourceforge.waters.analysis.efa.base.AbstractEFATransitionRelation;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


/**
 * A transition relation in a unified EFA system.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class UnifiedEFATransitionRelation
  extends AbstractEFATransitionRelation<UnifiedEFAEvent>
{

  //#########################################################################
  //# Constructors
  public UnifiedEFATransitionRelation(final ListBufferTransitionRelation rel,
                                      final UnifiedEFAEventEncoding events,
                                      final List<SimpleNodeProxy> nodes)
  {
    super(rel, events, nodes);
  }

  public UnifiedEFATransitionRelation(final ListBufferTransitionRelation rel,
                                      final UnifiedEFAEventEncoding events)
  {
    this(rel, events, null);
  }


  //#########################################################################
  //# Simple Access
  public UnifiedEFAEventEncoding getEventEncoding()
  {
    return (UnifiedEFAEventEncoding) super.getTransitionLabelEncoding();
  }


  //#########################################################################
  //# Data Members

 }
