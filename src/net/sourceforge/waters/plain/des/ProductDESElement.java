//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.des
//# CLASS:   ProductDESElement
//###########################################################################
//# $Id: ProductDESElement.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.des;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.IndexedHashSet;
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
   * @param  location     The file name to be associated with the new
   *                      document, or <CODE>null</CODE>.
   * @param  events       The event alphabet for the new product DES.
   * @param  automata     The set of transitions for the new product DES.
   * @throws DuplicateNameException to indicate that some event or automaton
   *                      name is used more than once.
   * @throws NameNotFoundException to indicate that some automaton refers
   *                      to an with an unknown name.
   * @throws ItemNotFoundException to indicate that some automaton uses
   *                      an event object that does not belong
   *                      to the given set of events.
   */
  ProductDESElement(final String name,
                    final File location,
                    final Collection<? extends EventProxy> events,
                    final Collection<? extends AutomatonProxy> automata)
  {
    super(name, location);
    final EventSet eventscopy = new EventSet(events);
    final AutomataSet automatacopy = new AutomataSet(automata.size());
    for (final AutomatonProxy aut : automata) {
      eventscopy.checkAllUnique(aut.getEvents());
      automatacopy.insertUnique(aut);
    }
    mEvents = Collections.unmodifiableSet(eventscopy);
    mAutomata = Collections.unmodifiableSet(automatacopy);
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
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final ProductDESElement des = (ProductDESElement) partner;
      return
        mEvents.equals(des.mEvents) &&
        mAutomata.equals(des.mAutomata);
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Local Class EventSet
  private class EventSet extends IndexedHashSet<EventProxy> {

    //#######################################################################
    //# Constructor
    EventSet(final Collection<? extends EventProxy> events)
      throws DuplicateNameException
    {
      super(events);
    }

    //#######################################################################
    //# Overrides from abstract class HashSetProxy
    protected ItemNotFoundException createItemNotFound(final String name)
    {
      return new ItemNotFoundException
        ("ProductDES '" + getName() +
         "' does not contain the event named '" + name + "'!");
    }

    protected NameNotFoundException createNameNotFound(final String name)
    {
      return new NameNotFoundException
        ("ProductDES '" + getName() +
         "' does not contain an event named '" + name + "'!");
    }

    protected DuplicateNameException createDuplicateName(final String name)
    {
      return new DuplicateNameException
        ("ProductDES '" + getName() +
         "' already contains an event named '" + name + "'!");
    }
  
  }


  //#########################################################################
  //# Local Class AutomataSet
  private class AutomataSet extends IndexedHashSet<AutomatonProxy> {

    //#######################################################################
    //# Constructor
    AutomataSet(final int size)
    {
      super(size);
    }

    //#######################################################################
    //# Overrides from abstract class HashSetProxy
    protected ItemNotFoundException createItemNotFound(final String name)
    {
      return new ItemNotFoundException
        ("Product DES '" + getName() +
         "' does not contain the state named '" + name + "'!");
    }

    protected NameNotFoundException createNameNotFound(final String name)
    {
      return new NameNotFoundException
        ("Product DES '" + getName() +
         "' does not contain a state named '" + name + "'!");
    }

    protected DuplicateNameException createDuplicateName(final String name)
    {
      return new DuplicateNameException
        ("Product DES '" + getName() +
         "' already contains a state named '" + name + "'!");
    }
  
  }


  //#########################################################################
  //# Data Members
  private final Set<EventProxy> mEvents;
  private final Set<AutomatonProxy> mAutomata;

}
