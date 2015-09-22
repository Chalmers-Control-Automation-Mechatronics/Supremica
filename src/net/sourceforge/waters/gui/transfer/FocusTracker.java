//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.SelectionChangedEvent;
import net.sourceforge.waters.gui.observer.Subject;


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
 * <P>This implementation supports two types of focus-owning components,
 * <UL>
 * <LI>WATERS panels implementing the {@link SelectionOwner} interface;</LI>
 * <LI>Swing components that are subclass of {@link JTextComponent}, in
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
  @Override
  public void propertyChange(final PropertyChangeEvent event)
  {
    final String prop = event.getPropertyName();
    if ("permanentFocusOwner".equals(prop)) {
      final Object newvalue = event.getNewValue();
      // System.err.println
      //   ("FocusTracker: " +
      //    (newvalue == null ? "null" : newvalue.getClass().getName()));
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
  @Override
  public void caretUpdate(final CaretEvent event)
  {
    fireSelectionChanged();
  }


  //#########################################################################
  //# Interface java.awt.event.WindowFocusListener
  @Override
  public void windowGainedFocus(final WindowEvent event)
  {
    fireSelectionChanged();
  }

  @Override
  public void windowLostFocus(final WindowEvent event)
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  @Override
  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.SELECTION_CHANGED) {
      fireEditorChangedEvent(event);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Subject
  @Override
  public void attach(final Observer observer)
  {
    mObservers.add(observer);
  }

  @Override
  public void detach(final Observer observer)
  {
    mObservers.remove(observer);
  }

  @Override
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







