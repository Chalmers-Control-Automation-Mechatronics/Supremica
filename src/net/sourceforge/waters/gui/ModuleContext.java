//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ModuleContext
//###########################################################################
//# $Id: ModuleContext.java,v 1.9 2008-03-07 04:11:02 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Icon;

import net.sourceforge.waters.gui.language.ProxyNamer;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.ForeachEventProxy;
import net.sourceforge.waters.model.module.IdentifiedProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
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

public class ModuleContext
{

  //#########################################################################
  //# Constructor
  public ModuleContext(final ModuleSubject module)
  {
    mModule = module;
    mCanDropVisitor = new CanDropVisitor();
    mIdentifierNameVisitor = new IdentifierNameVisitor();
    mIconGetterVisitor = new IconGetterVisitor();
    mToolTipGetterVisitor = new ToolTipGetterVisitor();
    mEventDeclListWrapper =
      new ListSubjectWrapper(module.getEventDeclListModifiable());
    mComponentListWrapper =
      new ListSubjectWrapper(module.getComponentListModifiable());
  }


  //#########################################################################
  //# General Access for all Types
  /**
   * Gets an icon to be displayed for the given module element.
   */
  public Icon getIcon(final Proxy item)
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
   * Tries to determine an event declaration for the given identifier.
   * Given an event name, this method inspects the module to determine
   * whether the name represents a controllable event, and uncontrollable
   * event, or a proposition.
   * @param  ident   The identifier representing the event name to be
   *                 looked up.
   * @return The event declaration that will be used when compiling the given
   *         identifier in the context of this module, or <CODE>null</CODE>
   *         if no suitable event declaration could be found.
   */
  public EventDeclProxy guessEventDecl(final IdentifierProxy ident)
  {
    final String name = mIdentifierNameVisitor.getIdentifierName(ident);
    return getEventDecl(name);
  }

  /**
   * Tries to determine an event kind for the given identifier.  Given an
   * event name, this method inspects the module to determine whether the
   * name represents a controllable event, and uncontrollable event, or a
   * proposition.
   * @param  ident   The identifier representing the event name to be
   *                 checked.
   * @return The event kind that will be associated with the given
   *         identifier after compilation of the module, or <CODE>null</CODE>
   *         to indicate that the event kind cannot be determined.
   */
  public EventKind guessEventKind(final IdentifierProxy ident)
  {
    final EventDeclProxy decl = guessEventDecl(ident);
    return decl == null ? null : decl.getKind();
  }

  /**
   * Tries to determine an icon for an event with a given identifier.
   * @return Always returns an icon, but it may be just the default
   *         'event' icon.
   */
  public Icon guessEventIcon(final IdentifierProxy ident)
  {
    final EventDeclProxy decl = guessEventDecl(ident);
    return decl == null ? IconLoader.ICON_EVENT : getIcon(decl);
  }

