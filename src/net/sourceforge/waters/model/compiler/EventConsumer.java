//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.compiler
//# CLASS:   EventConsumer
//###########################################################################
//# $Id: EventConsumer.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.expr.EvalException;


interface EventConsumer {

  public void processEvent(EventProxy event)
    throws EvalException;

}
