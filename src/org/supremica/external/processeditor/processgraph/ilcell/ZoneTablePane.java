package org.supremica.external.processeditor.processgraph.ilcell;

import java.util.List;

import org.supremica.external.processeditor.processgraph.table.BasicTablePane;
import org.supremica.manufacturingTables.xsd.il.Term;
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
		
		table.getModel().setRowEditable(0, false);

		table.setValueAt("- # -", 0, 0);
		table.setValueAt("- # -", 0, 1);
	}
	
	public Zones getZones(){
		return ILTableExtractor.getZonesFromTable(table);
	}
	
	public void insertTerms(List<Term> termList){
		for(Term term : termList){
			ILTableFiller.insertZoneConditionFromTermToTable(term, table);
		}
	}
}
