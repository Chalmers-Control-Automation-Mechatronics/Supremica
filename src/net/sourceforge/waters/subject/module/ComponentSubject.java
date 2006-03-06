//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   ComponentSubject
//###########################################################################
//# $Id: ComponentSubject.java,v 1.4 2006-03-06 17:08:46 markus Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;


/**
 * The subject implementation of the {@link ComponentProxy} interface.
 *
 * @author Robi Malik
 */

public abstract class ComponentSubject
  extends IdentifiedSubject
  implements ComponentProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new component.
   * @param identifier The identifier defining the name of the new component.
   */
  protected ComponentSubject(final IdentifierProxy identifier)
  {
    super(identifier);
  }


  //#########################################################################
  //# Cloning
  public ComponentSubject clone()
  {
    return (ComponentSubject) super.clone();
  }

}
