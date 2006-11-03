//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   EventValue
//###########################################################################
//# $Id: EventValue.java,v 1.3 2006-11-03 15:01:57 torda Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

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