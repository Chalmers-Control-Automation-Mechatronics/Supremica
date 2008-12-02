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
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.model.printer.ModuleProxyPrinter;


/**
 * A simple algorithm to determine reasonably short name suffixes for
 * EFA events. EFA event names are suffixed by a string representing
 * the guard conditions for each event. This class transforms guards
 * representing guard conditions into canonical names, by sorting the
 * constraints, and by removing constraints that are not needed to distinguish
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
                      final CompilerOperatorTable optable,
                      final VariableContext context)
  {
    mComparator = new ExpressionComparator(optable);
    mCollector = new VariableCollectVisitor(optable, context);
    mAllLiterals = null;
    mAllGuards = null;
    mNumGuards = 0;
    mRoot = null;
  }


  //#########################################################################
  //# Invocation
  void restart()
  {
    mAllLiterals = new HashMap<CountedLiteral,CountedLiteral>();
    mAllGuards = new LinkedList<List<CountedLiteral>>();
    mNumGuards = 0;
    mRoot = null;
  }

  void addGuard(final ConstraintList guard)
  {
    final int size = guard.size();
    final List<CountedLiteral> cguard = new ArrayList<CountedLiteral>(size);
    for (final SimpleExpressionProxy literal : guard.getConstraints()) {
      final CountedLiteral counted = createCountedLiteral(literal);
      counted.addOccurrence();
      cguard.add(counted);
    }
    mAllGuards.add(cguard);
  }

  String getNameSuffix(final ConstraintList guard)
  {
    buildTree();
    final int size = guard.size();
    final List<CountedLiteral> cguard = new ArrayList<CountedLiteral>(size);
    for (final SimpleExpressionProxy literal : guard.getConstraints()) {
      final CountedLiteral counted = getCountedLiteral(literal);
      cguard.add(counted);
    }
    Collections.sort(cguard);
    final Iterator<CountedLiteral> iter = cguard.iterator();
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
    mAllGuards = null;
    mNumGuards = 0;
    mRoot = null;
    mScratch = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void buildTree()
  {
    if (mRoot == null) {
      mNumGuards = mAllGuards.size();
      mRoot = new NameTreeNode();
      for (final List<CountedLiteral> cguard : mAllGuards) {
        Collections.sort(cguard);
        final Iterator<CountedLiteral> iter = cguard.iterator();
        mRoot.add(iter);
      }
      mAllGuards = null;
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
      final int oresult = mOccurrences - counted.mOccurrences;
      if (oresult != 0) {
        return oresult;
      }
      final OccurrenceKind kind1 = mCollector.collect(mLiteral);
      final boolean literal1 = mCollector.isLiteral();
      final IdentifierProxy ident1 = mCollector.getIdentifier();
      final OccurrenceKind kind2 = mCollector.collect(counted.mLiteral);
      final boolean literal2 = mCollector.isLiteral();
      final IdentifierProxy ident2 = mCollector.getIdentifier();
      if (kind1 != kind2) {
        return kind1.compareTo(kind2);
      } else if (literal1 != literal2) {
        return literal1 ? -1 : 1;
      }
      final int iresult = mComparator.compare(ident1, ident2);
      if (iresult != 0) {
        return iresult;
      }
      return mComparator.compare(mLiteral, counted.mLiteral);
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
      return mOccurrences < mNumGuards;
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
      mIsEndOfList = 0;
    }

    //#######################################################################
    //# Simple Access
    private int size()
    {
      return mChildren.size() + mIsEndOfList;
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
        mIsEndOfList = 1;
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
    private int mIsEndOfList;

  }


  //#########################################################################
  //# Inner Class VariableCollectVisitor
  private class VariableCollectVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Constructor
    private VariableCollectVisitor(final CompilerOperatorTable optable,
                                   final VariableContext context)
    {
      mOperatorTable = optable;
      mContext = context;
    }

    //#######################################################################
    //# Invocation
    private OccurrenceKind collect(final SimpleExpressionProxy expr)
    {
      try {
        mOccurrenceKind = OccurrenceKind.NONE;
        mIdentifier = null;
        mIsLiteral = (Boolean) expr.acceptVisitor(this);
        return mOccurrenceKind;
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Result Retrieval
    private OccurrenceKind getOccurrenceKind()
    {
      return mOccurrenceKind;
    }

    private boolean isLiteral()
    {
      return mIsLiteral;
    }

    private IdentifierProxy getIdentifier()
    {
      return mIdentifier;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Boolean visitIdentifierProxy(final IdentifierProxy ident)
    {
      if (mContext.getVariableRange(ident) != null) {
        if (mIdentifier == null) {
          mIdentifier = ident;
          mOccurrenceKind = OccurrenceKind.CURRENT;
        } else if (mIdentifier.equalsByContents(ident)) {
          if (mOccurrenceKind == OccurrenceKind.NEXT) {
            mOccurrenceKind = OccurrenceKind.BOTH;
          }
        } else {
          throw new IllegalArgumentException
            ("Multiple variable identifiers in expression!");
        }
      }
      return true;
    }

    public Boolean visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy lhs = expr.getLeft();
      lhs.acceptVisitor(this);
      if (mOccurrenceKind != OccurrenceKind.BOTH) {
        final SimpleExpressionProxy rhs = expr.getRight();
        rhs.acceptVisitor(this);
      }
      return false;
    }

    public Boolean visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
    {
      return false;
    }

    public Boolean visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
      throws VisitorException
    {
      final UnaryOperator op = expr.getOperator();
      final SimpleExpressionProxy subterm = expr.getSubTerm();
      if (op == mOperatorTable.getNotOperator()) {
        return (Boolean) subterm.acceptVisitor(this);
      } else if (op == mOperatorTable.getNextOperator()) {
        final IdentifierProxy ident = (IdentifierProxy) subterm;
        if (mIdentifier == null) {
          mIdentifier = ident;
          mOccurrenceKind = OccurrenceKind.NEXT;
        } else if (mIdentifier.equalsByContents(ident)) {
          if (mOccurrenceKind == OccurrenceKind.CURRENT) {
            mOccurrenceKind = OccurrenceKind.BOTH;
          }
        } else {
          throw new IllegalArgumentException
            ("Multiple variable identifiers in expression!");
        }
        return true;
      } else {
        subterm.acceptVisitor(this);
        return false;
      }
    }

    //#######################################################################
    //# Data Members
    private final CompilerOperatorTable mOperatorTable;
    private final VariableContext mContext;

    private OccurrenceKind mOccurrenceKind;
    private boolean mIsLiteral;
    private IdentifierProxy mIdentifier;

  }

  //#########################################################################
  //# Inner Class OccurrenceKind
  private static enum OccurrenceKind {
    NONE, CURRENT, NEXT, BOTH
  };


  //#########################################################################
  //# Data Members
  private final VariableCollectVisitor mCollector;
  private final Comparator<SimpleExpressionProxy> mComparator;

  private Map<CountedLiteral,CountedLiteral> mAllLiterals;
  private Collection<List<CountedLiteral>> mAllGuards;
  private int mNumGuards;
  private NameTreeNode mRoot;
  private CountedLiteral mScratch;
}
