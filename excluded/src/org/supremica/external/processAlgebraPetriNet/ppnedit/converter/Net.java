package org.supremica.external.processAlgebraPetriNet.ppnedit.converter;

import java.io.*;
import java.util.*;

import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.BaseCell;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.util.BaseCellArray;




public class Net {

	protected static ObjectFactory factory = new ObjectFactory();

	protected static BaseCell startcell = null;

	protected static BaseCell endcell = null;

	protected static boolean same_end = true;

	public static Relation createRelation(BaseCell start, BaseCell[] cells) {

		/* check indata */
		if (!startCell(startcell) || cells == null || cells.length == 0) {
			return null;
		}

		Relation relation = factory.createRelation();

		startcell = start;

		BaseCell[] targets = startcell.getTargetCells();

		/* check typ of nod */
		if (targets.length == 1) {
			System.out.println("sequence");
		} else {
			System.out.println("Alternative");
		}

		/* check typ of end */
		if (sameTargetCells(targets)) {

			BaseCell[] tmp = targets[0].getTargetCells();

			if (tmp != null) {
				if (tmp.length == 1) {
					if (endCell(tmp[0])) {
						System.out.println("end");
					}
				}
			}
		} else {
			;
		}

		return null;
	}

	public static boolean sameTargetCells(BaseCell[] cells) {

		if (cells == null || cells.length <= 1) {
			return true;
		}

		BaseCell[] tmp1 = null;
		BaseCell[] tmp2 = null;

		/* loop over all cells */
		for (int i = 0; i < cells.length - 1; i++) {
			tmp1 = cells[i].getTargetCells();
			tmp2 = cells[i + 1].getTargetCells();

			if (!BaseCellArray.equal(tmp1, tmp2)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * function to test if cell is star cell = no source cells but have target
	 * cells
	 */
	public static boolean startCell(BaseCell cell) {
		/* check indata */
		if (cell == null) {
			return false;
		}

		/* no source cells */
		if (cell.getSourceCells() != null) {
			return false;
		}

		/* need target cells */
		return cell.getTargetCells() != null;
	}

	public static boolean endCell(BaseCell cell) {
		/* check indata */
		if (cell == null) {
			return false;
		}

		/* no target cells */
		if (cell.getTargetCells() != null) {
			return false;
		}

		/* need source cells */
		return cell.getSourceCells() != null;
	}

}
