//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.dnf
//# CLASS:   DNFMinimizer
//###########################################################################
//# $Id: DNFMinimizer.java,v 1.1 2008-06-29 07:13:43 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.dnf;

import java.util.*;
import java.io.*;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * @author Martin Byr&ouml;d
 */

public class DNFMinimizer {

  //#########################################################################
  //# Constructors
  public DNFMinimizer(final DNFConverter converter,
		      final CompilerOperatorTable operatorTable)
  {
    mConverter = converter;
    mOperatorTable = operatorTable;
  }
      
  
  //#########################################################################
  //# Invocation
  public CompiledNormalForm minimize(CompiledNormalForm expression)
  {
    CompiledNormalForm newExpression;
    Formula convertedExpression;
                
    if (expression.isEmpty()) return expression;
                
    /*
     * Step 1: Convert the expression to the required input form
     */
    convertedExpression = compiledNormalForm2Formula(expression);
                
    /*
     * Step 2: Minimize 
     */
    convertedExpression.reduceToPrimeImplicants();
    convertedExpression.reducePrimeImplicantsToSubset();
                
    /*
     * Step 3: Convert the result to CompiledNormalForm 
     */
    newExpression = formula2CompiledNormalForm(convertedExpression);
                
    return newExpression;
  }
        

  //#########################################################################
  //# Auxiliary Methods
  private CompiledNormalForm formula2CompiledNormalForm(Formula formula)
  {
    CompiledNormalForm expression =
      new CompiledNormalForm(mOperatorTable.getOrOperator());
                
    // loop through all terms in the formula
    for (Term term : formula.termList) {
      CompiledClause clause =
	new CompiledClause(mOperatorTable.getAndOperator());
                        
      // loop through all variables in term
      for (int k = 0; k < term.getNumVars(); k++) {
        if(term.varVals[k]==1) {
          // insert literal
          clause.add(getLiteral(k));
        } else if(term.varVals[k]==0) {
          // insert negated literal
          try {
            clause.add(mConverter.getNegatedLiteral(getLiteral(k)));
          } catch (TypeMismatchException e) {
            e.printStackTrace();
          }
        } else if(term.varVals[k]==2) {
          // 2 is a "dont care". do nothing.
        } else {
          System.err.println
	    ("DNFMinimizer: Error converting Formula to CompiledNormalForm");
        }
      }
      expression.add(clause.clone());
    }
    return expression;
  }
        
  private SimpleExpressionProxy getLiteral(int k)
  {
    return mLiteralArray[k];
  }
        
  private Formula compiledNormalForm2Formula(CompiledNormalForm expression)
  {
    Formula formula = null;
    ArrayList<Term> convertedTerms = new ArrayList<Term>();
                
    // count the number of literals and create a literal array
    extractUniqueLiterals(expression);
                
    // loop through the clauses in the expression
    for(CompiledClause clause : expression.getClauses()) {
      byte[] variableValues = new byte[mLiteralArray.length];
      for(int k = 0; k < variableValues.length; k++) {
        variableValues[k] = 2;
      }
                        
      // loop through the literals in the clause
      for (SimpleExpressionProxy literal : clause.getLiterals()) {
        // set the right variable by finding the right literal
	// in the literal list
                                
        // possibly remove not operator first
        boolean hasNegateOp = false;
        if (literal instanceof UnaryExpressionProxy) {
          UnaryOperator op = ((UnaryExpressionProxy) literal).getOperator();
          if(op.equals(mOperatorTable.getNotOperator())) {
            hasNegateOp = true;
          }
        } else if (literal instanceof BinaryExpressionProxy) {
          BinaryOperator op = ((BinaryExpressionProxy) literal).getOperator();
          if(mOperatorTable.isNotEqualsOperator(op) ||
             mOperatorTable.isLessThanOperator(op) ||
             mOperatorTable.isLessEqualsOperator(op)) {
            hasNegateOp = true;
          }
        }
        if (hasNegateOp) {
          try {
            literal = mConverter.getNegatedLiteral(literal);
          } catch (TypeMismatchException exception) {
	    throw exception.getRuntimeException();
	  }
        }
                                
        variableValues[getLiteralIndex(literal)] =
	  (byte) (!hasNegateOp ? 1 : 0); 
      }
      convertedTerms.add(new Term(variableValues));
    }
    // create the converted expression
    formula = new Formula(convertedTerms);
                
    return formula;
  }
        
