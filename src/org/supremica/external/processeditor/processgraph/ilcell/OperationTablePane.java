package org.supremica.external.processeditor.processgraph.ilcell;

import java.util.List;

import org.supremica.external.processeditor.processgraph.table.BasicTablePane;
import org.supremica.manufacturingTables.xsd.il.Operations;
import org.supremica.manufacturingTables.xsd.il.Term;

public class OperationTablePane 
						extends 
							BasicTablePane
{
	OperationTablePane(){
		super();
		setHeader("Operations");
		
		addCol("NOT started");
		addCol("NOT ongoing");
		
		table.getModel().setRowEditable(0, false);

		table.setValueAt("- # -", 0, 0);
		table.setValueAt("- # -", 0, 1);
	}
	
	public Operations getOperations(){
		return ILTableExtractor.getOperationsFromTable(table);
	}
	
	public void insertTerms(List<Term> termList){
		for(Term term : termList){
			ILTableFiller.insertOperationConditionFromTermToTable(term, table);
		}
	}
}
