//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   ComponentProxy
//###########################################################################
//# $Id: ComponentProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.IdentifierProxy;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.ComponentListType;
import net.sourceforge.waters.xsd.module.ForeachComponentType;
import net.sourceforge.waters.xsd.module.ForeachType;
import net.sourceforge.waters.xsd.module.IdentifiedType;
import net.sourceforge.waters.xsd.module.InstanceType;
import net.sourceforge.waters.xsd.module.SimpleComponentType;


/**
 * The abstract base class for all components.
 *
 * This class represents the proper items that may occur in a module's
 * component list. Presently, these are <I>simple components</I> ({@link
 * SimpleComponentProxy}) and <I>instances</I> ({@link InstanceProxy}).
 *
 * @see SimpleComponentProxy
 * @see InstanceProxy
 *
 * @author Robi Malik
 */

public abstract class ComponentProxy extends IdentifiedElementProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates a new component.
   * @param  ident       The name for the new component.
   */
  ComponentProxy(final IdentifierProxy ident)
  {
    super(ident);
  }

  /**
   * Creates a component from a parsed XML structure.
   * @param  comp        The parsed XML structure of the new component.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  ComponentProxy(final IdentifiedType comp)
    throws ModelException
  {
    super(comp);
  }


  //#########################################################################
  //# Local Class ComponentFactory
  static class ComponentProxyFactory implements ForeachProxyFactory
  {

    //#######################################################################
    //# Interface waters.model.base.ProxyFactory
    public Proxy createProxy(final ElementType element)
      throws ModelException
    {
      if (element instanceof SimpleComponentType) {
	final SimpleComponentType comp = (SimpleComponentType) element;
	return new SimpleComponentProxy(comp);
      } else if (element instanceof InstanceType) {
	final InstanceType instance = (InstanceType) element;
	return new InstanceProxy(instance);
      } else if (element instanceof ForeachComponentType) {
	final ForeachComponentType foreach = (ForeachComponentType) element;
 	return new ForeachComponentProxy(foreach, this);
      } else {
	throw new ClassCastException
	  ("Can't create component proxy for class " +
	   element.getClass().getName() + "!");
      }
    }

    public List getList(final ElementType parent)
    {
      final ComponentListType list = (ComponentListType) parent;
      return list.getList();
    }

    //#######################################################################
    //# Interface waters.model.module.ForeachProxyFactory
    public ElementType getForeachBody(final ForeachType foreach)
    {
      final ForeachComponentType foreachcomp = (ForeachComponentType) foreach;
      return foreachcomp.getComponentList();
    }

  }


  //#########################################################################
  //# Local Class ComponentElementFactory
  static class ComponentElementFactory extends ModuleElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(final Proxy proxy)
      throws JAXBException
    {
      if (proxy instanceof SimpleComponentProxy) {
	return getFactory().createSimpleComponent();
      } else if (proxy instanceof InstanceProxy) {
	return getFactory().createInstance();
      } else if (proxy instanceof ForeachProxy) {
	return getFactory().createForeachComponent();
      } else {
	throw new ClassCastException
	  ("Can't marshal object of type " +
	   proxy.getClass().getName() + " as component!");
      }
    }

    public ElementType createContainerElement()
      throws JAXBException
    {
      return getFactory().createComponentList();
    }

    public List getElementList(final ElementType container)
    {
      final ComponentListType list = (ComponentListType) container;
      return list.getList();
    }

  }

}
