//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sourceforge.waters.gui.compiler.ModuleCompilationErrors;
import net.sourceforge.waters.gui.language.ProxyNamer;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.gui.util.PropositionIcon;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.ConditionalProxy;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifiedProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NestedBlockProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.base.SubjectTools;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.IdentifiedSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;


/**
 * <P>
 * An auxiliary class to retrieve context information from a module.
 * </P>
 *
 * <P>
 * A module context is linked to a module ({@link ModuleSubject}). It provides
 * some look-up functionality for events by their names, and acts as a central
 * access point to determine icons from object types. It is faster, but less
 * accurate, than the compiler (
 * {@link net.sourceforge.waters.model.compiler.ModuleCompiler}).
 * </P>
 *
 * <P>
 * The module context can be obtained from the module container (
 * {@link org.supremica.gui.ide.ModuleContainer}).
 * </P>
 *
 * @author Robi Malik
 */

public class ModuleContext
{

  //#########################################################################
  //# Constructor
  public ModuleContext(final ModuleProxy module)
  {
    mModule = module;
    mCanDropVisitor = new CanDropVisitor();
    mIdentifierNameVisitor = new IdentifierNameVisitor();
    mIconGetterVisitor = new IconGetterVisitor();
    mToolTipGetterVisitor = new ToolTipGetterVisitor();
    mPropositionFinderVisitor = new PropositionFinderVisitor();
    mPropositionColorCollectorVisitor =
      new PropositionColorCollectorVisitor();
    mWrapperGetter = new WrapperGetterVisitor();
    if (module instanceof ModuleSubject) {
      final ModuleSubject subject = (ModuleSubject) module;
      mEventDeclListWrapper =
        new ListSubjectWrapper(subject.getEventDeclListModifiable());
      mComponentListWrapper =
        new ListSubjectWrapper(subject.getComponentListModifiable());
      mEventAliasListWrapper =
        new ListSubjectWrapper(subject.getEventAliasListModifiable());
      mConstanAliasListWrapper =
        new ListSubjectWrapper(subject.getConstantAliasListModifiable());
    } else {
      mEventDeclListWrapper =
        new ListSubjectWrapper(module.getEventDeclList());
      mComponentListWrapper =
        new ListSubjectWrapper(module.getComponentList());
      mEventAliasListWrapper =
        new ListSubjectWrapper(module.getEventAliasList());
      mConstanAliasListWrapper =
        new ListSubjectWrapper(module.getConstantAliasList());
    }
    mGraphStatusMap = new HashMap<GraphProxy,GraphStatus>();
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
   * If the element has errors an appropriate error message is returned.
   */
  public String getToolTipText(final Proxy item)
  {
    String tooltip = mCompilationErrors.getDetailedMessage(item);
    if (tooltip == null) {
      tooltip = mCompilationErrors.getSummaryMessage(item);
    }
    if (tooltip == null) {
      tooltip = mToolTipGetterVisitor.getToolTipText(item);
    }
    return tooltip;
  }

  //#########################################################################
  //# Simple Access
  public ModuleProxy getModule()
  {
    return mModule;
  }

  public ModuleCompilationErrors getCompilationErrors()
  {
    return mCompilationErrors;
  }

  public void setCompilationErrors(final ModuleCompilationErrors errors)
  {
    mCompilationErrors = errors;
  }

  //#########################################################################
  //# Special Access for Events
  /**
   * Tries to determine an event declaration for the given identifier. Given
   * an event name, this method inspects the module to determine whether the
   * name represents a controllable event, and uncontrollable event, or a
   * proposition.
   *
   * @param ident
   *          The identifier representing the event name to be looked up.
   * @return The event declaration that will be used when compiling the given
   *         identifier in the context of this module, or <CODE>null</CODE> if
   *         no suitable event declaration could be found.
   */
  public EventDeclProxy guessEventDecl(final IdentifierProxy ident)
  {
    final String name = mIdentifierNameVisitor.getIdentifierName(ident);
    return getEventDecl(name);
  }

  /**
   * Tries to determine an event kind for the given identifier. Given an event
   * name, this method inspects the module to determine whether the name
   * represents a controllable event, and uncontrollable event, or a
   * proposition.
   *
   * @param ident
   *          The identifier representing the event name to be checked.
   * @return The event kind that will be associated with the given identifier
   *         after compilation of the module, or <CODE>null</CODE> to indicate
   *         that the event kind cannot be determined.
   */
  public EventKind guessEventKind(final IdentifierProxy ident)
  {
    final EventDeclProxy decl = guessEventDecl(ident);
    return decl == null ? null : decl.getKind();
  }

  /**
   * Tries to determine an icon for an event with a given identifier.
   *
   * @return Always returns an icon, but it may be just the default 'event'
   *         icon.
   */
  public Icon guessEventIcon(final IdentifierProxy ident)
  {
    final EventDeclProxy decl = guessEventDecl(ident);
    if (decl != null) {
      final boolean error = hasErrorIcon(ident);
      return getEventDeclIcon(decl, error);
    } else {
      return IconAndFontLoader.ICON_EVENT;
    }
  }

  /**
   * Tries to determine an icon for a proposition with a given identifier.
   * This method tries to be a bit smarter than
   * {@link #guessEventIcon(IdentifierProxy) guessEventIcon()} by checking the
   * names of the default propositions.
   *
   * @return Always returns an icon, but it may be just the default
   *         'proposition' icon.
   */
  public Icon guessPropositionIcon(final IdentifierProxy ident)
  {
    final String name = mIdentifierNameVisitor.getIdentifierName(ident);
    final EventDeclProxy decl = getEventDecl(name);
    if (decl != null) {
      return getEventDeclIcon(decl, false);
    } else if (ident instanceof SimpleIdentifierProxy &&
               name.equals(EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
      return PropositionIcon.getForbiddenIcon();
    } else {
      return PropositionIcon.getDefaultMarkedIcon();
    }
  }

  /**
   * Tries to determine the list of proposition for a given node.
   *
   * @return A {@link net.sourceforge.waters.gui.util.PropositionIcon.ColorInfo}
   *         object containing the list of all colours found on the node,
   *         including a flag indicating the presence of the 'forbidden'
   *         marking.
   */
  public PropositionIcon.ColorInfo guessPropositionColors(final GraphProxy graph,
                                                          final SimpleNodeProxy node)
  {
    return mPropositionColorCollectorVisitor
      .getPropositionColors(graph, node);
  }

  /**
   * Proposes a tool tip text for an event with a given identifier.
   *
   * @return Always returns a string, but it may be just the meaningless word
   *         "event".
   */
  public String guessEventToolTipText(final IdentifierProxy ident)
  {
    final EventDeclProxy decl = guessEventDecl(ident);
    return decl == null ? "Event" : mToolTipGetterVisitor.getToolTipText(decl);
  }

  /**
   * Tries to determine whether the given list of identifiers can be dropped
   * on a node. This method checks all the identifiers in the given list,
   * recursively entering foreach-event blocks.
   *
   * @return <CODE>false</CODE> if an identifier is found that can be
   *         determined <I>not</I> to be of type proposition (
   *         {@link EventKind#PROPOSITION}); <CODE>true</CODE> otherwise.
   */
  public boolean canDropOnNode(final Collection<? extends Proxy> idents)
  {
    return mCanDropVisitor.canDrop(idents, NODE_DROP_LIST);
  }

  /**
   * Tries to determine whether the given list of identifiers can be dropped
   * on an edge. This method checks all the identifiers in the given list,
   * recursively entering foreach-event blocks.
   *
   * @return <CODE>false</CODE> if an identifier is found that can be
   *         determined <I>not</I> to be controllable (
   *         {@link EventKind#CONTROLLABLE}) or uncontrollable (
   *         {@link EventKind#UNCONTROLLABLE}); <CODE>true</CODE> otherwise.
   */
  public boolean canDropOnEdge(final Collection<? extends Proxy> idents)
  {
    return mCanDropVisitor.canDrop(idents, EDGE_DROP_LIST);
  }

  //#########################################################################
  //# Event Handling Support
  /**
   * Returns whether given event may cause the proposition status of the given
   * graph to change. The proposition status of a graph indicates whether the
   * graph uses any propositions, i.e., whether states without propositions
   * are rendered with a filled or a transparent background.
   */
  public boolean causesPropositionStatusChange(final ModelChangeEvent event,
                                               final GraphProxy graph)
  {
    final GraphStatus status = mGraphStatusMap.get(graph);
    if (status == null) {
      return false;
    } else {
      return status.causesChange(event);
    }
  }

  //#########################################################################
  //# Checking for Duplicate Names
  /**
   * Checks whether the given string will represent an event declaration not
   * yet in the module, and throws an exception otherwise.
   *
   * @param name
   *          The name of an event declaration to be created.
   * @throws ParseException
   *           to indicate that the module already contains an event
   *           declaration with the given name.
   */
  public void checkNewEventName(final String name) throws ParseException
  {
    if (getEventDecl(name) != null) {
      throw new ParseException("Name '" + name
                               + "' is already taken by an event.", 0);
    }
  }

  /**
   * Checks whether the given identifier is likely to represent a component
   * not yet in the module, and throws an exception otherwise.
   *
   * @param ident
   *          The identifier representing a component (
   *          {@link SimpleComponentProxy}, {@link VariableComponentProxy},
   *          etc.) to be created.
   * @throws ParseException
   *           to indicate that the module already contains a component with
   *           the given name.
   */
  public void checkNewComponentName(final IdentifierProxy ident)
    throws ParseException
  {
    if (ident instanceof SimpleIdentifierProxy) {
      final SimpleIdentifierProxy simple = (SimpleIdentifierProxy) ident;
      final String name = simple.getName();
      final IdentifiedProxy found = getComponent(name);
      if (found != null) {
        final StringBuilder buffer = new StringBuilder("Name '");
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
   * Finds a name for an event declaration to be pasted into the module. If
   * the given name is not used, it is returned. Otherwise, this method
   * constructs an alternative name that is not yet taken, such that the
   * original name is still recognisable.
   *
   * @param name
   *          The name of an event to be added.
   */
  public String getPastedEventName(final String name)
  {
    final NameChecker checker = new NameChecker() {
      @Override
      public boolean isNameTaken(final String name)
      {
        return getEventDecl(name) != null;
      }
    };
    return getPastedName(name, checker);
  }

  /**
   * Finds a name for an event declaration to be pasted into the module. If
   * the given name is not used, it is returned. Otherwise, this method
   * constructs an alternative name that is not yet taken, such that the
   * original name is still recognisable.
   *
   * @param name
   *          The name of an event to be added.
   * @param alsoUsed
   *          A set of names also to be considered as taken.
   */
  public String getPastedEventName(final String name,
                                   final Set<String> alsoUsed)
  {
    final NameChecker checker = new NameChecker() {
      @Override
      public boolean isNameTaken(final String name)
      {
        return getEventDecl(name) != null || alsoUsed.contains(name);
      }
    };
    return getPastedName(name, checker);
  }

  /**
   * Finds a name for a component to be pasted into the module. If the given
   * name is not used, it is returned. Otherwise, this method constructs an
   * alternative name that is not yet taken, such that the original name is
   * still recognisable.
   *
   * @param ident
   *          The identifier representing a component (
   *          {@link SimpleComponentProxy}, {@link VariableComponentProxy},
   *          etc.) to be added.
   */
  public IdentifierSubject getPastedComponentName(final IdentifierSubject ident)
  {
    if (ident instanceof SimpleIdentifierProxy) {
      final SimpleIdentifierProxy simple = (SimpleIdentifierProxy) ident;
      final String name = simple.getName();
      final NameChecker checker = new NameChecker() {
        @Override
        public boolean isNameTaken(final String name)
        {
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
   * Finds a name for a component to be pasted into the module. If the given
   * name is not used, it is returned. Otherwise, this method constructs an
   * alternative name that is not yet taken, such that the original name is
   * still recognisable.
   * @param subject
   *          The identifier representing a component (
   *          {@link SimpleComponentProxy}, {@link VariableComponentProxy},
   *          etc.) to be added.
   */
  public IdentifierSubject getPastedName(final IdentifiedSubject subject,
                                         final Set<String> alsoUsed)
  {
    final IdentifierSubject ident = subject.getIdentifier();
    if (ident instanceof SimpleIdentifierProxy) {
      final ListSubjectWrapper wrapper = mWrapperGetter.getWrapper(subject);
      final SimpleIdentifierProxy simple = (SimpleIdentifierProxy) ident;
      final String name = simple.getName();
      final NameChecker checker = new NameChecker() {
        @Override
        public boolean isNameTaken(final String name)
        {
          return wrapper.get(name) != null || alsoUsed.contains(name);
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

  /**
   * Finds a name for a component to be pasted into the module. If the given
   * name is not used, it is returned. Otherwise, this method constructs an
   * alternative name that is not yet taken, such that the original name is
   * still recognisable.
   *
   * @param ident
   *          The identifier representing a component (
   *          {@link SimpleComponentProxy}, {@link VariableComponentProxy},
   *          etc.) to be added.
   * @param alsoUsed
   *          A set of names also to be considered as taken. If the chosen
   *          name is not in this set, it is automatically added.
   */
  public IdentifierSubject getPastedComponentName(final IdentifierSubject ident,
                                                  final Set<String> alsoUsed)
  {
    if (ident instanceof SimpleIdentifierProxy) {
      final SimpleIdentifierProxy simple = (SimpleIdentifierProxy) ident;
      final String name = simple.getName();
      final NameChecker checker = new NameChecker() {
        @Override
        public boolean isNameTaken(final String name)
        {
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
  /**
   * Gets an icon to represent a component of the given kind.
   * @param kind    The type ({@link ComponentKind#PLANT}, etc.) of the
   *                component to be displayed.
   */
  public static Icon getComponentKindIcon(final ComponentKind kind)
  {
    return getComponentKindIcon(kind, false);
  }

  /**
   * Gets an icon to represent a component of the given kind.
   * @param kind    The type ({@link ComponentKind#PLANT}, etc.) of the
   *                component to be displayed.
   * @param error   <CODE>true</CODE> to get the error icon.
   */
  public static Icon getComponentKindIcon(final ComponentKind kind,
                                          final boolean error)
  {
    switch (kind) {
    case PLANT:
      if (error) {
        return IconAndFontLoader.ICON_PLANT_ERROR;
      } else {
        return IconAndFontLoader.ICON_PLANT;
      }
    case PROPERTY:
      if (error) {
        return IconAndFontLoader.ICON_PROPERTY_ERROR;
      } else {
        return IconAndFontLoader.ICON_PROPERTY;
      }
    case SPEC:
      if (error) {
        return IconAndFontLoader.ICON_SPEC_ERROR;
      } else {
        return IconAndFontLoader.ICON_SPEC;
      }
    case SUPERVISOR:
      if (error) {
        return IconAndFontLoader.ICON_SUPERVISOR_ERROR;
      } else {
        return IconAndFontLoader.ICON_SUPERVISOR;
      }
    default:
      throw new IllegalArgumentException("Unknown component kind: " + kind
                                         + "!");
    }
  }

  /**
   * Gets an icon list to represent a component of the given kind.
   * This icon list can be used to set frame icons using the
   * {@link java.awt.Window#setIconImages(List) setIconImages()}
   * method.
   * @param kind    The type ({@link ComponentKind#PLANT}, etc.) of the
   *                component to be displayed.
   */
  public static List<ImageIcon> getComponentKindIconList(final ComponentKind kind)
  {
    switch (kind) {
    case PLANT:
      return IconAndFontLoader.ICONLIST_PLANT;
    case PROPERTY:
      return IconAndFontLoader.ICONLIST_PROPERTY;
    case SPEC:
      return IconAndFontLoader.ICONLIST_SPEC;
    case SUPERVISOR:
      return IconAndFontLoader.ICONLIST_SUPERVISOR;
    default:
      throw new IllegalArgumentException("Unknown component kind: " + kind
                                         + "!");
    }
  }

  public static Icon getEventKindIcon(final EventKind event,
                                      final boolean observable)
  {
    return getEventKindIcon(event, observable, false);
  }

  public static Icon getEventKindIcon(final EventKind event,
                                      final boolean observable,
                                      final boolean error)
  {
    switch (event) {
    case CONTROLLABLE:
      if (observable) {
        if (error) {
          return IconAndFontLoader.ICON_CONTROLLABLE_OBSERVABLE_ERROR;
        } else {
          return IconAndFontLoader.ICON_CONTROLLABLE_OBSERVABLE;
        }
      } else {
        return IconAndFontLoader.ICON_CONTROLLABLE_UNOBSERVABLE;
      }
    case UNCONTROLLABLE:
      if (observable) {
        if (error) {
          return IconAndFontLoader.ICON_UNCONTROLLABLE_OBSERVABLE_ERROR;
        } else {
          return IconAndFontLoader.ICON_UNCONTROLLABLE_OBSERVABLE;
        }
      } else {
        return IconAndFontLoader.ICON_UNCONTROLLABLE_UNOBSERVABLE;
      }
    case PROPOSITION:
      return PropositionIcon.getDefaultMarkedIcon();
    default:
      throw new IllegalArgumentException("Unknown event kind: " + event + "!");
    }
  }

  public static String getComponentKindToolTip(final ComponentKind kind)
  {
    return AutomatonTools.getComponentKindStringCapitalised(kind);
  }

  public static String getEventKindToolTip(final EventKind kind,
                                           final boolean event)
  {
    switch (kind) {
    case CONTROLLABLE:
      return event ? "Controllable event" : "Controllable";
    case UNCONTROLLABLE:
      return event ? "Uncontrollable event" : "Uncontrollable";
    case PROPOSITION:
      return "Proposition";
    default:
      throw new IllegalArgumentException("Unknown event kind: " + kind + "!");
    }
  }

  public static List<Color> getPropositionColours(final SimpleNodeProxy node)
  {
    final List<Color> result = new LinkedList<Color>();
    return result;
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
  //# Auxiliary Methods
  private boolean hasPropositions(final GraphProxy graph)
  {
    GraphStatus status = mGraphStatusMap.get(graph);
    if (status == null) {
      status = new GraphStatus(graph);
    }
    return status.hasPropositions();
  }

  private boolean hasErrorIcon(final Proxy proxy)
  {
    return mCompilationErrors.hasErrorIcon(proxy);
  }

  private Icon getEventDeclIcon(final EventDeclProxy decl, final boolean error)
  {
    final EventKind kind = decl.getKind();
    switch (kind) {
    case CONTROLLABLE:
    case UNCONTROLLABLE:
      final boolean observable = decl.isObservable();
      return getEventKindIcon(kind, observable, error);
    case PROPOSITION:
      final String name = decl.getName();
      final ColorGeometryProxy geo = decl.getColorGeometry();
      if (geo != null && !geo.getColorSet().isEmpty()) {
        final Color color = geo.getColorSet().iterator().next();
        return PropositionIcon.getIcon(color);
      } else if (name.equals(EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
        return PropositionIcon.getForbiddenIcon();
      } else {
        return PropositionIcon.getDefaultMarkedIcon();
      }
    default:
      throw new IllegalArgumentException("Unknown event kind: " + kind + "!");
    }
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
      final StringBuilder buffer = new StringBuilder();
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
  //# Private Interface NameChecker
  private interface NameChecker
  {

    //#######################################################################
    //# Invocation
    public boolean isNameTaken(String name);

  }


  //#########################################################################
  //# Inner Class ListSubjectWrapper
  private class ListSubjectWrapper implements ModelObserver
  {

    //#######################################################################
    //# Constructor
    private ListSubjectWrapper(final List<? extends Proxy> list)
    {
      mList = list;
      mListSubject = null;
      mMap = null;
    }

    private ListSubjectWrapper(final ListSubject<? extends ProxySubject> list)
    {
      mList = list;
      mListSubject = list;
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
        for (final Proxy item : mList) {
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
        if (mListSubject != null) {
          mListSubject.addModelObserver(this);
        }
      }
      return mMap;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.subject.base.ModelObserver
    @Override
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
            final SimpleIdentifierProxy simple =
              (SimpleIdentifierProxy) ident;
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
            mListSubject.removeModelObserver(this);
          }
        }
        break;
      case ModelChangeEvent.STATE_CHANGED:
        // Identifier and IdentifiedProxy objects fire STATE_CHANGED,
        // not NAME_CHANGED ...
        final Subject parent = source.getParent();
        if (source instanceof IdentifiedProxy && parent == mList
            || source instanceof SimpleIdentifierProxy
            && parent.getParent() == mList) {
          mMap = null;
          mListSubject.removeModelObserver(this);
        }
        break;
      default:
        break;
      }
    }

    @Override
    public int getModelObserverPriority()
    {
      return ModelObserver.CLEANUP_PRIORITY_1;
    }

    //#######################################################################
    //# Data Members
    private final List<? extends Proxy> mList;
    private final ListSubject<? extends ProxySubject> mListSubject;
    private Map<String,IdentifiedProxy> mMap;

  }


  //#########################################################################
  //# Inner Class GraphStatus
  private class GraphStatus implements ModelObserver
  {

    //#######################################################################
    //# Invocation
    private GraphStatus(final GraphProxy graph)
    {
      mGraph = graph;
      mHasPropositions = mPropositionFinderVisitor.hasPropositions(graph);
      mGraphStatusMap.put(graph, this);
      if (graph instanceof GraphSubject) {
        final GraphSubject subject = (GraphSubject) graph;
        subject.addModelObserver(this);
      }
    }

    //#######################################################################
    //# Simple Access
    private boolean hasPropositions()
    {
      return mHasPropositions;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.subject.base.ModelObserver
    @Override
    public void modelChanged(final ModelChangeEvent event)
    {
      if (causesChange(event)) {
        toggleStatus();
      }
    }

    @Override
    public int getModelObserverPriority()
    {
      return ModelObserver.CLEANUP_PRIORITY_1;
    }

    //#######################################################################
    //# Auxiliary Methods
    private boolean causesChange(final ModelChangeEvent event)
    {
      final Subject source = event.getSource();
      final int ekind = event.getKind();
      final Object value = event.getValue();
      if (mHasPropositions) {
        switch (ekind) {
        case ModelChangeEvent.ITEM_REMOVED:
          if (value instanceof SimpleNodeProxy) {
            final SimpleNodeProxy node = (SimpleNodeProxy) value;
            final PlainEventListProxy props = node.getPropositions();
            if (!props.getEventIdentifierList().isEmpty()) {
              return true;
            }
          } else if(value instanceof Proxy){
            final Proxy proxy = (Proxy) value;
            final Boolean found =
              mPropositionFinderVisitor.hasPropositions(proxy);
            if (found == null) {
              return SubjectTools
                .getAncestor(source, SimpleNodeSubject.class) != null;
            } else if (found) {
              return true;
            }
          }
          break;
        case ModelChangeEvent.STATE_CHANGED:
          if (source == mGraph && mGraph.getBlockedEvents() == null) {
            return true;
          } else if (source instanceof EventDeclProxy) {
            final EventDeclProxy edecl = (EventDeclProxy) source;
            return edecl.getKind() != EventKind.PROPOSITION;
          }
          break;
        case ModelChangeEvent.NAME_CHANGED:
          if (source instanceof EventDeclProxy) {
            final EventDeclProxy edecl = (EventDeclProxy) source;
            return edecl.getKind() == EventKind.PROPOSITION;
          }
          break;
        default:
          break;
        }
      } else {
        switch (ekind) {
        case ModelChangeEvent.ITEM_ADDED:
          if (value instanceof SimpleNodeProxy) {
            final SimpleNodeProxy node = (SimpleNodeProxy) value;
            final PlainEventListProxy props = node.getPropositions();
            if (!props.getEventIdentifierList().isEmpty()) {
              return true;
            }
          } else if(value instanceof Proxy){
            final Proxy proxy = (Proxy) value;
            final Boolean found =
              mPropositionFinderVisitor.hasPropositions(proxy);
            if (found == null
                && SubjectTools.getAncestor(source, SimpleNodeSubject.class) != null) {
              return true;
            } else if (found != null && found) {
              return true;
            }
          }
          break;
        case ModelChangeEvent.STATE_CHANGED:
          if (source == mGraph) {
            final LabelBlockProxy blocked = mGraph.getBlockedEvents();
            if (blocked != null) {
              final Boolean found =
                mPropositionFinderVisitor.hasPropositions(blocked);
              if (found != null && found) {
                return true;
              }
            }
          } else if (source instanceof EventDeclProxy) {
            final EventDeclProxy edecl = (EventDeclProxy) source;
            return edecl.getKind() == EventKind.PROPOSITION;
          }
          break;
        case ModelChangeEvent.NAME_CHANGED:
          if (source instanceof EventDeclProxy) {
            final EventDeclProxy edecl = (EventDeclProxy) source;
            return edecl.getKind() == EventKind.PROPOSITION;
          }
          break;
        default:
          break;
        }
      }
      return false;
    }

    private void toggleStatus()
    {
      if (mHasPropositions) {
        mGraphStatusMap.remove(mGraph);
        if (mGraph instanceof GraphSubject) {
          final GraphSubject subject = (GraphSubject) mGraph;
          subject.removeModelObserver(this);
        }
      } else {
        mHasPropositions = true;
      }
    }

    //#######################################################################
    //# Data Members
    private final GraphProxy mGraph;
    private boolean mHasPropositions;

  }


  //#########################################################################
  //# Inner Class CanDropVisitor
  private class CanDropVisitor extends DefaultModuleProxyVisitor
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
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
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

    @Override
    public Boolean visitNestedBlockProxy(final NestedBlockProxy block)
      throws VisitorException
    {
      for (final Proxy proxy : block.getBody()) {
        final boolean canDrop = (Boolean) proxy.acceptVisitor(this);
        if (!canDrop) {
          return false;
        }
      }
      return true;
    }

    //#######################################################################
    //# Data Members
    private EventKind[] mAllowed;

  }


  //#########################################################################
  //# Inner Class IdentifierNameVisitor
  private static class IdentifierNameVisitor extends
    DefaultModuleProxyVisitor
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
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    @Override
    public Icon visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public String visitIndexedIdentifierProxy(final IndexedIdentifierProxy ident)
    {
      return ident.getName();
    }

    @Override
    public String visitSimpleIdentifierProxy(final SimpleIdentifierProxy ident)
    {
      return ident.getName();
    }

  }


  //#########################################################################
  //# Inner Class IconGetterVisitor
  private class IconGetterVisitor extends DefaultModuleProxyVisitor
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
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    @Override
    public Icon visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Icon visitConditionalProxy(final ConditionalProxy cond)
    {
      if (hasErrorIcon(cond)) {
        return IconAndFontLoader.ICON_CONDITIONAL_ERROR;
      } else {
        return IconAndFontLoader.ICON_CONDITIONAL;
      }
    }

    @Override
    public Icon visitConstantAliasProxy(final ConstantAliasProxy var)
    {
      return IconAndFontLoader.ICON_CONSTANT;
    }

    @Override
    public Icon visitEventAliasProxy(final EventAliasProxy var)
    {
      if (hasErrorIcon(var)) {
        return IconAndFontLoader.ICON_EVENT_ALIAS_ERROR;
      } else {
        return IconAndFontLoader.ICON_EVENT_ALIAS;
      }
    }

    @Override
    public Icon visitEventDeclProxy(final EventDeclProxy decl)
    {
      final boolean error = hasErrorIcon(decl);
      return getEventDeclIcon(decl, error);
    }

    @Override
    public Icon visitForeachProxy(final ForeachProxy foreach)
    {
      if (hasErrorIcon(foreach)) {
        return IconAndFontLoader.ICON_FOREACH_ERROR;
      } else {
        return IconAndFontLoader.ICON_FOREACH;
      }
    }

    @Override
    public Icon visitIdentifierProxy(final IdentifierProxy id)
    {
      return guessEventIcon(id);
    }

    @Override
    public Icon visitInstanceProxy(final InstanceProxy inst)
    {
      if (hasErrorIcon(inst)) {
        return IconAndFontLoader.ICON_INSTANCE_ERROR;
      } else {
        return IconAndFontLoader.ICON_INSTANCE;
      }
    }

    @Override
    public Icon visitParameterBindingProxy(final ParameterBindingProxy binding)
    {
      return IconAndFontLoader.ICON_BINDING;
    }

    @Override
    public Icon visitSimpleComponentProxy(final SimpleComponentProxy comp)
    {
      final ComponentKind kind = comp.getKind();
      final boolean error = hasErrorIcon(comp);
      return getComponentKindIcon(kind, error);
    }

    @Override
    public Icon visitVariableComponentProxy(final VariableComponentProxy var)
    {
      return IconAndFontLoader.ICON_VARIABLE;
    }
  }


  //#########################################################################
  //# Inner Class ToolTipGetterVisitor
  private static class ToolTipGetterVisitor extends
    DefaultModuleProxyVisitor
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
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    @Override
    public String visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public String visitEventDeclProxy(final EventDeclProxy decl)
    {
      final EventKind kind = decl.getKind();
      return getEventKindToolTip(kind, true);
    }

    @Override
    public String visitInstanceProxy(final InstanceProxy inst)
    {
      return "Module instance";
    }

    @Override
    public String visitParameterBindingProxy(final ParameterBindingProxy binding)
    {
      return "Parameter binding";
    }

    @Override
    public String visitSimpleComponentProxy(final SimpleComponentProxy comp)
    {
      final ComponentKind kind = comp.getKind();
      return getComponentKindToolTip(kind);
    }

    @Override
    public String visitVariableComponentProxy(final VariableComponentProxy var)
    {
      return "EFA variable";
    }

  }


  //#########################################################################
  //# Inner Class PropositionColorCollectorVisitor
  private class PropositionColorCollectorVisitor extends
    DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private PropositionIcon.ColorInfo getPropositionColors(final GraphProxy graph,
                                                           final SimpleNodeProxy node)
    {
      try {
        if (hasPropositions(graph)) {
          mColorList = new LinkedList<Color>();
          mColorSet = new HashSet<Color>();
        }
        mForbidden = false;
        final List<Proxy> props = node.getPropositions().getEventIdentifierList();
        visitCollection(props);
        final PropositionIcon.ColorInfo result =
          new PropositionIcon.ColorInfo(mColorList, mForbidden);
        mColorList = null;
        mColorSet = null;
        return result;
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    @Override
    public Object visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitForeachProxy(final ForeachProxy foreach)
      throws VisitorException
    {
      final List<Proxy> body = foreach.getBody();
      visitCollection(body);
      return null;
    }

    @Override
    public Object visitIdentifierProxy(final IdentifierProxy ident)
    {
      final EventDeclProxy decl = guessEventDecl(ident);
      if (decl != null) {
        final ColorGeometryProxy geo = decl.getColorGeometry();
        if (geo == null) {
          final String name = decl.getName();
          if (name.equals(EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
            mForbidden = true;
          } else if (mColorSet != null
                     && mColorSet.add(EditorColor.DEFAULTMARKINGCOLOR)) {
            mColorList.add(EditorColor.DEFAULTMARKINGCOLOR);
          }
        } else {
          for (final Color colour : geo.getColorSet()) {
            if (mColorSet != null && mColorSet.add(colour)) {
              mColorList.add(colour);
            }
          }
        }
      }
      return null;
    }

    //#######################################################################
    //# Data Members
    private List<Color> mColorList;
    private Set<Color> mColorSet;
    private boolean mForbidden;
  }


  //#########################################################################
  //# Inner Class PropositionFinderVisitor
  private class PropositionFinderVisitor extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private Boolean hasPropositions(final Proxy proxy)
    {
      try {
        return (Boolean) proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    private boolean hasPropositions(final GraphProxy graph)
    {
      try {
        for (final NodeProxy node : graph.getNodes()) {
          final boolean found = (Boolean) node.acceptVisitor(this);
          if (found) {
            return true;
          }
        }
        final LabelBlockProxy blocked = graph.getBlockedEvents();
        if (blocked == null) {
          return false;
        } else {
          final Boolean found = visitEventListExpressionProxy(blocked);
          return found != null && found;
        }
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    @Override
    public Boolean visitProxy(final Proxy proxy)
    {
      return false;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Boolean visitEventListExpressionProxy(final EventListExpressionProxy elist)
      throws VisitorException
    {
      final List<Proxy> list = elist.getEventIdentifierList();
      return visitIdentifiers(list);
    }

    @Override
    public Boolean visitForeachProxy(final ForeachProxy foreach)
      throws VisitorException
    {
      final List<Proxy> body = foreach.getBody();
      return visitIdentifiers(body);
    }

    @Override
    public Boolean visitSimpleIdentifierProxy(final SimpleIdentifierProxy ident)
    {
      final String name = ident.getName();
      if (name.equals(EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
        return false;
      } else {
        return visitIdentifierProxy(ident);
      }
    }

    @Override
    public Boolean visitIdentifierProxy(final IdentifierProxy ident)
    {
      final EventKind kind = guessEventKind(ident);
      if (kind == null) {
        return null;
      } else {
        return kind == EventKind.PROPOSITION;
      }
    }

    @Override
    public Boolean visitSimpleNodeProxy(final SimpleNodeProxy node)
      throws VisitorException
    {
      final PlainEventListProxy props = node.getPropositions();
      final Boolean found = visitEventListExpressionProxy(props);
      return found == null || found;
    }

    //#######################################################################
    //# Auxiliary Methods
    private Boolean visitIdentifiers(final List<? extends Proxy> list)
      throws VisitorException
    {
      Boolean result = false;
      for (final Proxy proxy : list) {
        final Boolean found = (Boolean) proxy.acceptVisitor(this);
        if (found == null) {
          result = null;
        } else if (found) {
          return true;
        }
      }
      return result;
    }
  }

  //#########################################################################
  //# Inner Class WrapperGetterVisitor
  private class WrapperGetterVisitor extends
    DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private ListSubjectWrapper getWrapper(final Proxy proxy)
    {
      try {
        return (ListSubjectWrapper) proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.ProxyVisitor
    @Override
    public ListSubjectWrapper visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public ListSubjectWrapper visitEventDeclProxy(final EventDeclProxy decl)
    {
      return mEventDeclListWrapper;
    }

    @Override
    public ListSubjectWrapper visitEventAliasProxy(final EventAliasProxy decl)
    {
      return mEventAliasListWrapper;
    }

    @Override
    public ListSubjectWrapper visitConstantAliasProxy(final ConstantAliasProxy decl)
    {
      return mConstanAliasListWrapper;
    }

    @Override
    public ListSubjectWrapper visitComponentProxy(final ComponentProxy decl)
    {
      return mComponentListWrapper;
    }

  }

  //#########################################################################
  //# Data Members
  private final ModuleProxy mModule;
  private final CanDropVisitor mCanDropVisitor;
  private final IdentifierNameVisitor mIdentifierNameVisitor;
  private final IconGetterVisitor mIconGetterVisitor;
  private final ToolTipGetterVisitor mToolTipGetterVisitor;
  private final PropositionFinderVisitor mPropositionFinderVisitor;
  private final PropositionColorCollectorVisitor mPropositionColorCollectorVisitor;

  private final WrapperGetterVisitor mWrapperGetter;
  private final ListSubjectWrapper mEventDeclListWrapper;
  private final ListSubjectWrapper mComponentListWrapper;
  private final ListSubjectWrapper mConstanAliasListWrapper;
  private final ListSubjectWrapper mEventAliasListWrapper;
  private final Map<GraphProxy,GraphStatus> mGraphStatusMap;

  private ModuleCompilationErrors mCompilationErrors =
    ModuleCompilationErrors.NONE;

  //#########################################################################
  //# Static Class Variables
  private static Pattern PATTERN = null;

  private static final EventKind[] NODE_DROP_LIST = {EventKind.PROPOSITION};
  private static final EventKind[] EDGE_DROP_LIST =
    {EventKind.CONTROLLABLE, EventKind.UNCONTROLLABLE};

}
