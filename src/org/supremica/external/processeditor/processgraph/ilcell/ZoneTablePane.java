package org.supremica.external.processeditor.processgraph.ilcell;

import org.supremica.manufacturingTables.xsd.il.Zones;

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
	
	public Zones getZones(){
		return TableExtractor.getZonesFromTable(table);
	}
}
