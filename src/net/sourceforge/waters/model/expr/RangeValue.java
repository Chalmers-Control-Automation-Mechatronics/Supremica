//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   RangeValue
//###########################################################################
//# $Id: RangeValue.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.util.Iterator;


public interface RangeValue extends Value {

  public int size();

  public int indexOf(Value value);

  public boolean contains(Value value);

  public Iterator iterator();

}