  private int getLiteralIndex(SimpleExpressionProxy literal)
  {
    boolean literalFound = false;
    int index = -1;
    for(int k = 0; k < mLiteralArray.length; k ++) {
      if(literal.toString().equals(mLiteralArray[k].toString())) {
        index = k;
        literalFound = true;
      }
    }
    if(!literalFound) {
      System.err.println
	("DNFMinimizer: could not find literal in literal array");
    }
    return index;
  }
        
  private void extractUniqueLiterals(CompiledNormalForm expression)
  {
    ArrayList<SimpleExpressionProxy> literals =
      new ArrayList<SimpleExpressionProxy>();
    boolean notUnique = false;
    for(CompiledClause clause : expression.getClauses()) {
      for(SimpleExpressionProxy localLiteral : clause.getLiterals()) {
        notUnique = false;
        boolean hasNegateOp = false;
        if(localLiteral instanceof UnaryExpressionProxy) {
          UnaryOperator op =
	    ((UnaryExpressionProxy) localLiteral).getOperator();
          if(op.equals(mOperatorTable.getNotOperator())) {
            hasNegateOp = true;
          }
        } else if (localLiteral instanceof BinaryExpressionProxy) {
          BinaryOperator op =
	    ((BinaryExpressionProxy) localLiteral).getOperator();
          if(mOperatorTable.isNotEqualsOperator(op) ||
             mOperatorTable.isLessThanOperator(op) ||
             mOperatorTable.isLessEqualsOperator(op)) {
            hasNegateOp = true;
          }
        }
        if(hasNegateOp) {
          try {
            localLiteral = mConverter.getNegatedLiteral(localLiteral);
          } catch (TypeMismatchException e) {
            e.printStackTrace();
          }
        }
        for(SimpleExpressionProxy literal : literals) {
          if(literal.toString().equals(localLiteral.toString())) {
            notUnique = true;
            break;
          }
        }
        if(notUnique) {
          // do nothing
        } else {
          literals.add(localLiteral);
        }
      }
    }
                
    mLiteralArray = new SimpleExpressionProxy[literals.size()];
    for(int k = 0; k < literals.size(); k++) {
      mLiteralArray[k] = literals.get(k);
    }
  }
        

  private class Formula {
    /* This inner class is a piece of external source code included here by
     * Martin Byr&ouml;d. See below for details*/
                
    /* Copyright (c) 2007 the authors listed at the following URL, and/or
       the authors of referenced articles or incorporated external code:
       http://en.literateprograms.org/Quine-McCluskey_algorithm_(Java)?action=history&offset=20060711142850
                 
       Permission is hereby granted, free of charge, to any person obtaining
       a copy of this software and associated documentation files (the
       "Software"), to deal in the Software without restriction, including
       without limitation the rights to use, copy, modify, merge, publish,
       distribute, sublicense, and/or sell copies of the Software, and to
       permit persons to whom the Software is furnished to do so, subject to
       the following conditions:
                 
       The above copyright notice and this permission notice shall be
       included in all copies or substantial portions of the Software.
                 
       THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
       EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
       MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
       IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
       CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
       TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
       SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
                 
       Retrieved from: http://en.literateprograms.org/Quine-McCluskey_algorithm_(Java)?oldid=6964
    */

    Formula(List<Term> termList)
    {
      this.termList = termList;
    }
                
    public String toString()
    {
      String result = "";
      result += termList.size() + " terms, " + termList.get(0).getNumVars() + " variables\n";
      for(int i=0; i<termList.size(); i++) {
        result += termList.get(i).toString() + "\n";
      }
      return result;
    }
                
