//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   FocusTracker
//###########################################################################
//# $Id: FocusTracker.java,v 1.3 2007-12-05 06:48:06 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.transfer;

import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JRootPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.SelectionChangedEvent;
import net.sourceforge.waters.gui.observer.Subject;
import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>An auxiliary class to detect which component has the keyboard focus,
 * in order to redirect cut, copy, paste, and similar actions to it.</P>
 *
 * <P>The focus tracker detects changes of keyboard focus to and from
 * supported components, or to the window focus of the entire application,
 * and any changes of the selection in the component that has the keyboard
 * focus. All these changes are remapped to editor events of type {@link
 * EditorChangedEvent.Kind#SELECTION_CHANGED}, and interested components
 * can register listeners through the {@link Subject} interface.</P>
 *
 * <P>This implementation supports to types of focus-owning components,
 * <UL>
 * <LI>WATERS panels implementing the {@link SelectionOwner} interface;</LI>
 * <LI>Swing somponents that are subclass of {@link JTextComponent}, in
 *     order and support standard cut, copy, and paste of text.</LI>
 * </UL>
 *
 * @author Robi Malik
 */


public class FocusTracker
  implements PropertyChangeListener, CaretListener, WindowFocusListener,
             Observer, Subject
{

  //#########################################################################
  //# Constructors
  public FocusTracker(final Window window)
  {
    mObservers = new LinkedList<Observer>();
    mSelectionOwner = null;
    mWatersSelectionOwner = null;
    mSwingSelectionOwner = null;
    final KeyboardFocusManager manager =
      KeyboardFocusManager.getCurrentKeyboardFocusManager();
    manager.addPropertyChangeListener(this);
    window.addWindowFocusListener(this);
  }


  //#########################################################################
  //# Simple Access
  public SelectionOwner getWatersSelectionOwner()
  {
    return mWatersSelectionOwner;
  }

  public JTextComponent getSwingSelectionOwner()
  {
    return mSwingSelectionOwner;
  }


  //#########################################################################
  //# Interface java.beans.PropertyChangeListener
  public void propertyChange(final PropertyChangeEvent event)
  {
    final String prop = event.getPropertyName();
    if ("permanentFocusOwner".equals(prop)) {
      final Object newvalue = event.getNewValue();
      /*
      System.err.println
        ("FocusTracker: " +
         (newvalue == null ? "null" : newvalue.getClass().getName()));
      */
      if (mSelectionOwner != newvalue) {
	if (mWatersSelectionOwner != null) {
	  mWatersSelectionOwner.detach(this);
	} else if (mSwingSelectionOwner != null) {
	  mSwingSelectionOwner.removeCaretListener(this);
	}
	if (newvalue instanceof SelectionOwner) {
	  mSelectionOwner = newvalue;
	  mWatersSelectionOwner = (SelectionOwner) newvalue;
	  mSwingSelectionOwner = null;
	  mWatersSelectionOwner.attach(this);
	} else if (newvalue instanceof JTextComponent) {
	  mSelectionOwner = newvalue;
	  mWatersSelectionOwner = null;
	  mSwingSelectionOwner = (JTextComponent) newvalue;
	  mSwingSelectionOwner.addCaretListener(this);
	} else if (mSelectionOwner != null) {
	  mSelectionOwner = null;
	  mWatersSelectionOwner = null;
	  mSwingSelectionOwner = null;
	} else {
          return;
        }
        fireSelectionChanged();
      }
    }
  }


  //#########################################################################
  //# Interface javax.swing.event.CaretListener
  public void caretUpdate(final CaretEvent event)
  {
    fireSelectionChanged();
  }
 

  //#########################################################################
  //# Interface java.awt.event.WindowFocusListener
  public void windowGainedFocus(final WindowEvent event)
  {
    fireSelectionChanged();
  }

  public void windowLostFocus(final WindowEvent event)
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.SELECTION_CHANGED) {
      fireEditorChangedEvent(event);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Subject
  public void attach(final Observer observer)
  {
    mObservers.add(observer);
  }
    
  public void detach(final Observer observer)
  {
    mObservers.remove(observer);
  }
    
  public void fireEditorChangedEvent(final EditorChangedEvent event)
  {
    // Just in case they try to register or deregister observers
    // in response to the update ...
    final List<Observer> copy = new ArrayList<Observer>(mObservers);
    for (final Observer observer : copy) {
      observer.update(event);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void fireSelectionChanged()
  {
    final EditorChangedEvent event = new SelectionChangedEvent(this);
    fireEditorChangedEvent(event);
  }


  //#########################################################################
  //# Data Members
  private final List<Observer> mObservers;

  private Object mSelectionOwner;
  private SelectionOwner mWatersSelectionOwner;
  private JTextComponent mSwingSelectionOwner;

}