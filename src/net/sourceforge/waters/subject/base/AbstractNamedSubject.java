//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   AbstractNamedSubject
//###########################################################################
//# $Id: AbstractNamedSubject.java,v 1.4 2007-07-03 11:20:53 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.base;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.NamedProxy;


/**
 * <P>An abstract implementation of the {@link NamedProxy} interface.</P>
 *
 * <P>This is an abstract skeleton used to implement subjects with an name
 * attribute of type {@link String}. Although this implementation is not
 * necessarily immutable, the name attribute is used to calculate hash
 * codes and ordering without affecting any hash tables or ordered
 * lists. While not actually providing the name member, this abstract base
 * class uses the {@link #getName()} method to provide implementations for
 * the {@link #refequals(NamedProxy) refequals()}, {@link
 * #equalsByContents(Proxy) equals()}, {@link #hashCodeByContents()}, and
 * {@link #compareTo(NamedProxy) compareTo()} methods.</P>
 *
 * @author Robi Malik
 */

public abstract class AbstractNamedSubject
  extends MutableSubject
  implements NamedProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a named subject.
   */
  protected AbstractNamedSubject()
  {
  }

  /**
   * Creates a copy of a named subject.
   * @param  partner     The object to be copied from.
   */
  protected AbstractNamedSubject(final NamedProxy partner)
  {
    super(partner);
  }


  //#########################################################################
  //# Cloning
  public AbstractNamedSubject clone()
  {
    return (AbstractNamedSubject) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.NamedProxy
  public boolean refequals(final NamedProxy partner)
  {
    return getName().equals(partner.getName());
  }


  //#########################################################################
  //# Equals and Hashcode
  /**
   * Checks whether two subjects are equal. This method implements
   * content-based equality, i.e., two subjects will be equal if their
   * contents are the same. In addition to calling the superclass
   * method, this method compares the names of the two subjects.
   */
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final NamedProxy named = (NamedProxy) partner;
      return getName().equals(named.getName());
    } else {
      return false;
    }
  }

  public int refHashCode()
  {
    return getName().hashCode();
  }

  /**
   * Computes a hash code based on this object's contents.
   * This method uses the subject's name to calculate the hash code.
   */
  public int hashCodeByContents()
  {
    return super.hashCodeByContents() + 5 * getName().hashCode();
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  /**
   * Compares this named subject with another.
   * This method compares subjects only based on their names.
   */
  public int compareTo(final NamedProxy partner)
  {
    final String name = getName();
    final String partnername = partner.getName();
    final int result = name.compareToIgnoreCase(partnername);
    if (result != 0) {
      return result;
    }
    return name.compareTo(partnername);
  }

}
