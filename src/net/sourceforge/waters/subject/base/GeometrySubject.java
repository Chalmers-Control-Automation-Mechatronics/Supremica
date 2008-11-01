//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   GeometrySubject
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.subject.base;

import net.sourceforge.waters.model.base.GeometryProxy;


/**
 * <P>The common base class for all Waters mutable elements in the
 * <I>subject</I> implementation.</P>
 *
 * <P>This is the abstract base class of all mutable Waters elements
 * in the <I>subject</I> implementation. It provides the basic functionality
 * of a mutable object.</P>
 * 
 * @author Robi Malik
 */

public abstract class GeometrySubject
  extends MutableSubject
  implements GeometryProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty geometry subject.
   */
  protected GeometrySubject()
  {
  }

  /**
   * Creates a copy of a geometry subject.
   * @param  partner     The object to be copied from.
   */
  protected GeometrySubject(final GeometryProxy partner)
  {
    super(partner);
  }


  //#########################################################################
  //# Cloning and Assigning
  public GeometrySubject clone()
  {
    return (GeometrySubject) super.clone();
  }


  //#########################################################################
  //# Convenience Methods
  protected void fireStateChanged()
  {
    fireGeometryChanged();
  }

  protected void fireGeometryChanged()
  {
    final Subject parent = getParent();
    final Subject source = parent == null ? this : parent;
    final ModelChangeEvent event =
      ModelChangeEvent.createGeometryChanged(source, this);
    fireModelChanged(event);
  }

}
