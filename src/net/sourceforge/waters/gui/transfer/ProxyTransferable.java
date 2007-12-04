//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   ProxyTransferable
//###########################################################################
//# $Id: ProxyTransferable.java,v 1.2 2007-12-04 03:22:55 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.transfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyCloner;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * <P>A transferable that can copy and paster one or more WATERS
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

public class ProxyTransferable implements Transferable
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a transferable that holds a single item.
   */
  public ProxyTransferable(final DataFlavor flavor, final Proxy data)
  {
    this(flavor, Collections.singletonList(data));
  }

  /**
   * Creates a transferable that holds a whole list of items.
   */
  public ProxyTransferable(final DataFlavor flavor,
                           final List<? extends Proxy> data)
  {
    mFlavor = flavor;
    mFlavors = new DataFlavor[] {flavor, DataFlavor.stringFlavor};

    // This is not good enough for AutomatonProxy ...
    final ProxyCloner cloner = ModuleElementFactory.getCloningInstance();
    final int size = data.size();
    mData = new ArrayList<Proxy>(size);
    for (final Proxy proxy : data) {
      final Proxy cloned = cloner.getClone(proxy);
      mData.add(cloned);
    }
  }


  //#########################################################################
  //# Interface java.awt.datatransfer.Transferable
  public Object getTransferData(final DataFlavor flavor)
    throws IOException, UnsupportedFlavorException
  {
    if (mFlavor.equals(flavor)) {
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
    return
      isSameFlavor(mFlavor, flavor) ||
      DataFlavor.stringFlavor.equals(flavor);
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean isSameFlavor(final DataFlavor flavor1,
                               final DataFlavor flavor2)
  {
    if (flavor1.equals(flavor2)) {
      final String name1 = flavor1.getHumanPresentableName();
      final String name2 = flavor2.getHumanPresentableName();
      return name1.equals(name2);
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Data Members
  private final DataFlavor mFlavor;
  private final DataFlavor[] mFlavors;
  private final List<Proxy> mData;

}