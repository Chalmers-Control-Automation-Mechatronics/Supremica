//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   IdentifierElement
//###########################################################################
//# $Id: IdentifierElement.java,v 1.6 2006-09-06 11:52:21 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.base.Proxy;
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
   * @param plainText The original text of the new identifier, or <CODE>null</CODE>.
   * @param name The name of the new identifier.
   */
  protected IdentifierElement(final String plainText,
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
  protected IdentifierElement(final String name)
  {
    this(null,
         name);
  }


  //#########################################################################
  //# Cloning
  public IdentifierElement clone()
  {
    return (IdentifierElement) super.clone();
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final IdentifierElement downcast = (IdentifierElement) partner;
      return
        mName.equals(downcast.mName);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Proxy partner)
  {
    if (super.equalsWithGeometry(partner)) {
      final IdentifierElement downcast = (IdentifierElement) partner;
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
  //# Data Members
  private final String mName;

}
