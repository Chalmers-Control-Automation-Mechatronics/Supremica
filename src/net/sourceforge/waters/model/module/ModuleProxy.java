//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   ModuleProxy
//###########################################################################
//# $Id: ModuleProxy.java,v 1.2 2005-02-17 19:30:44 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.ElementProxy;
import net.sourceforge.waters.model.base.IndexedArrayListProxy;
import net.sourceforge.waters.model.base.ListProxy;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.base.MutableNamedProxy;
import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.model.base.TopLevelListProxy;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.module.ComponentListType;
import net.sourceforge.waters.xsd.module.ConstantAliasListType;
import net.sourceforge.waters.xsd.module.EventAliasListType;
import net.sourceforge.waters.xsd.module.EventDeclListType;
import net.sourceforge.waters.xsd.module.ModuleType;
import net.sourceforge.waters.xsd.module.ParameterListType;


/**
 * <P>The top-level information container in Waters.</P>
 *
 * <P>A module contains a list of automata and associated events.
 * Modules are stored in XML files with the extension <CODE>.wmod</CODE>,
 * which uses the XML schema called <CODE>waters-module.dtd</CODE>.</P>
 *
 * <P>In the simplest case, a module contains a list of event declarations
 * ({@link EventDeclProxy}) and a list of components, i.e. automata
 * ({@link SimpleComponentProxy}). For example, a simple version of
 * <I>Small Factory</I> may consist of the following parts.</P>
 *
 * <PRE>
 * MODULE small_factory_simple;
 * EVENTS
 *   controllable start1;
 *   controllable start2;
 *   uncontrollable finish1;
 *   uncontrollable finish2;
 *   uncontrollable break1;
 *   uncontrollable break2;
 *   controllable repair1;
 *   controllable repair2;
 * COMPONENTS
 *   plant mach1;
 *   plant mach2;
 *   spec buffer;
 * </PRE>
 *
 * <P>More advanced modules can also have parameters and aliases. These
 * features make it possible to describe complex parameterised structures.
 * Similar automata can be reused by replacing their events in various
 * ways.</P>
 *
 * <P>An more advanced version of the <I>small factory</I> example above
 * would use only one automaton for the two almost identical machines
 * <CODE>mach1</CODE> and <CODE>mach2</CODE>. This is achieved by creating
 * a machine module containing the one machine automaton. The advanced
 * small factory module uses event arrays and instantiates the machine
 * module twice using a loop.</P>
 *
 * <PRE>
 * MODULE machine;
 * PARAMETERS
 *   controllable start;
 *   uncontrollable finish;
 * EVENTS
 *   uncontrollable break;
 *   controllable repair;
 * COMPONENTS
 *   plant mach;
 *
 * MODULE small_factory_advanced;
 * EVENTS
 *   controllable start[1..2];
 *   uncontrollable finish[1..2];
 * COMPONENTS
 *   FOR i IN 1..2
 *     instance machine[i] = machine(
 *       start = start[i];
 *       finish = finish[i];
 *     );
 *   ENDFOR
 *   spec buffer;
 * </PRE>
 *
 * @author Robi Malik
 */

