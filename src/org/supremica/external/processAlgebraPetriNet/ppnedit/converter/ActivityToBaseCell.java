package org.supremica.external.processAlgebraPetriNet.ppnedit.converter;

import java.awt.Point;

import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.BaseCell;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.Transition;



//class to convert a Activity to BaseCell

public class ActivityToBaseCell {
	public static BaseCell activityToBaseCell(Activity a) {
		BaseCell cell = new Transition();
		cell.setExp(a.getOperation());
		
		if (a.getPosition() != null) {
			Position pos = a.getPosition();
			cell.setPos(new Point(pos.getXCoordinate(), pos.getYCoordinate()));
		}
		return cell;
	}
}
