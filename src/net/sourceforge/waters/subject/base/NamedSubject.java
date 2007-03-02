//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   NamedSubject
//###########################################################################
//# $Id: NamedSubject.java,v 1.4 2007-03-02 05:21:14 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.base;

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
  public NamedSubject clone()
  {
    return (NamedSubject) super.clone();
  }

  public boolean assignFrom(final ProxySubject partner)
  {
    if (this != partner) {
      final boolean changed = super.assignFrom(partner);
      final NamedProxy named = (NamedSubject) partner;
      setName(named.getName());
      return changed;
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.NamedProxy
  public String getName()
  {
    return mName;
  }

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
    if (!mName.equals(name)) {
      final Subject parent = getParent();
      if (parent instanceof IndexedCollection) {
        final IndexedCollection collection = (IndexedCollection) parent;
        collection.reinsert(this, name);
      }
      final ModelChangeEvent event =
        ModelChangeEvent.createNameChanged(this, mName);
      mName = name;
      fireModelChanged(event);
    }
  }


  //#########################################################################
  //# Data Members
  private String mName;

}
