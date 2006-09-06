//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   IndexedIdentifierProxy
//###########################################################################
//# $Id: IndexedIdentifierProxy.java,v 1.3 2006-09-06 11:52:21 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.util.List;


/**
 * <P>An identifier with array indexes.
 * Indexed identifiers are used to refer to event within an event array
 * structure. They consist of a name and a list of one or more expressions,
 * each of which evaluates to the index into an array.</P>
 *
 * <P>The textual representation of Waters expressions uses <CODE>[</CODE>
 * and&nbsp;<CODE>]</CODE> for indexing. Therefore, the following
 * strings produce indexed identifiers.</P>
 * <UL>
 * <LI><CODE>start[1]</CODE>;</LI>
 * <LI><CODE>put[i][2-i]</CODE>.</LI>
 * </UL>
 *
 * @author Robi Malik
 */

public interface IndexedIdentifierProxy extends IdentifierProxy {

  //#########################################################################
  //# Simple Access
  /**
   * Gets the list of array indexes of this identifier.
   * @return An unmodifiable list of expression, representing the
   *         indexes into the array identified by the identifier's name.
   *         Each element is of type {@link SimpleExpressionProxy}.
   */
  // @default none
  public List<SimpleExpressionProxy> getIndexes();

}
