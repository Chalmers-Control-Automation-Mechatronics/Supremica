//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   InstanceSubject
//###########################################################################
//# $Id: InstanceSubject.java,v 1.5 2006-03-06 17:08:46 markus Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.subject.base.IndexedArrayListSubject;
import net.sourceforge.waters.subject.base.IndexedListSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;


/**
 * The subject implementation of the {@link InstanceProxy} interface.
 *
 * @author Robi Malik
 */

public final class InstanceSubject
  extends ComponentSubject
  implements InstanceProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new instance.
   * @param identifier The identifier defining the name of the new instance.
   * @param moduleName The module name of the new instance.
   * @param bindingList The binding list of the new instance, or <CODE>null</CODE> if empty.
   */
  public InstanceSubject(final IdentifierProxy identifier,
                         final String moduleName,
                         final Collection<? extends ParameterBindingProxy> bindingList)
  {
    super(identifier);
    mModuleName = moduleName;
    if (bindingList == null) {
      mBindingList = new IndexedArrayListSubject<ParameterBindingSubject>();
    } else {
      mBindingList = new IndexedArrayListSubject<ParameterBindingSubject>
        (bindingList, ParameterBindingSubject.class);
    }
    mBindingList.setParent(this);
  }

  /**
   * Creates a new instance using default values.
   * This constructor creates an instance with
   * an empty binding list.
   * @param identifier The identifier defining the name of the new instance.
   * @param moduleName The module name of the new instance.
   */
  public InstanceSubject(final IdentifierProxy identifier,
                         final String moduleName)
  {
    this(identifier,
         moduleName,
         emptyParameterBindingProxyList());
  }


  //#########################################################################
  //# Cloning
  public InstanceSubject clone()
  {
    final InstanceSubject cloned = (InstanceSubject) super.clone();
    cloned.mBindingList = mBindingList.clone();
    cloned.mBindingList.setParent(cloned);
    return cloned;
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final InstanceSubject downcast = (InstanceSubject) partner;
      return
        mModuleName.equals(downcast.mModuleName) &&
        mBindingList.equals(downcast.mBindingList);
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
    return downcast.visitInstanceProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.InstanceProxy
  public String getModuleName()
  {
    return mModuleName;
  }

  public List<ParameterBindingProxy> getBindingList()
  {
    final List<ParameterBindingProxy> downcast = Casting.toList(mBindingList);
    return Collections.unmodifiableList(downcast);
  }


  //#########################################################################
  //# Setters
  /**
   * Sets the module name of this instance.
   */
  public void setModuleName(final String moduleName)
  {
    if (mModuleName.equals(moduleName)) {
      return;
    }
    mModuleName = moduleName;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }

  /**
   * Gets the modifiable binding list of this instance.
   */
  public IndexedListSubject<ParameterBindingSubject> getBindingListModifiable()
  {
    return mBindingList;
  }


  //#########################################################################
  //# Auxiliary Methods
  private static List<ParameterBindingProxy> emptyParameterBindingProxyList()
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# Data Members
  private String mModuleName;
  private IndexedListSubject<ParameterBindingSubject> mBindingList;

}
