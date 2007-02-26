//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   EventDeclSubject
//###########################################################################
//# $Id: EventDeclSubject.java,v 1.10 2007-02-26 21:41:18 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
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
         null,
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
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final EventDeclSubject downcast = (EventDeclSubject) partner;
      return
        mKind.equals(downcast.mKind) &&
        (mIsObservable == downcast.mIsObservable) &&
        ProxyTools.isEqualListByContents
          (mRanges, downcast.mRanges);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final EventDeclSubject downcast = (EventDeclSubject) partner;
      return
        mKind.equals(downcast.mKind) &&
        (mIsObservable == downcast.mIsObservable) &&
        ProxyTools.isEqualListWithGeometry
          (mRanges, downcast.mRanges) &&
        (mColorGeometry == null ? downcast.mColorGeometry == null :
         mColorGeometry.equalsWithGeometry(downcast.mColorGeometry));
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mKind.hashCode();
    result *= 5;
    if (mIsObservable) {
      result++;
    }
    result *= 5;
    result += ProxyTools.getListHashCodeByContents(mRanges);
    return result;
  }

  public int hashCodeWithGeometry()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mKind.hashCode();
    result *= 5;
    if (mIsObservable) {
      result++;
    }
    result *= 5;
    result += ProxyTools.getListHashCodeWithGeometry(mRanges);
    result *= 5;
    if (mColorGeometry != null) {
      result += mColorGeometry.hashCodeWithGeometry();
    }
    return result;
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
  //# Data Members
  private EventKind mKind;
  private boolean mIsObservable;
  private ListSubject<SimpleExpressionSubject> mRanges;
  private ColorGeometrySubject mColorGeometry;

}
