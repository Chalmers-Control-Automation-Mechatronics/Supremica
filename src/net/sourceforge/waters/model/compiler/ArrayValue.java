//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   ArrayValue
//###########################################################################
//# $Id: ArrayValue.java,v 1.2 2005-11-03 01:24:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.IndexValue;
import net.sourceforge.waters.model.expr.Value;


interface ArrayValue extends Value {

  public EventValue find(final IndexValue index)
    throws EvalException;

}