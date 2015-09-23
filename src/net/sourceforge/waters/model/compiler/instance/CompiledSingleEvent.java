//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.model.compiler.instance;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
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

  public CompiledEvent find(final SimpleExpressionProxy index)
    throws IndexOutOfRangeException
  {
    throw new IndexOutOfRangeException(this);
  }

  public SourceInfo getSourceInfo()
  {
    return null;
  }

  public Iterator<CompiledEvent> getChildrenIterator()
  {
    return null;
  }


  //#########################################################################
  //# Data Members
  private final CompiledEventDecl mDecl;
  private final List<SimpleExpressionProxy> mIndexes;

  private IdentifierProxy mIdentifier;

}
