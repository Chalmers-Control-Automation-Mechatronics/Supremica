package org.supremica.external.processeditor.processgraph.ilcell;

import java.util.List;

import org.supremica.manufacturingTables.xsd.il.Term;

public class ModeTablePane 
						extends 
							BasicTablePane
{
	public ModeTablePane(){
		super();
		setHeader(" ");
		
		table.addCol("Mode");
		
		table.getModel().setRowEditable(0, false);

		table.setValueAt("- # -", 0, 0);
	}
	
	public void insertTerms(List<Term> termList){
		for(Term term : termList){
			ILTableFiller.insertModeConditionFromTermToTable(term, table);
		}
	}
}
