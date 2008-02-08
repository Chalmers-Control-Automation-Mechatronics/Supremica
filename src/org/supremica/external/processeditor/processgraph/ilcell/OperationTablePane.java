package org.supremica.external.processeditor.processgraph.ilcell;

public class OperationTablePane 
						extends 
							BasicTablePane
{
	OperationTablePane(){
		super();
		setHeader("Operations");
		
		addCol("NOT started");
		addCol("NOT ongoing");
		
		addRow("Type");
		addRow("Initial");
		addRow("Action1");
	}
}
