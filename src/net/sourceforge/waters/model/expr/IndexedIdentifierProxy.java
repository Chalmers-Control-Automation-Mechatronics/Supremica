//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   IndexedIdentifierProxy
//###########################################################################
//# $Id: IndexedIdentifierProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
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
import net.sourceforge.waters.xsd.module.IndexedIdentifierType;


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

public class IndexedIdentifierProxy extends IdentifierProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates an indexed identifier.
   * @param  name        The name of the new identifier,
   *                     which corresponds to the name of an array.
   * @param  indexes     The list of index terms. Each element of
   *                     this list is of type {@link SimpleExpressionProxy}.
   *                     The constructor copies each element into the
   *                     new indexed identifier, respecting the order given.
   */
  public IndexedIdentifierProxy(final String name, final List indexes)
  {
    super(name);
    final List proxies = new ArrayList(indexes);
    mIndexes = Collections.unmodifiableList(proxies);    
  }

  /**
   * Creates an indexed identifier from a parsed XML structure.
   * @param  element     The parsed XML structure of the new identifier.
   * @param  factory     The factory to be used for creating subterms.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  IndexedIdentifierProxy(final IndexedIdentifierType ident,
			 final ProxyFactory factory)
    throws ModelException
  {
    super(ident);
    final List list = ident.getIndexes();
    final Iterator iter = list.iterator();
    final int size = list.size();
    final List proxies = new ArrayList(size);
    while (iter.hasNext()) {
      final SimpleExpressionType index = (SimpleExpressionType) iter.next();
      proxies.add(factory.createProxy(index));
    }
    mIndexes = Collections.unmodifiableList(proxies);
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the list of array indexes.
   * @return An unmodifiable list of expression, representing the
   *         indexes into the array identified by the identifier's name.
   *         Each element is of type {@link SimpleExpressionProxy}.
   */
  public List getIndexes()
  {
    return mIndexes;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (partner != null &&
	getClass() == partner.getClass() &&
	super.equals(partner)) {
      final IndexedIdentifierProxy ident = (IndexedIdentifierProxy) partner;
      return mIndexes.equals(ident.mIndexes);
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Printing
  void pprint(final ModelPrinter printer,
	      final int outerpri,
	      final boolean assocbraces)
    throws IOException
  {
    super.pprint(printer, outerpri, assocbraces);
    final Iterator iter = mIndexes.iterator();
    while (iter.hasNext()) {
      final SimpleExpressionProxy index = (SimpleExpressionProxy) iter.next();
      printer.print('[');
      index.pprint(printer, OperatorTable.PRIORITY_OUTER, false);
      printer.print(']');
    }
  }


  //#########################################################################
  //# Marshalling
  public SimpleExpressionType createElement(final ObjectFactory factory)
    throws JAXBException
  {
    return factory.createIndexedIdentifier();
  }

  public ElementType toJAXB(final ElementFactory factory)
    throws JAXBException
  {
    final IndexedIdentifierType ident =
      (IndexedIdentifierType) factory.createElement(this);
    toJAXBElement(ident);
    final List list = ident.getIndexes();
    final Iterator iter = mIndexes.iterator();
    while (iter.hasNext()) {
      final SimpleExpressionProxy proxy = (SimpleExpressionProxy) iter.next();
      final SimpleExpressionType index =
	(SimpleExpressionType) proxy.toJAXB(factory);
      list.add(index);
    }
    return ident;
  }


  //#########################################################################
  //# Evaluation
  public Value eval(final Context context)
    throws EvalException
  {
    Value result = context.find(getName());
    int count = 0;
    final Iterator iter = mIndexes.iterator();
    while (iter.hasNext()) {
      if (!(result instanceof ArrayValue)) {
	final String name = evalToName(context, count);
	throw new BadArityException(name);
      }
      final ArrayValue array = (ArrayValue) result;
      final SimpleExpressionProxy index = (SimpleExpressionProxy) iter.next();
      final Value indexvalue = index.evalToIndex(context);
      try {
	result = array.find(indexvalue, index);
      } catch (EvalException exception) {
	exception.provideLocation(this);
	throw exception;
      }
      count++;
    }
    return result;
  }

  public String evalToName(final Context context)
    throws EvalException
  {
    return evalToName(context, mIndexes.size());
  }

  public int getResultTypes()
  {
    return SimpleExpressionProxy.TYPE_NAME;
  }


  //#########################################################################
  //# Auxiliary Methods
  private String evalToName(final Context context, final int numindexes)
    throws EvalException
  {
    final StringBuffer result = new StringBuffer(getName());
    final Iterator iter = mIndexes.iterator();
    for (int i = 0; i < numindexes; i++) {
      final SimpleExpressionProxy index = (SimpleExpressionProxy) iter.next();
      final Value value = index.evalToIndex(context);
      result.append('[');
      result.append(value);
      result.append(']');
    }
    return result.toString();
  }


  //#########################################################################
  //# Data Members
  private final List mIndexes;

}
