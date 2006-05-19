package net.sourceforge.waters.model.compiler;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.GuardExpressionOperatorTable.BinaryAndOperator;
import net.sourceforge.waters.model.compiler.GuardExpressionOperatorTable.BinaryBooleanOperator;
import net.sourceforge.waters.model.compiler.GuardExpressionOperatorTable.BinaryEqualsOperator;
import net.sourceforge.waters.model.compiler.GuardExpressionOperatorTable.BinaryGreaterEqualOperator;
import net.sourceforge.waters.model.compiler.GuardExpressionOperatorTable.BinaryGreaterThanOperator;
import net.sourceforge.waters.model.compiler.GuardExpressionOperatorTable.BinaryLessEqualOperator;
import net.sourceforge.waters.model.compiler.GuardExpressionOperatorTable.BinaryLessThanOperator;
import net.sourceforge.waters.model.compiler.GuardExpressionOperatorTable.BinaryNotEqualOperator;
import net.sourceforge.waters.model.compiler.GuardExpressionOperatorTable.BinaryOrOperator;
import net.sourceforge.waters.model.compiler.GuardExpressionOperatorTable.UnaryBooleanOperator;
import net.sourceforge.waters.model.compiler.GuardExpressionOperatorTable.UnaryNotOperator;
import net.sourceforge.waters.model.compiler.GuardExpressionOperatorTable.IdentityOperator;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.BooleanOperator;
import net.sourceforge.waters.model.expr.BooleanValue;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.IntValue;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.expr.Value;
import net.sourceforge.waters.model.module.VariableProxy;
import net.sourceforge.waters.subject.module.BinaryExpressionSubject;
import net.sourceforge.waters.subject.module.IntConstantSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.subject.module.UnaryExpressionSubject;
import net.sourceforge.waters.xsd.module.IntConstant;
import net.sourceforge.waters.xsd.module.UnaryExpression;

public class GuardExpressionHandler extends ExpressionHandler{
	private boolean mDNFGenerated;
	private DNFExpression mDNFExpression;
	private boolean mHasPureAndExpression;
	protected ExpressionParser mParser;

	public String toString() {
		if(mExpressionSet) {
			if(mDNFGenerated) {
				return mDNFExpression.toString();
			} else {
				return mExpression.toString();
			}
		} else {
			return "expression not set";
		}
	}

	public void setExpression(SimpleExpressionSubject expression) {
		super.setExpression(expression);
		mDNFExpression = null;
		mDNFGenerated = false;
		mHasPureAndExpression = false;
	}

	public GuardExpressionHandler() {
		mParser = new ExpressionParser(mFactory,
				GuardExpressionOperatorTable.getInstance());
		mExpressionSet = false;
		mHasPureAndExpression = false;
		mDNFGenerated = false;
	}
	
	public Boolean evaluatePartialExpression(String variable) throws EvalException {
		if(mHasPureAndExpression) {
			return evaluatePartial(variable);
		} else {
			System.err.println("GuardExpressionHandler: partial evaluation" +
					" only applicable for pure and-expressions");
			return null;
		}
	}
		
	private boolean evaluatePartial(String variable) throws EvalException {
			final List<DNFAtom> atoms = mDNFExpression.getOrClause().getAndClause(0).getAtoms(); 
			boolean result = true;
			for(DNFAtom atom : atoms) {
				if(atom.containsVariable(variable)) {
					final boolean value = ((CompiledBooleanValue)
							atom.evaluate()).getValue();
					result = result & value;
				}
			}
			return result;
	}
	
	public void convertToDNF() {
		if(mExpressionSet) {
			notPushDown();
			equalPushDown();
			mDNFExpression = convertToDNF(mExpression);
			mDNFGenerated = true;
		} else {
			System.err.println("GuardExpressionHandler:" +
					" expression not initialized");
		}
	}

