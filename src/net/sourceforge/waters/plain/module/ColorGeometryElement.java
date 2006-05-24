//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   ColorGeometryElement
//###########################################################################
//# $Id: ColorGeometryElement.java,v 1.5 2006-05-24 09:13:02 markus Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.plain.base.GeometryElement;


/**
 * An immutable implementation of the {@link ColorGeometryProxy} interface.
 *
 * @author Robi Malik
 */

public final class ColorGeometryElement
  extends GeometryElement
  implements ColorGeometryProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new color geometry.
   * @param colorSet The colour set of the new color geometry, or <CODE>null</CODE> if empty.
   */
  public ColorGeometryElement(final Collection<? extends Color> colorSet)
  {
    if (colorSet == null) {
      mColorSet = Collections.emptySet();
    } else {
      final Set<Color> colorSetModifiable =
        new HashSet<Color>(colorSet);
      mColorSet =
        Collections.unmodifiableSet(colorSetModifiable);
    }
  }

  /**
   * Creates a new color geometry using default values.
   * This constructor creates a color geometry with
   * an empty colour set.
   */
  public ColorGeometryElement()
  {
    this(emptyColorSet());
  }


  //#########################################################################
  //# Cloning
  public ColorGeometryElement clone()
  {
    return (ColorGeometryElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final ColorGeometryElement downcast = (ColorGeometryElement) partner;
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
  private final Set<Color> mColorSet;

}
