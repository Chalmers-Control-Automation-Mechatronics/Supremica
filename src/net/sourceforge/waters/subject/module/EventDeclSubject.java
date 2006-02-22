//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   EventDeclSubject
//###########################################################################
//# $Id: EventDeclSubject.java,v 1.5 2006-02-22 03:35:07 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

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
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.subject.base.ArrayListSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.NamedSubject;

import net.sourceforge.waters.xsd.base.EventKind;


/**
 * The subject implementation of the {@link EventDeclProxy} interface.
 *
 * @author Robi Malik
 */

public final class EventDeclSubject
  extends NamedSubject
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
  public EventDeclSubject(final String name,
                          final EventKind kind,
                          final boolean observable,
                          final Collection<? extends SimpleExpressionProxy> ranges,
                          final ColorGeometryProxy colorGeometry)
  {
    super(name);
    mKind = kind;
    mIsObservable = observable;
    if (ranges == null) {
      mRanges = new ArrayListSubject<SimpleExpressionSubject>();
    } else {
      mRanges = new ArrayListSubject<SimpleExpressionSubject>
        (ranges, SimpleExpressionSubject.class);
    }
    mRanges.setParent(this);
    mColorGeometry = (ColorGeometrySubject) colorGeometry;
    if (mColorGeometry != null) {
      mColorGeometry.setParent(this);
    }
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
  public EventDeclSubject(final String name,
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
  public EventDeclSubject clone()
  {
    final EventDeclSubject cloned = (EventDeclSubject) super.clone();
    cloned.mRanges = mRanges.clone();
    cloned.mRanges.setParent(cloned);
    if (mColorGeometry != null) {
      cloned.mColorGeometry = mColorGeometry.clone();
      cloned.mColorGeometry.setParent(cloned);
    }
    return cloned;
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final EventDeclSubject downcast = (EventDeclSubject) partner;
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
      final EventDeclSubject downcast = (EventDeclSubject) partner;
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
    final List<SimpleExpressionProxy> downcast = Casting.toList(mRanges);
    return Collections.unmodifiableList(downcast);
  }

  public ColorGeometrySubject getColorGeometry()
  {
    return mColorGeometry;
  }


  //#########################################################################
  //# Setters
  /**
   * Sets the kind of this event declaration.
   */
  public void setKind(final EventKind kind)
  {
    if (mKind.equals(kind)) {
      return;
    }
    mKind = kind;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }

  /**
   * Sets the observability status of this event declaration.
   */
  public void setObservable(final boolean observable)
  {
    if (mIsObservable == observable) {
      return;
    }
    mIsObservable = observable;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }

  /**
   * Gets the modifiable list of index ranges of this event declaration.
   */
  public ListSubject<SimpleExpressionSubject> getRangesModifiable()
  {
    return mRanges;
  }

  /**
   * Sets the color information for this event declaration.
   */
  public void setColorGeometry(final ColorGeometrySubject colorGeometry)
  {
    if (mColorGeometry == colorGeometry) {
      return;
    }
    if (colorGeometry != null) {
      colorGeometry.setParent(this);
    }
    if (mColorGeometry != null) {
      mColorGeometry.setParent(null);
    }
    mColorGeometry = colorGeometry;
    final ModelChangeEvent event =
      ModelChangeEvent.createGeometryChanged(this, mColorGeometry);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Auxiliary Methods
  private static List<SimpleExpressionProxy> emptySimpleExpressionProxyList()
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# Data Members
  private EventKind mKind;
  private boolean mIsObservable;
  private ListSubject<SimpleExpressionSubject> mRanges;
  private ColorGeometrySubject mColorGeometry;

}
