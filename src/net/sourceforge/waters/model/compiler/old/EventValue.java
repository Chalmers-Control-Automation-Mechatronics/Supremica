//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   EventValue
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.old;

import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.expr.RangeValue;
import net.sourceforge.waters.model.expr.Value;


interface EventValue extends Value
{

  public int getKindMask();

  public boolean isObservable();

  public Iterator<CompiledSingleEventValue> getEventIterator();

  public List<RangeValue> getIndexRanges();

}