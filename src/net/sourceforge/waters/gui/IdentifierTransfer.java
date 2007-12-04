//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   IdentifierTransfer
//###########################################################################
//# $Id: IdentifierTransfer.java,v 1.5 2007-12-04 03:22:54 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.util.List;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.Transferable;

import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.xsd.base.EventKind;


public class IdentifierTransfer implements Transferable
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new transferable object containing the specified identifier.
   * @param idents  The identifier being transferred.
   * @param kind   The associated event kind.
   */
  public IdentifierTransfer(final List<IdentifierSubject> idents,
                            final EventType kind)
  {
    mData = new IdentifierWithKind(idents, kind);
  }


  //#########################################################################
  //# Interface java.awt.datatransfer.Transferable
  public IdentifierWithKind getTransferData(final DataFlavor flavor)
    throws UnsupportedFlavorException
  {
    if (isDataFlavorSupported(flavor)) {
      return mData;
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
  }

  public DataFlavor[] getTransferDataFlavors()
  {
    return FLAVORS;
  }

  public boolean isDataFlavorSupported(final DataFlavor flavor)
  {
    final Class otherclass = flavor.getRepresentationClass();
    final Class thisclass = FLAVOR.getRepresentationClass();
    return otherclass.isAssignableFrom(thisclass);
  }


  //#########################################################################
  //# Data Members
  private final IdentifierWithKind mData;


  //#########################################################################
  //# Class Constants
  private static final Class DATA_CLASS = IdentifierWithKind.class;
  private static final DataFlavor FLAVOR =
    new DataFlavor(DATA_CLASS, DATA_CLASS.getName());
  private static final DataFlavor[] FLAVORS = {FLAVOR};

}