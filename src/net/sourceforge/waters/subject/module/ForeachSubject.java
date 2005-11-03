//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   ForeachSubject
//###########################################################################
//# $Id: ForeachSubject.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ArrayListSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.NamedSubject;


/**
 * The subject implementation of the {@link ForeachProxy} interface.
 *
 * @author Robi Malik
 */

public abstract class ForeachSubject
  extends NamedSubject
  implements ForeachProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new foreach construct.
   * @param name The name of the new foreach construct.
   * @param range The range of the new foreach construct.
   * @param guard The guard of the new foreach construct, or <CODE>null</CODE>.
   * @param body The body of the new foreach construct.
   */
  protected ForeachSubject(final String name,
                           final SimpleExpressionProxy range,
                           final SimpleExpressionProxy guard,
                           final Collection<? extends Proxy> body)
  {
    super(name);
    mRange = (SimpleExpressionSubject) range;
    mRange.setParent(this);
    mGuard = (SimpleExpressionSubject) guard;
    if (mGuard != null) {
      mGuard.setParent(this);
    }
    mBody = new ArrayListSubject<AbstractSubject>
      (body, AbstractSubject.class);
    mBody.setParent(this);
  }

  /**
   * Creates a new foreach construct using default values.
   * This constructor creates a foreach construct with
   * the guard set to <CODE>null</CODE> and
   * an empty body.
   * @param name The name of the new foreach construct.
   * @param range The range of the new foreach construct.
   */
  protected ForeachSubject(final String name,
                           final SimpleExpressionProxy range)
  {
    this(name,
         range,
         null,
         emptyProxyList());
  }


  //#########################################################################
  //# Cloning
  public ForeachSubject clone()
  {
    final ForeachSubject cloned = (ForeachSubject) super.clone();
    cloned.mRange = mRange.clone();
    cloned.mRange.setParent(cloned);
    if (mGuard != null) {
      cloned.mGuard = mGuard.clone();
      cloned.mGuard.setParent(cloned);
    }
    cloned.mBody = mBody.clone();
    cloned.mBody.setParent(cloned);
    return cloned;
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final ForeachSubject downcast = (ForeachSubject) partner;
      return
        mRange.equals(downcast.mRange) &&
        (mGuard == null ? downcast.mGuard == null :
         mGuard.equals(downcast.mGuard)) &&
        mBody.equals(downcast.mBody);
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ForeachProxy
  public SimpleExpressionSubject getRange()
  {
    return mRange;
  }

  public SimpleExpressionSubject getGuard()
  {
    return mGuard;
  }

  public List<Proxy> getBody()
  {
    final List<Proxy> downcast = Casting.toList(mBody);
    return Collections.unmodifiableList(downcast);
  }


  //#########################################################################
  //# Setters
  /**
   * Sets the range of this foreach construct.
   */
  public void setRange(final SimpleExpressionSubject range)
  {
    final boolean change = (mRange != range);
    range.setParent(this);
    mRange.setParent(null);
    mRange = range;
    if (change) {
      final ModelChangeEvent event =
        ModelChangeEvent.createStateChanged(this);
      fireModelChanged(event);
    }
  }

  /**
   * Sets the guard of this foreach construct.
   */
  public void setGuard(final SimpleExpressionSubject guard)
  {
    final boolean change = (mGuard != guard);
    if (guard != null) {
      guard.setParent(this);
    }
    if (mGuard != null) {
      mGuard.setParent(null);
    }
    mGuard = guard;
    if (change) {
      final ModelChangeEvent event =
        ModelChangeEvent.createStateChanged(this);
      fireModelChanged(event);
    }
  }

  /**
   * Gets the modifiable body of this foreach construct.
   */
  public ListSubject<AbstractSubject> getBodyModifiable()
  {
    return mBody;
  }


  //#########################################################################
  //# Auxiliary Methods
  private static List<Proxy> emptyProxyList()
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# Data Members
  private SimpleExpressionSubject mRange;
  private SimpleExpressionSubject mGuard;
  private ListSubject<AbstractSubject> mBody;

}
