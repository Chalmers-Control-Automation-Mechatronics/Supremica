package org.supremica.external.processeditor.processgraph.ilcell;

import org.supremica.manufacturingTables.xsd.il.Operations;

public class OperationTablePane 
						extends 
							BasicTablePane
{
	OperationTablePane(){
		super();
		setHeader("Operations");
		
		addCol("NOT started");
		addCol("NOT ongoing");
		
		addRow("");
		addRow("Initial");
		addRow("Action1");
	}
	
	public Operations getOperations(){
		return TableExtractor.getOperationsFromTable(table);
	}
}
