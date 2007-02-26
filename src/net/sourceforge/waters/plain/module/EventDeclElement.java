//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   EventDeclElement
//###########################################################################
//# $Id: EventDeclElement.java,v 1.8 2007-02-26 21:41:18 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import java.util.ArrayList;
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
         null,
         null);
  }


  //#########################################################################
  //# Cloning
  public EventDeclElement clone()
  {
    return (EventDeclElement) super.clone();
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final EventDeclElement downcast = (EventDeclElement) partner;
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
      final EventDeclElement downcast = (EventDeclElement) partner;
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
    return mRanges;
  }

  public ColorGeometryProxy getColorGeometry()
  {
    return mColorGeometry;
  }


  //#########################################################################
  //# Data Members
  private final EventKind mKind;
  private final boolean mIsObservable;
  private final List<SimpleExpressionProxy> mRanges;
  private final ColorGeometryProxy mColorGeometry;

}
