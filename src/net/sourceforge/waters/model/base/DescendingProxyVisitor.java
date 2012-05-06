//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   DescendingProxyVisitor
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;


/**
 * <P>An empty implementation of the {@link ProxyVisitor} interface.</P>
 *
 * <P>This is an adapter class to make it more convenient to implement
 * visitors that do not explicitly implement all the visit methods.
 * All the visit methods in this adapter class call the visit method for the
 * immediate superclass and afterwards visit all children of their argument.
 * In all cases, <CODE>null</CODE> is returned.</P>
 *
 * @author Robi Malik
 */

abstract public class DescendingProxyVisitor extends DefaultProxyVisitor {

}
