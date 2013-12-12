//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   ModuleContainer
//###########################################################################
//# $Id$
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