//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   ArrayValue
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.old;

import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.IndexValue;
import net.sourceforge.waters.model.expr.Value;


interface ArrayValue extends Value {

  public EventValue find(final IndexValue index)
    throws EvalException;

}