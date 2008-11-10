//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAEventNameBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.dnf.CompiledClause;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.printer.ModuleProxyPrinter;


/**
 * A simple algorithm to determine reasonably short name suffixes for
 * EFA events. EFA event names are suffixed by a string representing
 * the guard conditions for each event. This class transforms clauses
 * representing guard conditions into canonical names, by sorting the
 * literals, and by removing literals that are not needed to distinguish
 * an event name suffix from others.
 *
 * @see EFAEvent
 * @see EFAEventDecl
 * @author Robi Malik
 */

class EFAEventNameBuilder {

  //#########################################################################
  //# Constructor
  EFAEventNameBuilder(final ModuleProxyFactory factory,
                      final Comparator<SimpleExpressionProxy> comparator)
  {
    mComparator = comparator;
    mAllLiterals = null;
    mAllClauses = null;
    mNumClauses = 0;
    mRoot = null;
  }


  //#########################################################################
  //# Invocation
  void restart()
  {
    mAllLiterals = new HashMap<CountedLiteral,CountedLiteral>();
    mAllClauses = new LinkedList<List<CountedLiteral>>();
    mNumClauses = 0;
    mRoot = null;
  }

  void addClause(final CompiledClause clause)
  {
    final int size = clause.size();
    final List<CountedLiteral> cclause = new ArrayList<CountedLiteral>(size);
    for (final SimpleExpressionProxy literal : clause.getLiterals()) {
      final CountedLiteral counted = createCountedLiteral(literal);
      counted.addOccurrence();
      cclause.add(counted);
    }
    mAllClauses.add(cclause);
  }

  String getNameSuffix(final CompiledClause clause)
  {
    buildTree();
    final int size = clause.size();
    final List<CountedLiteral> cclause = new ArrayList<CountedLiteral>(size);
    for (final SimpleExpressionProxy literal : clause.getLiterals()) {
      final CountedLiteral counted = getCountedLiteral(literal);
      cclause.add(counted);
    }
    Collections.sort(cclause);
    final Iterator<CountedLiteral> iter = cclause.iterator();
    final StringWriter writer = new StringWriter();
    final ModuleProxyPrinter printer = new ModuleProxyPrinter(writer);
    mRoot.print(printer, iter, true);
    final StringBuffer buffer = writer.getBuffer();
    if (buffer.length() > 0) {
      buffer.append('}');
    }
    return buffer.toString();
  }

  void clear()
  {
    mAllLiterals = null;
    mAllClauses = null;
    mNumClauses = 0;
    mRoot = null;
    mScratch = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void buildTree()
  {
    if (mRoot == null) {
      mNumClauses = mAllClauses.size();
      mRoot = new NameTreeNode();
      for (final List<CountedLiteral> cclause : mAllClauses) {
        Collections.sort(cclause);
        final Iterator<CountedLiteral> iter = cclause.iterator();
        mRoot.add(iter);
      }
      mAllClauses = null;
    }
  }

  private CountedLiteral createCountedLiteral
    (final SimpleExpressionProxy literal)
  {
    final CountedLiteral test = createTempLiteral(literal);
    final CountedLiteral found = mAllLiterals.get(test);
    if (found == null) {
      mScratch = null;
      mAllLiterals.put(test, test);
      return test;
    } else {
      return found;
    }    
  }

  private CountedLiteral getCountedLiteral(final SimpleExpressionProxy literal)
  {
    final CountedLiteral test = createTempLiteral(literal);
    return mAllLiterals.get(test);
  }

  private CountedLiteral createTempLiteral(final SimpleExpressionProxy literal)
  {
    if (mScratch == null) {
      mScratch = new CountedLiteral(literal);
    } else {
      mScratch.init(literal);
    }
    return mScratch;
  }


  //#########################################################################
  //# Inner Class CountedLiteral
  private class CountedLiteral implements Comparable<CountedLiteral>
  {

    //#######################################################################
    //# Constructor
    private CountedLiteral(final SimpleExpressionProxy literal)
    {
      mLiteral = literal;
      mOccurrences = 0;
    }

    //#######################################################################
    //# Hashing and Comparing
    public int compareTo(final CountedLiteral counted)
    {
      final int result = mOccurrences - counted.mOccurrences;
      if (result != 0) {
        return result;
      } else {
        return mComparator.compare(mLiteral, counted.mLiteral);
      }
    }

    public boolean equals(final Object other)
    {
      if (other != null && getClass() == other.getClass()) {
        final CountedLiteral counted = (CountedLiteral) other;
        return mLiteral.equalsByContents(counted.mLiteral);
      } else {
        return false;
      }
    }

    public int hashCode()
    {
      return mLiteral.hashCodeByContents();
    }

    //#######################################################################
    //# Simple Access
    private void init(final SimpleExpressionProxy literal)
    {
      mLiteral = literal;
      mOccurrences = 0;
    }

    private SimpleExpressionProxy getLiteral()
    {
      return mLiteral;
    }

    private void addOccurrence()
    {
      mOccurrences++;
    }

    private boolean isSignificant()
    {
      return mOccurrences < mNumClauses;
    }

    //#######################################################################
    //# Data Members
    private SimpleExpressionProxy mLiteral;
    private int mOccurrences;

  }


  //#########################################################################
  //# Inner Class NameTreeNode
  private class NameTreeNode
  {

    //#######################################################################
    //# Constructor
    private NameTreeNode()
    {
      mChildren = new HashMap<CountedLiteral,NameTreeNode>();
      mIsEndOfClause = 0;
    }

    //#######################################################################
    //# Simple Access
    private int size()
    {
      return mChildren.size() + mIsEndOfClause;
    }

    //#######################################################################
    //# Algorithm
    private void add(final Iterator<CountedLiteral> iter)
    {
      if (iter.hasNext()) {
        final CountedLiteral counted = iter.next();
        if (counted.isSignificant()) {
          NameTreeNode child = mChildren.get(counted);
          if (child == null) {
            child = new NameTreeNode();
            mChildren.put(counted, child);
          }
          child.add(iter);
        } else {
          add(iter);
        }
      } else {
        mIsEndOfClause = 1;
      }
    }

    private void print(final ModuleProxyPrinter printer,
                       final Iterator<CountedLiteral> iter,
                       final boolean first)
    {
      if (iter.hasNext()) {
        final CountedLiteral counted = iter.next();
        if (counted.isSignificant()) {
          final SimpleExpressionProxy literal = counted.getLiteral();
          final NameTreeNode child = mChildren.get(counted);
          final boolean nextfirst;
          if (size() == 1) {
            nextfirst = first;
          } else {
            try {
              printer.print(first ? '{' : ',');
              printer.printProxy(literal);
              nextfirst = false;
            } catch (final VisitorException exception) {
              throw exception.getRuntimeException();
            }
          }
          child.print(printer, iter, nextfirst);
        } else {
          print(printer, iter, first);
        }
      }
    }

    //#######################################################################
    //# Data Members
    private final Map<CountedLiteral,NameTreeNode> mChildren;
    private int mIsEndOfClause;

  }


  //#########################################################################
  //# Data Members
  private final Comparator<SimpleExpressionProxy> mComparator;

  private Map<CountedLiteral,CountedLiteral> mAllLiterals;
  private Collection<List<CountedLiteral>> mAllClauses;
  private int mNumClauses;
  private NameTreeNode mRoot;
  private CountedLiteral mScratch;
}
