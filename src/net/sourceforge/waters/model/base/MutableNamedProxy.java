//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.base
//# CLASS:   MutableNamedProxy
//###########################################################################
//# $Id: MutableNamedProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import javax.xml.bind.JAXBException;

import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.base.NamedType;


/**
 * <P>A mutable implementation of the {@link NamedProxy} interface.</P>
 *
 * <P>This abstract base class can be used to implement elements with a
 * mutable name attribute of type {@link String}. This implementation
 * provides a {@link #setName(String) setName()} to modify the name. The
 * name attribute is not used to calculate hash codes or an ordering, so
 * hash tables or ordered lists cannot become inconsistent by name
 * changes.</P>
 *
 * @see ImmutableNamedProxy
 * @see UniqueElementProxy
 *
 * @author Robi Malik
 */
/**
 * <P>The common base class of all Waters elements that are identified
 * by a name.</P>
 *
 * <P>Several elements in Waters have got some kind of name that is used to
 * find them within some list or set. This abstract base class provides a
 * uniform implementation of elements whose name is a {@link String}.</P>
 *
 * @author Robi Malik
 */

public abstract class MutableNamedProxy
  extends ElementProxy
  implements NamedProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a named element.
   * @param  name        The name of the new element.
   */
  protected MutableNamedProxy(final String name)
  {
    mName = name;
  }

  /**
   * Creates a named element from a parsed XML structure.
   * @param  element     The parsed XML structure representing the
   *                     element to be created.
   */
  protected MutableNamedProxy(final NamedType element)
  {
    mName = element.getName();
  }

  /**
   * Creates a copy of a named element.
   * @param  partner     The object to be copied from.
   */
  protected MutableNamedProxy(final MutableNamedProxy partner)
  {
    super(partner);
    mName = partner.mName;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.NamedProxy
  public String getName()
  {
    return mName;
  }

  public boolean refequals(final NamedProxy partner)
  {
    return getName().equals(partner.getName());
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final MutableNamedProxy named = (MutableNamedProxy) partner;
      return mName.equals(named.mName);
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Changing the Name
  /**
   * Sets the name of this element.
   */
  public void setName(final String name)
  {
    mName = name;
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    super.toJAXBElement(element);
    final NamedType named = (NamedType) element;
    named.setName(mName);
  }


  //#########################################################################
  //# Data Members
  private String mName;

}
