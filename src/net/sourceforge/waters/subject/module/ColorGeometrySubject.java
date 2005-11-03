//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   ColorGeometrySubject
//###########################################################################
//# $Id: ColorGeometrySubject.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.subject.base.GeometrySubject;
import net.sourceforge.waters.subject.base.NotCloningGeometrySetSubject;
import net.sourceforge.waters.subject.base.SimpleSetSubject;


/**
 * The subject implementation of the {@link ColorGeometryProxy} interface.
 *
 * @author Robi Malik
 */

public final class ColorGeometrySubject
  extends GeometrySubject
  implements ColorGeometryProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new color geometry.
   * @param colorSet The colour set of the new color geometry.
   */
  public ColorGeometrySubject(final Collection<? extends Color> colorSet)
  {
    mColorSet = new NotCloningGeometrySetSubject<Color>(colorSet);
    mColorSet.setParent(this);
  }

  /**
   * Creates a new color geometry using default values.
   * This constructor creates a color geometry with
   * an empty colour set.
   */
  public ColorGeometrySubject()
  {
    this(emptyColorSet());
  }


  //#########################################################################
  //# Cloning
  public ColorGeometrySubject clone()
  {
    final ColorGeometrySubject cloned = (ColorGeometrySubject) super.clone();
    cloned.mColorSet = mColorSet.clone();
    cloned.mColorSet.setParent(cloned);
    return cloned;
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final ColorGeometrySubject downcast = (ColorGeometrySubject) partner;
      return
        mColorSet.equals(downcast.mColorSet);
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitColorGeometryProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ColorGeometryProxy
  public Set<Color> getColorSet()
  {
    final Set<Color> downcast = Casting.toSet(mColorSet);
    return Collections.unmodifiableSet(downcast);
  }


  //#########################################################################
  //# Setters
  /**
   * Gets the modifiable colour set identifying this colour geometry.
   */
  public SimpleSetSubject<Color> getColorSetModifiable()
  {
    return mColorSet;
  }


  //#########################################################################
  //# Auxiliary Methods
  private static Set<Color> emptyColorSet()
  {
    return Collections.emptySet();
  }


  //#########################################################################
  //# Data Members
  private SimpleSetSubject<Color> mColorSet;

}
