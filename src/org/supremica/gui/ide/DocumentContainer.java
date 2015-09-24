//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2015 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui.ide;

import java.awt.Component;
import java.io.File;
import java.net.MalformedURLException;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.subject.base.DocumentSubject;


public abstract class DocumentContainer
{

  //#########################################################################
  //# Constructor
  public DocumentContainer(final IDE ide, final DocumentProxy document)
  {
    mIDE = ide;
    mDocument = document;
    mDocumentNameHasChanged = false;
  }


  //#3#######################################################################
  //# Simple Access
  public IDE getIDE()
  {
    return mIDE;
  }

  public String getName()
  {
    return mDocument.getName();
  }

  public DocumentProxy getDocument()
  {
    return mDocument;
  }


  //#########################################################################
  //# To be Overridden by Subclasses
  boolean hasUnsavedChanges()
  {
    return false;
  }

  void setCheckPoint()
  {
  }

  void setDocumentNameHasChanged(final boolean changed)
  {
    mDocumentNameHasChanged = changed;
  }

  /**
   * Cleans up. This method is called by the GUI to notify that the
   * document of this container has been closed by the user. It should
   * unregister all listeners on external components and perform any
   * other cleanup that may be necessary. The component does not have to
   * support any other methods once <CODE>close()</CODE> has been called.
   */
  void close()
  {
  }


  public abstract Component getPanel();

  public abstract EditorPanel getEditorPanel();

  public abstract AnalyzerPanel getAnalyzerPanel();

  public abstract Component getActivePanel();

  public abstract String getTypeString();


  //#######################################################################
  //# Titling
  public File getFileLocation()
  {
    final DocumentProxy doc = getDocument();
    return getFileLocation(doc);
  }

  public String getWindowTitle()
  {
    final String type = getTypeString();
    final DocumentProxy doc = getDocument();
    final String name = doc.getName();
    final File file = getFileLocation();
    final StringBuilder buffer = new StringBuilder(type);
    if (name != null && !name.equals(""))
    {
      buffer.append(": ");
      buffer.append(name);
    }
    if (file != null)
    {
      buffer.append(" [");
      buffer.append(file);
      buffer.append(']');
    }
    return buffer.toString();
  }

  void adjustDocumentName(final File file)
  {
    if (!mDocumentNameHasChanged) {
      String tail = file.getName();
      final int dotpos = tail.indexOf('.');
      if (dotpos >= 0) {
        tail = tail.substring(0, dotpos);
      }
      if (tail.length() > 0) {
        final DocumentSubject subject = (DocumentSubject) mDocument;
        subject.setName(tail);
      }
    }
    mDocumentNameHasChanged = false;
  }


  //#######################################################################
  //# Auxiliary Static Access
  static File getFileLocation(final DocumentProxy doc)
  {
    try
    {
      return doc.getFileLocation();
    }
    catch (final MalformedURLException exception)
    {
      return null;
    }
  }


  //#######################################################################
  //# Data Members
  private final DocumentProxy mDocument;
  private final IDE mIDE;

  private boolean mDocumentNameHasChanged;

}
