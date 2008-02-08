package org.supremica.external.processeditor.processgraph.ilcell;

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import org.supremica.manufacturingTables.xsd.il.*;

public class ILStructureGroupPane 
							extends 
								TableGroupPane
{
	BasicTablePane tableInternal = null;
	BasicTablePane tableExternal = null;
	BasicTablePane tableOperation = null;
	BasicTablePane tableZone = null;
	
	ILStructureGroupPane(){
		super();
		
		tableInternal = new InternalTablePane();
		tableExternal = new ExternalTablePane();
    	tableOperation = new OperationTablePane();
    	tableZone = new ZoneTablePane();
    	
    	tableInternal.showRowHeader(true);
    	
    	addTable(tableInternal);
    	addTable(tableExternal);
    	addTable(tableOperation);
    	addTable(tableZone);
	}
	
	public ILStructure getILStructure(){
		Term[] terms = null;
		ObjectFactory factory = new ObjectFactory();
		ILStructure ilStructure = factory.createILStructure();
		
		ilStructure.setInternalComponents(getInternalComponents());
		ilStructure.setExternalComponents(getExternalComponents());
		ilStructure.setOperations(getOperations());
		ilStructure.setZones(getZones());
		
		terms = getTerms();
		for(int i = 0; i < terms.length; i++){
			ilStructure.getTerm().add(terms[i]);
		}
		
		return ilStructure;
	}
	
	public InternalComponents getInternalComponents(){
		return TableExtractor.
					getInternalComponentsFromTable(tableInternal.getTable());
	}
	
	public ExternalComponents getExternalComponents(){
		return TableExtractor.
					getExternalComponentsFromTable(tableExternal.getTable());
	}
	
	public Zones getZones(){
		return TableExtractor.
					getZonesFromTable(tableZone.getTable());
	}
	
	public Operations getOperations(){
		return TableExtractor.
					getOperationsFromTable(tableOperation.getTable());
	}
	
	
	public Term[] getTerms(){
		return TableExtractor.getTerms(tableInternal, tableExternal, tableOperation, tableZone);
	}
	
	public void addAction(){
		
		String rowName = "Action" + Integer.toString(tableInternal.getRowCount() - 1); 
			
		tableInternal.addRow(rowName);
		tableExternal.addRow(rowName);
    	tableOperation.addRow(rowName);
    	tableZone.addRow(rowName);
	}
	
	public void deleteAction(int index){
		tableInternal.removeRow(index);
		tableExternal.removeRow(index);
    	tableOperation.removeRow(index);
    	tableZone.removeRow(index);
    	
    	setRowNames();
	}
	
	protected void makePopupMenu(){
		super.makePopupMenu();
		// Create some menu items for the popup
		JMenuItem menuEdit = new JMenuItem( "new Action" );
		popupMenu.add( menuEdit );
		menuEdit.addActionListener( this );
		
		menuEdit = new JMenuItem( "remove Action" );
		popupMenu.add( menuEdit );
		menuEdit.addActionListener( this );
	
		// Action and mouse listener support
		enableEvents( AWTEvent.MOUSE_EVENT_MASK );
	}
	
	//override
	public void actionPerformed( ActionEvent event ){
		if(event.getActionCommand().equals("new Action")){
			addAction();
		}else if(event.getActionCommand().equals("remove Action")){
			for(int i = 0; i < selectedRows.length; i++){
				deleteAction(selectedRows[i]);
			}
		}else{
			super.actionPerformed(event);
		}
	}
	
	private void setRowNames(){
		
		tableInternal.getTable().getModel().setRowName(1, "Initial");
		tableExternal.getTable().getModel().setRowName(1, "Initial");
    	tableOperation.getTable().getModel().setRowName(1, "Initial");
    	tableZone.getTable().getModel().setRowName(1, "Initial");
    	
    	String rowName = "Action";
    	for(int i = 1; i < tableInternal.getRowCount(); i++ ){
    		tableInternal.getTable().getModel().setRowName(i, rowName + i);
    		tableExternal.getTable().getModel().setRowName(i, rowName + i);
    		tableOperation.getTable().getModel().setRowName(i, rowName + i);
    		tableZone.getTable().getModel().setRowName(i, rowName + i);
    	}
    	
    	tableInternal.getTable().getModel().fireTableStructureChanged();
		tableExternal.getTable().getModel().fireTableStructureChanged();
		tableOperation.getTable().getModel().fireTableStructureChanged();
		tableZone.getTable().getModel().fireTableStructureChanged();
	}
}
