//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.instance
//# CLASS:   CompiledEvent
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.instance;

import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


interface CompiledEvent
{

  public int getKindMask();

  public boolean isObservable();

  public List<CompiledRange> getIndexRanges();

  public CompiledEvent find(SimpleExpressionProxy index)
    throws EvalException;

  public SourceInfo getSourceInfo();

  public Iterator<? extends CompiledEvent> getChildrenIterator();

}