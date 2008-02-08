package org.supremica.external.processeditor.processgraph.ilcell;

public class ZoneTablePane 
						extends 
							BasicTablePane
{
	ZoneTablePane(){
		super();
		setHeader("Booked Zones");
		
		table.addCol("Before");
		table.addCol("After");
		
		table.addRow("Type");
		
		table.addRow("Initial");
		table.addRow("Action1");
	}
}
