//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   EventDeclElement
//###########################################################################
//# $Id: EventDeclElement.java,v 1.3 2006-02-22 03:35:07 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.Geometry;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.plain.base.NamedElement;

import net.sourceforge.waters.xsd.base.EventKind;


/**
 * An immutable implementation of the {@link EventDeclProxy} interface.
 *
 * @author Robi Malik
 */

public final class EventDeclElement
  extends NamedElement
  implements EventDeclProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new event declaration.
   * @param name The name of the new event declaration.
   * @param kind The kind of the new event declaration.
   * @param observable The observability status of the new event declaration.
   * @param ranges The list of index ranges of the new event declaration, or <CODE>null</CODE> if empty.
   * @param colorGeometry The color information of the new event declaration, or <CODE>null</CODE>.
   */
  public EventDeclElement(final String name,
                          final EventKind kind,
                          final boolean observable,
                          final Collection<? extends SimpleExpressionProxy> ranges,
                          final ColorGeometryProxy colorGeometry)
  {
    super(name);
    mKind = kind;
    mIsObservable = observable;
    if (ranges == null) {
      mRanges = Collections.emptyList();
    } else {
      final List<SimpleExpressionProxy> rangesModifiable =
        new ArrayList<SimpleExpressionProxy>(ranges);
      mRanges =
        Collections.unmodifiableList(rangesModifiable);
    }
    mColorGeometry = colorGeometry;
  }

  /**
   * Creates a new event declaration using default values.
   * This constructor creates an event declaration with
   * the observability status set to <CODE>true</CODE>,
   * an empty list of index ranges, and
   * the color information set to <CODE>null</CODE>.
   * @param name The name of the new event declaration.
   * @param kind The kind of the new event declaration.
   */
  public EventDeclElement(final String name,
                          final EventKind kind)
  {
    this(name,
         kind,
         true,
         emptySimpleExpressionProxyList(),
         null);
  }


  //#########################################################################
  //# Cloning
  public EventDeclElement clone()
  {
    return (EventDeclElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final EventDeclElement downcast = (EventDeclElement) partner;
      return
        mKind.equals(downcast.mKind) &&
        (mIsObservable == downcast.mIsObservable) &&
        mRanges.equals(downcast.mRanges);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Object partner)
  {
    if (super.equalsWithGeometry(partner)) {
      final EventDeclElement downcast = (EventDeclElement) partner;
      return
        mKind.equals(downcast.mKind) &&
        (mIsObservable == downcast.mIsObservable) &&
        Geometry.equalList(mRanges, downcast.mRanges) &&
        Geometry.equalGeometry(mColorGeometry, downcast.mColorGeometry);
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
    return downcast.visitEventDeclProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.EventDeclProxy
  public EventKind getKind()
  {
    return mKind;
  }

  public boolean isObservable()
  {
    return mIsObservable;
  }

  public List<SimpleExpressionProxy> getRanges()
  {
    return mRanges;
  }

  public ColorGeometryProxy getColorGeometry()
  {
    return mColorGeometry;
  }


  //#########################################################################
  //# Auxiliary Methods
  private static List<SimpleExpressionProxy> emptySimpleExpressionProxyList()
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# Data Members
  private final EventKind mKind;
  private final boolean mIsObservable;
  private final List<SimpleExpressionProxy> mRanges;
  private final ColorGeometryProxy mColorGeometry;

}