public class ModuleProxy
  extends MutableNamedProxy
  implements DocumentProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty module.
   * @param  name        The name for the new module.
   */
  public ModuleProxy(final String name)
  {
    this(name, null);
  }

  /**
   * Creates an empty module.
   * @param  name        The name for the new module.
   * @param  location    The full path name of the file where this module
   *                     will be saved to.
   */
  public ModuleProxy(final String name, final File location)
  {
    super(name);
    mLocation = location;
    mParameterListProxy = new ParameterListProxy();
    mConstantAliasListProxy = new ConstantAliasListProxy();
    mEventDeclListProxy = new EventDeclListProxy();
    mEventAliasListProxy = new EventAliasListProxy();
    mComponentListProxy = new ComponentListProxy();
  }

  /**
   * Creates a module from a parsed XML structure.
   * This method is for internal use only and should not be called
   * directly; use class
   * {@link net.sourceforge.waters.model.base.ProxyMarshaller
   * ProxyMarshaller} instead.
   * @param  module      The parsed XML structure of the new module.
   * @param  location    The full path name of the file where this module
   *                     will be saved to.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  public ModuleProxy(final ModuleType module, final File location)
    throws ModelException
  {
    super(module);
    mLocation = location;
    mParameterListProxy = new ParameterListProxy(module);
    mConstantAliasListProxy = new ConstantAliasListProxy(module);
    mEventDeclListProxy = new EventDeclListProxy(module);
    mEventAliasListProxy = new EventAliasListProxy(module);
    mComponentListProxy = new ComponentListProxy(module);
  }


  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the parameter list of this module.
   * @return The (modifiable) parameter list.
   *         Each element is of type {@link ParameterProxy}.
   */
  public List getParameterList()
  {
    return mParameterListProxy;
  }

  /**
   * Gets the constant definition list of this module.
   * @return The (modifiable) constant definition list.
   *         Each element is of type {@link ConstantAliasProxy}.
   */
  public List getConstantAliasList()
  {
    return mConstantAliasListProxy;
  }

  /**
   * Gets the event declaration list of this module.
   * @return The (modifiable) list of event declarations.
   *         Each element is of type {@link EventDeclProxy}.
   */
  public List getEventDeclList()
  {
    return mEventDeclListProxy;
  }

  /**
   * Gets the event alias list of this module.
   * @return The (modifiable) list event aliases.
   *         Each element is of type {@link EventAliasProxy}
   *         or {@link ForeachEventAliasProxy}.
   */
  public List getEventAliasList()
  {
    return mEventAliasListProxy;
  }

  /**
   * Gets the component list of this module.
   * @return The (modifiable) component list.
   *         Each element is of type {@link ComponentProxy}
   *         or {@link ForeachComponentProxy}.
   */
  public List getComponentList()
  {
    return mComponentListProxy;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final ModuleProxy module = (ModuleProxy) partner;
      return
	getParameterList().equals(module.getParameterList()) &&
	getConstantAliasList().equals(module.getConstantAliasList()) &&
	getEventDeclList().equals(module.getEventDeclList()) &&
	getEventAliasList().equals(module.getEventAliasList()) &&
	getComponentList().equals(module.getComponentList());
    } else {
      return false;
    }    
  }

  public boolean equalsWithGeometry(final Object partner)
  {
    if (super.equals(partner)) {
      final ModuleProxy module = (ModuleProxy) partner;
      return
	mConstantAliasListProxy.equals(module.mConstantAliasListProxy) &&
	mParameterListProxy.equalsWithGeometry(module.mParameterListProxy) &&
	mEventAliasListProxy.equals(module.mEventAliasListProxy) &&
	mEventDeclListProxy.equalsWithGeometry(module.mEventDeclListProxy) &&
	mComponentListProxy.equalsWithGeometry(module.mComponentListProxy);
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Accessing the Event Declaration Lists
  /**
   * Finds an parameter with given name.
   * @param  name        The name of the parameter to be found.
   * @return The corresponding parameter object.
   * @throws NameNotFoundException to indicate that the module does not
   *                      contain any parameter with the given name.
   */
  public ParameterProxy findParameter(final String name)
    throws NameNotFoundException
  {
    return (ParameterProxy) mParameterListProxy.find(name);
  }

  /**
   * Tries to find an parameter with given name.
   * @param  name        The name of the parameter to be found.
   * @return The corresponding parameter object,
   *         or <CODE>null</CODE> if none was found.
   */
  public ParameterProxy getParameter(final String name)
  {
    return (ParameterProxy) mParameterListProxy.get(name);
  }  

  /**
   * Adds a new parameter.
   * @param  param       The parameter to be added.
   * @throws DuplicateNameException to indicate that the module already
   *                     contains an parameter with the same name.
   */
  public void insertParameter(final ParameterProxy param)
    throws DuplicateNameException
  {
    mParameterListProxy.insert(param);
  }
    
  /**
   * Finds an event declaration with given name.
   * @param  name        The name of the event declaration to be found.
   * @return The corresponding event declaration object.
   * @throws NameNotFoundException to indicate that the module does not
   *                      contain any event declaration with the given name.
   */
  public EventDeclProxy findEventDeclaration(final String name)
    throws NameNotFoundException
  {
    return (EventDeclProxy) mEventDeclListProxy.find(name);
  }

  /**
   * Tries to find an event declaration with given name.
   * @param  name        The name of the event declaration to be found.
   * @return The corresponding event declaration object,
   *         or <CODE>null</CODE> if none was found.
   */
  public EventDeclProxy getEventDeclaration(final String name)
  {
    return (EventDeclProxy) mEventDeclListProxy.get(name);
  }  

  /**
   * Adds a new event declaration.
   * @param  decl        The event declaration to be added.
   * @throws DuplicateNameException to indicate that the module already
   *                     contains an event declaration with the same name.
   */
  public void insertEventDeclaration(final EventDeclProxy decl)
    throws DuplicateNameException
  {
    mEventDeclListProxy.insert(decl);
  }
    

  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    printer.print("MODULE ");
    printer.println(getName());
    mParameterListProxy.pprint(printer);
    mConstantAliasListProxy.pprint(printer);
    mEventDeclListProxy.pprint(printer);
    mEventAliasListProxy.pprint(printer);
    mComponentListProxy.pprint(printer);
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    final ModuleType module = (ModuleType) element;
    final ElementFactory paramfactory =
      new ParameterProxy.ParameterElementFactory();
    final ParameterListType paramlist =
      (ParameterListType) mParameterListProxy.toJAXB(paramfactory);
    module.setParameterList(paramlist);
    final ElementFactory constantfactory =
      new ConstantAliasProxy.ConstantAliasElementFactory();
    final ConstantAliasListType constantlist =
      (ConstantAliasListType) mConstantAliasListProxy.toJAXB(constantfactory);
    module.setConstantAliasList(constantlist);
    final ElementFactory eventfactory =
      new EventDeclProxy.EventDeclElementFactory();
    final EventDeclListType eventlist =
      (EventDeclListType) mEventDeclListProxy.toJAXB(eventfactory);
    module.setEventDeclList(eventlist);
    final ElementFactory aliasfactory =
      new EventAliasProxy.EventAliasElementFactory();
    final EventAliasListType aliaslist =
      (EventAliasListType) mEventAliasListProxy.toJAXB(aliasfactory);
    module.setEventAliasList(aliaslist);
    final ElementFactory compfactory =
      new ComponentProxy.ComponentElementFactory();
    final ComponentListType complist =
      (ComponentListType) mComponentListProxy.toJAXB(compfactory);
    module.setComponentList(complist);
  }

  /**
   * Creates an XML element representing the contents of this module.
   */
  public ModuleType toModuleType()
    throws JAXBException
  {
    final ElementFactory factory = new RealModuleElementFactory();
    return (ModuleType) toJAXB(factory);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.DocumentProxy
  public File getLocation()
  {
    return mLocation;
  }

  public void setLocation(final File location)
  {
    mLocation = location;
  }


  //#########################################################################
  //# Local Class ParameterListProxy
  private static class ParameterListProxy extends IndexedArrayListProxy {

    //#######################################################################
    //# Constructors
    ParameterListProxy()
    {
    }

    ParameterListProxy(final ModuleType module)
      throws ModelException
    {
      super(module.getParameterList(),
	    new ParameterProxy.ParameterProxyFactory());
    }

    //#######################################################################
    //# Overrides from abstract class TopLevelListProxy
    protected String getPPrintName()
    {
      return "PARAMETERS";
    }
  
  }


  //#########################################################################
  //# Local Class ConstantAliasListProxy
  private static class ConstantAliasListProxy extends TopLevelListProxy {

    //#######################################################################
    //# Constructors
    ConstantAliasListProxy()
    {
    }

    ConstantAliasListProxy(final ModuleType module)
      throws ModelException
    {
      super(module.getConstantAliasList(),
	    new ConstantAliasProxy.ConstantAliasProxyFactory());
    }

    //#######################################################################
    //# Overrides from abstract class TopLevelListProxy
    protected String getPPrintName()
    {
      return "ALIASES";
    }
  
  }


  //#########################################################################
  //# Local Class EventDeclListProxy
  private static class EventDeclListProxy extends IndexedArrayListProxy {

    //#######################################################################
    //# Constructors
    EventDeclListProxy()
    {
    }

    EventDeclListProxy(final ModuleType module)
      throws ModelException
    {
      super(module.getEventDeclList(),
	    new EventDeclProxy.EventDeclProxyFactory());
    }

    //#######################################################################
    //# Overrides from abstract class TopLevelListProxy
    protected String getPPrintName()
    {
      return "EVENTS";
    }
  
  }


  //#########################################################################
  //# Local Class EventAliasListProxy
  private static class EventAliasListProxy extends TopLevelListProxy {

    //#######################################################################
    //# Constructors
    EventAliasListProxy()
    {
    }

    EventAliasListProxy(final ModuleType module)
      throws ModelException
    {
      super(module.getEventAliasList(),
	    new EventAliasProxy.EventAliasProxyFactory());
    }

    //#######################################################################
    //# Overrides from abstract class TopLevelListProxy
    protected String getPPrintName()
    {
      return "ALIASES";
    }
  
  }


  //#########################################################################
  //# Local Class ComponentListProxy
  private static class ComponentListProxy extends TopLevelListProxy {

    //#######################################################################
    //# Constructor
    ComponentListProxy()
    {
    }

    ComponentListProxy(final ModuleType module)
      throws ModelException
    {
      super(module.getComponentList(),
	    new ComponentProxy.ComponentProxyFactory());
    }

    //#######################################################################
    //# Overrides from abstract class TopLevelListProxy
    protected String getPPrintName()
    {
      return "COMPONENTS";
    }
  
  }


  //#########################################################################
  //# Local Class RealModuleElementFactory
  private static class RealModuleElementFactory extends ModuleElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(final Proxy proxy)
      throws JAXBException
    {
      return getFactory().createModule();
    }

    public ElementType createContainerElement()
      throws JAXBException
    {
      throw new UnsupportedOperationException
	("Module has no containing list!");
    }

    public List getElementList(final ElementType container)
    {
      throw new UnsupportedOperationException
	("Module has no containing list!");
    }

  }


  //#########################################################################
  //# Data Members
  private File mLocation;
  private final ListProxy mConstantAliasListProxy;
  private final IndexedArrayListProxy mParameterListProxy;
  private final ListProxy mEventAliasListProxy;
  private final IndexedArrayListProxy mEventDeclListProxy;
  private final ListProxy mComponentListProxy;

}
