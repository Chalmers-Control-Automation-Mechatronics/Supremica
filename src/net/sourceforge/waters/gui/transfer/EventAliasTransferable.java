//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   ComponentTransferable
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.transfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * <P>A transferable that can hold a list of event aliases</P>
 *
 * <P>This transferable can hold all types of objects that can occur
 * in an event alias, i.e,</P>
 * <P>All the functionality of this transferable is included in the base
 * class {@link ProxyTransferable}. The extension is only done to have
 * a separate class for the data flavour.</P>
 *
 * @author Robi Malik
 */

public class EventAliasTransferable extends ProxyTransferable<Proxy>
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a transferable that holds a single event declaration.
   */
  public EventAliasTransferable(final Proxy data)
  {
    super(FLAVORS, data);
    mIdentifierList = null;
    mTypeVisitor = new TypeVisitor();
  }

  /**
   * Creates a transferable that holds a whole list of event declarations.
   */
  public EventAliasTransferable(final List<? extends Proxy> data)
  {
    super(FLAVORS, data);
    mIdentifierList = null;
    mTypeVisitor = new TypeVisitor();
  }

  //#########################################################################
  //# Interface java.awt.datatransfer.Transferable
  public Object getTransferData(final DataFlavor flavor)
    throws IOException, UnsupportedFlavorException
  {
    if (WatersDataFlavor.IDENTIFIER_LIST.equals(flavor)) {
      if (mIdentifierList == null) {
        final List<Proxy> data = getRawData();
        mIdentifierList = new ArrayList<Proxy>(data.size());
        for (final Proxy proxy : data) {
          final Proxy p = mTypeVisitor.getIdentifier(proxy);
          mIdentifierList.add(p);
        }
      }
      return mIdentifierList;
    } else {
      return super.getTransferData(flavor);
    }
  }

//#########################################################################
  //# Inner Class TypeVisitor
  private class TypeVisitor extends AbstractModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private Proxy getIdentifier(final Proxy proxy)
    {
      try {
        return (Proxy) proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

  //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
    @Override
    public IdentifierProxy visitEventAliasProxy(final EventAliasProxy alias)
    {
      return alias.getIdentifier();
    }

    @Override
    public ForeachProxy visitForeachProxy(final ForeachProxy foreach)
    {
      final List<Proxy> newBody = new ArrayList<Proxy>(foreach.getBody().size());
      for(final Proxy proxy : foreach.getBody()){
        final Proxy identifier = getIdentifier(proxy);
        newBody.add(identifier);
      }
      return ModuleElementFactory.getInstance().createForeachProxy
      (foreach.getName(), foreach.getRange(), foreach.getGuard(), newBody);
    }

    @Override
    public IdentifierProxy visitIdentifierProxy(final IdentifierProxy proxy)
      throws VisitorException
    {
      return proxy;
    }
  }

  private final TypeVisitor mTypeVisitor;
  private List<Proxy> mIdentifierList;

  //#########################################################################
  //# Class Constants
  private static final DataFlavor[] FLAVORS = {
    WatersDataFlavor.EVENT_ALIAS_LIST,
    WatersDataFlavor.IDENTIFIER_LIST,
    DataFlavor.stringFlavor
  };

}