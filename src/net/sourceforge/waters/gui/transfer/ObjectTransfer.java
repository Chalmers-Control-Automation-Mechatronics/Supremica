package net.sourceforge.waters.gui.transfer;

import java.util.Collection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.Transferable;

import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.xsd.base.EventKind;


public class ObjectTransfer implements Transferable
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new transferable object containing the specified identifier.
   * @param idents  The identifier being transferred.
   * @param kind   The associated event kind.
   */
  public ObjectTransfer(final Object data)
  {
    mData = data;
    mFlavor = new DataFlavor(data.getClass(),
                                        data.getClass().getName());
    mFlavors = new DataFlavor[] {mFlavor};
  }


  //#########################################################################
  //# Interface java.awt.datatransfer.Transferable
  public Object getTransferData(final DataFlavor flavor)
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
    return mFlavors;
  }

  public boolean isDataFlavorSupported(final DataFlavor flavor)
  {
    final Class otherclass = flavor.getRepresentationClass();
    final Class thisclass = mFlavor.getRepresentationClass();
    return otherclass.isAssignableFrom(thisclass);
  }

  //#########################################################################
  //# Data Members
  private final Object mData;


  //#########################################################################
  //# Class Constants
  private final DataFlavor mFlavor;
  private final DataFlavor[] mFlavors;

}
