//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   EventDeclTransferable
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.transfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;


/**
 * <P>A transferable that can hold a list of WATERS event declarations
 * ({@link EventDeclProxy}) objects.</P>
 *
 * <P>This extension of the base class {@link ProxyTransferable} also
 * supports the identifier list data flavour ({@link
 * WatersDataFlavor#IDENTIFIER_LIST}) in addition to the standard event
 * declaration list ({@link WatersDataFlavor#EVENTDECL_LIST}).</P>
 *
 * @author Robi Malik
 */

public class EventDeclTransferable extends ProxyTransferable<EventDeclProxy>
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a transferable that holds a single event declaration.
   */
  public EventDeclTransferable(final EventDeclProxy data)
  {
    super(FLAVORS, data);
    mIdentifierList = null;
  }

  /**
   * Creates a transferable that holds a whole list of event declarations.
   */
  public EventDeclTransferable(final List<? extends EventDeclProxy> data)
  {
    super(FLAVORS, data);
    mIdentifierList = null;
  }


  //#########################################################################
  //# Interface java.awt.datatransfer.Transferable
  public Object getTransferData(final DataFlavor flavor)
    throws IOException, UnsupportedFlavorException
  {
    if (WatersDataFlavor.IDENTIFIER_LIST.equals(flavor)) {
      if (mIdentifierList == null) {
        final List<EventDeclProxy> data = getRawData();
        final int size = data.size();
        mIdentifierList = new ArrayList<IdentifierProxy>(size);
        for (final EventDeclProxy decl : data) {
          final IdentifierProxy ident = decl.getIdentifier();
          mIdentifierList.add(ident);
        }
      }
      return mIdentifierList;
    } else {
      return super.getTransferData(flavor);
    }
  }


  //#########################################################################
  //# Data Members
  private List<IdentifierProxy> mIdentifierList;


  //#########################################################################
  //# Class Constants
  private static final DataFlavor[] FLAVORS = {
    WatersDataFlavor.EVENTDECL_LIST,
    WatersDataFlavor.IDENTIFIER_LIST,
    DataFlavor.stringFlavor
  };

}