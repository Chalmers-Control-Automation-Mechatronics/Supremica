//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   ExpressionProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.module;

import net.sourceforge.waters.model.base.Proxy;


/**
 * The base class of all expressions.
 *
 * This is the base class of all expressions that may occur in a
 * module. This includes the expressions in package {@link
 * net.sourceforge.waters.model.expr} as well as the special event list
 * expressions ({@link
 * net.sourceforge.waters.model.module.EventListExpressionProxy}) that are
 * specific to modules.
 *
 * @author Robi Malik
 */

public interface ExpressionProxy extends Proxy {

}
