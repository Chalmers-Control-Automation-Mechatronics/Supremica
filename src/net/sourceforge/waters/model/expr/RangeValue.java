//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   RangeValue
//###########################################################################
//# $Id: RangeValue.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.util.List;


public interface RangeValue extends Value {

  public int size();

  public int indexOf(Value value);

  public boolean contains(Value value);

  public List<IndexValue> getValues();

}
