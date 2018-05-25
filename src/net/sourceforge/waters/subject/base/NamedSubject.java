//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.subject.base;

import java.util.Set;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.IndexedCollection;
import net.sourceforge.waters.model.base.ItemNotFoundException;
import net.sourceforge.waters.model.base.NamedProxy;


/**
 * <P>A mutable implementation of the {@link NamedSubject} interface.</P>
 *
 * <P>This abstract base class can be used to implement elements with a
 * mutable name attribute of type {@link String}. This implementation
 * provides a {@link #setName(String) setName()} to modify the name.</P>
 *
 * @author Robi Malik
 */

public abstract class NamedSubject
  extends AbstractNamedSubject
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a named element.
   * @param  name        The name of the new element.
   */
  protected NamedSubject(final String name)
  {
    mName = name;
  }

  /**
   * Creates a copy of a named element.
   * @param  partner     The object to be copied from.
   */
  protected NamedSubject(final NamedProxy partner)
  {
    super(partner);
    mName = partner.getName();
  }


  //#########################################################################
  //# Cloning and Assigning
  @Override
  public NamedSubject clone()
  {
    return (NamedSubject) super.clone();
  }

  @Override
  public ModelChangeEvent assignMember(final int index,
                                       final Object oldValue,
                                       final Object newValue)
  {
    switch (index) {
    case 1:
      final String name = (String) newValue;
      return assignName(name);
    default:
      return null;
    }
  }

  @Override
  protected void collectUndoInfo(final ProxySubject newState,
                                 final RecursiveUndoInfo info,
                                 final Set<? extends Subject> boundary)
  {
    super.collectUndoInfo(newState, info, boundary);
    final NamedSubject named = (NamedSubject) newState;
    if (!named.getName().equals(mName)) {
      final UndoInfo step = new ReplacementUndoInfo(1, mName, named.getName());
      info.add(step);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.NamedProxy
  @Override
  public String getName()
  {
    return mName;
  }

  @Override
  public boolean refequals(final NamedProxy partner)
  {
    return getName().equals(partner.getName());
  }


  //#########################################################################
  //# Setters
  /**
   * Changes the name of this element.
   * This method sets the name of this element to the given value, and
   * checks whether its parent is a set indexed by names. If so, it
   * reinserts this element into its parent. This ensures that the
   * consistency of this one symbol table. If the element has been added to
   * other hash tables or sorted maps, it is the user's responsibility to
   * change them.
   * @param  name        The new name.
   * @throws DuplicateNameException to indicate that the name could not
   *                     be changed because the parent is an indexed set
   *                     that already contains another element with the
   *                     same name.
   */
  public void setName(final String name)
    throws DuplicateNameException, ItemNotFoundException
  {
    final ModelChangeEvent event = assignName(name);
    if (event != null) {
      event.fire();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private ModelChangeEvent assignName(final String name)
  {
    if (mName.equals(name)) {
      return null;
    } else {
      final Subject parent = getParent();
      if (parent instanceof IndexedCollection<?>) {
        final IndexedCollection<?> collection = (IndexedCollection<?>) parent;
        collection.reinsert(this, name);
      }
      final ModelChangeEvent event =
        ModelChangeEvent.createNameChanged(this, mName);
      mName = name;
      return event;
    }
  }


  //#########################################################################
  //# Data Members
  private String mName;

}
