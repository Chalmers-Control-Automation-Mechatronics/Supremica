//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   ModuleSubject
//###########################################################################
//# $Id: ModuleSubject.java,v 1.9 2006-09-20 16:24:13 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.EqualCollection;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.AliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.ParameterProxy;
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ArrayListSubject;
import net.sourceforge.waters.subject.base.DocumentSubject;
import net.sourceforge.waters.subject.base.IndexedArrayListSubject;
import net.sourceforge.waters.subject.base.IndexedListSubject;
import net.sourceforge.waters.subject.base.ListSubject;


/**
 * The subject implementation of the {@link ModuleProxy} interface.
 *
 * @author Robi Malik
 */

public final class ModuleSubject
  extends DocumentSubject
  implements ModuleProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new module.
   * @param name The name of the new module.
   * @param comment The comment of the new module, or <CODE>null</CODE>.
   * @param location The location of the new module.
   * @param parameterList The parameter list of the new module, or <CODE>null</CODE> if empty.
   * @param constantAliasList The constant definition list of the new module, or <CODE>null</CODE> if empty.
   * @param eventDeclList The event declaration list of the new module, or <CODE>null</CODE> if empty.
   * @param eventAliasList The event alias list of the new module, or <CODE>null</CODE> if empty.
   * @param componentList The component list of the new module, or <CODE>null</CODE> if empty.
   */
  public ModuleSubject(final String name,
                       final String comment,
                       final URI location,
                       final Collection<? extends ParameterProxy> parameterList,
                       final Collection<? extends AliasProxy> constantAliasList,
                       final Collection<? extends EventDeclProxy> eventDeclList,
                       final Collection<? extends Proxy> eventAliasList,
                       final Collection<? extends Proxy> componentList)
  {
    super(name, comment, location);
    if (parameterList == null) {
      mParameterList = new IndexedArrayListSubject<ParameterSubject>();
    } else {
      mParameterList = new IndexedArrayListSubject<ParameterSubject>
        (parameterList, ParameterSubject.class);
    }
    mParameterList.setParent(this);
    if (constantAliasList == null) {
      mConstantAliasList = new ArrayListSubject<AliasSubject>();
    } else {
      mConstantAliasList = new ArrayListSubject<AliasSubject>
        (constantAliasList, AliasSubject.class);
    }
    mConstantAliasList.setParent(this);
    if (eventDeclList == null) {
      mEventDeclList = new IndexedArrayListSubject<EventDeclSubject>();
    } else {
      mEventDeclList = new IndexedArrayListSubject<EventDeclSubject>
        (eventDeclList, EventDeclSubject.class);
    }
    mEventDeclList.setParent(this);
    if (eventAliasList == null) {
      mEventAliasList = new ArrayListSubject<AbstractSubject>();
    } else {
      mEventAliasList = new ArrayListSubject<AbstractSubject>
        (eventAliasList, AbstractSubject.class);
    }
    mEventAliasList.setParent(this);
    if (componentList == null) {
      mComponentList = new ArrayListSubject<AbstractSubject>();
    } else {
      mComponentList = new ArrayListSubject<AbstractSubject>
        (componentList, AbstractSubject.class);
    }
    mComponentList.setParent(this);
  }

  /**
   * Creates a new module using default values.
   * This constructor creates a module with
   * the comment set to <CODE>null</CODE>,
   * an empty parameter list,
   * an empty constant definition list,
   * an empty event declaration list,
   * an empty event alias list, and
   * an empty component list.
   * @param name The name of the new module.
   * @param location The location of the new module.
   */
  public ModuleSubject(final String name,
                       final URI location)
  {
    this(name,
         null,
         location,
         null,
         null,
         null,
         null,
         null);
  }


  //#########################################################################
  //# Cloning
  public ModuleSubject clone()
  {
    final ModuleSubject cloned = (ModuleSubject) super.clone();
    cloned.mParameterList = mParameterList.clone();
    cloned.mParameterList.setParent(cloned);
    cloned.mConstantAliasList = mConstantAliasList.clone();
    cloned.mConstantAliasList.setParent(cloned);
    cloned.mEventDeclList = mEventDeclList.clone();
    cloned.mEventDeclList.setParent(cloned);
    cloned.mEventAliasList = mEventAliasList.clone();
    cloned.mEventAliasList.setParent(cloned);
    cloned.mComponentList = mComponentList.clone();
    cloned.mComponentList.setParent(cloned);
    return cloned;
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final ModuleSubject downcast = (ModuleSubject) partner;
      return
        EqualCollection.isEqualListByContents
          (mParameterList, downcast.mParameterList) &&
        EqualCollection.isEqualListByContents
          (mConstantAliasList, downcast.mConstantAliasList) &&
        EqualCollection.isEqualListByContents
          (mEventDeclList, downcast.mEventDeclList) &&
        EqualCollection.isEqualListByContents
          (mEventAliasList, downcast.mEventAliasList) &&
        EqualCollection.isEqualListByContents
          (mComponentList, downcast.mComponentList);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final ModuleSubject downcast = (ModuleSubject) partner;
      return
        EqualCollection.isEqualListWithGeometry
          (mParameterList, downcast.mParameterList) &&
        EqualCollection.isEqualListWithGeometry
          (mConstantAliasList, downcast.mConstantAliasList) &&
        EqualCollection.isEqualListWithGeometry
          (mEventDeclList, downcast.mEventDeclList) &&
        EqualCollection.isEqualListWithGeometry
          (mEventAliasList, downcast.mEventAliasList) &&
        EqualCollection.isEqualListWithGeometry
          (mComponentList, downcast.mComponentList);
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += EqualCollection.getListHashCodeByContents(mParameterList);
    result *= 5;
    result += EqualCollection.getListHashCodeByContents(mConstantAliasList);
    result *= 5;
    result += EqualCollection.getListHashCodeByContents(mEventDeclList);
    result *= 5;
    result += EqualCollection.getListHashCodeByContents(mEventAliasList);
    result *= 5;
    result += EqualCollection.getListHashCodeByContents(mComponentList);
    return result;
  }

  public int hashCodeWithGeometry()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += EqualCollection.getListHashCodeWithGeometry(mParameterList);
    result *= 5;
    result += EqualCollection.getListHashCodeWithGeometry(mConstantAliasList);
    result *= 5;
    result += EqualCollection.getListHashCodeWithGeometry(mEventDeclList);
    result *= 5;
    result += EqualCollection.getListHashCodeWithGeometry(mEventAliasList);
    result *= 5;
    result += EqualCollection.getListHashCodeWithGeometry(mComponentList);
    return result;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitModuleProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxy
  public List<ParameterProxy> getParameterList()
  {
    final List<ParameterProxy> downcast = Casting.toList(mParameterList);
    return Collections.unmodifiableList(downcast);
  }

  public List<AliasProxy> getConstantAliasList()
  {
    final List<AliasProxy> downcast = Casting.toList(mConstantAliasList);
    return Collections.unmodifiableList(downcast);
  }

  public List<EventDeclProxy> getEventDeclList()
  {
    final List<EventDeclProxy> downcast = Casting.toList(mEventDeclList);
    return Collections.unmodifiableList(downcast);
  }

  public List<Proxy> getEventAliasList()
  {
    final List<Proxy> downcast = Casting.toList(mEventAliasList);
    return Collections.unmodifiableList(downcast);
  }

  public List<Proxy> getComponentList()
  {
    final List<Proxy> downcast = Casting.toList(mComponentList);
    return Collections.unmodifiableList(downcast);
  }


  //#########################################################################
  //# Setters
  /**
   * Gets the modifiable parameter list of this module.
   */
  public IndexedListSubject<ParameterSubject> getParameterListModifiable()
  {
    return mParameterList;
  }

  /**
   * Gets the modifiable constant definition list of this module.
   */
  public ListSubject<AliasSubject> getConstantAliasListModifiable()
  {
    return mConstantAliasList;
  }

  /**
   * Gets the modifiable event declaration list of this module.
   */
  public IndexedListSubject<EventDeclSubject> getEventDeclListModifiable()
  {
    return mEventDeclList;
  }

  /**
   * Gets the modifiable event alias list of this module.
   */
  public ListSubject<AbstractSubject> getEventAliasListModifiable()
  {
    return mEventAliasList;
  }

  /**
   * Gets the modifiable component list of this module.
   */
  public ListSubject<AbstractSubject> getComponentListModifiable()
  {
    return mComponentList;
  }


  //#########################################################################
  //# Data Members
  private IndexedListSubject<ParameterSubject> mParameterList;
  private ListSubject<AliasSubject> mConstantAliasList;
  private IndexedListSubject<EventDeclSubject> mEventDeclList;
  private ListSubject<AbstractSubject> mEventAliasList;
  private ListSubject<AbstractSubject> mComponentList;

}
