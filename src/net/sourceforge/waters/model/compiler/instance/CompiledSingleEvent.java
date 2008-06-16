//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.instance
//# CLASS:   CompiledSingleEvent
//###########################################################################
//# $Id: CompiledSingleEvent.java,v 1.1 2008-06-16 07:09:51 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.instance;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.printer.ProxyPrinter;

import net.sourceforge.waters.xsd.base.EventKind;


class CompiledSingleEvent implements CompiledEvent
{

  //#########################################################################
  //# Constructor
  CompiledSingleEvent(final CompiledEventDecl decl,
                      final List<? extends SimpleExpressionProxy> indexes)
  {
    mDecl = decl;
    mIndexes = Collections.unmodifiableList(indexes);
    mIdentifier = null;
  }


  //#########################################################################
  //# General Object Handling
  /**
   * Gets a string representation of this event.
   * Used for exceptions.
   */
  public String toString()
  {
    try {
      final StringWriter writer = new StringWriter();
      ProxyPrinter.printProxy(writer, mDecl.getIdentifier());
      for (final SimpleExpressionProxy index : mIndexes) {
        writer.write('[');
        ProxyPrinter.printProxy(writer, index);
        writer.write(']');
      }
      return writer.toString();
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Simple Access
  CompiledEventDecl getCompiledEventDecl()
  {
    return mDecl;
  }

  EventKind getKind()
  {
    return mDecl.getKind();
  }

  List<SimpleExpressionProxy> getIndexes()
  {
    return mIndexes;
  }

  IdentifierProxy getIdentifier()
  {
    return mIdentifier;
  }

  void setIdentifier(final IdentifierProxy ident)
  {
    mIdentifier = ident;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.instance.CompiledEvent
  public int getKindMask()
  {
    final EventKind kind = mDecl.getKind();
    return EventKindMask.getMask(kind);
  }

  public boolean isObservable()
  {
    return mDecl.isObservable();
  }

  public List<CompiledRange> getIndexRanges()
  {
    return Collections.emptyList();
  }

  public Iterator<CompiledSingleEvent> getEventIterator()
  {
    return Collections.singletonList(this).iterator();
  }

  public CompiledEvent find(final SimpleExpressionProxy index)
    throws IndexOutOfRangeException
  {
    throw new IndexOutOfRangeException(this);
  }


  //#########################################################################
  //# Data Members
  private final CompiledEventDecl mDecl;
  private final List<SimpleExpressionProxy> mIndexes;

  private IdentifierProxy mIdentifier;

}
