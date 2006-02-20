//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   ModuleSubject
//###########################################################################
//# $Id: ModuleSubject.java,v 1.3 2006-02-20 22:20:22 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.Geometry;
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
   * @param location The location of the new module.
   * @param parameterList The parameter list of the new module.
   * @param constantAliasList The constant definition list of the new module.
   * @param eventDeclList The event declaration list of the new module.
   * @param eventAliasList The event alias list of the new module.
   * @param componentList The component list of the new module.
   */
  public ModuleSubject(final String name,
                       final URI location,
                       final Collection<? extends ParameterProxy> parameterList,
                       final Collection<? extends AliasProxy> constantAliasList,
                       final Collection<? extends EventDeclProxy> eventDeclList,
                       final Collection<? extends Proxy> eventAliasList,
                       final Collection<? extends Proxy> componentList)
  {
    super(name, location);
    mParameterList = new IndexedArrayListSubject<ParameterSubject>
      (parameterList, ParameterSubject.class);
    mParameterList.setParent(this);
    mConstantAliasList = new ArrayListSubject<AliasSubject>
      (constantAliasList, AliasSubject.class);
    mConstantAliasList.setParent(this);
    mEventDeclList = new IndexedArrayListSubject<EventDeclSubject>
      (eventDeclList, EventDeclSubject.class);
    mEventDeclList.setParent(this);
    mEventAliasList = new ArrayListSubject<AbstractSubject>
      (eventAliasList, AbstractSubject.class);
    mEventAliasList.setParent(this);
    mComponentList = new ArrayListSubject<AbstractSubject>
      (componentList, AbstractSubject.class);
    mComponentList.setParent(this);
  }

  /**
   * Creates a new module using default values.
   * This constructor creates a module with
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
         location,
         emptyParameterProxyList(),
         emptyAliasProxyList(),
         emptyEventDeclProxyList(),
         emptyProxyList(),
         emptyProxyList());
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
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final ModuleSubject downcast = (ModuleSubject) partner;
      return
        mParameterList.equals(downcast.mParameterList) &&
        mConstantAliasList.equals(downcast.mConstantAliasList) &&
        mEventDeclList.equals(downcast.mEventDeclList) &&
        mEventAliasList.equals(downcast.mEventAliasList) &&
        mComponentList.equals(downcast.mComponentList);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Object partner)
  {
    if (super.equalsWithGeometry(partner)) {
      final ModuleSubject downcast = (ModuleSubject) partner;
      return
        Geometry.equalList(mParameterList, downcast.mParameterList) &&
        Geometry.equalList(mConstantAliasList, downcast.mConstantAliasList) &&
        Geometry.equalList(mEventDeclList, downcast.mEventDeclList) &&
        Geometry.equalList(mEventAliasList, downcast.mEventAliasList) &&
        Geometry.equalList(mComponentList, downcast.mComponentList);
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
  //# Auxiliary Methods
  private static List<ParameterProxy> emptyParameterProxyList()
  {
    return Collections.emptyList();
  }

  private static List<AliasProxy> emptyAliasProxyList()
  {
    return Collections.emptyList();
  }

  private static List<EventDeclProxy> emptyEventDeclProxyList()
  {
    return Collections.emptyList();
  }

  private static List<Proxy> emptyProxyList()
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# Data Members
  private IndexedListSubject<ParameterSubject> mParameterList;
  private ListSubject<AliasSubject> mConstantAliasList;
  private IndexedListSubject<EventDeclSubject> mEventDeclList;
  private ListSubject<AbstractSubject> mEventAliasList;
  private ListSubject<AbstractSubject> mComponentList;

}
