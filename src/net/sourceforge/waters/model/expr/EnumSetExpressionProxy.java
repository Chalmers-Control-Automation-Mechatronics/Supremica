//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   EnumSetExpressionProxy
//###########################################################################
//# $Id: EnumSetExpressionProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.ObjectFactory;
import net.sourceforge.waters.xsd.module.SimpleExpressionType;
import net.sourceforge.waters.xsd.module.SimpleIdentifierType;
import net.sourceforge.waters.xsd.module.EnumSetExpressionType;


/**
 * <P>An expression representing an enumerated range.</P>
 *
 * <P>An enumerated range consists of a list of names, its <I>atoms</I>,
 * which can be used as array indexes or as the range for a <I>foreach</I>
 * construct ({@link net.sourceforge.waters.model.module.ForeachProxy}). In
 * textual representation, enumerated ranges are listed within curled
 * braces, with their elements separated by commas.</P>
 *
 * <P>Examples:</P>
 * <UL>
 * <LI><CODE>{a, b, c}</CODE>;
 * <LI><CODE>{}</CODE>.
 * </UL>
 *
 * @author Robi Malik
 */

public class EnumSetExpressionProxy extends SimpleExpressionProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates an enumerated range.
   * @param  items       The list of enumerated items. Each element of
   *                     this list is of type {@link SimpleIdentifierProxy}.
   *                     The constructor copies each element into the
   *                     new enumeration range, respecting the order given.
   */
  public EnumSetExpressionProxy(final List items)
  {
    final List proxies = new ArrayList(items);
    mItems = Collections.unmodifiableList(proxies);    
  }

  /**
   * Creates an enumerated range from a parsed XML structure.
   * @param  enumset     The parsed XML structure of the new enumration.
   * @param  factory     The factory to be used for creating subterms.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  EnumSetExpressionProxy(final EnumSetExpressionType enumset,
			 final ProxyFactory factory)
    throws ModelException
  {
    super(enumset);
    final List list = enumset.getItems();
    final Iterator iter = list.iterator();
    final int size = list.size();
    final List proxies = new ArrayList(size);
    while (iter.hasNext()) {
      final SimpleIdentifierType index = (SimpleIdentifierType) iter.next();
      proxies.add(factory.createProxy(index));
    }
    mItems = Collections.unmodifiableList(proxies);
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the list of items in this enumeration.
   * @return An unmodifiable list of enumerated items.
   *         Each element is of type {@link SimpleIdentifierProxy}.
   */
  public List getItems()
  {
    return mItems;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final EnumSetExpressionProxy enumset = (EnumSetExpressionProxy) partner;
      return mItems.equals(enumset.mItems);
    } else {
      return false;
    }    
  }

  public int hashCode()
  {
    return 5 * super.hashCode() + mItems.hashCode();
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  public int compareTo(final Object partner)
  {
    if (partner instanceof EnumSetExpressionProxy) {
      final EnumSetExpressionProxy enumset = (EnumSetExpressionProxy) partner;
      final Iterator iter1 = mItems.iterator();
      final Iterator iter2 = enumset.mItems.iterator();
      while (iter1.hasNext() && iter2.hasNext()) {
	final SimpleExpressionProxy item1 =
	  (SimpleExpressionProxy) iter1.next();
	final SimpleExpressionProxy item2 =
	  (SimpleExpressionProxy) iter2.next();
	final int result = item1.compareTo(item2);
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
    final Iterator iter = mItems.iterator();
    printer.print('{');
    while (iter.hasNext()) {
      final SimpleIdentifierProxy item = (SimpleIdentifierProxy) iter.next();
      item.pprint(printer, OperatorTable.PRIORITY_OUTER, false);
      if (iter.hasNext()) {
	printer.print(", ");
      }	
    }
    printer.print('}');
  }


  //#########################################################################
  //# Marshalling
  public SimpleExpressionType createElement(final ObjectFactory factory)
    throws JAXBException
  {
    return factory.createEnumSetExpression();
  }

  public ElementType toJAXB(final ElementFactory factory)
    throws JAXBException
  {
    final EnumSetExpressionType enumset =
      (EnumSetExpressionType) factory.createElement(this);
    toJAXBElement(enumset);
    final List list = enumset.getItems();
    final Iterator iter = mItems.iterator();
    while (iter.hasNext()) {
      final SimpleIdentifierProxy proxy = (SimpleIdentifierProxy) iter.next();
      final SimpleIdentifierType item =
	(SimpleIdentifierType) proxy.toJAXB(factory);
      list.add(item);
    }
    return enumset;
  }


  //#########################################################################
  //# Evaluation
  public Value eval(final Context context)
    throws EvalException
  {
    final List atoms = new ArrayList(mItems.size());
    final Iterator iter = mItems.iterator();
    while (iter.hasNext()) {
      final SimpleIdentifierProxy ident =
	(SimpleIdentifierProxy) iter.next();
      final String name = ident.getName();
      final Value value = context.get(name);
      AtomValue atom;
      if (value == null) {
	atom = new AtomValue(name);
	context.set(name, atom);
      } else if (value instanceof AtomValue) {
	atom = (AtomValue) value;
      } else {
	throw new TypeMismatchException
	  (ident, value, SimpleExpressionProxy.TYPE_ATOM);
      }
      atoms.add(atom);
    }
    return new EnumRangeValue(atoms);
  }

  public int getResultTypes()
  {
    return SimpleExpressionProxy.TYPE_RANGE;
  }


  //#########################################################################
  //# Comparing
  int getOrderIndex()
  {
    return SimpleExpressionProxy.ORDERINDEX_ENUMSET;
  }


  //#########################################################################
  //# Data Members
  private final List mItems;

}
