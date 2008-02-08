package org.supremica.external.processeditor.processgraph.ilcell;

public class TableEvent {
	
	private BasicTable myTable = null;
	
	public TableEvent(BasicTable table){
		myTable = table;
	}
	
	public BasicTable getSource(){
		return myTable;
	}
}
