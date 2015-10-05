package org.supremica.external.processAlgebraPetriNet.ppnedit.converter;

import java.util.List;
import java.awt.Point;

import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.BaseCell;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation.AlternativeCell;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation.ArbitaryOrderCell;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation.OpePetriCell;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation.ParallelCell;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation.SequenceCell;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation.SynchronizeCell;




//class to convert a Relation to BaseCell
public class RelationToBaseCell extends ActivityToBaseCell {
	public static BaseCell relationToBaseCell(Relation relation) {

		if (relation == null) {
			return null;
		}

		List list = relation.getActivityRelationGroup();

		if (list == null) {
			return null;
		}

		Object[] array = list.toArray();

		if (array.length == 1) {
			BaseCell cell = null;
			if (array[0] instanceof Activity) {
				Activity a = (Activity) array[0];
				cell = activityToBaseCell(a);
			} else {
				System.out.println("problem in RelationToBaseCell");
				return makeBaseCell(relation);
			}
			return cell;
		}

		return makeBaseCell(relation);
	}

	private static BaseCell[] makeCells(Object[] array) {
		BaseCell[] cells = new BaseCell[array.length];

		for (int i = 0; i < array.length; i++) {
			if (array[i] instanceof Activity) {
				Activity a = (Activity) array[i];
				cells[i] = activityToBaseCell(a);
			} else if (array[i] instanceof Relation) {
				// recursion
				cells[i] = makeBaseCell((Relation) array[i]);
			}
		}
		return cells;
	}

	private static BaseCell addProperties(BaseCell cell, Relation relation) {

		if (cell == null || relation == null) {
			return cell;
		}

		// add view
		if (cell instanceof OpePetriCell) {
			if (relation.getAlgebraic() != null) {
				((OpePetriCell) cell).setCompressed(relation.getAlgebraic().isCompressed());
			}
		}

		// add position
		// do this last
		if (relation.getPosition() != null) {
			Position pos = relation.getPosition();
			cell.setPos(new Point(pos.getXCoordinate(), pos.getYCoordinate()));
		}

		return cell;
	}

	private static BaseCell makeBaseCell(Relation relation) {

		if (relation == null) {
			System.out.println("RelationToBaseCell.java " + "makeBaseCell"
					+ " Relation = NULL!!");
			return null;
		}

		RelationType relationtype = relation.getType();
		if (relationtype == null) {
			System.out.println("RelationToBaseCell.java " + "makeBaseCell"
					+ " RelationType = NULL!!");

			return null;
		}

		List list = relation.getActivityRelationGroup();
		if (list == null) {
			System.out.println("RelationToBaseCell.java " + "makeBaseCell "
					+ " activityRelationGroup = NULL!!");

			return null;
		}

		Object[] array = list.toArray();
		if (array == null) {
			return null;
		}

		/* Everything is ok */

		BaseCell[] cells = new BaseCell[array.length];

		if (relationtype.equals(RelationType.SEQUENCE)) {

			cells = makeCells(array);
			BaseCell cell = new SequenceCell(cells);

			return addProperties(cell, relation);
		} else if (relationtype.equals(RelationType.ALTERNATIVE)) {

			cells = makeCells(array);
			BaseCell cell = new AlternativeCell(cells);

			return addProperties(cell, relation);
		} else if (relationtype.equals(RelationType.ARBITRARY)) {

			cells = makeCells(array);
			BaseCell cell = new ArbitaryOrderCell(cells);

			return addProperties(cell, relation);
		} else if (relationtype.value().equals("Synchronize")) {

			cells = makeCells(array);
			BaseCell cell = new SynchronizeCell(cells);

			return addProperties(cell, relation);
		} else if (relationtype.equals(RelationType.PARALLEL)) {

			cells = makeCells(array);
			BaseCell cell = new ParallelCell(cells);
			return addProperties(cell, relation);
		}

		System.out.println("Not implemented: " + relationtype.value());
		return null;
	}
}
