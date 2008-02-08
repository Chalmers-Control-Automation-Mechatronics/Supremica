package org.supremica.external.processeditor.processgraph.ilcell;

public class ExternalTablePane 
						extends 
							BasicTablePane
{
	ExternalTablePane(){
		super();
		setHeader("External components state");
		
		addCol("Pallet in pos");
		addCol("Pallet in cell");
		
		addRow("Type");
		addRow("Initial");
		addRow("Action1");
	}
}
