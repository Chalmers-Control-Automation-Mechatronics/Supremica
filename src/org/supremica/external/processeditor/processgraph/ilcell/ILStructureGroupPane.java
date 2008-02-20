package org.supremica.external.processeditor.processgraph.ilcell;

import java.util.List;
import java.util.LinkedList;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.math.BigInteger;
import javax.swing.JMenuItem;



import org.supremica.external.processeditor.processgraph.table.BasicTable;
import org.supremica.external.processeditor.processgraph.table.BasicTablePane;
import org.supremica.external.processeditor.processgraph.table.TableEvent;
import org.supremica.external.processeditor.processgraph.table.TableGroupPane;
import org.supremica.manufacturingTables.xsd.il.*;

public class ILStructureGroupPane 
							extends 
								TableGroupPane
{
	private static final String ROWNAME = "or"; 
	
	
	ModeTablePane tableMode = null;
	InternalTablePane tableInternal = null;
	ExternalTablePane tableExternal = null;
	OperationTablePane tableOperation = null;
	ZoneTablePane tableZone = null;
	ProductTablePane tableProduct = null;
	
	List<BasicTablePane> tableList = null;
	
	boolean showTableMode = true;
	boolean showTableInternal = true;
	boolean showTableExternal = true;
	boolean showTableOperation = true;
	boolean showTableZone = true;
	boolean showTableProduct = true;
	
	boolean showRowHeader = true;
	
	private long timeStamp = 0;
	private boolean controlDown = false;
	
	private List<Term> termCopyList = null;
	
	public ILStructureGroupPane(ILStructure ilStructure){
		super();
		
		if(ilStructure == null){
			ilStructure = (new ObjectFactory()).createILStructure();
		}
		
		tableMode = new ModeTablePane();
		tableInternal = new InternalTablePane(ilStructure.getInternalComponents());
		tableExternal = new ExternalTablePane(ilStructure.getExternalComponents());
    	tableOperation = new OperationTablePane();
    	tableZone = new ZoneTablePane();
    	tableProduct = new ProductTablePane();
    	
    	addConditionRow(); //first row contain additional information
    	addConditionRow(); //first condition row
    	
    	insertTerms(ilStructure.getTerm());
    	
    	setRowNames();
    	
    	//add all tables one time
    	addTable( tableMode );
    	addTable( tableInternal );
    	addTable( tableExternal );
    	addTable( tableOperation );
    	addTable( tableZone );
    	addTable( tableProduct );		
    	
    	//show selected tables only
    	showTables();
    	
	}
	
	public void insertTerms(List<Term> termList){
		
		tableMode.insertTerms(termList);
		tableInternal.insertTerms(termList);
		tableExternal.insertTerms(termList);
		tableOperation.insertTerms(termList);
		tableZone.insertTerms(termList);
		tableProduct.insertTerms(termList);
	}
	
	private void showTables(){
		boolean rowHeader = showRowHeader;
		
		removeAll();
		validate();
		
		if( showTableMode ){
			tableMode.showRowHeader(rowHeader);
			rowHeader = false;
			
			addTable( tableMode );
		}
		
		if( showTableInternal ){
			tableInternal.showRowHeader(rowHeader);
			rowHeader = false;
			
			addTable( tableInternal );
		}
		
		if( showTableExternal ){
			tableExternal.showRowHeader(rowHeader);
			rowHeader = false;
			
			addTable( tableExternal );
		}
		
		if( showTableOperation ){
			tableOperation.showRowHeader(rowHeader);
			rowHeader = false;
			
			addTable( tableOperation );
		}
		
		if( showTableZone ){
			tableZone.showRowHeader(rowHeader);
			rowHeader = false;
			
			addTable( tableZone );
		}
		
		if( showTableProduct ){
			
			tableProduct.showRowHeader(rowHeader);
			rowHeader = false;
			
			addTable( tableProduct );
		}
		
		validate();
		repaint();
	}
	
	public void setRowHeaderVisible(boolean show){
		if(showRowHeader == show){
			return;
		}
		
		showRowHeader = show;
		showTables();
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
		return ILTableExtractor.getTerms(tableMode.getTable(),
										 tableInternal.getTable(),
										 tableExternal.getTable(),
										 tableOperation.getTable(),
										 tableZone.getTable(),
										 tableProduct.getTable());
	}
	
	public void showModeTable(boolean show){
		if(show == showTableMode){
			return;
		}
		
		showTableMode = show;
		showTables();
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
	
	public void showOperationTable( boolean show ){
		
		if(show == showTableOperation){
			return;
		}
		
		showTableOperation = show;
		showTables();
	}
	
	public void showZoneTable( boolean show ){
		
		if(show == showTableZone){
			return;
		}
		
		showTableZone = show;
		showTables();
	}
	
	public void showProductTable( boolean show ){
		if(show == showTableProduct){
			return;
		}
		
		showTableProduct = show;
		showTables();
	}
	
	public void copyTerms(){
		Term[] terms = getTerms();
		
		if(selectedRows.length > 0){
			termCopyList = new LinkedList<Term>();
			
			for(int i = 0; i < selectedRows.length; i++){
				
				/*
				 * terms[0] is row 1 in table.
				 * Thats why selectedRows[i]-1 
				 */
				if(0 <= selectedRows[i] - 1){
					termCopyList.add(terms[selectedRows[i]-1]);
				}
			}
		}
	}
	
	public void insertTermCopies(){
		int diff = 0;
		
		if(null == termCopyList || termCopyList.size() == 0){
			return;
		}
		
		if(selectedRows.length == 0){
			return;
		}
		
		diff = selectedRows[0] - termCopyList.get(0).getRow().intValue();
		for(Term term : termCopyList){
			
			term.setRow(term.getRow().add(BigInteger.valueOf(diff - 1)));
			//add empty row
			addConditionRow(term.getRow().intValue() + 1, null);
		}
		
		//writhe to empty row
		insertTerms( termCopyList );
	}
	
	public void addConditionRow(){
		tableMode.addRow(ROWNAME);
		tableInternal.addRow(ROWNAME);
		tableExternal.addRow(ROWNAME);
    	tableOperation.addRow(ROWNAME);
    	tableZone.addRow(ROWNAME);
    	tableProduct.addRow(ROWNAME);
	}
	
	public void addConditionRow(int rowIndex, BasicTable table){
		
		if(rowIndex == 0){
			rowIndex = 1;
		}else if(rowIndex > tableMode.getRowCount()){
			addConditionRow();
			return;
		}
		
		tableMode.addRow(rowIndex, ROWNAME);
		tableInternal.addRow(rowIndex, ROWNAME);
		tableExternal.addRow(rowIndex, ROWNAME);
    	tableOperation.addRow(rowIndex, ROWNAME);
    	tableZone.addRow(rowIndex, ROWNAME);
    	tableProduct.addRow(rowIndex, ROWNAME);
    	
    	if(null != table){
    		table.getSelectionModel().setSelectionInterval(rowIndex, rowIndex);
    	}
	}
	
	public void deleteConditionRow(int index){
		
		//don't remove type identifier row
		if(index == 0){
			return;
		}
		
		//don't delete last condition row
		if(tableMode.getRowCount() <= 2){
			return;
		}
		
		tableMode.removeRow(index);
		tableInternal.removeRow(index);
		tableExternal.removeRow(index);
    	tableOperation.removeRow(index);
    	tableZone.removeRow(index);
    	tableProduct.removeRow(index);
    	
    	setRowNames();
	}
	
	private void deleteSelectedRows( BasicTable table ){
		
		int row = selectedRows[0];
		
		for(int i = 0; i < selectedRows.length; i++){
			deleteConditionRow(selectedRows[i]);
		}
		
		if(row >= table.getRowCount()){
			row = table.getRowCount() - 1;
		}
		
		table.getSelectionModel().setSelectionInterval(row, row);
	}
	
	
	//override
	protected void makePopupMenu(){
		
		super.makePopupMenu();
		
		// Create some menu items for the popup
		JMenuItem menuEdit = new JMenuItem( "add condition" );
		popupMenu.add( menuEdit );
		menuEdit.addActionListener( this );
		
		menuEdit = new JMenuItem( "remove condition" );
		popupMenu.add( menuEdit );
		menuEdit.addActionListener( this );
	}
	
	//override
	public void actionPerformed( ActionEvent event ){
		if(event.getActionCommand().equals("add condition")){
			addConditionRow();
		}else if(event.getActionCommand().equals("remove condition")){
			
			for(int i = 0; i < selectedRows.length; i++){
				deleteConditionRow(selectedRows[i]);
			}
		}else{
			super.actionPerformed(event);
		}
	}
	
	public void columnRemoved(TableEvent e){
		termCopyList = null;
	}
	
	private void setRowNames(){
		
		if(tableInternal.getRowCount() == 0){
			return;
		}
		
		tableMode.getTable().getModel().setRowName(0, "");
		tableInternal.getTable().getModel().setRowName(0, "");
		tableExternal.getTable().getModel().setRowName(0, "");
    	tableOperation.getTable().getModel().setRowName(0, "");
    	tableZone.getTable().getModel().setRowName(0, "");
    	tableProduct.getTable().getModel().setRowName(0, "");
    	
    	if(tableInternal.getRowCount() >= 1){
    		tableMode.getTable().getModel().setRowName(1, "");
    		tableInternal.getTable().getModel().setRowName(1, "");
    		tableExternal.getTable().getModel().setRowName(1, "");
    		tableOperation.getTable().getModel().setRowName(1, "");
    		tableZone.getTable().getModel().setRowName(1, "");
    		tableProduct.getTable().getModel().setRowName(1, "");
    	}
    	
    	for(int i = 2; i < tableInternal.getRowCount(); i++ ){
    		tableMode.getTable().getModel().setRowName(i, ROWNAME);
    		tableInternal.getTable().getModel().setRowName(i, ROWNAME);
    		tableExternal.getTable().getModel().setRowName(i, ROWNAME);
    		tableOperation.getTable().getModel().setRowName(i, ROWNAME);
    		tableZone.getTable().getModel().setRowName(i, ROWNAME);
    		tableProduct.getTable().getModel().setRowName(i, ROWNAME);
    	}
    	
    	tableMode.getTable().getModel().fireTableStructureChanged();
    	tableInternal.getTable().getModel().fireTableStructureChanged();
		tableExternal.getTable().getModel().fireTableStructureChanged();
		tableOperation.getTable().getModel().fireTableStructureChanged();
		tableZone.getTable().getModel().fireTableStructureChanged();
		tableProduct.getTable().getModel().fireTableStructureChanged();
		
	}
	
	//override
	public void keyPressed(KeyEvent e){
		controlDown = (e.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK);
	} 
	
	public void keyReleased(KeyEvent e){
		
		/*
		 * All tables fire KeyEvent on same event
		 * if we have a different time stamp it is a new event
		 */
		
		if(timeStamp != e.getWhen()){
			timeStamp = e.getWhen();
			
			//----------------------------------------
			//	DELETE
			//----------------------------------------
			if(e.getKeyCode() == KeyEvent.VK_DELETE){
				if(e.getSource() instanceof BasicTable){
					deleteSelectedRows((BasicTable) e.getSource());
				}
			}
			
			//Is CTRL down 
			if(controlDown){
				
				//----------------------------------
				//	CTRL + N
				//----------------------------------
				if(e.getKeyCode() == KeyEvent.VK_N){
					if(selectedRows.length > 0){
						if(e.getSource() instanceof BasicTable){
							addConditionRow(selectedRows[0] + 1, (BasicTable) e.getSource());
						}
					}
				}
				
				//----------------------------------
				//	CTRL + C
				//----------------------------------
				if(e.getKeyCode() == KeyEvent.VK_C){
						copyTerms();
				}
				
				//------------------------------------
				//	CTRL + V
				//------------------------------------
				if(e.getKeyCode() == KeyEvent.VK_V){
					int row = selectedRows[0];
					insertTermCopies();	
					
					if(e.getSource() instanceof BasicTable){
						
						((BasicTable)e.getSource())
								.getSelectionModel()
									.setSelectionInterval(row,row);
					}
				}
				
				//------------------------------------
				//	CTRL + X
				//------------------------------------
				if(e.getKeyCode() == KeyEvent.VK_X){
					copyTerms();
					if(e.getSource() instanceof BasicTable){
						deleteSelectedRows((BasicTable) e.getSource());
					}
				}
			}
		}
	}
}
