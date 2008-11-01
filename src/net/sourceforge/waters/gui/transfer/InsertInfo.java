//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   InsertInfo
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.transfer;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;


/**
 * An auxiliary class to store information about insertions in the module
 * data structure.  In addition to the item inserted, it holds an insert
 * position object of unspecified type. Different panels will use this to
 * store different types of information. Among others, this is used by
 * delete operations to record the positions of deleted items in order to
 * facilitate undo.
 *
 * @see ListInsertPosition
 * @author Robi Malik
 */

public class InsertInfo
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new insert information record with <CODE>null</CODE>
   * insert position.
   * @param  proxy    The item inserted.
   */
  public InsertInfo(final Proxy proxy)
  {
    this(proxy, null);
  }

  /**
   * Creates a new insert information record.
   * @param  proxy    The item inserted.
   * @param  inspos   An object that specfies how and where to insert the
   *                  item.
   */
  public InsertInfo(final Proxy proxy, final Object inspos)
  {
    mProxy = proxy;
    mPosition = inspos;
  }



  //#########################################################################
  //# Simple Access
  /**
   * Gets the item inserted by this operation.
   */
  public Proxy getProxy()
  {
    return mProxy;
  }

  /**
   * Gets the position for the item inserted. The type of the insert
   * position is application-specific, as it depends on the operation how
   * excatly to specify an insertion. The default, <CODE>null</CODE> is
   * used if the insert position can already be determined from the item
   * inserted, e.g., when inserting into a sorted list.
   */
  public Object getInsertPosition()
  {
    return mPosition;
  }


  //#########################################################################
  //# Static Methods
  /**
   * A convenience method to convert a list of <CODE>InsertInfo</CODE>
   * objects back into the list of their proxies.
   */
  public static List<Proxy> getProxies(final List<InsertInfo> inserts)
  {
    final int size = inserts.size();
    final List<Proxy> result = new ArrayList<Proxy>(size);
    for (final InsertInfo insert : inserts) {
      final Proxy proxy = insert.getProxy();
      result.add(proxy);
    }
    return result;
  }


  //#########################################################################
  //# Data Members
  private final Proxy mProxy;
  private final Object mPosition;

}
