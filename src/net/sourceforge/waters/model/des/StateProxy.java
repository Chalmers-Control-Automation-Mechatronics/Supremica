//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.des
//# CLASS:   StateProxy
//###########################################################################
//# $Id: StateProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.des;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ArrayListProxy;
import net.sourceforge.waters.model.base.ElementFactory;
import net.sourceforge.waters.model.base.ImmutableNamedProxy;
import net.sourceforge.waters.model.base.ListProxy;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyFactory;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.des.EventRefListType;
import net.sourceforge.waters.xsd.des.StateListType;
import net.sourceforge.waters.xsd.des.StateType;


/**
 * <P>A state of an automaton.</P>
 *
 * @author Robi Malik
 */

public class StateProxy extends ImmutableNamedProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates a new state without associated propositions.
   * @param  name         The name to be given to the new state.
   * @param  initial      A flag indicating whether the new state is to be
   *                      an initial state.
   */
  public StateProxy(final String name,
		    final boolean initial)
  {
    super(name);
    mIsInitial = initial;
    mPropositionListProxy = new ArrayListProxy();
  }

  /**
   * Creates a new state.
   * @param  name         The name to be given to the new state.
   * @param  initial      A flag indicating whether the new state is to be
   *                      an initial state.
   * @param  propositions The initial list of propositions for the new state.
   *                      Each element should be of type {@link EventProxy}.
   */
  public StateProxy(final String name,
		    final boolean initial,
		    final Collection propositions)
  {
    super(name);
    mIsInitial = initial;
    mPropositionListProxy = new ArrayListProxy(propositions);
  }

  /**
   * Creates a copy of a state.
   * @param  partner     The object to be copied from.
   */
  public StateProxy(final StateProxy partner)
  {
    super(partner);
    mIsInitial = partner.mIsInitial;
    mPropositionListProxy = new ArrayListProxy(partner.mPropositionListProxy);
  }

  /**
   * Creates a state from a parsed XML structure.
   * @param  state       The parsed XML structure of the new state.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  StateProxy(final StateType state,
	     final ProxyFactory eventfactory)
    throws ModelException
  {
    super(state);
    mIsInitial = state.isInitial();
    mPropositionListProxy =
      new ArrayListProxy(state.getPropositions(), eventfactory);
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  /**
   * Returns a copy of this state.
   */
  public Object clone()
  {
    return new StateProxy(this);
  }


  //#########################################################################
  //# Getters and Setters
  public boolean isInitial()
  {
    return mIsInitial;
  }

  public void setInitial(boolean initial)
  {
    mIsInitial = initial;
  }

  /**
   * Get the list of propositions associated with this state.
   * @return  An unmodifiable list of objects of type EventProxy.
   */
  public List getPropositions()
  {
    return Collections.unmodifiableList(mPropositionListProxy);
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final StateProxy state = (StateProxy) partner;
      return
	isInitial() == state.isInitial() &&
	mPropositionListProxy.equals(state.mPropositionListProxy);
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    if (isInitial()) {
      printer.print("initial ");
    }
    printer.print(getName());
    if (getPropositions().size() > 0) {
      printer.print(' ');
      mPropositionListProxy.pprint(printer);
    }
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    final StateType state = (StateType) element;
    state.setInitial(isInitial());
    final ElementFactory factory = new EventProxy.EventRefElementFactory();
    final EventRefListType list =
      (EventRefListType) mPropositionListProxy.toJAXB(factory);
    state.setPropositions(list);
  }


  //#########################################################################
  //# Data Members
  private boolean mIsInitial;
  private final ListProxy mPropositionListProxy;


  //#########################################################################
  //# Local Class StateProxyFactory
  static class StateProxyFactory implements ProxyFactory
  {

    //#######################################################################
    //# Constructor
    StateProxyFactory(final ProxyFactory eventfactory)
    {
      mEventFactory = eventfactory;
    }

    //#######################################################################
    //# Interface waters.model.base.ProxyFactory
    public Proxy createProxy(final ElementType element)
      throws ModelException
    {
      final StateType state = (StateType) element;
      return new StateProxy(state, mEventFactory);
    }
    
    public List getList(final ElementType parent)
    {
      final StateListType list = (StateListType) parent;
      return list.getList();
    }

    //#######################################################################
    //# Data Members
    private final ProxyFactory mEventFactory;

  }


  //#########################################################################
  //# Local Class StateElementFactory
  static class StateElementFactory extends DESElementFactory
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ElementFactory
    public ElementType createElement(final Proxy proxy)
      throws JAXBException
    {
      return getFactory().createState();
    }

    public ElementType createContainerElement()
      throws JAXBException
    {
      return getFactory().createStateList();
    }

    public List getElementList(final ElementType container)
    {
      final StateListType list = (StateListType) container;
      return list.getList();
    }

  }

}
