//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   ModuleElement
//###########################################################################
//# $Id: ModuleElement.java,v 1.6 2006-05-24 09:13:02 markus Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import java.net.URI;
import java.util.ArrayList;
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
import net.sourceforge.waters.plain.base.DocumentElement;


/**
 * An immutable implementation of the {@link ModuleProxy} interface.
 *
 * @author Robi Malik
 */

public final class ModuleElement
  extends DocumentElement
  implements ModuleProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new module.
   * @param name The name of the new module.
   * @param location The location of the new module.
   * @param parameterList The parameter list of the new module, or <CODE>null</CODE> if empty.
   * @param constantAliasList The constant definition list of the new module, or <CODE>null</CODE> if empty.
   * @param eventDeclList The event declaration list of the new module, or <CODE>null</CODE> if empty.
   * @param eventAliasList The event alias list of the new module, or <CODE>null</CODE> if empty.
   * @param componentList The component list of the new module, or <CODE>null</CODE> if empty.
   */
  public ModuleElement(final String name,
                       final URI location,
                       final Collection<? extends ParameterProxy> parameterList,
                       final Collection<? extends AliasProxy> constantAliasList,
                       final Collection<? extends EventDeclProxy> eventDeclList,
                       final Collection<? extends Proxy> eventAliasList,
                       final Collection<? extends Proxy> componentList)
  {
    super(name, location);
    if (parameterList == null) {
      mParameterList = Collections.emptyList();
    } else {
      final List<ParameterProxy> parameterListModifiable =
        new ArrayList<ParameterProxy>(parameterList);
      mParameterList =
        Collections.unmodifiableList(parameterListModifiable);
    }
    if (constantAliasList == null) {
      mConstantAliasList = Collections.emptyList();
    } else {
      final List<AliasProxy> constantAliasListModifiable =
        new ArrayList<AliasProxy>(constantAliasList);
      mConstantAliasList =
        Collections.unmodifiableList(constantAliasListModifiable);
    }
    if (eventDeclList == null) {
      mEventDeclList = Collections.emptyList();
    } else {
      final List<EventDeclProxy> eventDeclListModifiable =
        new ArrayList<EventDeclProxy>(eventDeclList);
      mEventDeclList =
        Collections.unmodifiableList(eventDeclListModifiable);
    }
    if (eventAliasList == null) {
      mEventAliasList = Collections.emptyList();
    } else {
      final List<Proxy> eventAliasListModifiable =
        new ArrayList<Proxy>(eventAliasList);
      mEventAliasList =
        Collections.unmodifiableList(eventAliasListModifiable);
    }
    if (componentList == null) {
      mComponentList = Collections.emptyList();
    } else {
      final List<Proxy> componentListModifiable =
        new ArrayList<Proxy>(componentList);
      mComponentList =
        Collections.unmodifiableList(componentListModifiable);
    }
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
  public ModuleElement(final String name,
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
  public ModuleElement clone()
  {
    return (ModuleElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final ModuleElement downcast = (ModuleElement) partner;
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
      final ModuleElement downcast = (ModuleElement) partner;
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
    return mParameterList;
  }

  public List<AliasProxy> getConstantAliasList()
  {
    return mConstantAliasList;
  }

  public List<EventDeclProxy> getEventDeclList()
  {
    return mEventDeclList;
  }

  public List<Proxy> getEventAliasList()
  {
    return mEventAliasList;
  }

  public List<Proxy> getComponentList()
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
  private final List<ParameterProxy> mParameterList;
  private final List<AliasProxy> mConstantAliasList;
  private final List<EventDeclProxy> mEventDeclList;
  private final List<Proxy> mEventAliasList;
  private final List<Proxy> mComponentList;

}