    @SuppressWarnings("unchecked")
    public void reduceToPrimeImplicants()
    {
      originalTermList = new ArrayList<Term>(termList);
      int numVars = termList.get(0).getNumVars();
      ArrayList<Term>[][] table = new ArrayList[numVars + 1][numVars + 1];
      for(int dontKnows=0; dontKnows <= numVars; dontKnows++) {
        for(int ones=0; ones <= numVars; ones++) {
          table[dontKnows][ones] = new ArrayList<Term>();
        }
      }
      for(int i=0; i<termList.size(); i++) {
        int dontCares = termList.get(i).countValues(Term.DontCare);
        int ones      = termList.get(i).countValues((byte)1);
        table[dontCares][ones].add(termList.get(i));
      }
                        
      for(int dontKnows=0; dontKnows <= numVars - 1; dontKnows++) {
        for(int ones=0; ones <= numVars - 1; ones++) {
          ArrayList<Term> left   = table[dontKnows][ones];
          ArrayList<Term> right  = table[dontKnows][ones + 1];
          ArrayList<Term> out    = table[dontKnows+1][ones];
          for(int leftIdx = 0; leftIdx < left.size(); leftIdx++) {
            for(int rightIdx = 0; rightIdx < right.size(); rightIdx++) {
              Term combined = left.get(leftIdx).combine(right.get(rightIdx));
              if (combined != null) {
                if (!out.contains(combined)) {
                  out.add(combined); 
                }
                termList.remove(left.get(leftIdx));
                termList.remove(right.get(rightIdx));
                if (!termList.contains(combined)) {
                  termList.add(combined);
                }
                                                                
              }
            }
          }
        }
      }
                        
    }
                
    void reducePrimeImplicantsToSubset() {
      int numPrimeImplicants = termList.size();
      int numOriginalTerms   = originalTermList.size();
      boolean[][] table = new boolean[numPrimeImplicants][numOriginalTerms];
      for (int impl = 0; impl < numPrimeImplicants; impl++) {
        for (int term = 0; term < numOriginalTerms; term++) {
          table[impl][term] =
	    termList.get(impl).implies(originalTermList.get(term));
        }
      }
      ArrayList<Term> newTermList = new ArrayList<Term>();
      boolean done = false;
      int impl;
      while (!done) {
        impl = extractEssentialImplicant(table);
        if (impl != -1) {
          newTermList.add(termList.get(impl));
        } else {
          impl = extractLargestImplicant(table);
          if (impl != -1) {
            newTermList.add(termList.get(impl));
          } else {
            done = true;
          }
        }
      }
      termList = newTermList;
      originalTermList = null;
    }
                
    Formula read(Reader reader) throws IOException
    {
      ArrayList<Term> terms = new ArrayList<Term>();
      Term term = new Term(null);
      while ((term = term.read(reader)) != null) {
        terms.add(term);
      }
      return new Formula(terms);
    }
                
                
    private int extractEssentialImplicant(boolean[][] table)
    {
      for (int term=0; term < table[0].length; term++) {
        int lastImplFound = -1;
        for (int impl=0; impl < table.length; impl++) {
          if (table[impl][term]) {
            if (lastImplFound == -1) {
              lastImplFound = impl;
            } else {
              // This term has multiple implications
              lastImplFound = -1;
              break;
            }
          }
        }
        if (lastImplFound != -1) {
          extractImplicant(table, lastImplFound);
          return lastImplFound;
        }
      }
      return -1;
    }
                
    private void extractImplicant(boolean[][] table, int impl)
    {
      for (int term=0; term < table[0].length; term++) {
        if (table[impl][term]) {
          for (int impl2=0; impl2 < table.length; impl2++) {
            table[impl2][term] = false;
          }
        }
      }
    }
                
    private int extractLargestImplicant(boolean[][] table)
    {
      int maxNumTerms = 0;
      int maxNumTermsImpl = -1;
      for (int impl=0; impl < table.length; impl++) {
        int numTerms = 0;
        for (int term=0; term < table[0].length; term++) {
          if (table[impl][term]) {
            numTerms++;
          }
        }
        if (numTerms > maxNumTerms) {
          maxNumTerms = numTerms;
          maxNumTermsImpl = impl;
        }
      }
      if (maxNumTermsImpl != -1) {
        extractImplicant(table, maxNumTermsImpl);
        return maxNumTermsImpl;
      }
      return -1;
    }
                
    private List<Term> termList;
    private List<Term> originalTermList;
  }
        
  private class Term {
    /* This inner class is a piece of external source code included here by
     * Martin Byr&oumld. See below for details*/
                
    /* Copyright (c) 2007 the authors listed at the following URL, and/or
       the authors of referenced articles or incorporated external code:
       http://en.literateprograms.org/Quine-McCluskey_algorithm_(Java)?action=history&offset=20060711142850
                 
       Permission is hereby granted, free of charge, to any person obtaining
       a copy of this software and associated documentation files (the
       "Software"), to deal in the Software without restriction, including
       without limitation the rights to use, copy, modify, merge, publish,
       distribute, sublicense, and/or sell copies of the Software, and to
       permit persons to whom the Software is furnished to do so, subject to
       the following conditions:
                 
       The above copyright notice and this permission notice shall be
       included in all copies or substantial portions of the Software.
                 
       THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
       EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
       MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
       IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
       CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
       TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
       SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
                 
       Retrieved from: http://en.literateprograms.org/Quine-McCluskey_algorithm_(Java)?oldid=6964
    */
                
