//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.base
//# CLASS:   NamedElement
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.plain.base;

import net.sourceforge.waters.model.base.NamedProxy;


/**
 * <P>An immutable implementation of the {@link NamedProxy} interface.</P>
 *
 * <P>This abstract base class can be used to implement elements with an
 * immutable name attribute of type {@link String}. Immutability means that
 * the name cannot be changed once an element is constructed. Therefore,
 * the name attribute can be used to calculate hash codes and ordering without
 * affecting any hash tables or ordered lists.</P>
 *
 * @author Robi Malik
 */

public abstract class NamedElement
  extends AbstractNamedElement
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a named element.
   * @param  name        The name of the new element.
   */
  protected NamedElement(final String name)
  {
    mName = name;
  }

  /**
   * Creates a copy of a named element.
   * @param  partner     The object to be copied from.
   */
  protected NamedElement(final NamedProxy partner)
  {
    super(partner);
    mName = partner.getName();
  }


  //#########################################################################
  //# Cloning
  public NamedElement clone()
  {
    return (NamedElement) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.NamedProxy
  public String getName()
  {
    return mName;
  }


  //#########################################################################
  //# Data Members
  private final String mName;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
