//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.compiler
//# CLASS:   EventValue
//###########################################################################
//# $Id: EventValue.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.Iterator;

import net.sourceforge.waters.model.expr.Value;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.EventKind;


interface EventValue extends Value {

  public Iterator getEventIterator();

  public EventKind getKind();

  public ColorGeometryProxy getColorGeometry();

  public void checkParameterType(final EventDeclProxy decl)
    throws EventKindException;

}
