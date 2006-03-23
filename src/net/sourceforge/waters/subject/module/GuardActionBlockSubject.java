//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   GuardActionBlockSubject
//###########################################################################
//# $Id: GuardActionBlockSubject.java,v 1.5 2006-03-23 13:54:17 flordal Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

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
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.subject.base.ArrayListSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.MutableSubject;


/**
 * The subject implementation of the {@link GuardActionBlockProxy} interface.
 *
 * @author Robi Malik
 */

public final class GuardActionBlockSubject
  extends MutableSubject
  implements GuardActionBlockProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new guard action block.
   * @param guard The guard of the new guard action block, or <CODE>null</CODE>.
   * @param actionList The action list of the new guard action block, or <CODE>null</CODE> if empty.
   * @param geometry The geometry of the new guard action block, or <CODE>null</CODE>.
   */
  public GuardActionBlockSubject(final String guard,
                                 final Collection<? extends BinaryExpressionProxy> actionList,
                                 final LabelGeometryProxy geometry)
  {
    mGuard = guard;
    if (actionList == null) {
      mActionList = new ArrayListSubject<BinaryExpressionSubject>();
    } else {
      mActionList = new ArrayListSubject<BinaryExpressionSubject>
        (actionList, BinaryExpressionSubject.class);
    }
    mActionList.setParent(this);
    mGeometry = (LabelGeometrySubject) geometry;
    if (mGeometry != null) {
      mGeometry.setParent(this);
    }
  }

  /**
   * Creates a new guard action block using default values.
   * This constructor creates a guard action block with
   * the guard set to <CODE>null</CODE>,
   * an empty action list, and
   * the geometry set to <CODE>null</CODE>.
   */
  public GuardActionBlockSubject()
  {
    this(null,
         emptyBinaryExpressionProxyList(),
         null);
  }


  //#########################################################################
  //# Cloning
  public GuardActionBlockSubject clone()
  {
    final GuardActionBlockSubject cloned = (GuardActionBlockSubject) super.clone();
    cloned.mActionList = mActionList.clone();
    cloned.mActionList.setParent(cloned);
    if (mGeometry != null) {
      cloned.mGeometry = mGeometry.clone();
      cloned.mGeometry.setParent(cloned);
    }
    return cloned;
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final GuardActionBlockSubject downcast = (GuardActionBlockSubject) partner;
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
      final GuardActionBlockSubject downcast = (GuardActionBlockSubject) partner;
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
    final List<BinaryExpressionProxy> downcast = Casting.toList(mActionList);
    return Collections.unmodifiableList(downcast);
  }

  public LabelGeometrySubject getGeometry()
  {
    return mGeometry;
  }


  //#########################################################################
  //# Setters
  public void setGuard(final String guard)
  {
    if (guard == null || guard.equals(mGuard)) {
      return;
    }
    mGuard = guard;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }

  public ListSubject<BinaryExpressionSubject> getActionListModifiable()
  {
    return mActionList;
  }

  public void setGeometry(final LabelGeometrySubject geometry)
  {
    if (mGeometry == geometry) {
      return;
    }
    if (geometry != null) {
      geometry.setParent(this);
    }
    if (mGeometry != null) {
      mGeometry.setParent(null);
    }
    mGeometry = geometry;
    final ModelChangeEvent event =
      ModelChangeEvent.createGeometryChanged(this, mGeometry);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Auxiliary Methods
  private static List<BinaryExpressionProxy> emptyBinaryExpressionProxyList()
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# Data Members
  private String mGuard;
  private ListSubject<BinaryExpressionSubject> mActionList;
  private LabelGeometrySubject mGeometry;

}
