//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   SynthesisAbstractionStep
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;

/**
 * @author Sahar Mohajerani, Robi Malik
 */
class SynthesisAbstractionStep extends AbstractionStep
{

  //#########################################################################
  //# Constructor
  SynthesisAbstractionStep(final CompositionalSynthesizer synthesizer,
                           final AutomatonProxy result,
                           final AutomatonProxy original,
                           final EventEncoding coding)
  {
    super(synthesizer, result, original);
    mEventEncoding = coding;
  }

  SynthesisAbstractionStep(final CompositionalSynthesizer synthesizer,
                           final AutomatonProxy result,
                           final AutomatonProxy original,
                           final Map<EventProxy, List<EventProxy>> renaming,
                           final EventEncoding coding)
  {
    super(synthesizer, result, original);
    mRenaming = renaming;
    mEventEncoding = coding;
  }

  //#########################################################################
  //# Simple Access
  ListBufferTransitionRelation getSupervisor()
  {
    return mSupervisor;
  }

  void setSupervisor(final ListBufferTransitionRelation supervisor)
  {
    mSupervisor = supervisor;
  }

  Map<EventProxy, List<EventProxy>> getRenaming()
  {
    return mRenaming;
  }

  EventEncoding getEventEncoding()
  {
    return mEventEncoding;
  }


  //#######################################################################
  //# Data Members
  private ListBufferTransitionRelation mSupervisor;
  private final EventEncoding mEventEncoding;
  private Map<EventProxy, List<EventProxy>> mRenaming;

}
