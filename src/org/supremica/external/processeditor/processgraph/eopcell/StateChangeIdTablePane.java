package org.supremica.external.processeditor.processgraph.eopcell;

import java.util.List;

import org.supremica.external.processeditor.processgraph.table.BasicTablePane;
import org.supremica.manufacturingTables.xsd.eop.Action;

public class StateChangeIdTablePane
                                extends
                                    BasicTablePane
{
    private static final long serialVersionUID = 1L;

	public StateChangeIdTablePane(){
		super();
		setHeader(" ");
		
		table.addCol("Id");
		
		table.getModel().setRowEditable(0, false);
		table.getModel().setRowEditable(1, false);
		
		table.setValueAt("- # -", 0, 0);
	}
	
	public void insertActions(List<Action> actionList){
		for(Action action : actionList){
			EOPTableFiller.insertIdFromActionToTable(action, table);
		}
	}
}
