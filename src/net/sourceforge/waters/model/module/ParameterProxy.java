//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   ParameterProxy
//###########################################################################
//# $Id: ParameterProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.io.IOException;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.base.MutableNamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.xsd.module.EventParameterType;
import net.sourceforge.waters.xsd.module.IntParameterType;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.base.NamedType;
import net.sourceforge.waters.xsd.module.ParameterListType;
import net.sourceforge.waters.xsd.module.RangeParameterType;


/**
 * <P>A parameter of a Waters module.</P>
 *
 * <P>Modules can have parameters that are substituted by actual values
 * when the module is compiled. This can be used to describe interface
 * events and bind them to different events each time a module is used, or
 * to parameterise the number of components in a module.</P>
 *
 * <P>There can be different types of parameters, represented by
 * the subclasses of this general base class.</P>
 *
 * @see ModuleProxy
 * @see InstanceProxy
 *
 * @author Robi Malik
 */

public abstract class ParameterProxy extends MutableNamedProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates a parameter.
   * @param  name        The name of the new parameter.
   */
  public ParameterProxy(final String name)
  {
    super(name);
  }

  /**
   * Creates a parameter from a parsed XML structure.
   * @param  param       The parsed XML structure of the parameter.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  ParameterProxy(final NamedType param)
  {
    super(param);
  }


  //#########################################################################
  //# Getters and Setters
  /**
   * Checks whether this is a required parameter.
   * A required parameter must be given a value when its module is
   * instantiated, otherwise an error will be produced.
   * @return <CODE>true</CODE> if this parameter is required.
   */
  public abstract boolean isRequired();

  /**
   * Sets the required status of this parameter.
   * @param  required    <CODE>true</CODE> if the parameter should become
   *                     required, <CODE>false</CODE> otherwise.
   */
  public abstract void setRequired(boolean required);


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    if (isRequired()) {
      printer.print("required ");
    } else {
      printer.print("optional ");
    }
  }


  //#########################################################################
  //# Local Class ParameterProxyFactory
  static class ParameterProxyFactory implements ProxyFactory
  {

    //#######################################################################
    //# Interface waters.model.base.ProxyFactory
    public Proxy createProxy(final ElementType element)
      throws ModelException
    {
      if (element instanceof IntParameterType) {
	final IntParameterType param = (IntParameterType) element;
	return new IntParameterProxy(param);
      } else if (element instanceof RangeParameterType) {
	final RangeParameterType param = (RangeParameterType) element;
	return new RangeParameterProxy(param);
      } else if (element instanceof EventParameterType) {
	final EventParameterType param = (EventParameterType) element;
	return new EventParameterProxy(param);
      } else {
	throw new ClassCastException
	  ("Can't create parameter proxy for class " +
	   element.getClass().getName() + "!");
      }
    }

    public List getList(final ElementType parent)
    {
      final ParameterListType list = (ParameterListType) parent;
      return list.getList();
    }

  }


  //#########################################################################
  //# Local Class ParameterElementFactory
  static class ParameterElementFactory extends ModuleElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(final Proxy proxy)
      throws JAXBException
    {
      if (proxy instanceof IntParameterProxy) {
	return getFactory().createIntParameter();
      } else if (proxy instanceof RangeParameterProxy) {
	return getFactory().createRangeParameter();
      } else if (proxy instanceof EventParameterProxy) {
	return getFactory().createEventParameter();
      } else {
	throw new ClassCastException
	  ("Can't marshal object of type " +
	   proxy.getClass().getName() + " as parameter!");
      }
    }

    public ElementType createContainerElement()
      throws JAXBException
    {
      return getFactory().createParameterList();
    }

    public List getElementList(final ElementType container)
    {
      final ParameterListType list = (ParameterListType) container;
      return list.getList();
    }

  }

}
