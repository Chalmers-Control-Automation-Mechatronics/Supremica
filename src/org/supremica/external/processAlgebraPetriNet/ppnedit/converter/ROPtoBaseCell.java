package org.supremica.external.processAlgebraPetriNet.ppnedit.converter;

import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation.RopCell;


//class to convert a ROP to BaseCell
public class ROPtoBaseCell
						extends RelationToBaseCell {
	
	public static BaseCell ROPtoBaseCell(ROP rop) {

		if (rop == null) {
			return null;
		}
		
		BaseCell cell = new RopCell(rop);
		return cell;
	}
}
