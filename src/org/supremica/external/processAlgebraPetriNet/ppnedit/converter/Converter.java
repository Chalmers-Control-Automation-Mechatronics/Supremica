package org.supremica.external.processAlgebraPetriNet.ppnedit.converter;

import java.util.*;

import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn.*;




public class Converter {

	/* convert to BaseCell from xml object ROP, Relation, Activity */
	public static BaseCell createBaseCell(final ROP rop) {
		return ROPtoBaseCell.ROPtoBaseCell(rop);
	}

	public static BaseCell createBaseCell(final Relation relation) {
		return RelationToBaseCell.relationToBaseCell(relation);
	}

	public static BaseCell createBaseCell(final Activity activity) {
		return ActivityToBaseCell.activityToBaseCell(activity);
	}

	/* convert to BaseCell from String PPN expression */
	public static BaseCell createBaseCell(String exp) {
		exp = PPNparser.toInternalExp(exp);
		if (PPNparser.containsNoOperations(exp)) {
			return createBaseCell(PPNtoActivity.createActivity(exp));
		} else if (exp.contains(PPNparser.EQUAL)) {
			return createBaseCell(PPNtoActivity.createActivity(exp));
		}else {
			return createBaseCell(PPNtoRelation.createRelation(exp));
		}
	}

	public static ROP createROP(final String exp) {
		return PPNtoROP.createROP(exp);
	}

	/* convert to relation */
	public static Relation createRelation(final String exp) {
		final Relation r = PPNtoRelation.createRelation(exp);
		//printRelation(r);
		return r;
	}

	public static Activity createActivity(final String exp) {
		return PPNtoActivity.createActivity(exp);
	}

	/*
	 * public static ROP createROP(Object o) { if(o instanceof PetriPro){
	 * 
	 * PetriPro p = (PetriPro)o; rop = new CreateROP(p); return rop.getRop(); }
	 * return null; }
	 * 
	 * public static ROP createROP(PetriPro p) { rop = new CreateROP(p); return
	 * rop.getRop(); }
	 */

	public static void printROP(final Object o) {
		// DEBUG
		System.out.println("Converter.printROP()");
		// END DEBUG
		if (o instanceof ROP) {
			try {
				// rop
				final ROP rop = (ROP) o;

				System.out.println("Machine: " + rop.getMachine());
				System.out.println("Comment: " + rop.getComment());
				System.out.println("Id: " + rop.getId());

				final ROPType roptype = rop.getType();

				if (roptype != null) {
					System.out.println("Value: " + roptype.value());
				} else {
					System.out.println("Value: ");
				}

				System.out.println();
				System.out.println("Relation");

				// relation
				final Relation relation = rop.getRelation();

				printRelation(relation);

				System.out
						.println("\n--------------- Finished -----------------\n");

			} catch (final Exception ex) {
				if (ex instanceof NullPointerException) {
					System.out.println("EMPTY ROP!");
				} else {
					System.out.println("ERROR! while printing rop"
							+ "in Converter.printROP");
				}
			}
		} else {
			System.out
					.println("ERROR! in Converter.printROP: wrong type of object");
			System.out.println(o);
		}
	}

	public static void printRelation(final Relation relation) {
		final RelationType relationtype = relation.getType();

		// relationtype
		System.out.println("------------ " + relationtype.value()
				+ " ------------");

		final List list = relation.getActivityRelationGroup();
		final Iterator iterator = list.listIterator();

		while (iterator.hasNext()) {
			final Object o = iterator.next();
			if (o instanceof Relation) {
				printRelation((Relation) o);
			} else if (o instanceof Activity) {
				final Activity a = (Activity) o;

				System.out.println("Preconditions to operation "
						+ a.getOperation());
				final Precondition pre = a.getPrecondition();
				if (pre != null) {
					printPrecondition(pre);
				} else {
					System.out.println("No preconditions");
				}
				System.out.println("End " + a.getOperation());
				System.out.println();
			}
		}
		System.out.println("-----------------------------------------");
	}

	public static void printPrecondition(final Precondition precon) {
		final List list = precon.getPredecessor();
		final Iterator iterator = list.listIterator();

		while (iterator.hasNext()) {
			final Object o = iterator.next();
			if (o instanceof OperationReferenceType) {

				final String machine = ((OperationReferenceType) o).getMachine();
				final String operation = ((OperationReferenceType) o).getOperation();

				System.out.println("Machine: " + machine);
				System.out.println("Operation: " + operation);
			}
		}
	}
	
	/*
	public static String convertToString(final Object o) {
		return null;
	}

	public static String convertRelationToString(final RelationType relation) {
		return "Not implemented yet";
	}

	public static int numOfActivities(final Object o) {
		return -1;
	}
	*/
}
