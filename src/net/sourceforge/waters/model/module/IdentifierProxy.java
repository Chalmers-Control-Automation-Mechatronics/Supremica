//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   IdentifierProxy
//###########################################################################
//# $Id: IdentifierProxy.java,v 1.4 2008-06-18 09:35:34 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;


/**
 * The base class for all identifiers.
 *
 * This class represents all expressions whose main component is an
 * identifier, where an identifier is a name that can be bound to different
 * values in different contexts. There can be simple identifiers ({@link
 * SimpleIdentifierProxy}) that only consist of a name, indexed identifiers
 * ({@link IndexedIdentifierProxy}) that consist of a name and a sequence
 * of array indexes, and qualified identifiers ({@link
 * QualifiedIdentifierProxy}) that combine the name of a context and an
 * enclosed component.
 *
 * @author Robi Malik
 */

public interface IdentifierProxy
  extends SimpleExpressionProxy, Comparable<IdentifierProxy>
{

  public IdentifierProxy clone();

}
