//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   IdentifierProxy
//###########################################################################
//# $Id: IdentifierProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.IdentifierType;


/**
 * The abstract base class for all identifiers.
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

public abstract class IdentifierProxy extends SimpleExpressionProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates an identifier.
   * @param  name        The name of the new identifier.
   */
  IdentifierProxy(final String name)
  {
    mName = name;
  }

  /**
   * Creates an identifier from a parsed XML structure.
   * @param  element     The parsed XML structure of the new identifier.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  IdentifierProxy(final IdentifierType expr)
  {
    super(expr);
    mName = expr.getName();
  }


  //#########################################################################
  //# Getters
  public String getName()
  {
    return mName;
  }

  abstract List getIndexes();


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final IdentifierProxy expr = (IdentifierProxy) partner;
      return mName.equals(expr.mName);
    } else {
      return false;
    }    
  }

  public int hashCode()
  {
    return 5 * super.hashCode() + mName.hashCode();
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  public int compareTo(final Object partner)
  {
    if (partner instanceof IdentifierProxy) {
      final IdentifierProxy ident = (IdentifierProxy) partner;
      int result = mName.compareToIgnoreCase(ident.mName);
      if (result != 0) {
	return result;
      }
      result = mName.compareTo(ident.mName);
      if (result != 0) {
	return result;
      }
      final List list1 = getIndexes();
      final List list2 = ident.getIndexes();
      final Iterator iter1 = list1.iterator();
      final Iterator iter2 = list2.iterator();
      while (iter1.hasNext() && iter2.hasNext()) {
	final SimpleExpressionProxy index1 =
	  (SimpleExpressionProxy) iter1.next();
	final SimpleExpressionProxy index2 =
	  (SimpleExpressionProxy) iter2.next();
	result = index1.compareTo(index2);
	if (result != 0) {
	  return result;
	}
      }
      if (iter1.hasNext()) {
	return 1;
      } else if (iter2.hasNext()) {
	return -1;
      } else {
	return 0;
      }
    } else {
      return super.compareTo(partner);
    }
  }


  //#########################################################################
  //# Printing
  void pprint(final ModelPrinter printer,
	      final int outerpri,
	      final boolean assocbraces)
    throws IOException
  {
    printer.print(mName);
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    final IdentifierType expr = (IdentifierType) element;
    expr.setName(mName);
  }


  //#########################################################################
  //# Comparing
  int getOrderIndex()
  {
    return SimpleExpressionProxy.ORDERINDEX_IDENTIFIER;
  }


  //#########################################################################
  //# Data Members
  private final String mName;

}
