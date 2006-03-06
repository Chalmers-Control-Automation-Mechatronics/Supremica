//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   IdentifiedElement
//###########################################################################
//# $Id: IdentifiedElement.java,v 1.3 2006-03-06 17:08:46 markus Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.module.IdentifiedProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.plain.base.AbstractNamedElement;


/**
 * An immutable implementation of the {@link IdentifiedProxy} interface.
 *
 * @author Robi Malik
 */

public abstract class IdentifiedElement
  extends AbstractNamedElement
  implements IdentifiedProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new identified object.
   * @param identifier The identifier defining the name of the new identified object.
   */
  protected IdentifiedElement(final IdentifierProxy identifier)
  {
    mIdentifier = identifier;
  }


  //#########################################################################
  //# Cloning
  public IdentifiedElement clone()
  {
    return (IdentifiedElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final IdentifiedElement downcast = (IdentifiedElement) partner;
      return
        mIdentifier.equals(downcast.mIdentifier);
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.IdentifiedProxy
  public IdentifierProxy getIdentifier()
  {
    return mIdentifier;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.NamedProxy
  public String getName()
  {
    return mIdentifier.getName();
  }


  //#########################################################################
  //# Data Members
  private final IdentifierProxy mIdentifier;

}