	private DNFExpression convertToDNF(SimpleExpressionSubject expression) {
		DNFExpression  dnfExpression = new DNFExpression();
		DNFOrClause orClause = new DNFOrClause();
		
		//recursively convert to disjunctive normal form
		//binary expression
		if(expression instanceof BinaryExpressionSubject) {
			final BinaryExpressionSubject binExpr = 
				(BinaryExpressionSubject) expression;
			final DNFExpression lhs = convertToDNF(binExpr.getLeft());
			final DNFExpression rhs = convertToDNF(binExpr.getRight());
			
			if(binExpr.getOperator() instanceof BinaryAndOperator) {
				for(DNFAndClause clauseL : lhs.getOrClause().getAndClauses()) {
					for(DNFAndClause clauseR : rhs.getOrClause().getAndClauses()) {
						orClause.addAndClause(clauseL.mergeWith(clauseR));
					}
				}
				dnfExpression.setOrClause(orClause);
				return dnfExpression;
				
			} else if(binExpr.getOperator() instanceof BinaryOrOperator) {
				dnfExpression = lhs.mergeWith(rhs);
				return dnfExpression;
				
			} else {
				//base case reached
				DNFAndClause andClause = new DNFAndClause();
				
				DNFAtom atom = new DNFBinaryAtom(
						(BinaryOperator) binExpr.getOperator(),
									new DNFUnaryAtom(binExpr.getLeft()),
									new DNFUnaryAtom(binExpr.getRight()));
				andClause.addAtom(atom);
				orClause.addAndClause(andClause);
				dnfExpression.setOrClause(orClause);
				return dnfExpression;
			}
		} 
		
		//unary expression
		else if(expression instanceof UnaryExpressionSubject) {
			dnfExpression = convertToDNF(((UnaryExpressionSubject)
					expression).getSubTerm());
			if(((UnaryExpressionSubject) expression)
					.getOperator() instanceof UnaryNotOperator){
				((DNFUnaryAtom) dnfExpression.getOrClause()
						.getAndClause(0).getAtom(0)).setOperator(new UnaryNotOperator());
			}
			return dnfExpression;
		} 
		
		else if(expression instanceof SimpleIdentifierSubject) {
			DNFAndClause andClause = new DNFAndClause();
			
			DNFIdentifier identifier = new DNFIdentifier(
					(SimpleIdentifierSubject) expression);
			DNFUnaryAtom atom = new DNFUnaryAtom(
					new IdentityOperator(), identifier);
			andClause.addAtom(atom);
			orClause.addAndClause(andClause);
			dnfExpression.setOrClause(orClause);
			return dnfExpression;
		}
		
		else if(expression instanceof IntConstantSubject) {
			DNFAndClause andClause = new DNFAndClause();
			
			DNFIntConstant constant = new DNFIntConstant(
					(IntConstantSubject) expression);
			DNFUnaryAtom atom = new DNFUnaryAtom(
					new IdentityOperator(), constant);
			andClause.addAtom(atom);
			orClause.addAndClause(andClause);
			dnfExpression.setOrClause(orClause);
			return dnfExpression;
		}
		
		//base case
		else {
			System.err.println("GuardExpressionHandler: illegal expression type");
			return null;
		}
	}

