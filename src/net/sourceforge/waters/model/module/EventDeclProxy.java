//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   EventDeclProxy
//###########################################################################
//# $Id: EventDeclProxy.java,v 1.10 2007-06-08 10:57:34 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.util.List;

import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;


/**
 * <P>An event declaration.</P>
 *
 * <P>Event declarations are used to declare an event or array of events by
 * introducing its name and giving the required type information. They are
 * found in the event declaration list of a module ({@link
 * ModuleProxy}).</P>
 *
 * <P>Each event declaration has the following components.</P>
 * <DL>
 * <DT><STRONG>Name.</STRONG></DT>
 * <DD>A string defining the name of the event. It is unique within an
 * a module and must obey syntactic restrictions for identifiers.</DD>
 * <DT><STRONG>Kind.</STRONG></DT>
 * <DD>The type of the events obtained from the declaration.
 * This can be <I>controllable</I>, <I>uncontrollable</I>,
 * or <I>proposition</I>.</DD>
 * <DT><STRONG>Observability.</STRONG></DT>
 * <DD>A boolean flag, indicating whether the events obtained from the
 * declaration are considered <I>observable</I>.</DD>
 * <DT><STRONG>Indexes.</STRONG></DT>
 * <DD>Event arrays are declared by specifying a list of index ranges,
 * each defining the possible range of indexes at one index position.</DD>
 * <DT><STRONG>Color Geometry.</STRONG></DT>
 * <DD>Events of kind <I>proposition</I> may have a color associated to
 * them, which then defines how nodes marked with the proposition are
 * rendered.</DD>
 * </DL>
 *
 * <P>An event can be declared as a parameter to be bound to an event or
 * set of events when a module is compiled. An event parameter behaves like
 * an event declaration in that it introduces an event or event array that
 * can be used in the module's components.  The only difference is that the
 * events do not come from their module itself, but from another module
 * from which their module is instantiated. The documentation of class
 * {@link ModuleProxy} contains an example demonstrating the use of event
 * parameters.</P>
 *
 * @author Robi Malik
 */
// @short event declaration

public interface EventDeclProxy extends NamedProxy {

  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the kind of this event declaration.
   * @return One of {@link EventKind#CONTROLLABLE},
   *         {@link EventKind#UNCONTROLLABLE}, or
   *         {@link EventKind#PROPOSITION}.
   */
  public EventKind getKind();

  /**
   * Gets the observability status of this event declaration.
   * @return <CODE>true</CODE> if the event declaration is observable,
   *         <CODE>false</CODE> otherwise.
   */
  // @default true
  public boolean isObservable();

  /**
   * Gets the scope of this event declaration.
   * @return One of {@link ScopeKind#LOCAL},
   *         {@link ScopeKind#OPTIONAL_PARAMETER}, or
   *         {@link ScopeKind#REQUIRED_PARAMETER}.
   */
  // @default ScopeKind.LOCAL
  public ScopeKind getScope();

  /**
   * Gets the list of index ranges of this event declaration.
   * @return A (modifiable) list of expressions, each identifying
   *         range of array indexes for the corresponding dimension.
   *         Each element is of type {@link SimpleExpressionProxy}.
   */
  public List<SimpleExpressionProxy> getRanges();

  /**
   * Gets the color information for this event declaration.
   * Events of kind <I>proposition</I> may have a color associated to them,
   * which defines how nodes marked with the proposition are rendered. This
   * information may be missing, in which case a default color is to be
   * used, and it is meaningless for events that are not of kind
   * <I>proposition</I>.
   * @return A color geometry object, or <CODE>null</CODE> if none
   *         is specified.
   */
  public ColorGeometryProxy getColorGeometry();

  /**
   * The name to be used for the default marking proposition.
   * This constant is provided for use by tools that do not support
   * multiple propositions/marking conditions. It is recommended that
   * they use proposition events with this default name to label their
   * marked states. In this way it is more likely that the same name
   * is used consistently.
   */
  public static final String DEFAULT_MARKING_NAME = ":accepting";

  /**
   * The name to be used for the default forbidden proposition.
   * It is recommended that they use proposition events with this default
   * name to label their forbidden states. In this way it is more likely
   * that the same name is used consistently.
   */
  public static final String DEFAULT_FORBIDDEN_NAME = ":forbidden";

}
