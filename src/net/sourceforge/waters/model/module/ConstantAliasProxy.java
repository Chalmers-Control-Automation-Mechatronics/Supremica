//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   ConstantAliasProxy
//###########################################################################
//# $Id: ConstantAliasProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.model.expr.IdentifierProxy;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.expr.SimpleExpressionProxyFactory;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.ConstantAliasType;
import net.sourceforge.waters.xsd.module.ConstantAliasListType;


/**
 * <P>An alias representing a constant definition.</P>
 *
 * <P>A constant alias is used to assign a simple constant to a name.  The
 * name of a constant alias must be a simple identifier ({@link
 * net.sourceforge.waters.model.expr.SimpleIdentifierProxy}), and its value
 * a simple expression ({@link SimpleExpressionProxy}) of integer ({@link
 * SimpleExpressionProxy#TYPE_INT TYPE_INT}) or range ({@link
 * SimpleExpressionProxy#TYPE_RANGE TYPE_RANGE}) type.</P>
 *
 * @author Robi Malik
 */

public class ConstantAliasProxy extends AliasProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates a constant alias.
   * @param  ident       The name for the new alias.
   * @param  expr        The expression defining the value for the new alias.
   */
  public ConstantAliasProxy(final IdentifierProxy ident,
			    final SimpleExpressionProxy expr)
  {
    super(ident);
    mExpression = expr;
  }

  /**
   * Creates a constant alias from a parsed XML structure.
   * This method is for internal use only and should not be called
   * directly.
   * @param  alias       The parsed XML structure of the new element.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  ConstantAliasProxy(final ConstantAliasType alias)
    throws ModelException
  {
    super(alias);
    final ProxyFactory factory = new SimpleExpressionProxyFactory();
    mExpression =
      (SimpleExpressionProxy) factory.createProxy(alias.getExpression());
  }


  //#########################################################################
  //# Getters and Setters
  public ExpressionProxy getExpression()
  {
    return mExpression;
  }


  public void setExpression(final ExpressionProxy expr)
  {
    mExpression = (SimpleExpressionProxy) expr;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final ConstantAliasProxy alias = (ConstantAliasProxy) partner;
      return
	getExpression().equals(alias.getExpression());
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
    final ConstantAliasType alias = (ConstantAliasType) element;
    alias.setExpression(mExpression.toSimpleExpressionType());
  }


  //#########################################################################
  //# Data Members
  private SimpleExpressionProxy mExpression;

 
  //#########################################################################
  //# Local Class ConstantAliasFactory
  static class ConstantAliasProxyFactory implements ProxyFactory
  {
    //#######################################################################
    //# Interface waters.model.module.ProxyFactory
    public Proxy createProxy(final ElementType element)
      throws ModelException
    {
      final ConstantAliasType alias = (ConstantAliasType) element;
      return new ConstantAliasProxy(alias);
    }

    public List getList(final ElementType parent)
    {
      final ConstantAliasListType list = (ConstantAliasListType) parent;
      return list.getList();
    }
  }


  //#########################################################################
  //# Local Class ConstantAliasElementFactory
  static class ConstantAliasElementFactory extends ModuleElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(Proxy proxy)
      throws JAXBException
    {
      return getFactory().createConstantAlias();
    }

    public ElementType createContainerElement()
      throws JAXBException
    {
      return getFactory().createConstantAliasList();
    }

    public List getElementList(final ElementType container)
    {
      final ConstantAliasListType list = (ConstantAliasListType) container;
      return list.getList();
    }

  }

}
