//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

import java.io.Serializable;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.printer.ProxyPrinter;


/**
 * <P>The common base class for all Waters elements in the <I>plain</I>
 * implementation.</P>
 *
 * <P>This is the abstract base class of all non-aggregrate Waters elements
 * in the <I>plain</I> implementation. It provides the basic functionality
 * of an immutable object.</P>
 *
 * @author Robi Malik
 */

public abstract class Element
  implements Proxy, Cloneable, Serializable
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty element.
   */
  protected Element()
  {
  }

  /**
   * Creates a copy of an element.
   * @param  partner     The object to be copied from.
   */
  protected Element(final Proxy partner)
  {
  }


  //#########################################################################
  //# Cloning
  /**
   * Creates and returns a copy of this element.
   * Immutable objects of the plain implementation are cloned by shallow
   * copying. All contained objects are shared between an element and its
   * clones.
   */
  @Override
  public Proxy clone()
  {
    try {
      return (Element) super.clone();
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Printing
  @Override
  public String toString()
  {
    return ProxyPrinter.getPrintString(this);
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
