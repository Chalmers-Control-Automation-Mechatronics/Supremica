//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   CompiledRange
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.context;

import java.util.List;

import net.sourceforge.waters.model.module.SimpleExpressionProxy;


public interface CompiledRange {

  public int size();

  public int indexOf(SimpleExpressionProxy value);

  public boolean contains(SimpleExpressionProxy value);

  public boolean intersects(CompiledRange range);

  public CompiledRange intersection(CompiledRange range);

  public CompiledRange remove(SimpleExpressionProxy value);

  public List<? extends SimpleExpressionProxy> getValues();

}
