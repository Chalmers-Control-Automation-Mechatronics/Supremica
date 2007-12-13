//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   ProxyTransferable
//###########################################################################
//# $Id: ProxyTransferable.java,v 1.4 2007-12-13 23:49:37 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.transfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyCloner;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * <P>A transferable that can copy and paste one or more WATERS
 * objects.</P>
 *
 * <P>In addition to a type-specific data flavour as defined in class
 * {@link WatersDataFlavor}, this class also supports Java's string
 * data flavour ({@link DataFlavor#stringFlavor}): when requested, the
 * data is converted to text form using a {@link ProxyPrinter}.</P>
 *
 * <P>When creating the transferable, the transferred objects are cloned
 * as plain elements. When extracting the data, the user needs to clone
 * them once again into subjects.</P>
 *
 * @author Robi Malik
 */

public class ProxyTransferable<P extends Proxy> implements Transferable
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a transferable that holds a single item.
   */
  ProxyTransferable(final DataFlavor flavor, final P data)
  {
    this(flavor, Collections.singletonList(data));
  }

  /**
   * Creates a transferable that holds a single item.
   */
  ProxyTransferable(final DataFlavor[] flavors, final P data)
  {
    this(flavors, Collections.singletonList(data));
  }

  /**
   * Creates a transferable that holds a whole list of items.
   */
  ProxyTransferable(final DataFlavor flavor, final List<? extends P> data)
  {
    this(new DataFlavor[] {flavor, DataFlavor.stringFlavor}, data);
  }

  /**
   * Creates a transferable that holds a whole list of items.
   */
  ProxyTransferable(final DataFlavor[] flavors, final List<? extends P> data)
  {
    mFlavors = flavors;
    // *** BUG ***
    // This is not good enough for ProductDESProxy ...
    // ***
    final ProxyCloner cloner = ModuleElementFactory.getCloningInstance();
    mData = cloner.getClonedList(data);
  }


  //#########################################################################
  //# Interface java.awt.datatransfer.Transferable
  public Object getTransferData(final DataFlavor flavor)
    throws IOException, UnsupportedFlavorException
  {
    if (mFlavors[0].equals(flavor)) {
      return mData;
    } else if (DataFlavor.stringFlavor.equals(flavor)) {
      final StringWriter writer = new StringWriter();
      for (final Proxy proxy : mData) {
	ProxyPrinter.printProxy(writer, proxy);
        writer.write('\n');
      }
      return writer.toString();
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
  }

  public DataFlavor[] getTransferDataFlavors()
  {
    return mFlavors;
  }

  public boolean isDataFlavorSupported(final DataFlavor flavor)
  {
    for (int i = 0; i < mFlavors.length; i++) {
      if (mFlavors[i].equals(flavor)) {
        return true;
      }
    }
    return false;
  }


  //#########################################################################
  //# Simple Access
  List<P> getRawData()
  {
    return mData;
  }


  //#########################################################################
  //# Data Members
  private final DataFlavor[] mFlavors;
  private final List<P> mData;

}