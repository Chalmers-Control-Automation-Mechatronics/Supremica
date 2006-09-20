//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   ForeachElement
//###########################################################################
//# $Id: ForeachElement.java,v 1.8 2006-09-20 16:24:13 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.EqualCollection;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.plain.base.NamedElement;


/**
 * An immutable implementation of the {@link ForeachProxy} interface.
 *
 * @author Robi Malik
 */

public abstract class ForeachElement
  extends NamedElement
  implements ForeachProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new foreach construct.
   * @param name The name of the new foreach construct.
   * @param range The range of the new foreach construct.
   * @param guard The guard of the new foreach construct, or <CODE>null</CODE>.
   * @param body The body of the new foreach construct, or <CODE>null</CODE> if empty.
   */
  protected ForeachElement(final String name,
                           final SimpleExpressionProxy range,
                           final SimpleExpressionProxy guard,
                           final Collection<? extends Proxy> body)
  {
    super(name);
    mRange = range;
    mGuard = guard;
    if (body == null) {
      mBody = Collections.emptyList();
    } else {
      final List<Proxy> bodyModifiable =
        new ArrayList<Proxy>(body);
      mBody =
        Collections.unmodifiableList(bodyModifiable);
    }
  }

  /**
   * Creates a new foreach construct using default values.
   * This constructor creates a foreach construct with
   * the guard set to <CODE>null</CODE> and
   * an empty body.
   * @param name The name of the new foreach construct.
   * @param range The range of the new foreach construct.
   */
  protected ForeachElement(final String name,
                           final SimpleExpressionProxy range)
  {
    this(name,
         range,
         null,
         null);
  }


  //#########################################################################
  //# Cloning
  public ForeachElement clone()
  {
    return (ForeachElement) super.clone();
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final ForeachElement downcast = (ForeachElement) partner;
      return
        mRange.equalsByContents(downcast.mRange) &&
        (mGuard == null ? downcast.mGuard == null :
         mGuard.equalsByContents(downcast.mGuard)) &&
        EqualCollection.isEqualListByContents
          (mBody, downcast.mBody);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final ForeachElement downcast = (ForeachElement) partner;
      return
        mRange.equalsWithGeometry(downcast.mRange) &&
        (mGuard == null ? downcast.mGuard == null :
         mGuard.equalsWithGeometry(downcast.mGuard)) &&
        EqualCollection.isEqualListWithGeometry
          (mBody, downcast.mBody);
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mRange.hashCodeByContents();
    result *= 5;
    if (mGuard != null) {
      result += mGuard.hashCodeByContents();
    }
    result *= 5;
    result += EqualCollection.getListHashCodeByContents(mBody);
    return result;
  }

  public int hashCodeWithGeometry()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mRange.hashCodeWithGeometry();
    result *= 5;
    if (mGuard != null) {
      result += mGuard.hashCodeWithGeometry();
    }
    result *= 5;
    result += EqualCollection.getListHashCodeWithGeometry(mBody);
    return result;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ForeachProxy
  public SimpleExpressionProxy getRange()
  {
    return mRange;
  }

  public SimpleExpressionProxy getGuard()
  {
    return mGuard;
  }

  public List<Proxy> getBody()
  {
    return mBody;
  }


  //#########################################################################
  //# Data Members
  private final SimpleExpressionProxy mRange;
  private final SimpleExpressionProxy mGuard;
  private final List<Proxy> mBody;

}
