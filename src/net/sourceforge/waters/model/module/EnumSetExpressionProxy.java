//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   EnumSetExpressionProxy
//###########################################################################
//# $Id: EnumSetExpressionProxy.java,v 1.3 2006-09-06 11:52:21 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.util.List;


/**
 * <P>An expression representing an enumerated range.</P>
 *
 * <P>An enumerated range consists of a list of names, its <I>atoms</I>,
 * which can be used as array indexes or as the range for a <I>foreach</I>
 * construct ({@link ForeachProxy}). In textual representation, enumerated
 * ranges are listed within curled braces, with their elements separated by
 * commas.</P>
 *
 * <P>Examples:</P>
 * <UL>
 * <LI><CODE>{a, b, c}</CODE>;
 * <LI><CODE>{}</CODE>.
 * </UL>
 *
 * @author Robi Malik
 */
// @short enumerated range

public interface EnumSetExpressionProxy extends SimpleExpressionProxy {

  //#########################################################################
  //# Simple Access
  /**
   * Gets the list of items in this enumeration.
   * @return An unmodifiable list of enumerated items.
   */
  // @default none
  public List<SimpleIdentifierProxy> getItems();

}
