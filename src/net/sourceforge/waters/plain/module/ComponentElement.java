//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   ComponentElement
//###########################################################################
//# $Id: ComponentElement.java,v 1.6 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;


/**
 * An immutable implementation of the {@link ComponentProxy} interface.
 *
 * @author Robi Malik
 */

public abstract class ComponentElement
  extends IdentifiedElement
  implements ComponentProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new component.
   * @param identifier The identifier defining the name of the new component.
   */
  protected ComponentElement(final IdentifierProxy identifier)
  {
    super(identifier);
  }


  //#########################################################################
  //# Cloning
  public ComponentElement clone()
  {
    return (ComponentElement) super.clone();
  }

}
