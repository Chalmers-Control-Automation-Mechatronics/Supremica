//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   GuardActionBlockSubject
//###########################################################################
//# $Id: GuardActionBlockSubject.java,v 1.12 2007-02-26 21:41:18 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
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
   * @param guards The guards of the new guard action block, or <CODE>null</CODE> if empty.
   * @param actions The actions of the new guard action block, or <CODE>null</CODE> if empty.
   * @param geometry The geometry of the new guard action block, or <CODE>null</CODE>.
   */
  public GuardActionBlockSubject(final Collection<? extends SimpleExpressionProxy> guards,
                                 final Collection<? extends BinaryExpressionProxy> actions,
                                 final LabelGeometryProxy geometry)
  {
    if (guards == null) {
      mGuards = new ArrayListSubject<SimpleExpressionSubject>();
    } else {
      mGuards = new ArrayListSubject<SimpleExpressionSubject>
        (guards, SimpleExpressionSubject.class);
    }
    mGuards.setParent(this);
    if (actions == null) {
      mActions = new ArrayListSubject<BinaryExpressionSubject>();
    } else {
      mActions = new ArrayListSubject<BinaryExpressionSubject>
        (actions, BinaryExpressionSubject.class);
    }
    mActions.setParent(this);
    mGeometry = (LabelGeometrySubject) geometry;
    if (mGeometry != null) {
      mGeometry.setParent(this);
    }
  }

  /**
   * Creates a new guard action block using default values.
   * This constructor creates a guard action block with
   * an empty guards,
   * an empty actions, and
   * the geometry set to <CODE>null</CODE>.
   */
  public GuardActionBlockSubject()
  {
    this(null,
         null,
         null);
  }


  //#########################################################################
  //# Cloning
  public GuardActionBlockSubject clone()
  {
    final GuardActionBlockSubject cloned = (GuardActionBlockSubject) super.clone();
    cloned.mGuards = mGuards.clone();
    cloned.mGuards.setParent(cloned);
    cloned.mActions = mActions.clone();
    cloned.mActions.setParent(cloned);
    if (mGeometry != null) {
      cloned.mGeometry = mGeometry.clone();
      cloned.mGeometry.setParent(cloned);
    }
    return cloned;
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final GuardActionBlockSubject downcast = (GuardActionBlockSubject) partner;
      return
        ProxyTools.isEqualListByContents
          (mGuards, downcast.mGuards) &&
        ProxyTools.isEqualListByContents
          (mActions, downcast.mActions);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final GuardActionBlockSubject downcast = (GuardActionBlockSubject) partner;
      return
        ProxyTools.isEqualListWithGeometry
          (mGuards, downcast.mGuards) &&
        ProxyTools.isEqualListWithGeometry
          (mActions, downcast.mActions) &&
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
    result += ProxyTools.getListHashCodeByContents(mGuards);
    result *= 5;
    result += ProxyTools.getListHashCodeByContents(mActions);
    return result;
  }

  public int hashCodeWithGeometry()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += ProxyTools.getListHashCodeWithGeometry(mGuards);
    result *= 5;
    result += ProxyTools.getListHashCodeWithGeometry(mActions);
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
  public List<SimpleExpressionProxy> getGuards()
  {
    final List<SimpleExpressionProxy> downcast = Casting.toList(mGuards);
    return Collections.unmodifiableList(downcast);
  }

  public List<BinaryExpressionProxy> getActions()
  {
    final List<BinaryExpressionProxy> downcast = Casting.toList(mActions);
    return Collections.unmodifiableList(downcast);
  }

  public LabelGeometrySubject getGeometry()
  {
    return mGeometry;
  }


  //#########################################################################
  //# Setters
  public ListSubject<SimpleExpressionSubject> getGuardsModifiable()
  {
    return mGuards;
  }

  public ListSubject<BinaryExpressionSubject> getActionsModifiable()
  {
    return mActions;
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
  //# Data Members
  private ListSubject<SimpleExpressionSubject> mGuards;
  private ListSubject<BinaryExpressionSubject> mActions;
  private LabelGeometrySubject mGeometry;

}
