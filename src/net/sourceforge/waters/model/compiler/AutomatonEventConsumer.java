//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.compiler
//# CLASS:   AutomatonEventConsumer
//###########################################################################
//# $Id: AutomatonEventConsumer.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.Iterator;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.expr.EvalException;


class AutomatonEventConsumer implements EventValueConsumer
{

  //#########################################################################
  //# Constructor
  AutomatonEventConsumer(final EventConsumer proc)
  {
    mEventConsumer = proc;
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.ValueConsumer
  public void processValue(final EventValue value)
    throws EvalException
  {
    final Iterator iter = value.getEventIterator();
    while (iter.hasNext()) {
      final EventProxy event = (EventProxy) iter.next();
      mEventConsumer.processEvent(event);
    }
  }

  //#########################################################################
  //# Data Members
  private final EventConsumer mEventConsumer;

}
