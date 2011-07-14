//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: org.supremica.automata
//# CLASS:   TauEvent
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.automata;

/**
 * TODO
 * Added this class because it was missing and Supremica would not compile.
 * It most certainly does not work.
 *
 * @author Robi Malik
 */
public class TauEvent extends LabeledEvent
{

  public TauEvent(final LabeledEvent event)
  {
    super(event);
  }

  public LabeledEvent getOriginalEvent()
  {
    return this;
  }

}
