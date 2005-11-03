//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   EventValue
//###########################################################################
//# $Id: EventValue.java,v 1.2 2005-11-03 01:24:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.expr.RangeValue;
import net.sourceforge.waters.model.expr.Value;
import net.sourceforge.waters.xsd.base.EventKind;


interface EventValue extends Value
{

  public int getKindMask();

  public boolean isObservable();

  public Iterator<CompiledSingleEventValue> getEventIterator();

  public List<RangeValue> getIndexRanges();

}