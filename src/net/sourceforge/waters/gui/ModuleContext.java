//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ModuleContext
//###########################################################################
//# $Id: ModuleContext.java,v 1.2 2007-11-21 04:14:46 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import javax.swing.ImageIcon;

import net.sourceforge.waters.model.base.IndexedList;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>An auxiliary class to retrieve context information from a module.</P>
 *
 * <P>A module context is linked to a module ({@link ModuleSubject}).
 * It provides some look-up functionality for events by their names,
 * and acts as a central access point to determine icons from object
 * types. It is faster, but less accurate, than the compiler ({@link
 * net.sourceforge.waters.model.compiler.ModuleCompiler}).</P>
 *
 * <P>The module context can be obtained from the module container
 * ({@link org.supremica.gui.ide.ModuleContainer}).</P>
 * 
 * @author Robi Malik
 */

public class ModuleContext {

  //#########################################################################
  //# Constructor
  public ModuleContext(final ModuleSubject module)
  {
    mModule = module;
    mIconGetterVisitor = new IconGetterVisitor();
    mToolTipGetterVisitor = new ToolTipGetterVisitor();
  }


  //#########################################################################
  //# General Access for all Types
  /**
   * Gets an icon to be displayed for the given module element.
   */
  public ImageIcon getImageIcon(final Proxy item)
  {
    return mIconGetterVisitor.getIcon(item);
  }

  /**
   * Gets a tool tip text to describe the given module element.
   */
  public String getToolTipText(final Proxy item)
  {
    return mToolTipGetterVisitor.getToolTipText(item);
  }


  //#########################################################################
  //# Special Access for Events
  /**
   * Tries to determine an event kind for the given identifier.  Given an
   * event name, this method inspects the module to determine whether the
   * name represents a controllable event, and uncontrollable event, or a
   * proposition.
   * @param  ident   The identifier representing the event name to be
   *                 checked.
   * @return The event kind that will be associated with the given
   *         identifier after compilation of the module, or <CODE>null</CODE>
   *         that the event kind cannot be determined.
   */
  public EventKind guessEventKind(final IdentifierProxy ident)
  {
    final String name = ident.getName();
    final EventDeclProxy decl = getEventDecl(name);
    return decl == null ? null : decl.getKind();
  }

  /**
   * Tries to determine an icon for an event with a given identifier.
   * @return Always returns an icon, but it may be just the default
   *         'event' icon.
   */
  public ImageIcon guessEventIcon(final IdentifierProxy ident)
  {
    final String name = ident.getName();
    final EventDeclProxy decl = getEventDecl(name);
    return decl == null ? IconLoader.ICON_EVENT : getImageIcon(decl);
  }

  /**
   * Proposes a tool tip text for an event with a given identifier.
   * @return Always returns a string, but it may be just the meaningless
   *         word "event".
   */
  public String guessEventToolTipText(final IdentifierProxy ident)
  {
    final String name = ident.getName();
    final EventDeclProxy decl = getEventDecl(name);
    return decl == null ? "Event" : getToolTipText(decl);
  }


  //#########################################################################
  //# Checking for Duplicate Names
  /**
   * Checks whether the given string will represent an event declaration
   * not yet in the module, and throws an exception otherwise.
   * @param  name     The name of an event declaration to be created.
   * @throws ParseException to indicate that the module already contains
   *                  an event declaration with the given name.
   */
  public void checkNewEventName(final String name)
    throws ParseException
  {
    if (getEventDecl(name) != null) {
      throw new ParseException
	("Name '" + name + "' is already taken by an event!", 0);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private EventDeclProxy getEventDecl(final String name)
  {
    final IndexedList<EventDeclSubject> decls =
      mModule.getEventDeclListModifiable();
    return decls.get(name);
  }


  //#########################################################################
  //# Inner Class IconGetterVisitor
  private static class IconGetterVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private ImageIcon getIcon(final Proxy proxy)
    {
      try {
	return (ImageIcon) proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
	throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ProxyVisitor
    public ImageIcon visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
    public ImageIcon visitEventDeclProxy(final EventDeclProxy decl)
    {
      final EventKind kind = decl.getKind();
      switch (kind) {
      case CONTROLLABLE:
	return IconLoader.ICON_CONTROLLABLE;
      case UNCONTROLLABLE:
	return IconLoader.ICON_UNCONTROLLABLE;
      case PROPOSITION:
	final String name = decl.getName();
	if (name.equals(EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
	  return IconLoader.ICON_FORBIDDEN;
	} else {
	  return IconLoader.ICON_PROPOSITION;
	}
      default:
	throw new IllegalArgumentException
	  ("Unknown event kind: " + kind + "!");
      }
    }

    public ImageIcon visitForeachProxy(final ForeachProxy foreach)
    {
      return IconLoader.ICON_FOREACH;
    }

    public ImageIcon visitInstanceProxy(final InstanceProxy inst)
    {
      return IconLoader.ICON_INSTANCE;
    }

    public ImageIcon visitParameterBindingProxy
      (final ParameterBindingProxy binding)
    {
      return IconLoader.ICON_BINDING;
    }

    public ImageIcon visitSimpleComponentProxy(final SimpleComponentProxy comp)
    {
      final ComponentKind kind = comp.getKind();
      switch (kind) {
      case PLANT:
	return IconLoader.ICON_PLANT;
      case PROPERTY:
	return IconLoader.ICON_PROPERTY;
      case SPEC:
	return IconLoader.ICON_SPEC;
      case SUPERVISOR:
	return IconLoader.ICON_SUPERVISOR;
      default:
	throw new IllegalArgumentException
	  ("Unknown component kind: " + kind + "!");
      }
    }

    public ImageIcon visitVariableComponentProxy
      (final VariableComponentProxy var)
    {
      return IconLoader.ICON_VARIABLE;
    }

  }


  //#########################################################################
  //# Inner Class ToolTipGetterVisitor
  private static class ToolTipGetterVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private String getToolTipText(final Proxy proxy)
    {
      try {
	return (String) proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
	throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ProxyVisitor
    public String visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
    public String visitEventDeclProxy(final EventDeclProxy decl)
    {
      final EventKind kind = decl.getKind();
      switch (kind) {
      case CONTROLLABLE:
	return "Controllable event";
      case UNCONTROLLABLE:
	return "Uncontrollable event";
      case PROPOSITION:
	return "Proposition";
      default:
	throw new IllegalArgumentException
	  ("Unknown event kind: " + kind + "!");
      }
    }

    public String visitInstanceProxy(final InstanceProxy inst)
    {
      return "Module instance";
    }

    public String visitParameterBindingProxy
      (final ParameterBindingProxy binding)
    {
      return "Parameter binding";
    }

    public String visitSimpleComponentProxy(final SimpleComponentProxy comp)
    {
      final ComponentKind kind = comp.getKind();
      switch (kind) {
      case PLANT:
	return "Plant";
      case PROPERTY:
	return "Property";
      case SPEC:
	return "Specification";
      case SUPERVISOR:
	return "Supervisor";
      default:
	throw new IllegalArgumentException
	  ("Unknown component kind: " + kind + "!");
      }
    }

    public String visitVariableComponentProxy
      (final VariableComponentProxy var)
    {
      return "EFA variable";
    }

  }


  //#########################################################################
  //# Data Members
  private final ModuleSubject mModule;
  private final IconGetterVisitor mIconGetterVisitor;
  private final ToolTipGetterVisitor mToolTipGetterVisitor;

}