    static final byte DontCare = 2;
                
    Term(byte[] varVals)
    {
      this.varVals = varVals;
    }
                
    int getNumVars()
    {
      return varVals.length;
    }
                
    public String toString()
    {
      String result = "{";
      for(int i=0; i<varVals.length; i++) {
        if (varVals[i] == DontCare)
          result += "X";
        else
          result += varVals[i];
        result += " ";
      }
      result += "}";
      return result;
    }
                
    Term combine(Term term)
    {
      int diffVarNum = -1; // The position where they differ
      for(int i=0; i<varVals.length; i++) {
        if (this.varVals[i] != term.varVals[i]) {
          if (diffVarNum == -1) {
            diffVarNum = i;
          } else {
            // They're different in at least two places
            return null;
          }
        }
      }
      if (diffVarNum == -1) {
        // They're identical
        return null;
      }
      byte[] resultVars = varVals.clone();
      resultVars[diffVarNum] = DontCare;
      return new Term(resultVars);
    }
                
    int countValues(byte value)
    {
      int result = 0;
      for (int i = 0; i < varVals.length; i++) {
        if (varVals[i] == value) {
          result++;
        }
      }
      return result;
    }
                
    public boolean equals(Object o)
    {
      if (o == this) {
        return true;
      } else if (o == null || !getClass().equals(o.getClass())) {
        return false;
      } else {
        Term rhs = (Term)o;
        return Arrays.equals(this.varVals, rhs.varVals);
      }
    }
                
    public int hashCode() {
      return varVals.hashCode();
    }
                
    boolean implies(Term term)
    {
      for(int i=0; i<varVals.length; i++) {
        if (this.varVals[i] != DontCare &&
            this.varVals[i] != term.varVals[i]) {
          return false;
        }
      }
      return true;
    }
                
    Term read(Reader reader) throws IOException
    {
      int c = '\0';
      ArrayList<Byte> t = new ArrayList<Byte>();
      while (c != '\n' && c != -1) {
        c = reader.read();
        if (c == '0') {
          t.add((byte)0);
        } else if (c == '1') {
          t.add((byte)1);
        }
      }
      if (t.size() > 0) {
        byte[] resultBytes = new byte[t.size()];
        for(int i=0; i<t.size(); i++) {
          resultBytes[i] = (byte)t.get(i);
        }
        return new Term(resultBytes);
      } else {
        return null;
      }
    }
                
    private byte[] varVals;
  }
        
        
  public static void main(String[] args)
  {
    /*
     * this main function is here for testing purposes only
     */ 
                
    String expression = "(x<=5)&(y|z)|(x>5)&y";
    //String expression = "(z|y)";
    CompilerOperatorTable opTable = CompilerOperatorTable.getInstance();
    ModuleElementFactory factory = ModuleElementFactory.getInstance();
    ExpressionParser parser = new ExpressionParser(factory, opTable);
    SimpleExpressionProxy expr = null;
    DNFConverter converter =
      new DNFConverter(factory, opTable, ExpressionComparator.getInstance());
    CompiledNormalForm cnf = null;
    DNFMinimizer minimizer = new DNFMinimizer(converter, opTable);
    try {
      expr = parser.parse(expression);
      cnf = converter.convertToDNF(expr);
    } catch (ParseException e) {
      e.printStackTrace();
    } catch (EvalException e1) {
      e1.printStackTrace();
    }
                
    System.out.println("Original expression: " + expression);
    System.out.println("DNF expresion: " + cnf.toString());
    minimizer.extractUniqueLiterals(cnf);
    // System.out.println("Unique literals: " + 
    //                    minimizer.mLiteralArray.toString());
    System.out.println("Minimized DNF expression: " +
		       minimizer.minimize(cnf).toString());
    System.out.println("Number of literals: " +
		       minimizer.mLiteralArray.length);
  }
        
  private DNFConverter mConverter;
  private CompilerOperatorTable mOperatorTable;
  private SimpleExpressionProxy[] mLiteralArray;
}
