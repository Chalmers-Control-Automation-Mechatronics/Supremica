//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   IndexedCollection
//###########################################################################
//# $Id: IndexedCollection.java,v 1.2 2005-11-03 01:24:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Collection;


/**
 * <P>A set that uses the names of Waters elements as an index.</P>
 *
 * @author Robi Malik
 */

public interface IndexedCollection<P extends NamedProxy>
  extends Collection<P>
{

  //#########################################################################
  //# Specific Access Methods
  /**
   * Checks whether this collection contains a collection of items.
   * This method can be used when setting up a symbol table to ensure
   * that no duplicate items have been created.
   * @param  collection  The collection of items to be checked.
   * @throws NameNotFoundException to indicate that the collection does not
   *                     contain some item with the same name as some
   *                     member of the input collection.
   * @throws ItemNotFoundException to indicate that the collection contains
   *                     an item with the same name as some
   *                     member of the input collection, which
   *                     is not the same object as that member.
   */
  public void checkAllUnique(Collection<? extends P> collection);

  /**
   * Checks whether this collection contains an item.
   * This method can be used when setting up a symbol table to ensure
   * that no duplicate items have been created.
   * @param  proxy       The item to be checked.
   * @throws NameNotFoundException to indicate that the collection does not
   *                     contain any item with the same name as the argument.
   * @throws ItemNotFoundException to indicate that the collection contains
   *                     an item with the same name as the argument, which
   *                     is not the same object as the argument.
   */
  public void checkUnique(P proxy);

  /**
   * Checks whether this collection contains an item with a given name.
   * @param  name        The name to be checked.
   * @return <CODE>true</CODE> if the collection contains an item with the
   *         given name.
   */
  public boolean containsName(String name);

  /**
   * Gets the item with the given name in this collection.
   * In contrast to the {@link #get(String) get()} method, this method does
   * not return <CODE>null</CODE> if the collection does not contain any
   * item with the given name, but throws an exception.
   * @param  name        The name of the item to be retrieved.
   * @return The item with the given name.
   * @throws NameNotFoundException to indicate that the
   *         collection does not contain any item with the given name.
   */
  public P find(String name);

  /**
   * Gets the item with the given name in this collection.
   * @param  name        The name of the item to be retrieved.
   * @return The item with the given name, or <CODE>null</CODE> if the
   *         collection does not contain any such item.
   */
  public P get(String name);

  /**
   * Adds an item to this collection. This method makes sure that the
   * collection contains an item equal to the given argument. If it does
   * not yet contain such an item, it will attempt to add the argument.
   * @param  proxy       The item to be added.
   * @return The item equal to the argument that is in the collection.
   *         This may be the argument itself or another object equal to it
   *         that was there before.
   * @throws DuplicateNameException to indicate that the item could not be
   *                     added because the collection already contains a
   *                     different item with the same name.
   */
  public P insert(P proxy);

  /**
   * Adds a set of items to this collection. This method makes sure that
   * the collection contains items equal to all the members of the given
   * collection. It will attempt to add items not yet contained.
   * @param  collection  A list of items to be added.
   * @return <CODE>true</CODE> of the collection changed as a result of
   *         this call.
   * @throws DuplicateNameException to indicate that some item could not be
   *                added because the collection already contains a different
   *                item with the same name.
   */
  public boolean insertAll(Collection<? extends P> collection);

  /**
   * Adds a set of items to this collection. In contrast to the {@link
   * #insertAll(Collection) insertAll()} method, this method
   * does not allow an item to be added if the collection already contains
   * an item with the same name, no matter whether it is equal to the given
   * argument or not. If some name is already taken, a {@link
   * DuplicateNameException} is thrown.
   * @param  collection  A list of items to be added.
   * @throws DuplicateNameException to indicate that some item could not be
   *                added because the collection already contains an
   *                item with the same name.
   */
  public void insertAllUnique(Collection<? extends P> collection);

  /**
   * Adds an item to this collection. In contrast to the {@link
   * #insert(NamedProxy) insert()} method, this method does not allow an
   * item to be added if the collection already contains an item with the
   * same name, no matter whether it is equal to the given argument or
   * not. If the name is already taken, a {@link DuplicateNameException} is
   * thrown.
   * @param  proxy       The item to be added.
   * @throws DuplicateNameException to indicate that the item could not be
   *                     added because the collection already contains an
   *                     item with the same name.
   */
  public void insertUnique(P proxy);

  /**
   * <P>Reinserts an item into this collection to see whether its name can be
   * changed. This method removes the given item from this collection and
   * inserts it under the given new name. The name of the item must be
   * unchanged prior to this call.</P>
   * <P>This method is intended to be called prior to changing the name
   * of an item in a <CODE>setName()</CODE> method. An exception will be
   * thrown if the item cannot be renamed in this collection. If this
   * method returns successfully, the caller should change the item's
   * name to restore consistency.</P>
   * @param  proxy       The item to be renamed.
   * @param  newname     The new name for the item.
   * @throws DuplicateNameException to indicate that the item cannot be
   *                     renamed because the collection already contains
   *                     another item with the same name.
   * @throws ItemNotFoundException to indicate that this collection does
   *                     not contain the indicated item.
   */
  public void reinsert(NamedProxy proxy, String newname);

  /**
   * Removes the item with the given name from this collection.
   * @param  name        The name of the item to be removed.
   * @return The item that was removed, or <CODE>null</CODE> if the
   *         collection did not contain any item with the given name.
   */
  public P removeName(String name);

}
