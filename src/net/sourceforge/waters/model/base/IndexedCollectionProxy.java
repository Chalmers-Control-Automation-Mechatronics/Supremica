//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.base
//# CLASS:   IndexedCollectionProxy
//###########################################################################
//# $Id: IndexedCollectionProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Collection;
import java.util.Map;


/**
 * <P>A collection of {@link NamedProxy} objects.</P>
 *
 * <P>This interface represents a collection of named elements that can
 * be accessed both through the standard {@link Collection} interface, and
 * through more specialised methods enabling quick access based on
 * names. It also includes some more sophisticated exception handling.</P>
 *
 * <P>Implementations of this interface can be used like a collection with
 * the restriction that they only accept elements of type {@link
 * NamedProxy}. Furthermore, these elements will be indexed using
 * their names as obtained by the {@link NamedProxy#getName()}
 * method. Implementations must ensure that there cannot be two different
 * elements with the same in a collection at the same time, where the
 * {@link java.lang.Object#equals(Object) equals()} method is used to
 * determine whether two elements are to be considered as equal.</P>
 *
 * @author Robi Malik
 */

public interface IndexedCollectionProxy extends Collection, Proxy {

  //#########################################################################
  //# Initialisation
  /**
   * Initialise the collection with a set of JAXB elements.
   * This method creates proxies for all elements of the given
   * collection and adds them to this collection. This method is
   * intended for internal use during marshalling only.
   * @param  elist      List of elements to be added,
   *                    each element should be of type
   *                    {@link net.sourceforge.waters.xsd.base.ElementType}.
   * @param  factory    A factory used to create ElementProxy objects
   *                    from the members of elist.
   * @throws DuplicateNameException to indicate that it was attempted
   *                    to create two elements with the same name in this
   *                    collection.
   * @throws ModelException to indicate that proxy creation has failed
   *                    for one of the elements.
   */
  public void init(final Collection elist, final ProxyFactory factory)
    throws ModelException;


  //#########################################################################
  //# Accessing the Collection
  /**
   * Ensures that this collection contains the specified element.
   * @throws IllegalArgumentException to indicate that the element could
   *                    not be added because of a duplicate name or some
   *                    other reason depending on the specific type of element.
   */
  public boolean add(final NamedProxy proxy);

  /**
   * Returns <CODE>true</CODE> if this collection contains an element with
   * the specified name.
   */
  public boolean containsName(final String name);

  /**
   * Returns <CODE>true</CODE> if this collection contains the specified
   * element.
   */
  public boolean contains(final NamedProxy proxy);

  /**
   * Returns the element to which this collection maps the specified name.
   * @param  name      The name of the item to be looked for.
   * @return The corresponding item found in the collection.
   * @throws NameNotFoundException to indicate that the collection
   *                   does not contain any item with the given name.
   */
  public NamedProxy find(final String name)
    throws NameNotFoundException;

  /**
   * Returns the element to which this collection maps the specified name,
   * or <CODE>null</CODE>.
   */
  public NamedProxy get(final String name);

  /**
   * Tries to add an element to this collection.
   * This method first checks whether the collection already contains a
   * different item with the same name, and fails if this is the case.  The
   * {@link java.lang.Object#equals(Object) equals()} method is used to
   * check whether elements are equal.
   * @param  proxy     The item to be added.
   * @return The item that is now contained in the collection. This may be
   *         the given item or another one that is equal and was there
   *         before.
   * @throws DuplicateNameException to indicate that the collection already
   *                   has a different item with the same name.
   * @throws ModelException to indicate that the element cannot be added
   *                   because of some reason depending on the specific type
   *                   of collection and element.
   */
  public NamedProxy insert
    (final NamedProxy proxy)
    throws ModelException;

  /**
   * Adds another collection to this collection.
   * This method calls the {@link #insert(NamedProxy)} method
   * for all elements of the given collection.
   * @param  collection A collection of elements to be added.
   *                    Each element should be of type
   *                    {@link NamedProxy}.
   * @return <CODE>true</CODE> if this collection changed as a result of the
   *         call.
   * @throws DuplicateNameException to indicate that it was attempted
   *                    to create two elements with the same name in this
   *                    collection.
   * @throws ModelException to indicate that some element cannot be added
   *                    because of some reason depending on the specific type
   *                    of collection and element.
   */
  public boolean insertAll(final Collection collection)
    throws ModelException;

  /**
   * Removes the element with a given name from this collection if present.
   * @param  name       The name to be removed.
   * @return The element that was removed, or <CODE>null</CODE> if no
   *         element with the given name was found.
   */
  public NamedProxy removeName(final String name);

  /**
   * Removes an element from this collection if present. More
   * formally, this method tries to remove an element that is equal to the
   * given element, if this collection contains such an element. It uses
   * {@link java.lang.Object#equals(Object) equals()} to compare elements.
   * @param  proxy      The element to be removed.
   * @return <CODE>true</CODE> if an element was removed,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean remove(final NamedProxy proxy);

  /**
   * Returns a map view of this collection.
   * This method produces an unmodifiable map that maps {@link String} objects
   * to {@link Proxy} objects.
   */
  public Map getMap();

}
