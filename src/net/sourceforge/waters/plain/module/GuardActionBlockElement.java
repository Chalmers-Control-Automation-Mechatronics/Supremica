//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   GuardActionBlockElement
//###########################################################################
//# $Id: GuardActionBlockElement.java,v 1.2 2006-03-02 12:12:49 martin Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.Geometry;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
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
   * @param actionList The action list of the new guard action block.
   * @param geometry The geometry of the new guard action block, or <CODE>null</CODE>.
   */
  public GuardActionBlockElement(final String guard,
                                 final Collection<? extends BinaryExpressionProxy> actionList,
                                 final LabelGeometryProxy geometry)
  {
    mGuard = guard;
    final List<BinaryExpressionProxy> actionListModifiable =
      new ArrayList<BinaryExpressionProxy>(actionList);
    mActionList =
      Collections.unmodifiableList(actionListModifiable);
    mGeometry = geometry;
  }

  /**
   * Creates a new guard action block using default values.
   * This constructor creates a guard action block with
   * the guard set to <CODE>null</CODE>,
   * an empty action list, and
   * the geometry set to <CODE>null</CODE>.
   */
  public GuardActionBlockElement()
  {
    this(null,
         emptyBinaryExpressionProxyList(),
         null);
  }


  //#########################################################################
  //# Cloning
  public GuardActionBlockElement clone()
  {
    return (GuardActionBlockElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final GuardActionBlockElement downcast = (GuardActionBlockElement) partner;
      return
        (mGuard == null ? downcast.mGuard == null :
         mGuard.equals(downcast.mGuard)) &&
        mActionList.equals(downcast.mActionList);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Object partner)
  {
    if (super.equalsWithGeometry(partner)) {
      final GuardActionBlockElement downcast = (GuardActionBlockElement) partner;
      return
        (mGuard == null ? downcast.mGuard == null :
         mGuard.equals(downcast.mGuard)) &&
        Geometry.equalList(mActionList, downcast.mActionList) &&
        Geometry.equalGeometry(mGeometry, downcast.mGeometry);
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
    return downcast.visitGuardActionBlockProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.GuardActionBlockProxy
  public String getGuard()
  {
    return mGuard;
  }

  public List<BinaryExpressionProxy> getActionList()
  {
    return mActionList;
  }

  public LabelGeometryProxy getGeometry()
  {
    return mGeometry;
  }


  //#########################################################################
  //# Auxiliary Methods
  private static List<BinaryExpressionProxy> emptyBinaryExpressionProxyList()
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# Data Members
  private final String mGuard;
  private final List<BinaryExpressionProxy> mActionList;
  private final LabelGeometryProxy mGeometry;

}
