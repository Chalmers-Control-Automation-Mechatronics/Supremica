//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   CompiledRange
//###########################################################################
//# $Id: CompiledRange.java,v 1.1 2008-06-16 07:09:51 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.context;

import java.util.List;

import net.sourceforge.waters.model.module.SimpleExpressionProxy;


public interface CompiledRange {

  public int size();

  public int indexOf(final SimpleExpressionProxy value);

  public boolean contains(final SimpleExpressionProxy value);

  public List<? extends SimpleExpressionProxy> getValues();

}
