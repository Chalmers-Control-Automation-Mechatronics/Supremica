//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.instance
//# CLASS:   CompiledEventDecl
//###########################################################################
//# $Id: CompiledEventDecl.java,v 1.1 2008-06-16 07:09:51 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.instance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A compiler-internal representation of an event declaration.</P>
 *
 * <P>This class supports the mapping from a module's event declarations
 * and event expressions to the simple events later to be used in a
 * product DES.</P>
 *
 * @author Robi Malik
 */

class CompiledEventDecl
{

  //#########################################################################
  //# Constructors
  CompiledEventDecl(final CompiledNameSpace namespace,
                    final EventDeclProxy decl,
                    final List<CompiledRange> ranges)
  {
    mNameSpace = namespace;
    mDecl = decl;
    mRanges = ranges;
    int size = 1;
    for (final CompiledRange range : ranges) {
      size *= range.size();
    }
    mIndexValueMap =
      new HashMap<List<SimpleExpressionProxy>,CompiledEvent>(size);
  }


  //#########################################################################
  //# Simple Access
  CompiledNameSpace getNameSpace()
  {
    return mNameSpace;
  }

  EventDeclProxy getEventDeclProxy()
  {
    return mDecl;
  }

  IdentifierProxy getIdentifier()
  {
    return mDecl.getIdentifier();
  }

  EventKind getKind()
  {
    return mDecl.getKind();
  }

  boolean isObservable()
  {
    return mDecl.isObservable();
  }

  int getArity()
  {
    return mRanges.size();
  }

  List<CompiledRange> getRanges()
  {
    return mRanges;
  }

  CompiledRange getRange(final int index)
  {
    return mRanges.get(index);
  }


  //#########################################################################
  //# Auxiliary Methods
  CompiledEvent getCompiledEvent()
  {
    final List<SimpleExpressionProxy> empty = Collections.emptyList();
    return getCompiledEvent(empty);
  }

  CompiledEvent getCompiledEvent(List<? extends SimpleExpressionProxy> indexes)
  {
    CompiledEvent result = mIndexValueMap.get(indexes);
    if (result == null) {
      final List<SimpleExpressionProxy> indexcopy;
      if (indexes.isEmpty()) {
        indexcopy = Collections.emptyList();
      } else {
        indexcopy = new ArrayList<SimpleExpressionProxy>(indexes);
      }
      if (indexes.size() < mRanges.size()) {
	result = new CompiledArrayEvent(this, indexcopy);
      } else {
	result = new CompiledSingleEvent(this, indexcopy);
      }
      mIndexValueMap.put(indexcopy, result);
    }
    return result;
  }

  void checkIndex(final int pos, final SimpleExpressionProxy value)
    throws IndexOutOfRangeException
  {
    final CompiledRange range = mRanges.get(pos);
    if (!range.contains(value)) {
      throw new IndexOutOfRangeException(value, range);
    }
  }


  //#########################################################################
  //# Data Members
  private final CompiledNameSpace mNameSpace;
  private final EventDeclProxy mDecl;
  private final List<CompiledRange> mRanges;
  private final Map<List<SimpleExpressionProxy>,CompiledEvent> mIndexValueMap;

}
