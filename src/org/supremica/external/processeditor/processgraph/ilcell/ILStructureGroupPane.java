package org.supremica.external.processeditor.processgraph.ilcell;

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import java.util.List;
import java.util.LinkedList;

import org.supremica.manufacturingTables.xsd.il.*;

public class ILStructureGroupPane 
							extends 
								TableGroupPane
{
	InternalTablePane tableInternal = null;
	ExternalTablePane tableExternal = null;
	OperationTablePane tableOperation = null;
	ZoneTablePane tableZone = null;
	
	List<BasicTablePane> tableList = null;
	
	boolean showTableInternal = true;
	boolean showTableExternal = true;
	boolean showTableOperation = true;
	boolean showTableZone = true;
	
	public ILStructureGroupPane(ILStructure ilStructure){
		super();
		
		if(ilStructure == null){
			ilStructure = (new ObjectFactory()).createILStructure();
		}
		
		tableInternal = new InternalTablePane(ilStructure.getInternalComponents());
		tableExternal = new ExternalTablePane(ilStructure.getExternalComponents());
    	tableOperation = new OperationTablePane();
    	tableZone = new ZoneTablePane();
    	
    	setRowNames();
    	
    	showTables();
	}
	
	private void showTables(){
		boolean rowHeader = true;
		
		removeAll();
		
		if( showTableInternal ){
			tableInternal.showRowHeader(rowHeader);
			rowHeader = false;
			
			addTable(tableInternal);
		}
		
		if( showTableExternal ){
			tableExternal.showRowHeader(rowHeader);
			rowHeader = false;
			
			addTable(tableExternal);
		}
		
		if( showTableOperation ){
			tableOperation.showRowHeader(rowHeader);
			rowHeader = false;
			
			addTable(tableOperation);
		}
		
		if( showTableZone ){
			tableZone.showRowHeader(rowHeader);
			rowHeader = false;
			
			addTable(tableZone);
		}
		
		validate();
		repaint();
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
		return tableInternal.getInternalComponents();
	}
	
	public ExternalComponents getExternalComponents(){
		return tableExternal.getExternalComponents();
	}
	
	public Zones getZones(){
		return tableZone.getZones();
	}
	
	public Operations getOperations(){
		return tableOperation.getOperations();
	}
	
	public Term[] getTerms(){
		return TableExtractor.getTerms(tableInternal, tableExternal, tableOperation, tableZone);
	}
	
	public void showInternalTable(boolean show){
		if(show == showTableInternal){
			return;
		}
		
		showTableInternal = show;
		showTables();
	}
	
	public void showExternalTable(boolean show){
		
		if(show == showTableExternal){
			return;
		}
		
		showTableExternal = show;
		showTables();
	}
	
	public void showOperationTable(boolean show){
		
		if(show == showTableOperation){
			return;
		}
		
		showTableOperation = show;
		showTables();
	}
	
	public void showZoneTable(boolean show){
		
		if(show == showTableZone){
			return;
		}
		
		showTableZone = show;
		showTables();
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
		
		tableInternal.getTable().getModel().setRowName(0, "");
		tableExternal.getTable().getModel().setRowName(0, "");
    	tableOperation.getTable().getModel().setRowName(0, "");
    	tableZone.getTable().getModel().setRowName(0, "");
    	
		tableInternal.getTable().getModel().setRowName(1, "Initial");
		tableExternal.getTable().getModel().setRowName(1, "Initial");
    	tableOperation.getTable().getModel().setRowName(1, "Initial");
    	tableZone.getTable().getModel().setRowName(1, "Initial");
    	
    	String rowName = "Action";
    	int row;
    	for(int i = 2; i < tableInternal.getRowCount(); i++ ){
    		row = i - 1;
    		
    		tableInternal.getTable().getModel().setRowName(i, rowName + row);
    		tableExternal.getTable().getModel().setRowName(i, rowName + row);
    		tableOperation.getTable().getModel().setRowName(i, rowName + row);
    		tableZone.getTable().getModel().setRowName(i, rowName + row);
    	}
    	
    	tableInternal.getTable().getModel().fireTableStructureChanged();
		tableExternal.getTable().getModel().fireTableStructureChanged();
		tableOperation.getTable().getModel().fireTableStructureChanged();
		tableZone.getTable().getModel().fireTableStructureChanged();
	}
}
