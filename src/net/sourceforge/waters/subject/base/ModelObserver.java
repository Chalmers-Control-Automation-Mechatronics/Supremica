//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   ModelObserver
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.subject.base;

/**
 * The callback interface for changes in modifiable Waters data.
 * A model observer is registered on a {@link Subject} using {@link
 * Subject#addModelObserver(ModelObserver) addModelObserver()} to be called
 * whenever the data of the subject or one of its children gets modified
 * in some way.
 *
 * @see Subject
 * @see ModelChangeEvent
 *
 * @author Robi Malik
 */

public interface ModelObserver {

  /**
   * Notifies the recipient of a change to the observed {@link Subject}.
   * @param  event    A model change event containing information about
   *                  what specific change was made to the model.
   */
  public void modelChanged(ModelChangeEvent event);

  /**
   * Gets the priority of this model observer. If more than one model
   * observer is registered with the same subject, the priority determines
   * the order in which they are called. Observers with a smaller number
   * as their priority are called first. For observer with equal priority,
   * the order is unspecified.
   */
  public int getModelObserverPriority();


  /**
   * Constant defining very high priority.
   */
  public static final int CLEANUP_PRIORITY_0 = 0;

  /**
   * Constant defining high priority.
   */
  public static final int CLEANUP_PRIORITY_1 = 10;

  /**
   * Constant defining medium priority.
   */
  public static final int DEFAULT_PRIORITY = 20;

  /**
   * Constant defining low priority.
   */
  public static final int RENDERING_PRIORITY = 30;

}
