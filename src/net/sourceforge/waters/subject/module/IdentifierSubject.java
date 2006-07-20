//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   IdentifierSubject
//###########################################################################
//# $Id: IdentifierSubject.java,v 1.7 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;


/**
 * The subject implementation of the {@link IdentifierProxy} interface.
 *
 * @author Robi Malik
 */

public abstract class IdentifierSubject
  extends SimpleExpressionSubject
  implements IdentifierProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new identifier.
   * @param name The name of the new identifier.
   */
  protected IdentifierSubject(final String name)
  {
    mName = name;
  }


  //#########################################################################
  //# Cloning
  public IdentifierSubject clone()
  {
    final IdentifierSubject cloned = (IdentifierSubject) super.clone();
    return cloned;
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final IdentifierSubject downcast = (IdentifierSubject) partner;
      return
        mName.equals(downcast.mName);
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mName.hashCode();
    return result;
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
  //# Setters
  public void setName(final String name)
  {
    if (mName.equals(name)) {
      return;
    }
    mName = name;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Data Members
  private String mName;

}
