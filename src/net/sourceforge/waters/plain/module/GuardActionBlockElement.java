//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   GuardActionBlockElement
//###########################################################################
//# $Id: GuardActionBlockElement.java,v 1.5 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.plain.base.Element;


/**
 * An immutable implementation of the {@link GuardActionBlockProxy} interface.
 *
 * @author Robi Malik
 */

public final class GuardActionBlockElement
  extends Element
  implements GuardActionBlockProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new guard action block.
   * @param guard The guard of the new guard action block, or <CODE>null</CODE>.
   * @param action The action of the new guard action block, or <CODE>null</CODE>.
   * @param geometry The geometry of the new guard action block, or <CODE>null</CODE>.
   */
  public GuardActionBlockElement(final String guard,
                                 final String action,
                                 final LabelGeometryProxy geometry)
  {
    mGuard = guard;
    mAction = action;
    mGeometry = geometry;
  }

  /**
   * Creates a new guard action block using default values.
   * This constructor creates a guard action block with
   * the guard set to <CODE>null</CODE>,
   * the action set to <CODE>null</CODE>, and
   * the geometry set to <CODE>null</CODE>.
   */
  public GuardActionBlockElement()
  {
    this(null,
         null,
         null);
  }


  //#########################################################################
  //# Cloning
  public GuardActionBlockElement clone()
  {
    return (GuardActionBlockElement) super.clone();
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final GuardActionBlockElement downcast = (GuardActionBlockElement) partner;
      return
        mGuard.equals(downcast.mGuard) &&
        mAction.equals(downcast.mAction);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final GuardActionBlockElement downcast = (GuardActionBlockElement) partner;
      return
        mGuard.equals(downcast.mGuard) &&
        mAction.equals(downcast.mAction) &&
        (mGeometry == null ? downcast.mGeometry == null :
         mGeometry.equalsWithGeometry(downcast.mGeometry));
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mGuard.hashCode();
    result *= 5;
    result += mAction.hashCode();
    return result;
  }

  public int hashCodeWithGeometry()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mGuard.hashCode();
    result *= 5;
    result += mAction.hashCode();
    result *= 5;
    if (mGeometry != null) {
      result += mGeometry.hashCodeWithGeometry();
    }
    return result;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitGuardActionBlockProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.GuardActionBlockProxy
  public String getGuard()
  {
    return mGuard;
  }

  public String getAction()
  {
    return mAction;
  }

  public LabelGeometryProxy getGeometry()
  {
    return mGeometry;
  }


  //#########################################################################
  //# Data Members
  private final String mGuard;
  private final String mAction;
  private final LabelGeometryProxy mGeometry;

}
