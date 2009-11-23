package net.sourceforge.waters.gui.simulator;

import java.util.EventObject;

import net.sourceforge.waters.gui.observer.Subject;


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
* <LI>{@link #MODEL_CHANGED}</LI>
* </UL>
* <P>In addition, each event has its <EMPH>source</EMPH> that identifies
* the item that was affected by the change, and may have an additional
* <EMPH>value</EMPH> and an index representing the position of a change in
* a list. The precise meaning of these fields depends on the event
* kind.</P>
*
* @see Subject
* @see ModelObserver
*
* @author Andrew Holland
*/

public class SimulationChangeEvent extends EventObject
{

//#########################################################################
//# Static Creator Methods
/**
 * Creates an <CODE>MODEL_CHANGED</CODE> notification.
 * @param  container    The list or collection that has received a new item.
 * @param  element      The item added to the container.
 * @return A model change event that has the container as source
 *         and the element as value.
 */
public static SimulationChangeEvent createdModelChanged(final Subject container,
                                               final Object element)
{
  return new SimulationChangeEvent(container, MODEL_CHANGED, element);
}



//#########################################################################
//# Constructors
/**
 * Creates a new model change event with <CODE>null</CODE> value.
 * @param  source       The source of the event.
 * @param  kind         The kind of notification.
 */
public SimulationChangeEvent(final Subject source, final int kind)
{
  this(source, kind, null);
}

/**
 * Creates a new model change event with a specified value.
 * @param  source       The source of the event.
 * @param  kind         The kind of notification.
 * @param  value        The value to be passed with the event.
 */
public SimulationChangeEvent(final Subject source,
                        final int kind,
                        final Object value)
{
  this(source, kind, value, -1);
}


/**
 * Creates a new model change event with a specified value.
 * @param  source       The source of the event.
 * @param  kind         The kind of notification.
 * @param  value        The value to be passed with the event.
 * @param  index        The index specifying where the value was inserted
 *                      or deleted in its parent.
 */
public SimulationChangeEvent(final Subject source,
                        final int kind,
                        final Object value,
                        final int index)
{
  super(source);
  mKind = kind;
  mValue = value;
  mIndex = index;
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
 * Gets the specific kind of notification. There is only one possible value:
 * <UL>
 * <LI>{@link #MODEL_CHANGED)</LI>
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

/**
 * Gets the index associated with this event.
 * For an item-addition notification in a list, the index specifies the
 * position of the inserted item after the insertion.
 * For an item-removal notification in a list, the index specifies the
 * position of the removed item before the deletion.
 * In all other cases, the index is -1.
 */
public int getIndex()
{
  return mIndex;
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
 * The constant identifying a model change notification.  This
 * notification is sent after a new simulation has been loaded, or
 * the simulation is modified in some way
 * Arguments are to be added later, once I understand what this class actually
 * does
 */
public static final int MODEL_CHANGED = 0x10;


//#########################################################################
//# Data Members
private final int mKind;
private final Object mValue;
private final int mIndex;

}

