//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   IdentifierProxy
//###########################################################################
//# $Id: IdentifierProxy.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;


/**
 * The base class for all identifiers.
 *
 * This class represents all expressions whose main component is an
 * identifier, where an identifier is a name that can be bound to different
 * values in different contexts. There can be simple identifiers ({@link
 * SimpleIdentifierProxy}) that only consist of a name, or indexed
 * identifiers ({@link IndexedIdentifierProxy}) that consist of a name and a
 * sequence of array indexes.
 *
 * @author Robi Malik
 */

public interface IdentifierProxy
  extends SimpleExpressionProxy, Comparable<IdentifierProxy>
{

  //#########################################################################
  //# Getters
  public String getName();

}
