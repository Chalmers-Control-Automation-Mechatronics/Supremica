//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.decl
//# CLASS:   InstanceProxy
//###########################################################################
//# $Id: InstanceProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.IndexedArrayListProxy;
import net.sourceforge.waters.model.base.IndexedListProxy;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.IdentifierProxy;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.InstanceType;


/**
 * <P>A module component to be replaced by the contents of another module.</P>
 *
 * <P>Instances can occur in a module's component list. They represent
 * instructions to insert all components of another module after
 * substitution often parameters.</P>
 *
 * <P>Each instance has the following components.</P>
 * <DL>
 * <DT><STRONG>Name.</STRONG></DT>
 * <DD>The name to be used for the instance. The name is used to prefix all
 * the components and events in the instantiated module. It can be a
 * structured identifier.</DD>
 * <DT><STRONG>Module.</STRONG></DT>
 * <DD>The module to be instantiated.</DD>
 * <DT><STRONG>Binding list.</STRONG></DT>
 * <DD>A list of pairs of names and expressions ({@link ParameterBindingProxy})
 * that describes the values to be used for the parameters of the instantiated
 * module.</DD>
 * </DL>
 *
 * <P>As an example, consider the following simple <I>machine</I> module,
 * which has two event parameters <CODE>start</CODE> and
 * <CODE>finish</CODE>.</P>
 *
 * <PRE>
 *   MODULE machine;
 *   PARAMETERS
 *     controllable start;
 *     uncontrollable finish;
 *   EVENTS
 *     uncontrollable break;
 *     controllable repair;
 *   COMPONENTS
 *     plant mach;
 * </PRE>
 *
 * <P>This module may be instantiated from another module <I>factory</I>
 * as follows.</P>
 *
 * <PRE>
 *   MODULE factory;
 *   EVENTS
 *     controllable start1;
 *     uncontrollable finish1;
 *     ...
 *   COMPONENTS
 *     machine1 = machine(
 *                  start = start1;
 *                  finish = finish1;
 *                );
 *     ...
 * </PRE>
 *
 * <P>This will include all automata of the <I>machine</I> module in
 * <I>factory</I> after replacing the events <CODE>start</CODE> and
 * <CODE>finish</CODE> in <I>machine</I> by <CODE>start1</CODE> and
 * <CODE>finish1</CODE> from <I>factory</I>, respectively. The compiled
 * <I>factory</I> model will include new events <CODE>machine1.break</CODE>
 * and <CODE>machine1.repair</CODE>, and a plant automaton
 * <CODE>machine1.mach</CODE> which is result of applying the event
 * substitution to the automaton from the <I>machine</I> module.</P>
 *
 * @author Robi Malik
 */

public class InstanceProxy extends ComponentProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates an instance without bindings.
   * @param  ident       The name for the new instance.
   * @param  modulename  The name of the module to be instantiated,
   *                     given as a string identifying a complete name,
   *                     if necessary, but without extension.
   */
  public InstanceProxy(final IdentifierProxy ident, final String modulename)
  {
    super(ident);
    mModuleName = modulename;
    mBindingListProxy = new BindingListProxy();
  }

  /**
   * Creates an instance from a parsed XML structure.
   * @param  instance    The parsed XML structure of the new instance.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  InstanceProxy(final InstanceType instance)
    throws ModelException
  {
    super(instance);
    mModuleName = instance.getModuleName();
    mBindingListProxy = new BindingListProxy(instance);
  }


  //#########################################################################
  //# Getters and Setters
  /**
   * Returns the module name of this instance.
   * The module name is given as a string identifying a complete name,
   * if necessary, but without extension.
   */
  public String getModuleName()
  {
    return mModuleName;
  }

  /**
   * Sets the module name of this instance.
   * The module name is given as a string identifying a complete name,
   * if necessary, but without extension.
   */
  public void setModuleName(final String module)
  {
    mModuleName = module;
  }

  /**
   * Returns the binding list of this instance.
   * @return A list of name-value pairs describing how the parameters
   *         of the instantiated module are bound to values.
   *         Each element is of type {@link ParameterBindingProxy}.
   */
  public List getBindingList()
  {
    return mBindingListProxy;
  }

  /**
   * Returns a map view of binding list of this instance.
   * @return An unmodifiable map that maps strings ({@link java.lang.String})
   *         to name-value pairs ({@link ParameterBindingProxy}), where
   *         each entry describes the value to be bound to the
   *         corresponding name.
   */
  public Map getBindingMap()
  {
    return mBindingListProxy.getMap();
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (partner != null &&
	getClass() == partner.getClass() &&
	super.equals(partner)) {
      final InstanceProxy inst = (InstanceProxy) partner;
      return getBindingList().equals(inst.getBindingList());
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    final IdentifierProxy ident = getIdentifier();
    final Iterator iter = mBindingListProxy.iterator();
    printer.print("instance ");
    ident.pprint(printer);
    printer.print(" = ");
    printer.print(getModuleName());
    printer.print('(');
    if (iter.hasNext()) {
      printer.println();
      printer.indentIn();
      while (iter.hasNext()) {
	final ParameterBindingProxy binding =
	  (ParameterBindingProxy) iter.next();
	binding.pprint(printer);
	if (iter.hasNext()) {
	  printer.print(',');
	}
	printer.println();
      }
      printer.indentOut();
    }
    printer.print(')');
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    final InstanceType inst = (InstanceType) element;
    inst.setModuleName(getModuleName());
    final ElementFactory factory = new BindingElementFactory(inst);
    mBindingListProxy.toJAXB(factory);
  }


  //#########################################################################
  //# Data Members
  private String mModuleName;
  private final IndexedListProxy mBindingListProxy;


  //#########################################################################
  //# Local Classes
  private static class BindingListProxy extends IndexedArrayListProxy {

    //#######################################################################
    //# Constructors
    BindingListProxy()
    {
    }

    BindingListProxy(final InstanceType instance)
      throws ModelException
    {
      super(instance, new BindingFactory());
    }

  }


  //#########################################################################
  //# Local Class BindingFactory
  private static class BindingFactory
    extends ParameterBindingProxy.ParameterBindingProxyFactory
  {

    //#######################################################################
    //# Interface waters.model.base.ProxyFactory
    public List getList(final ElementType parent)
    {
      final InstanceType instance = (InstanceType) parent;
      return instance.getBindings();
    }

  }


  //#########################################################################
  //# Local Class BindingElementFactory
  private static class BindingElementFactory
    extends ParameterBindingProxy.ParameterBindingElementFactory
  {

    //#######################################################################
    //# Constructor
    BindingElementFactory(final InstanceType inst)
    {
      mInstance = inst;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createContainerElement()
    {
      return mInstance;
    }

    public List getElementList(final ElementType container)
    {
      final InstanceType list = (InstanceType) container;
      return list.getBindings();
    }

    //#######################################################################
    //# Data Members
    private final InstanceType mInstance;

  }
}
