package org.supremica.external.processAlgebraPetriNet.ppnedit.converter;

import java.io.*;
import java.util.*;
import java.awt.Point;

import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn.*;



//class to convert a String expression to relation
public class PPNtoRelation 
					extends PPNtoActivity {

	public static Relation createRelation(String exp) {
		/* check indata */
		if (exp == null || exp.length() == 0) {
			return null;
		}

		/* make relation */
		if (PPN.validExp(exp)) {
			exp = PPN.toInternalExp(exp);
			exp = exp.replace(" ", ""); // remove spaces

			/* No operations no relation */
			if (PPN.containsNoOperations(exp)) {
				System.out.println("createRelation exp: " + exp
						+ " contains no operations");
			} else {
				return makeRelation(exp);
			}
		} else {
			System.err.println("Not a valid exp: " + exp);
		}

		/* something wrong */
		return null;
	}

	private static Object makeRelationOrActivity(String exp) {

		if (PPN.containsNoOperations(exp)) {
			return createActivity(exp);
		}
		return makeRelation(exp);
	}

	// måste bryta ner denna funktion så den blir mer lättläst

	private static Relation makeRelation(String expToParse) {
		/* check indata */
		if (expToParse == null || expToParse.length() == 0) {
			return null;
		}

		Relation relation = null;

		Relation tmpRelation = null;
		RelationType relationType = null;

		ExpStack expStack = new ExpStack();

		String operation = "";
		String tmpOp = "";

		String tmpExp = "";

		// remove unnessesary parhenthesis
		expToParse = PPN.trimParenthesis(expToParse);

		while (expToParse.length() > 0) {

			// fase one
			// take expresion
			//

			tmpExp = PPN.getNextExp(expToParse);

			if (tmpExp.length() > 0) {
				// remove from expression
				expToParse = PPN.removeFirst(tmpExp, expToParse);

				// test if next operation is equal
				if (PPN.EQUAL.equals(PPN.getNextOp(expToParse))) {

					expToParse = PPN.removeFirst(PPN.getNextOp(expToParse),
												 expToParse);

					if (operation.length() > 0) {
						// make alredy parsed relation
						relation = addRelationFromStack(tmpRelation, operation,
								expStack);

						// create equal from rest of expression
						tmpRelation = factory.createRelation();
						tmpRelation.setType(fromValue(PPN.EQUAL));

						tmpRelation.getActivityRelationGroup().add(
								createActivity(tmpExp));
						tmpRelation.getActivityRelationGroup().add(
								makeRelationOrActivity(expToParse));

						// add equal to already parsed
						relation.getActivityRelationGroup().add(tmpRelation);
					} else {
						// make new relation
						relation = factory.createRelation();
						relation.setType(fromValue(PPN.EQUAL));

						relation.getActivityRelationGroup().add(
								createActivity(tmpExp));
						relation.getActivityRelationGroup().add(
								makeRelationOrActivity(expToParse));
					}
					return relation;
				}

				//System.out.println("expToParse is now " + expToParse);

				expToParse = PPN.trimParenthesis(expToParse);

				if (tmpOp.length() == 0) {
					if (tmpExp.length() > 0) {
						// add this expression to
						// first string in the stack
						expStack.concatExp(tmpExp);
					}
				} else {
					expStack.pushExp(tmpExp);
				}

			} else {
				System.out.println("Fel i makeRelation: " + expToParse);
			}

			// fase two
			// take operation
			//

			tmpOp = PPN.getNextOp(expToParse);

			// check if new operation found
			if (tmpOp.length() > 0 && operation.length() > 0
					&& !operation.equals(tmpOp)) {

				// New operation found

				// check operation priority
				if (!PPN.SEQUENCE.equals(tmpOp)) {

					// make relation from stack
					Relation tmp = factory.createRelation();
					tmp.setType(fromValue(operation));

					// store relation
					// or add previus from tmp
					if (tmpRelation == null) {
						tmpRelation = tmp;
					} else {
						tmp.getActivityRelationGroup().add(tmpRelation);
						tmpRelation = tmp;
					}

					// add rest from stack
					relation = addRelation(tmp, expStack.getExpStack());

					// empty stack
					expStack.flush();
				} else {
					expStack.concatExp(tmpOp);
					expToParse = PPN.removeFirst(tmpOp, expToParse);
					tmpOp = "";
				}
			}

			// store operation
			// if ok
			if (tmpOp.length() > 0) {
				operation = tmpOp;
				// remove from expression
				expToParse = PPN.removeFirst(tmpOp, expToParse);
			}
		} // end while

		// make relation from stack

		relation = factory.createRelation();

		if (operation.length() > 0) {
			relation.setType(fromValue(operation));

			if (tmpRelation != null) {
				relation.getActivityRelationGroup().add(tmpRelation);
			}

			relation = addRelation(relation, expStack.getExpStack());

		} else if (expStack.getExpStack() != null
				&& expStack.getExpStack().length > 0) {
			expToParse = (expStack.getExpStack())[0];

			System.out.println(expToParse);
			return makeRelation(expToParse);
		}
		return relation;
	}

	private static Relation addRelationFromStack(Relation tmpRelation,
			String operation, ExpStack expStack) {

		if (operation == null) {
			return null;
		}

		if (expStack == null) {
			return null;
		}

		// make relation from stack
		String expToParse = null;
		Relation relation = factory.createRelation();

		if (operation.length() > 0) {
			relation.setType(fromValue(operation));

			if (tmpRelation != null) {
				relation.getActivityRelationGroup().add(tmpRelation);
			}

			relation = addRelation(relation, expStack.getExpStack());

		} else if (expStack.getExpStack().length > 0) {
			expToParse = (expStack.getExpStack())[0];
			return makeRelation(expToParse);
		}
		return relation;

	}

	/*
	 * Help function to makeRelation()
	 * 
	 */
	private static Relation addRelation(Relation relation, String[] seq_exp) {
		// check indata
		if (relation == null || seq_exp == null || seq_exp.length == 0) {
			return null;
		}

		for (int i = 0; i < seq_exp.length; i++) {
			if (PPN.containsNoOperations(seq_exp[i])) {
				relation.getActivityRelationGroup().add(
						makeRelationOrActivity(seq_exp[i]));
			} else {
				// recursion
				Relation tmpRelation = makeRelation(seq_exp[i]);
				relation.getActivityRelationGroup().add(tmpRelation);
			}
		}
		return relation;
	}

	private static RelationType fromValue(String value) {

		if (value == null) {
			return null;
		}

		if (value.equals(PPN.EQUAL)) {
			return RelationType.fromValue("Equal");
		} else if (value.equals(PPN.SEQUENCE)) {
			return RelationType.SEQUENCE;
		} else if (value.equals(PPN.ALTERNATIVE)) {
			return RelationType.ALTERNATIVE;
		} else if (value.equals(PPN.ARBITARY_ORDER)) {
			return RelationType.ARBITRARY;
		} else if (value.equals(PPN.SYNCHRONIZE)) {
			return RelationType.fromValue("Synchronize");
		} else if (value.equals(PPN.PARALLEL)) {
			return RelationType.PARALLEL;
		} else {
			System.out.println("Unknown relation " + value);
		}
		return null;
	}
}
