//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.compiler
//# CLASS:   EventValueConsumer
//###########################################################################
//# $Id: EventValueConsumer.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import net.sourceforge.waters.model.expr.EvalException;


interface EventValueConsumer {

  public void processValue(EventValue value)
    throws EvalException;

}