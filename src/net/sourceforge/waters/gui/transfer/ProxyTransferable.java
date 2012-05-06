//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   ProxyTransferable
//###########################################################################
//# $Id$
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
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;


/**
 * <P>A {@link Transferable} that can hold an arbitrary collection of WATERS
 * objects. Transferables are used to hold data during copy/paste and
 * drag/drop operations. All transferables created by the GUI are instances of
 * this class.</P>
 *
 * <P>This general class is not intended for direct use. To create a
 * transferable, please use the methods {@link
 * WatersDataFlavor#createTransferable(java.util.Collection)
 * createTransferable()} in class {@link WatersDataFlavor}, which can
 * automatically determine the correct data flavours and transferable
 * class for a given list of {@link Proxy} objects.</P>
 *
 * <P>A proxy transferable is created using an array of data flavours
 * and the data. The first data flavour is the <I>primary flavour</I>,
 * which determines how the transferable is initialised. Many transferables
 * will support more data flavours, providing alternative ways of retrieving
 * the data. All transferables also support Java's string data flavour
 * ({@link DataFlavor#stringFlavor}): when requested, the data is converted
 * to text form using a {@link ProxyPrinter}.</P>
 *
 * @see WatersDataFlavor
 * @author Robi Malik
 */

public class ProxyTransferable implements Transferable
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a transferable that holds a list of items.
   * @param  flavors  The data flavours to be supported by the transferable,
   *                  in order of preference.
   * @param  data     The list of objects to be stored in the transferable,
   *                  already cloned and distinct from other objects used
   *                  by the transferable's creator.
   */
  ProxyTransferable(final DataFlavor[] flavors, final List<Proxy> data)
  {
    mData = data;
    mFlavors = flavors;
  }


  //#########################################################################
  //# Interface java.awt.datatransfer.Transferable
  public Object getTransferData(final DataFlavor flavor)
    throws IOException, UnsupportedFlavorException
  {
    if (flavor instanceof WatersDataFlavor) {
      final WatersDataFlavor wflavor = (WatersDataFlavor) flavor;
      final ModuleProxyFactory factory = ModuleSubjectFactory.getInstance();
      return wflavor.createImportData(mData, factory);
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
    final DataFlavor[] flavors = getTransferDataFlavors();
    for (int i = 0; i < flavors.length; i++) {
      if (flavors[i].equals(flavor)) {
        return true;
      }
    }
    return false;
  }


  //#########################################################################
  //# Simple Access
  /**
   * Retrieves the data contained in this transferable.
   * This method directly returns the list of objects stored in the
   * transferable, without any copying.
   * @return An unmodifiable list.
   */
  public List<Proxy> getRawData()
  {
    return Collections.unmodifiableList(mData);
  }


  //#########################################################################
  //# Data Members
  private final List<Proxy> mData;
  private final DataFlavor[] mFlavors;

}