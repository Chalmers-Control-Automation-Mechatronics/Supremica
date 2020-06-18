//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.printer.ProxyPrinter;


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
  @Override
  public Object getTransferData(final DataFlavor flavor)
    throws IOException, UnsupportedFlavorException
  {
    if (flavor instanceof WatersDataFlavor) {
      final WatersDataFlavor wflavor = (WatersDataFlavor) flavor;
      return wflavor.createImportData(mData);
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

  @Override
  public DataFlavor[] getTransferDataFlavors()
  {
    return mFlavors;
  }

  @Override
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
