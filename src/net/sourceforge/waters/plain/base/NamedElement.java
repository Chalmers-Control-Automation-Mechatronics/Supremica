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
