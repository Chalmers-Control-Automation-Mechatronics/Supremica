//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   SimpleIdentifierProxy
//###########################################################################
//# $Id: SimpleIdentifierProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.ObjectFactory;
import net.sourceforge.waters.xsd.module.SimpleExpressionType;
import net.sourceforge.waters.xsd.module.SimpleIdentifierType;


/**
 * A simple identifier.
 *
 * A simple identifier is an identifier that only consists of a single
 * name. It has no structure or indexes.
 *
 * @author Robi Malik
 */

public class SimpleIdentifierProxy extends IdentifierProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates a simple identifier.
   * @param  name        The name of the new identifier.
   */
  public SimpleIdentifierProxy(final String name)
  {
    super(name);
  }

  /**
   * Creates a simple identifier from a parsed XML structure.
   * @param  element     The parsed XML structure of the new identifier.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  SimpleIdentifierProxy(final SimpleIdentifierType expr)
  {
    super(expr);
  }


  //#########################################################################
  //# Getters
  List getIndexes()
  {
    return EMPTY;
  }


  //#########################################################################
  //# Marshalling
  public SimpleExpressionType createElement(final ObjectFactory factory)
    throws JAXBException
  {
    return factory.createSimpleIdentifier();
  }

  public ElementType toJAXB(final ElementFactory factory)
    throws JAXBException
  {
    final SimpleIdentifierType expr =
      (SimpleIdentifierType) factory.createElement(this);
    toJAXBElement(expr);
    return expr;
  }


  //#########################################################################
  //# Evaluation
  public Value eval(final Context context)
    throws EvalException
  {
    try {
      return context.find(getName());
    } catch (EvalException exception) {
      exception.provideLocation(this);
      throw exception;
    }
  }

  public String evalToName(final Context context)
  {
    return getName();
  }

  public int getResultTypes()
  {
    return SimpleExpressionProxy.TYPE_ANY;
  }


  //#########################################################################
  //# Class Constants
  private static final List EMPTY = new LinkedList();

}
