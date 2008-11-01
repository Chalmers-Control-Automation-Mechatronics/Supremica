//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   RangeValue
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.util.List;


public interface RangeValue extends SimpleValue {

  public int size();

  public int indexOf(Value value);

  public boolean contains(Value value);

  public List<IndexValue> getValues();

}
