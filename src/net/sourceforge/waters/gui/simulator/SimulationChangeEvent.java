package net.sourceforge.waters.gui.simulator;

import java.util.EventObject;

import net.sourceforge.waters.gui.observer.Subject;


/**
 * <P>
 * A notification sent by a subject to inform its observers that its state has
 * been changed.
 * </P>
 *
 * <P>
 * All objects in the subject implementation support the <EMPH>observer design
 * pattern</EMPH>. Whenever some aspect of a subject is changed, a
 * <CODE>ModelChangeEvent</CODE> is created and passed to the
 * {@link ModelObserver#modelChanged(ModelChangeEvent) modelChanged()} method of
 * all registered observers, as well as all observers registered on some of its
 * parents.
 * </P>
 *
 * <P>
 * The model change event class provides detailed information about the change
 * that is signalled. The specific type of change signalled is identified by the
 * event <EMPH>kind</EMPH>, which can take the following values.
 * </P>
 * <UL>
 * <LI>{@link #MODEL_CHANGED}</LI>
 * <LI>{@link #STATE_CHANGED}</LI>
 * </UL>
 * <P>
 * In addition, each event has its <EMPH>source</EMPH> that identifies the item
 * that was affected by the change, and may have an additional
 * <EMPH>value</EMPH> and an index representing the position of a change in a
 * list. The precise meaning of these fields depends on the event kind.
 * </P>
 *
 * @see Subject
 * @see ModelObserver
 *
 * @author Andrew Holland
 */

public class SimulationChangeEvent extends EventObject
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new simulation change event with <CODE>null</CODE> value.
   *
   * @param source
   *          The source of the event.
   * @param kind
   *          The kind of notification.
   */
  public SimulationChangeEvent(final Simulation source, final int kind)
  {
    super(source);
    mKind = kind;
  }

  //#########################################################################
  //# Simple Access
  /**
   * Gets the source of this event.
   */
  public Simulation getSource()
  {
    return (Simulation) super.getSource();
  }

  /**
   * Gets the specific kind of notification. There are two possible values
   * <UL>
   * <LI>{@link #MODEL_CHANGED)</LI>
   * <LI>{@link #STATE_CHANGED)</LI>
   * </UL>
   */
  public int getKind()
  {
    return mKind;
  }


  //#########################################################################
  //# Data Members
  private final int mKind;


  //#########################################################################
  //# Class Constants
  /**
   * The constant identifying a model change notification. This notification is
   * sent after a new simulation has been loaded, or a new ProductDES has been
   * compiled and loaded into the simulation.
   */
  public static final int MODEL_CHANGED = 1;

  /**
   * The constant identifying a state change notification.
   * This notification is sent when the current state of the simulation has
   * changed, for example after an event has been executed.
   */
  public static final int STATE_CHANGED = 2;


  private static final long serialVersionUID = 1;

}
