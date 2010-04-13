//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.des
//# CLASS:   ProductDESElement
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.plain.des;

import java.net.URI;
import java.util.Collection;
import java.util.Set;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.ImmutableOrderedSet;
import net.sourceforge.waters.model.base.ItemNotFoundException;
import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.plain.base.DocumentElement;


/**
 * A system of finite-state machines.
 * This is a simple immutable implementation of the {@link ProductDESProxy}
 * interface.
 *
 * @author Robi Malik
 */

public class ProductDESElement
  extends DocumentElement
  implements ProductDESProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new product DES.
   * @param  name         The name to be given to the new product DES.
   * @param  comment      The comment for the new product DES.
   * @param  location     The URI to be associated with the new
   *                      document, or <CODE>null</CODE>.
   * @param  events       The event alphabet for the new product DES,
   *                      or <CODE>null</CODE> if empty.
   * @param  automata     The set of automata for the new product DES,
   *                      or <CODE>null</CODE> if empty.
   * @throws DuplicateNameException to indicate that some automaton, event,
   *                      or state name is used more than once.
   */
  ProductDESElement(final String name,
                    final String comment,
                    final URI location,
                    final Collection<? extends EventProxy> events,
                    final Collection<? extends AutomatonProxy> automata)
  {
    super(name, comment, location);
    mEvents = new EventSet(events);
    mAutomata = new AutomataSet(automata);
  }

  /**
   * Creates a new product DES using default values.
   * This constructor creates a product DES with a <CODE>null</CODE> file
   * location, and with no associated comment.
   * @param  name         The name to be given to the new product DES.
   * @param  events       The event alphabet for the new product DES,
   *                      or <CODE>null</CODE> if empty.
   * @param  automata     The set of automata for the new product DES,
   *                      or <CODE>null</CODE> if empty.
   * @throws DuplicateNameException to indicate that some event or automaton
   *                      name is used more than once.
   * @throws NameNotFoundException to indicate that some automaton refers
   *                      to an with an unknown name.
   * @throws ItemNotFoundException to indicate that some automaton uses
   *                      an event object that does not belong
   *                      to the given set of events.
   */
  ProductDESElement(final String name,
                    final Collection<? extends EventProxy> events,
                    final Collection<? extends AutomatonProxy> automata)
  {
    this(name, null, null, events, automata);
  }


  /**
   * Creates a new product DES using default values.
   * This constructor creates a product DES with a <CODE>null</CODE> file
   * location, and empty lists of events and automata.
   * @param  name         The name to be given to the new product DES.
   * @throws DuplicateNameException to indicate that some event or automaton
   *                      name is used more than once.
   * @throws NameNotFoundException to indicate that some automaton refers
   *                      to an with an unknown name.
   * @throws ItemNotFoundException to indicate that some automaton uses
   *                      an event object that does not belong
   *                      to the given set of events.
   */
  ProductDESElement(final String name)
  {
    this(name, null, null, null, null);
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  /**
   * Returns a copy of this product DES.
   */
  public ProductDESElement clone()
  {
    return (ProductDESElement) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
    return desvisitor.visitProductDESProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.ProductDESProxy
  /**
   * Gets the event alphabet for this product DES.
   * This method returns the set of events on which this automaton
   * synchronises, or the set of all events that can occur on its
   * transitions.
   * @return  An unmodifiable set of objects of type {@link EventProxy}.
   */
  public Set<EventProxy> getEvents()
  {
    return mEvents;
  }

  /**
   * Gets the set of automata for this product DES.
   * @return  An unmodifiable set of objects of type {@link AutomatonProxy}.
   */
  public Set<AutomatonProxy> getAutomata()
  {
    return mAutomata;
  }


  //#########################################################################
  //# Equals and Hashcode
  public Class<ProductDESProxy> getProxyInterface()
  {
    return ProductDESProxy.class;
  }


  //#########################################################################
  //# Local Class EventSet
  private class EventSet extends ImmutableOrderedSet<EventProxy>
  {

    //#######################################################################
    //# Constructors
    EventSet(final Collection<? extends EventProxy> events)
    {
      super(events);
    }

    //#######################################################################
    //# Overrides from base class ImmutableOrderedSet
    protected DuplicateNameException createDuplicateName(final String name)
    {
      return new DuplicateNameException
        ("Product DES '" + getName() +
         "' already contains an event named '" + name + "'!");
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

  }


  //#########################################################################
  //# Local Class AutomataSet
  private class AutomataSet extends ImmutableOrderedSet<AutomatonProxy>
  {

    //#######################################################################
    //# Constructor
    AutomataSet(final Collection<? extends AutomatonProxy> automata)
    {
      super(automata);
    }

    //#######################################################################
    //# Overrides from base class ImmutableOrderedSet
    protected DuplicateNameException createDuplicateName(final String name)
    {
      return new DuplicateNameException
        ("Product DES '" + getName() +
         "' already contains an automaton named '" + name + "'!");
    }

    //#########################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

  }


  //#########################################################################
  //# Data Members
  private final Set<EventProxy> mEvents;
  private final Set<AutomatonProxy> mAutomata;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
