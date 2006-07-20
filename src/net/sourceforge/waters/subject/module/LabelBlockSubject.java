//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   LabelBlockSubject
//###########################################################################
//# $Id: LabelBlockSubject.java,v 1.8 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.subject.base.ModelChangeEvent;


/**
 * The subject implementation of the {@link LabelBlockProxy} interface.
 *
 * @author Robi Malik
 */

public final class LabelBlockSubject
  extends EventListExpressionSubject
  implements LabelBlockProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new label block.
   * @param eventList The list of events of the new label block, or <CODE>null</CODE> if empty.
   * @param geometry The geometry of the new label block, or <CODE>null</CODE>.
   */
  public LabelBlockSubject(final Collection<? extends Proxy> eventList,
                           final LabelGeometryProxy geometry)
  {
    super(eventList);
    mGeometry = (LabelGeometrySubject) geometry;
    if (mGeometry != null) {
      mGeometry.setParent(this);
    }
  }

  /**
   * Creates a new label block using default values.
   * This constructor creates a label block with
   * an empty list of events and
   * the geometry set to <CODE>null</CODE>.
   */
  public LabelBlockSubject()
  {
    this(emptyProxyList(),
         null);
  }


  //#########################################################################
  //# Cloning
  public LabelBlockSubject clone()
  {
    final LabelBlockSubject cloned = (LabelBlockSubject) super.clone();
    if (mGeometry != null) {
      cloned.mGeometry = mGeometry.clone();
      cloned.mGeometry.setParent(cloned);
    }
    return cloned;
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsWithGeometry(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final LabelBlockSubject downcast = (LabelBlockSubject) partner;
      return
        (mGeometry == null ? downcast.mGeometry == null :
         mGeometry.equalsWithGeometry(downcast.mGeometry));
    } else {
      return false;
    }
  }

  public int hashCodeWithGeometry()
  {
    int result = super.hashCodeByContents();
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
    return downcast.visitLabelBlockProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.LabelBlockProxy
  public LabelGeometrySubject getGeometry()
  {
    return mGeometry;
  }


  //#########################################################################
  //# Setters
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
  private static List<Proxy> emptyProxyList()
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# Data Members
  private LabelGeometrySubject mGeometry;

}