	public List<SimpleExpressionSubject> getAndClauses() {
		if(mExpressionSet) {
			List<SimpleExpressionSubject> andClauses
				= new LinkedList<SimpleExpressionSubject>();
			List<DNFAndClause> dnfAndClauses;
			
			//perform DNF conversion if neccesary
			if(! mDNFGenerated) {
				convertToDNF();
			} 
			
			//type conversion of dnf expression to SimpleExpression
			dnfAndClauses = mDNFExpression.getOrClause().getAndClauses();
			for(DNFAndClause clause : dnfAndClauses) {
				try {
					andClauses.add((SimpleExpressionSubject) mParser.parse(clause.toString()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return andClauses;
		} else {
			System.err.println("GuardExpressionHandler: not initialized");
			return null;
		}
	}
	

	public void equalPushDown() {
		if(mExpressionSet) {
			equalPushDown(mExpression);
		} else {
			System.err.println("expression not initialized");
		}
	}

	private void equalPushDown(SimpleExpressionSubject expression) {
		if(expression instanceof BinaryExpressionSubject) {
			BinaryExpressionSubject binExpr = (BinaryExpressionSubject) expression;
			SimpleExpressionSubject lhs = binExpr.getLeft();
			SimpleExpressionSubject rhs = binExpr.getRight();
			if(lhs instanceof BinaryExpressionSubject |
					lhs instanceof UnaryExpressionSubject |
					rhs instanceof BinaryExpressionSubject |
					rhs instanceof UnaryExpressionSubject) {
				
				//recursion step
				equalPushDown(lhs);
				equalPushDown(rhs);
				
				//create negated versions of lhs and rhs
				UnaryExpressionSubject notLhs = 
					mFactory.createUnaryExpressionProxy
					(new UnaryNotOperator(), lhs.clone());
				UnaryExpressionSubject notRhs = 
					mFactory.createUnaryExpressionProxy
					(new UnaryNotOperator(), rhs.clone());
				
				//equality operator "=="
				if (binExpr.getOperator() instanceof BinaryEqualsOperator) {
					// create new lhs and rhs
					BinaryExpressionSubject newLhs = mFactory
							.createBinaryExpressionProxy(
							new BinaryAndOperator(), lhs.clone(), rhs.clone());
					BinaryExpressionSubject newRhs = mFactory
							.createBinaryExpressionProxy(
							new BinaryAndOperator(), notLhs, notRhs);
					
					//construct new expression
					binExpr.setOperator(new BinaryOrOperator());
					binExpr.setLeft(newLhs);
					binExpr.setRight(newRhs);
				} 
				
				//not equal operator "!="
				else if(binExpr.getOperator() instanceof BinaryNotEqualOperator) {
					// create new lhs and rhs
					BinaryExpressionSubject newLhs = mFactory
							.createBinaryExpressionProxy(
									new BinaryAndOperator(), notLhs, rhs.clone());
					BinaryExpressionSubject newRhs = mFactory
							.createBinaryExpressionProxy(
									new BinaryAndOperator(), lhs.clone(), notRhs);
					
					//construct new expression
					binExpr.setOperator(new BinaryOrOperator());
					binExpr.setLeft(newLhs);
					binExpr.setRight(newRhs);
					
				}
				
				//other operators
				else {
					//do nothing
				}
			}
			
			//otherwise both sides are constants or identifiers
			else {
				//do nothing
			}
		}
		
		else if(expression instanceof UnaryExpressionSubject) {
			//do recursion step
			equalPushDown(((UnaryExpressionSubject) expression).getSubTerm());
		}
		
		//base case
		else {
			//do nothing
		}
	}

	public void notPushDown() {
		if(mExpressionSet) {
			notPushDown(mExpression);
		} else {
			System.err.println("expression not initialized");
		}
	}
	
	private void notPushDown(SimpleExpressionSubject expression) {
		//binary expression
		if(expression instanceof BinaryExpressionSubject) {
			final BinaryExpressionSubject binExpr = 
				(BinaryExpressionSubject) expression;
			
			//recursion step
			notPushDown(binExpr.getLeft());
			notPushDown(binExpr.getRight());
		}
		
		//unary expression
		else if (expression instanceof UnaryExpressionSubject) {
			final UnaryExpressionSubject unExpr = 
				(UnaryExpressionSubject) expression;
			
			//negate subterm if neccesary
			final Operator operator = unExpr.getOperator();
			if(operator instanceof
					GuardExpressionOperatorTable.UnaryNotOperator) {
				
				//subexpression negated => negate this operator
				if(negateExpression(unExpr.getSubTerm())) {
					(unExpr).setOperator((UnaryOperator)
						((GuardExpressionOperatorTable.UnaryNotOperator)
						operator).getNegative());
				}
			}
			
			//recursion step
			notPushDown(unExpr.getSubTerm());
		}
		
		//base case
		else{
			//do nothing
		}
	}

	private boolean negateExpression(SimpleExpressionSubject expression) {
		boolean wasNegated;
		
		//binary expression
		if(expression instanceof BinaryExpressionSubject) {
			final BinaryExpressionSubject binExpr =
				(BinaryExpressionSubject) expression;
			Operator operator = binExpr.getOperator();
			
			//only negate boolean operators
			if(operator instanceof BooleanOperator) {
				binExpr.setOperator((BinaryOperator) 
						((BooleanOperator) operator).getNegative());
				
				//do not negate subterms of equals/notequal/inequalities operator
				if(!(operator instanceof BinaryEqualsOperator |
						operator instanceof BinaryNotEqualOperator |
						operator instanceof BinaryLessThanOperator |
						operator instanceof BinaryGreaterThanOperator |
						operator instanceof BinaryLessEqualOperator |
						operator instanceof BinaryGreaterEqualOperator)) {
					
					SimpleExpressionSubject newLeft =
						mFactory.createUnaryExpressionProxy(
								new UnaryNotOperator(),
								binExpr.getLeft().clone());
					SimpleExpressionSubject newRight =
						mFactory.createUnaryExpressionProxy(
								new UnaryNotOperator(),
								binExpr.getRight().clone());
					binExpr.setLeft(newLeft);
					binExpr.setRight(newRight);
				}
				wasNegated = true;
			}
			else {
				wasNegated = false;
			}
		}
		
		//unary expression
		else if(expression instanceof UnaryExpressionSubject) {
			final UnaryExpressionSubject unExpr =
				(UnaryExpressionSubject) expression;
			Operator operator = unExpr.getOperator();
			
			//only negate boolean operators
			if(operator instanceof BooleanOperator) {
				unExpr.setOperator((UnaryOperator)
						((BooleanOperator) operator).getNegative());
				wasNegated = true;
			}
			else {
				wasNegated = false;
			}
		}
		
		//true/false constant
		else if(expression instanceof SimpleIdentifierSubject) {
			SimpleIdentifierSubject identifier = 
				(SimpleIdentifierSubject) expression;
			if(identifier .getName().equals("true")) {
				identifier.setName("false");
				wasNegated = true;
			}
			else if(identifier.getName().equals("false")){
				identifier.setName("true");
				wasNegated = true;
			} else {
				wasNegated = false;
			}
		} else {
			wasNegated = false;
		}
		return wasNegated;
	}
	
	private abstract class AbstractDNF {
		public abstract String toString();
		public abstract Value evaluate() throws EvalException;
	}
	private class DNFExpression extends AbstractDNF{
		DNFOrClause mOrClause;
		
		public DNFExpression() {
			super();
			mOrClause = new DNFOrClause();
		}

		public DNFExpression mergeWith(DNFExpression rhs) {
			DNFExpression dnfExpression = new DNFExpression();
			dnfExpression.setOrClause(
					this.getOrClause().mergeWith(
							rhs.getOrClause()));
			return dnfExpression;
		}

		public String toString() {
			return mOrClause.toString();
		}

		@Override
		public Value evaluate() throws EvalException {
			return mOrClause.evaluate();
		}

		public DNFOrClause getOrClause() {
			return mOrClause;
		}

		public void setOrClause(DNFOrClause orClause) {
			mOrClause = orClause;
		}
	}
	
	private class DNFOrClause extends AbstractDNF{
		List<DNFAndClause> mAndClauseList;

		public DNFOrClause() {
			super();
			mAndClauseList = new LinkedList<DNFAndClause>();
		}
		
		public DNFOrClause mergeWith(DNFOrClause orClause) {
			DNFOrClause clause = new DNFOrClause();
			clause.addAndClauses(mAndClauseList);
			clause.addAndClauses(orClause.getAndClauses());
			return clause;
		}

		private void addAndClauses(List<DNFAndClause> andClauses) {
			mAndClauseList.addAll(andClauses);
		}

		public List<DNFAndClause> getAndClauses() {
			return mAndClauseList;
		}

		public DNFAndClause getAndClause(int i) {
			return mAndClauseList.get(i);
		}
		
		public void addAndClause(DNFAndClause clause) {
			mAndClauseList.add(clause);
		}
		
		@Override
		public String toString() {
			String returnString = mAndClauseList.get(0).toString();
			for(int i = 1; i < mAndClauseList.size(); i ++) {
				returnString += "|" + mAndClauseList.get(i).toString();
			}
			return returnString;
		}

		@Override
		public Value evaluate() throws EvalException {
			boolean returnValue = false;
			for(DNFAndClause clause : mAndClauseList) {
				returnValue = returnValue |
					((BooleanValue) clause.evaluate()).getValue();
			}
			return new CompiledBooleanValue(returnValue);
		}
	}
	
	private class DNFAndClause extends AbstractDNF{
		List<DNFAtom> mAtomList;

		public DNFAndClause() {
			super();
			mAtomList = new LinkedList<DNFAtom>();
		}
		
		public DNFAndClause mergeWith(DNFAndClause clauseR) {
			DNFAndClause returnClause = new DNFAndClause();
			returnClause.addAtoms(mAtomList);
			returnClause.addAtoms(clauseR.getAtoms());
			return returnClause;
		}

		private List<DNFAtom> getAtoms() {
			return mAtomList;
		}

		private void addAtoms(List<DNFAtom> atomList) {
			mAtomList.addAll(atomList);
		}

		public DNFAtom getAtom(int i) {
			return mAtomList.get(i);
		}
		
		public void addAtom(DNFAtom atom) {
			mAtomList.add(atom);
		}

		@Override
		public String toString() {
			String returnString = mAtomList.get(0).toString();
			for(int i = 1; i < mAtomList.size(); i++) {
				returnString += "&" + mAtomList.get(i).toString();
			}
			return returnString;
		}

		@Override
		public Value evaluate() throws EvalException {
			boolean returnValue = true;
			for(DNFAtom atom : mAtomList) {
				returnValue = returnValue &
					((BooleanValue) atom.evaluate()).getValue();
			}
			return new CompiledBooleanValue(returnValue);
		}
	}
	
	private abstract class DNFAtom extends DNFExpression{

		public abstract boolean containsVariable(String variable);
	}
	
	private class DNFBinaryAtom extends DNFAtom {
		BinaryOperator mOperator;
		DNFUnaryAtom mLhs;
		DNFUnaryAtom mRhs;
		
		public DNFBinaryAtom(BinaryOperator operator, DNFUnaryAtom lhs, DNFUnaryAtom rhs) {
			super();
			mOperator = operator;
			mLhs = lhs;
			mRhs = rhs;
		}
		
		@Override
		public String toString() {
			return "(" + mLhs.toString() + 
					mOperator.getName() +
					mRhs.toString() +
					")";
		}
		@Override
		public Value evaluate() throws EvalException {
			return mOperator.eval(mLhs.evaluate(), mRhs.evaluate());
		}

		@Override
		public boolean containsVariable(String variable) {
			return mLhs.containsVariable(variable) | mRhs.containsVariable(variable);
		}
	}
	
	private class DNFUnaryAtom extends DNFAtom {
		UnaryOperator mOperator;
		DNFTerm mTerm;
		@Override
		public String toString() {
			return mOperator.getName() + mTerm.toString();
		}
		public void setOperator(UnaryNotOperator operator) {
			mOperator = operator;
		}
		@Override
		public Value evaluate() throws EvalException {
			return mOperator.eval(mTerm.evaluate());
		}
		
		public DNFUnaryAtom(UnaryOperator operator, DNFTerm term) {
			super();
			mOperator = operator;
			mTerm = term;
		}
		public DNFUnaryAtom(SimpleExpressionSubject expression) {
			if(expression instanceof SimpleIdentifierSubject) {
				mOperator = new IdentityOperator();
				mTerm = new DNFIdentifier((SimpleIdentifierSubject) expression);
			} else if(expression instanceof IntConstantSubject) {
				mOperator = new IdentityOperator();
				mTerm = new DNFIntConstant((IntConstantSubject) expression);
			} else if(expression instanceof UnaryExpressionSubject) {
				mOperator = (UnaryBooleanOperator) 
					((UnaryExpressionSubject) expression).getOperator();
				mTerm = createTerm(((UnaryExpressionSubject) expression)
						.getSubTerm());
			} else {
				
			}
		}
		@Override
		public boolean containsVariable(String variable) {
			if(mTerm instanceof DNFIdentifier) {
				return ((DNFIdentifier) mTerm).toString().equals(variable);
			}
			else {
				return false;
			}
		}
	}
	
	private abstract class DNFTerm extends AbstractDNF {
	}
	
	private class DNFIdentifier extends DNFTerm {
		SimpleIdentifierSubject mIdentifier;
		@Override
		public String toString() {
			return mIdentifier.toString();
		}

		@Override
		public Value evaluate() throws EvalException {
			return mVariables.get(mIdentifier.getName()).getValue();
		}

		public DNFIdentifier(SimpleIdentifierSubject identifier) {
			super();
			mIdentifier = identifier;
		}
		
	}
	
	private class DNFIntConstant extends DNFTerm {
		IntConstantSubject mConstant;
		@Override
		public String toString() {
			return mConstant.toString();
		}

		@Override
		public Value evaluate() throws EvalException {
			return new CompiledIntValue(mConstant.getValue());
		}

		public DNFIntConstant(IntConstantSubject constant) {
			super();
			mConstant = constant;
		}
		
	}

	public DNFTerm createTerm(SimpleExpressionSubject term) {
		if(term instanceof IntConstantSubject) {
			return new DNFIntConstant((IntConstantSubject) term);
		} else if(term instanceof SimpleIdentifierSubject) {
			return new DNFIdentifier((SimpleIdentifierSubject) term);
		} else {
			System.err.println("GuardExpressionHandler: cannot create term of type " + term.getShortClassName());
			return null;
		}
	}

	public void setPureAndExpression(SimpleExpressionSubject andClause) {
		mExpression = andClause;
		mExpressionSet = true;
		convertToDNF();
		
		//check pure and
		if(mDNFExpression.getOrClause().getAndClauses().size() > 1) {
			System.err.println("GuardExpressionHandler: not pure and-expression");
			mExpression = null;
			mExpressionSet = false;
		} else {
			mHasPureAndExpression = true;
		}
	}

	public boolean variableInExpression(VariableProxy variable) {
		/* Returns true if there is a reference to the variable
		 * in this expression.
		 * 
		 * Requires this expression to be in a pure and-format.
		 * 
		 */

		String name = variable.getName();
		final List<DNFAtom> atoms = mDNFExpression.getOrClause()
				.getAndClause(0).getAtoms();
		boolean result = false;
		for (DNFAtom atom : atoms) {
			result = result || atom.containsVariable(name);
		}

		return result;
	}

	
	/* TODO remove comments when BooleanConstant is implemented
	 * private class DNFBooleanConstant extends DNFTerm {
		BooleanConstantSubject mConstant;

		@Override
		public String toString() {
			return mConstant.toString();
		}

		@Override
		public Value evaluate() throws EvalException {
			return new CompiledBooleanValue(mConstant.getValue());
		}
	}*/
}

	

