//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   IdentifierSubject
//###########################################################################
//# $Id: IdentifierSubject.java,v 1.8 2006-09-06 11:52:21 robi Exp $
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
   * @param plainText The original text of the new identifier, or <CODE>null</CODE>.
   * @param name The name of the new identifier.
   */
  protected IdentifierSubject(final String plainText,
                              final String name)
  {
    super(plainText);
    mName = name;
  }

  /**
   * Creates a new identifier using default values.
   * This constructor creates an identifier with
   * the original text set to <CODE>null</CODE>.
   * @param name The name of the new identifier.
   */
  protected IdentifierSubject(final String name)
  {
    this(null,
         name);
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

  public boolean equalsWithGeometry(final Proxy partner)
  {
    if (super.equalsWithGeometry(partner)) {
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

  public int hashCodeWithGeometry()
  {
    int result = super.hashCodeWithGeometry();
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