  /**
   * Tries to determine an icon for a propsition with a given identifier.
   * This method tries to be a bit smarter that {@link
   * #guessEventIcon(IdentifierProxy) guessEventIcon()} by checking the
   * names of the default propositions.
   * @return Always returns an icon, but it may be just the default
   *         'proposition' icon.
   */
  public Icon guessPropositionIcon(final IdentifierProxy ident)
  {
    final String name = mIdentifierNameVisitor.getIdentifierName(ident);
    final EventDeclProxy decl = getEventDecl(name);
    if (decl != null) {
      return getIcon(decl);
    } else if (ident instanceof SimpleIdentifierProxy &&
               name.equals(EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
      return IconLoader.ICON_FORBIDDEN;      
    } else {
      return PropositionIcon.getDefaultIcon();
    }
  }

  /**
   * Proposes a tool tip text for an event with a given identifier.
   * @return Always returns a string, but it may be just the meaningless
   *         word "event".
   */
  public String guessEventToolTipText(final IdentifierProxy ident)
  {
    final EventDeclProxy decl = guessEventDecl(ident);
    return decl == null ? "Event" : getToolTipText(decl);
  }

  /**
   * Tries to determine whether the given list of identifiers can be
   * dropped on a node. This method checks all the identifiers in the given
   * list, recursively entering foreach-event blocks
   * @return <CODE>false</CODE> if an identifier is found that can be
   *         determined <I>not</I> to be of type proposition ({@link
   *         EventKind#PROPOSITION}); <CODE>true</CODE> otherwise.
   */
  public boolean canDropOnNode(final Collection<? extends Proxy> idents)
  {
    return mCanDropVisitor.canDrop(idents, NODE_DROP_LIST);
  }

  /**
   * Tries to determine whether the given list of identifiers can be
   * dropped on a node. This method checks all the identifiers in the given
   * list, recursively entering foreach-event blocks
   * @return <CODE>false</CODE> if an identifier is found that can be
   *         determined <I>not</I> to be controllable ({@link
   *         EventKind#CONTROLLABLE}) or uncontrollable ({@link
   *         EventKind#UNCONTROLLABLE}); <CODE>true</CODE> otherwise.
   */
  public boolean canDropOnEdge(final Collection<? extends Proxy> idents)
  {
    return mCanDropVisitor.canDrop(idents, EDGE_DROP_LIST);
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

  /**
   * Checks whether the given identifier is likely to represent a component
   * not yet in the module, and throws an exception otherwise.
   * @param  ident    The identifier representing a component ({@link
   *                  SimpleComponentProxy}, {@link VariableComponentProxy},
   *                  etc.) to be created.
   * @throws ParseException to indicate that the module already contains
   *                  a component with the given name.
   */
  public void checkNewComponentName(final IdentifierProxy ident)
    throws ParseException
  {
    if (ident instanceof SimpleIdentifierProxy) {
      final SimpleIdentifierProxy simple = (SimpleIdentifierProxy) ident;
      final String name = simple.getName();
      final IdentifiedProxy found = getComponent(name);
      if (found != null) {
        final StringBuffer buffer = new StringBuffer("Name '");
        buffer.append(name);
        buffer.append("' is already taken by ");
        final String typename = ProxyNamer.getUnqualifiedClassName(found);
        buffer.append(typename);
        buffer.append('!');
        final String msg = buffer.toString();
        throw new ParseException(msg, 0);
      }
    }      
  }

  /**
   * Finds a name for an event declaration to be pasted into the module.
   * If the given name is not used, it is returned. Otherwise, this
   * method constructs an alternative name that is not yet taken, such
   * that the original name is still recognisable.
   * @param  name     The name of an event to be added.
   */
  public String getPastedEventName(final String name)
  {
    final NameChecker checker = new NameChecker() {
        public boolean isNameTaken(final String name) {
          return getEventDecl(name) != null;
        }
      };
    return getPastedName(name, checker);
  }

  /**
   * Finds a name for an event declaration to be pasted into the module.
   * If the given name is not used, it is returned. Otherwise, this
   * method constructs an alternative name that is not yet taken, such
   * that the original name is still recognisable.
   * @param  name     The name of an event to be added.
   * @param  alsoUsed A set of names also to be considered as taken.
   */
  public String getPastedEventName(final String name,
                                   final Set<String> alsoUsed)
  {
    final NameChecker checker = new NameChecker() {
        public boolean isNameTaken(final String name) {
          return getEventDecl(name) != null || alsoUsed.contains(name);
        }
      };
    return getPastedName(name, checker);
  }

  /**
   * Finds a name for a component to be pasted into the module.  If the
   * given name is not used, it is returned. Otherwise, this method
   * constructs an alternative name that is not yet taken, such that the
   * original name is still recognisable.
   * @param  ident    The identifier representing a component ({@link
   *                  SimpleComponentProxy}, {@link VariableComponentProxy},
   *                  etc.) to be added.
   */
  public IdentifierSubject getPastedComponentName
    (final IdentifierSubject ident)
  {
    if (ident instanceof SimpleIdentifierProxy) {
      final SimpleIdentifierProxy simple = (SimpleIdentifierProxy) ident;
      final String name = simple.getName();
      final NameChecker checker = new NameChecker() {
          public boolean isNameTaken(final String name) {
            return getComponent(name) != null;
          }
        };
      final String newname = getPastedName(name, checker);
      return newname == name ? ident : new SimpleIdentifierSubject(newname);
    } else {
      return ident;
    }
  }

  /**
   * Finds a name for a component to be pasted into the module.  If the
   * given name is not used, it is returned. Otherwise, this method
   * constructs an alternative name that is not yet taken, such that the
   * original name is still recognisable.
   * @param  ident    The identifier representing a component ({@link
   *                  SimpleComponentProxy}, {@link VariableComponentProxy},
   *                  etc.) to be added.
   * @param  alsoUsed A set of names also to be considered as taken.
   *                  If the chosen name is not in this set, it is
   *                  automatically added.
   */
  public IdentifierSubject getPastedComponentName
    (final IdentifierSubject ident, final Set<String> alsoUsed)
  {
    if (ident instanceof SimpleIdentifierProxy) {
      final SimpleIdentifierProxy simple = (SimpleIdentifierProxy) ident;
      final String name = simple.getName();
      final NameChecker checker = new NameChecker() {
          public boolean isNameTaken(final String name) {
            return getComponent(name) != null || alsoUsed.contains(name);
          }
        };
      final String newname = getPastedName(name, checker);
      if (newname == name) {
        return ident;
      } else {
        alsoUsed.add(newname);
        return new SimpleIdentifierSubject(newname);
      }
    } else {
      return ident;
    }
  }


  //#########################################################################
  //# Static Methods
  public static Icon getComponentKindIcon(final ComponentKind kind)
  {
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
     
  public static String getComponentKindToolTip(final ComponentKind kind)
  {
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


  //#########################################################################
  //# Direct Access
  public EventDeclProxy getEventDecl(final String name)
  {
    return (EventDeclProxy) mEventDeclListWrapper.get(name);
  }

  public IdentifiedProxy getComponent(final String name)
  {
    return mComponentListWrapper.get(name);
  }


  //#########################################################################
  //# Auxiliary Static Methods
  private static String getPastedName(final String name,
                                      final NameChecker checker)
  {
    if (checker.isNameTaken(name)) {
      final Pattern pattern = getPattern();
      final Matcher matcher = pattern.matcher(name);
      final String stripped = matcher.replaceFirst("");
      final StringBuffer buffer = new StringBuffer();
      String newname;
      int index = 1;
      do {
        buffer.append("copy");
        if (index > 1) {
          buffer.append(index);
        }
        buffer.append("_of_");
        buffer.append(stripped);
        newname = buffer.toString();
        index++;
        final int len = buffer.length();
        buffer.delete(0, len);
      } while (checker.isNameTaken(newname));
      return newname;
    } else {
      return name;
    }
  }

  private static Pattern getPattern()
  {
    if (PATTERN == null) {
      PATTERN = Pattern.compile("^copy[0-9]*_of_");
    }
    return PATTERN;
  }


  //#########################################################################
  //# Private Interface Class NameChecker
  private interface NameChecker
  {

    //#######################################################################
    //# Invocation
    public boolean isNameTaken(String name);

  }


  //#########################################################################
  //# Inner Class ListSubjectWrapper
  private class ListSubjectWrapper
    implements ModelObserver
  {

    //#######################################################################
    //# Constructor
    private ListSubjectWrapper(final ListSubject<? extends ProxySubject> list)
    {
      mList = list;
      mMap = null;
    }

    //#######################################################################
    //# Access
    private IdentifiedProxy get(final String name)
    {
      final Map<String,IdentifiedProxy> map = getMap();
      return map.get(name);
    }

    private Map<String,IdentifiedProxy> getMap()
    {
      if (mMap == null) {
        final int size = mList.size();
        mMap = new HashMap<String,IdentifiedProxy>(size);
        for (final ProxySubject item : mList) {
          if (item instanceof IdentifiedProxy) {
            final IdentifiedProxy comp = (IdentifiedProxy) item;
            final IdentifierProxy ident = comp.getIdentifier();
            if (ident instanceof SimpleIdentifierProxy) {
              final SimpleIdentifierProxy simple =
                (SimpleIdentifierProxy) ident;
              final String name = simple.getName();
              mMap.put(name, comp);
            }
          }
        }
        mList.addModelObserver(this);
      }
      return mMap;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.subject.base.ModelObserver
    public void modelChanged(final ModelChangeEvent event)
    {
      final Subject source = event.getSource();
      final Object value = event.getValue();
      switch (event.getKind()) {
      case ModelChangeEvent.ITEM_ADDED:
        if (source == mList && value instanceof IdentifiedProxy) {
          final IdentifiedProxy comp = (IdentifiedProxy) value;
          final IdentifierProxy ident = comp.getIdentifier();
          if (ident instanceof SimpleIdentifierProxy) {
            final SimpleIdentifierProxy simple = (SimpleIdentifierProxy) ident;
            final String name = simple.getName();
            if (!mMap.containsKey(name)) {
              mMap.put(name, comp);
            }
          }
        }
        break;
      case ModelChangeEvent.ITEM_REMOVED:
        if (source == mList && value instanceof IdentifiedProxy) {
          final IdentifiedProxy comp = (IdentifiedProxy) value;
          final IdentifierProxy ident = comp.getIdentifier();
          if (ident instanceof SimpleIdentifierProxy) {
            mMap = null;
            mList.removeModelObserver(this);
          }
        }
        break;
      case ModelChangeEvent.STATE_CHANGED:
        // Identifier and IdentifiedProxy objects fire STATE_CHANGED,
        // not NAME_CHANGED ...
        final Subject parent = source.getParent();
        if (source instanceof IdentifiedProxy &&
            parent == mList ||
            source instanceof SimpleIdentifierProxy &&
            parent.getParent() == mList) {
          mMap = null;
          mList.removeModelObserver(this);
        }
        break;
      default:
        break;
      }
    }

    //#######################################################################
    //# Data Members
    private final ListSubject<? extends ProxySubject> mList;
    private Map<String,IdentifiedProxy> mMap;

  }


  //#########################################################################
  //# Inner Class CanDropVisitor
  private class CanDropVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private boolean canDrop(final Collection<? extends Proxy> idents,
                            final EventKind[] allowed)
    {
      try {
        mAllowed = allowed;
        for (final Proxy proxy : idents) {
          final boolean candrop = (Boolean) proxy.acceptVisitor(this);
          if (!candrop) {
            return false;
          }
        }
        return true;
      } catch (final VisitorException exception) {
	throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
    public Boolean visitForeachEventProxy(final ForeachEventProxy foreach)
      throws VisitorException
    {
      for (final Proxy proxy : foreach.getBody()) {
        final boolean candrop = (Boolean) proxy.acceptVisitor(this);
        if (!candrop) {
          return false;
        }
      }
      return true;
    }

    public Boolean visitIdentifierProxy(final IdentifierProxy ident)
    {
      final EventKind kind = guessEventKind(ident);
      if (kind == null) {
        return true;
      } else {
        for (int i = 0; i < mAllowed.length; i++) {
          if (kind == mAllowed[i]) {
            return true;
          }
        }
        return false;
      }
    }

    //#######################################################################
    //# Data Members
    private EventKind[] mAllowed;

  }


  //#########################################################################
  //# Inner Class IdentifierNameVisitor
  private static class IdentifierNameVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private String getIdentifierName(final IdentifierProxy ident)
    {
      try {
        if (ident == null) {
          return null;
        } else {
          return (String) ident.acceptVisitor(this);
        }
      } catch (final VisitorException exception) {
	throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ProxyVisitor
    public Icon visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
    public String visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
    {
      return ident.getName();
    }

    public String visitSimpleIdentifierProxy
      (final SimpleIdentifierProxy ident)
    {
      return ident.getName();
    }

  }


  //#########################################################################
  //# Inner Class IconGetterVisitor
  private static class IconGetterVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private Icon getIcon(final Proxy proxy)
    {
      try {
	return (Icon) proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
	throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ProxyVisitor
    public Icon visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
    public Icon visitEventDeclProxy(final EventDeclProxy decl)
    {
      final EventKind kind = decl.getKind();
      switch (kind) {
      case CONTROLLABLE:
	return IconLoader.ICON_CONTROLLABLE;
      case UNCONTROLLABLE:
	return IconLoader.ICON_UNCONTROLLABLE;
      case PROPOSITION:
	final String name = decl.getName();
        final ColorGeometryProxy geo = decl.getColorGeometry();
        if (geo != null && !geo.getColorSet().isEmpty()) {
          final Color color = geo.getColorSet().iterator().next();
          return PropositionIcon.getIcon(color);
        } else if (name.equals(EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
	  return IconLoader.ICON_FORBIDDEN;
	} else {
          return PropositionIcon.getDefaultIcon();
	}
      default:
	throw new IllegalArgumentException
	  ("Unknown event kind: " + kind + "!");
      }
    }

    public Icon visitForeachProxy(final ForeachProxy foreach)
    {
      return IconLoader.ICON_FOREACH;
    }

    public Icon visitInstanceProxy(final InstanceProxy inst)
    {
      return IconLoader.ICON_INSTANCE;
    }

    public Icon visitParameterBindingProxy
      (final ParameterBindingProxy binding)
    {
      return IconLoader.ICON_BINDING;
    }

    public Icon visitSimpleComponentProxy(final SimpleComponentProxy comp)
    {
      final ComponentKind kind = comp.getKind();
      return getComponentKindIcon(kind);
    }

    public Icon visitVariableComponentProxy
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
      return getComponentKindToolTip(kind);
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
  private final CanDropVisitor mCanDropVisitor;
  private final IdentifierNameVisitor mIdentifierNameVisitor;
  private final IconGetterVisitor mIconGetterVisitor;
  private final ToolTipGetterVisitor mToolTipGetterVisitor;

  private final ListSubjectWrapper mEventDeclListWrapper;
  private final ListSubjectWrapper mComponentListWrapper;


  //#########################################################################
  //# Static Class Variables
  private static Pattern PATTERN = null;

  private static final EventKind[] NODE_DROP_LIST =
    {EventKind.PROPOSITION};
  private static final EventKind[] EDGE_DROP_LIST =
    {EventKind.CONTROLLABLE, EventKind.UNCONTROLLABLE};

}