//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   SimpleExpressionProxy
//###########################################################################
//# $Id: SimpleExpressionProxy.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;


/**
 * The base class of all expressions.
 *
 * This is the base class of all expressions that have a simple textual
 * representation, i.e., one that easily fits into a single line. This
 * includes all expressions except event lists ({@link
 * EventListExpressionProxy}).
 *
 * @author Robi Malik
 */

public interface SimpleExpressionProxy
  extends ExpressionProxy
{

}
