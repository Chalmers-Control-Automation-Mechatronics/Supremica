//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   Subject
//###########################################################################
//# $Id: Subject.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.base;

/**
 * <P>The common interface for all Waters elements.</P>
 *
 * @author Robi Malik
 */

public interface Subject {

  //#########################################################################
  //# Hierarchy
  /**
   * Gets the parent of this subject.
   * @return The parent of this subject, or <CODE>null</CODE> if this
   *         subject does not have any parent.
   */
  public Subject getParent();

  /**
   * Gets the document this subject belongs to. The document is the
   * root of the parent hierarchy, i.e., the ancestor that does not
   * have a parent anymore.
   * @return The document of this subject, or <CODE>null</CODE> if this
   *         subject or some of its parents has not yet been added to any
   *         document.
   */
  public DocumentSubject getDocument();

  /**
   * Sets the parent of this subject can be set to the given new value.
   * A non-<CODE>null</CODE> parent can only be assigned to a subject
   * that does not have a parent assigned yet.
   * @param  parent  The new parent to be assigned, or <CODE>null</CODE>
   *                 to reset the subject's parent.
   * @throws IllegalStateException to indicate that the <CODE>parent</CODE>
   *                 argument is not <CODE>null</CODE> and this subject
   *                 already has a non-<CODE>null</CODE> parent.
   */
  public void setParent(Subject parent);

  /**
   * Checks whether the parent of this subject can be set to the given
   * new value. A new parent can only be assigned to a subject that
   * does not have a parent assigned yet. This method can be used to
   * check whether a call to {@link #setParent(Subject) setParent()}
   * will throw an exception.
   * @param  parent  The new parent to be tested.
   * @throws IllegalStateException if the <CODE>parent</CODE> argument
   *                 is not <CODE>null</CODE> and this subject already
   *                 has a non-<CODE>null</CODE> parent.
   */
  public void checkSetParent(Subject parent);


  //#########################################################################
  //# Observers
  public void addModelObserver(ModelObserver observer);

  public void removeModelObserver(ModelObserver observer);

  public void fireModelChanged(ModelChangeEvent event);

}
