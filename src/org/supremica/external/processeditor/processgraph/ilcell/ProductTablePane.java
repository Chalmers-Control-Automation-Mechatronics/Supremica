package org.supremica.external.processeditor.processgraph.ilcell;

import java.util.List;

import org.supremica.external.processeditor.processgraph.table.BasicTablePane;
import org.supremica.manufacturingTables.xsd.il.Term;

public class ProductTablePane 
						extends 
							BasicTablePane
{
	private static final long serialVersionUID = 1L;

	public ProductTablePane(){
		super();
		setHeader("Product");
		
		table.addCol(" ");
		
		table.getModel().setRowEditable(0, false);

		table.setValueAt("- # -", 0, 0);
	}
	
	public void insertTerms(List<Term> termList){
		for(Term term : termList){
			ILTableFiller.insertProductConditionFromTermToTable(term, table);
		}
	}
}
