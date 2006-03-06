//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   IdentifierElement
//###########################################################################
//# $Id: IdentifierElement.java,v 1.3 2006-03-06 17:08:46 markus Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.module.IdentifierProxy;


/**
 * An immutable implementation of the {@link IdentifierProxy} interface.
 *
 * @author Robi Malik
 */

public abstract class IdentifierElement
  extends SimpleExpressionElement
  implements IdentifierProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new identifier.
   * @param name The name of the new identifier.
   */
  protected IdentifierElement(final String name)
  {
    mName = name;
  }


  //#########################################################################
  //# Cloning
  public IdentifierElement clone()
  {
    return (IdentifierElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final IdentifierElement downcast = (IdentifierElement) partner;
      return
        mName.equals(downcast.mName);
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.IdentifierProxy
  public String getName()
  {
    return mName;
  }


  //#########################################################################
  //# Interface java.lang.Comparable<IdentifierProxy>
  public int compareTo(final IdentifierProxy partner)
  {
    return getName().compareTo(partner.getName());
  }


  //#########################################################################
  //# Data Members
  private final String mName;

}
