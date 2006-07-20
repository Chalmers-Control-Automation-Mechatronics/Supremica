//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   IdentifiedSubject
//###########################################################################
//# $Id: IdentifiedSubject.java,v 1.6 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.IdentifiedProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.subject.base.AbstractNamedSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;


/**
 * The subject implementation of the {@link IdentifiedProxy} interface.
 *
 * @author Robi Malik
 */

public abstract class IdentifiedSubject
  extends AbstractNamedSubject
  implements IdentifiedProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new identified object.
   * @param identifier The identifier defining the name of the new identified object.
   */
  protected IdentifiedSubject(final IdentifierProxy identifier)
  {
    mIdentifier = (IdentifierSubject) identifier;
    mIdentifier.setParent(this);
  }


  //#########################################################################
  //# Cloning
  public IdentifiedSubject clone()
  {
    final IdentifiedSubject cloned = (IdentifiedSubject) super.clone();
    cloned.mIdentifier = mIdentifier.clone();
    cloned.mIdentifier.setParent(cloned);
    return cloned;
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final IdentifiedSubject downcast = (IdentifiedSubject) partner;
      return
        mIdentifier.equalsByContents(downcast.mIdentifier);
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mIdentifier.hashCodeByContents();
    return result;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.IdentifiedProxy
  public IdentifierSubject getIdentifier()
  {
    return mIdentifier;
  }


  //#########################################################################
  //# Setters
  /**
   * Sets the identifier defining the name of this object.
   */
  public void setIdentifier(final IdentifierSubject identifier)
  {
    if (mIdentifier == identifier) {
      return;
    }
    identifier.setParent(this);
    mIdentifier.setParent(null);
    mIdentifier = identifier;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.NamedProxy
  public String getName()
  {
    return mIdentifier.getName();
  }


  //#########################################################################
  //# Data Members
  private IdentifierSubject mIdentifier;

}
