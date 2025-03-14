//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
  public void checkUnique(NamedProxy proxy);

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
   * @return <CODE>true</CODE> if the collection changed as a result of
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
