//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   ModelChangeEvent
//###########################################################################
//# $Id: ModelChangeEvent.java,v 1.7 2007-08-11 10:44:03 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.base;

import java.util.EventObject;


/**
 * <P>A notification sent by a subject to inform its observers that its
 * state has been changed.</P>
 *
 * <P>All objects in the subject implementation support the <EMPH>observer
 * design pattern</EMPH>. Whenever some aspect of a subject is changed,
 * a <CODE>ModelChangeEvent</CODE> is created and passed to the
 * {@link ModelObserver#modelChanged(ModelChangeEvent) modelChanged()}
 * method of all registered observers, as well as all observers registered
 * on some of its parents.</P>
 *
 * <P>The model change event class provides detailed information about the
 * change that is signalled.  The specific type of change signalled is
 * identified by the event <EMPH>kind</EMPH>, which can take the following
 * values.</P>
 * <UL>
 * <LI>{@link #ITEM_ADDED}</LI>
 * <LI>{@link #ITEM_REMOVED}</LI>
 * <LI>{@link #NAME_CHANGED}</LI>
 * <LI>{@link #STATE_CHANGED}</LI>
 * <LI>{@link #GEOMETRY_CHANGED}</LI>
 * </UL>
 * <P>In addition, each event has its <EMPH>source</EMPH> that identifies
 * the item that was affected by the change, and may have an additional
 * <EMPH>value</EMPH>. The precise meaning of these fields depends on the
 * event kind.</P>
 *
 * @see Subject
 * @see ModelObserver
 *
 * @author Robi Malik
 */

public class ModelChangeEvent extends EventObject
{

  //#########################################################################
  //# Static Creator Methods
  /**
   * Creates an <CODE>ITEM_ADDED</CODE> notification.
   * @param  container    The list or collection that has received a new item.
   * @param  element      The item added to the container.
   * @return A model change event that has the container as source
   *         and the element as value.
   */
  public static ModelChangeEvent createItemAdded(final Subject container,
                                                 final Object element)
  {
    return new ModelChangeEvent(container, ITEM_ADDED, element);
  }

  /**
   * Creates an <CODE>ITEM_REMOVED</CODE> notification.
   * @param  container    The list or collection from which an item has
   *                      been removed.
   * @param  element      The item removed from the container.
   * @return A model change event that has the container as source
   *         and the element as value.
   */
  public static ModelChangeEvent createItemRemoved(final Subject container,
                                                   final Object element)
  {
    return new ModelChangeEvent(container, ITEM_REMOVED, element);
  }

  /**
   * Creates an <CODE>NAME_CHANGED</CODE> notification.
   * @param  item         The item that has been renamed.
   * @param  oldname      The name the item had before the operation.
   * @return A model change event that has the item as source
   *         and the old name as value.
   */
  public static ModelChangeEvent createNameChanged(final Subject item,
                                                   final String oldname)
  {
    return new ModelChangeEvent(item, NAME_CHANGED, oldname);
  }

  /**
   * Creates an <CODE>STATE_CHANGED</CODE> notification.
   * @param  item         The item that has been modified.
   * @return A model change event that has the item as source
   *         and a <CODE>null</CODE> value.
   */
  public static ModelChangeEvent createStateChanged(final Subject item)
  {
    return new ModelChangeEvent(item, STATE_CHANGED);
  }

  /**
   * Creates an <CODE>GEOMETRY_CHANGED</CODE> notification.
   * @param  item         The item that has been renamed.
   * @param  geo          The value of the affected geometry after the change.
   * @return A model change event that has the item as source
   *         and the new geometry information as value.
   */
  public static ModelChangeEvent createGeometryChanged
    (final Subject item, final Object geo)
  {
    return new ModelChangeEvent(item, GEOMETRY_CHANGED, geo);
  }


  //#########################################################################
  //# Constructors
  /**
   * Creates a new model change event with <CODE>null</CODE> value.
   * @param  source       The source of the event.
   * @param  kind         The kind of notification.
   */
  public ModelChangeEvent(final Subject source, final int kind)
  {
    this(source, kind, null);
  }

  /**
   * Creates a new model change event with a specified value.
   * @param  source       The source of the event.
   * @param  kind         The kind of notification.
   * @param  value        The value to be passed with the event.
   */
  public ModelChangeEvent(final Subject source,
                          final int kind,
                          final Object value)
  {
    super(source);
    mKind = kind;
    mValue = value;
  }


  //#########################################################################
  //# Getters
  /**
   * Gets the source of this event.
   * The source generally identifies the object that was directly modified
   * by the change.
   */
  public Subject getSource()
  {
    return (Subject) super.getSource();
  }

  /**
   * Gets the specific kind of notification. There are five possible values:
   * <UL>
   * <LI>{@link #ITEM_ADDED}</LI>
   * <LI>{@link #ITEM_REMOVED}</LI>
   * <LI>{@link #NAME_CHANGED}</LI>
   * <LI>{@link #STATE_CHANGED}</LI>
   * <LI>{@link #GEOMETRY_CHANGED}</LI>
   * </UL>
   */
  public int getKind()
  {
    return mKind;
  }

  /**
   * Gets the value of this event.
   * The value provides additional information to identify the signalled
   * change more specifically. 
   */
  public Object getValue()
  {
    return mValue;
  }


  //#########################################################################
  //# Class Constants
  public static final long serialVersionUID = 1;

  /**
   * A constant representing that no change occured.
   * This can be used as a dummy or default value for the type of
   * a change event.
   */
  public static final int NO_CHANGE = 0x00;
  /**
   * The constant identifying an item-addition notification.  This
   * notification is sent after an item has been added to some collection
   * or list. The event source is the container that has received the new
   * item, and the value is the item that was added. This event is only
   * received by the container that was modified, not by the item that was
   * added. When the message is received, the addition of the item is
   * already completed, so its parent points to the container.
   */
  public static final int ITEM_ADDED = 0x01;
  /**
   * The constant identifying an item-removal notification. This
   * notification is sent after an item has been removed from some
   * collection or list. The event source is the container from which the
   * the item has been removed, and the value is the item that was removed.
   * This event is only received by the container that was modified, not by
   * the removed item. When the message is received, the removal of the
   * item is already completed, so its parent is <CODE>null</CODE>.
   */
  public static final int ITEM_REMOVED = 0x02;
  /**
   * The constant identifying a name change notification.  This
   * notification is sent after the name of a {@link NamedSubject} has been
   * changed. The event source is the item that has been renamed, and the
   * value is the name the item had before the operation. The message is
   * received by the item that has been renamed.
   */
  public static final int NAME_CHANGED = 0x04;
  /**
   * The constant identifying a state change notification.  This
   * notification is sent after the change of an attribute of some
   * subject. The event source is the item that has been modified, and the
   * value is <CODE>null</CODE>. This notification is used for all
   * attribute changes except name and geometry, which have their own event
   * kinds. The message is received by the item whose attribute has been
   * changed.
   */
  public static final int STATE_CHANGED = 0x08;
  /**
   * The constant identifying a geometry change notification.  This
   * notification is sent after the change of the geometry of some
   * subject. The event source is the item whose geometry has been
   * modified, and the value is the affected {@link GeometrySubject} in the
   * state after the change. This message is received by the geometry
   * object that has been changed, or by its parent if the change involved
   * the introduction of a new geometry object.
   */
  public static final int GEOMETRY_CHANGED = 0x10;


  //#########################################################################
  //# Data Members
  private final int mKind;
  private final Object mValue;

}
