//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.decl
//# CLASS:   IdentifiedElementProxy
//###########################################################################
//# $Id: IdentifiedElementProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementProxy;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.model.expr.IdentifierProxy;
import net.sourceforge.waters.model.expr.SimpleExpressionProxyFactory;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.IdentifiedType;
import net.sourceforge.waters.xsd.module.IdentifierType;


/**
 * The abstract base class for elements with a complex name.
 *
 * This class represents all those elements in a module whose
 * name is not stored as a string but as an identifier. This
 * typically includes elements that can have indexed names
 * such as components or aliases.
 *
 * @author Robi Malik
 */

public abstract class IdentifiedElementProxy extends ElementProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty identified element.
   * @param  ident       The name for the new element.
   */
  IdentifiedElementProxy(final IdentifierProxy ident)
  {
    mIdentifier = ident;
  }

  /**
   * Creates an identified element from a parsed XML structure.
   * This method is for internal use only and should not be called
   * directly.
   * @param  element     The parsed XML structure of the new element.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  IdentifiedElementProxy(final IdentifiedType element)
    throws ModelException
  {
    final ProxyFactory factory = new SimpleExpressionProxyFactory();
    mIdentifier =
      (IdentifierProxy) factory.createProxy(element.getIdentifier());
  }


  //#########################################################################
  //# Getters and Setters
  public String getName()
  {
    return getIdentifier().toString();
  }

  public IdentifierProxy getIdentifier()
  {
    return mIdentifier;
  }

  public void setIdentifier(final IdentifierProxy ident)
  {
    mIdentifier = ident;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (partner != null &&
	getClass() == partner.getClass() &&
	super.equals(partner)) {
      final IdentifiedElementProxy named = (IdentifiedElementProxy) partner;
      return getIdentifier().equals(named.getIdentifier());
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    final IdentifiedType named = (IdentifiedType) element;
    named.setIdentifier
      ((IdentifierType) getIdentifier().toSimpleExpressionType());
  }


  //#########################################################################
  //# Data Members
  private IdentifierProxy mIdentifier;

}
