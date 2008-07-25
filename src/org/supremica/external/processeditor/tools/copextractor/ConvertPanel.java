package org.supremica.external.processeditor.tools.copextractor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.List;
import java.util.LinkedList;

import java.awt.*;
import java.awt.event.*;

import javax.swing.JPanel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.jdom.Document;
import org.jdom.output.XMLOutputter;

import org.supremica.manufacturingTables.xsd.processeditor.ROP;
import org.supremica.manufacturingTables.xsd.eop.EOP;
import org.supremica.manufacturingTables.xsd.il.IL;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;

import org.supremica.external.processeditor.xml.Loader;

import org.supremica.external.avocades.COPBuilder;

import org.supremica.gui.ide.IDE;
import org.supremica.external.processeditor.SOCGraphContainer;
import org.supremica.external.processeditor.SOCFileFilter;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

public class ConvertPanel 
					extends 
					    JPanel 
							implements 
							    ActionListener,
							    MouseListener
{
	
	private static final String XML_EXTENSION = ".xml";
	private static final String WATER_MODULE_EXTENSION = ".wmod";
	
	private final String[] jbuttons = new String[]{"Specification synthesis",
			                                       "DOP to EFA",
			                                       "Generate Operations",
			                                       "Build together",
			                                       "Synthes",
			                                       "RelationExtraction",
			                                       "Save adapted"};
	
	private TablePane tablePane;
	private JPanel buttonPane;
    
    private JFrame dialogReferenceFrame = null;
    private SOCGraphContainer container = null;
    
    private COPBuilder builder;
    
    private ModuleSubjectFactory factory;
    
    private JFileChooser fcOutput;
    
    //constructor
    public ConvertPanel() {
        super( new BorderLayout() );
        
        tablePane = new TablePane();
    	buttonPane = new JPanel();
    
    	tablePane.addMouseListener( this );
    	
        tablePane.setBorder(BorderFactory.createCompoundBorder(
                  BorderFactory.createTitledBorder( "Specification files" ),
                  BorderFactory.createEmptyBorder( 5, 5, 5, 5)));
        
        buttonPane.setBorder(BorderFactory.createCompoundBorder(
                   BorderFactory.createTitledBorder( "Action" ),
                   BorderFactory.createEmptyBorder( 5, 5, 5, 5)));
        
        
        //add components to buttonPane
        buttonPane.setLayout( new BoxLayout( buttonPane, BoxLayout.Y_AXIS ) );
        for( int i = 0; i < jbuttons.length; i++ ){
        	
        	JButton jb = new JButton( jbuttons[i] );
        	jb.setActionCommand( jbuttons[i] );
        	jb.addActionListener( this );
        	
        	buttonPane.add( jb );
        	buttonPane.add( Box.createRigidArea( new Dimension( 1, 10 )) );
        }
        
        fcOutput = new JFileChooser();
        
        //Add Components to this panel.
        add( tablePane, BorderLayout.CENTER );
        add( buttonPane, BorderLayout.EAST );
        
        setPreferredSize(new Dimension(tablePane.getPreferredSize().width*2 +
        		                       buttonPane.getPreferredSize().width,
        		                       Math.max(tablePane.getPreferredSize().height*2,
        		                    		    buttonPane.getPreferredSize().height*2)));
    }
    
    public void setFrame(JFrame frame) {
    	dialogReferenceFrame = frame;
    }
    
    public void setGraphContainer(SOCGraphContainer container) {
    	this.container = container;
    }
    
    public SOCGraphContainer getGraphContainer(){
    	return container;
    }
    
    //TODO: Make this work
    public void setOutputFileChooser(JFileChooser fc) {
    	this.fcOutput = fc;
    }
    
    public void setInputFileChooser(JFileChooser fc) {
    	tablePane.setFileChooser(fc);
    }
    
    public void refreshTable(){
    	tablePane.refresh();
    }
    
    public void addFile( File file ){
    	tablePane.addFile( file );
    }
    
    /**
     *	Take care of action
     */
    public void actionPerformed( ActionEvent evt ) {
    	
        if( "Specification synthesis".equals( evt.getActionCommand() ) ){
        	
        	if( checkInputToConvertAndInformUser() ){
            	buildSpecificationSynthes();
        	}
        	
        }else if( "DOP to EFA".equals( evt.getActionCommand() ) ){
        	
        	if( checkInputToConvertAndInformUser() ){
        		buildRelationsFromROP();
        	}
        	
        }else if( "Generate Operations".equals( evt.getActionCommand() ) ){
        	
        	if( checkInputToConvertAndInformUser() ){
        		buildOperationsFromEOP();
        	}
        	
        }else if( "Build together".equals( evt.getActionCommand() ) ){
        	
        	if( checkInputToConvertAndInformUser() ){
        		buildTogether();
        	}
        	
        }else if( "Synthes".equals( evt.getActionCommand() ) ){
        	
        	if( checkInputToConvertAndInformUser() ){
        		buildSynthes();
        	}
        	
        }else if( "RelationExtraction".equals( evt.getActionCommand() ) ){
        	
        	if( checkInputToConvertAndInformUser() ){
        		relationExtraction();
        	}
        	
        }else if( "Save adapted".equals( evt.getActionCommand() ) ){
        	
        	if( checkInputToConvertAndInformUser() ){
        		saveAdaptedSpecifications();
        	}
        	
        }else{
        	System.err.println( "Unknown action: " + evt.getActionCommand() );
        }
        
    }
    
    private void loadFiles(){
        Object o = null;
    	
        try{
        	builder = new COPBuilder();
        }catch(JAXBException e){
        	;
        }catch(SAXException e){
        	;
        }
    	
    	Loader loader = null;
    	
    	List<String> filePathList = null;
    	
    	//init
    	loader = new Loader();
    	filePathList = tablePane.getMarkedFilePathList();
    	
    	//Sanity check
    	if( null == filePathList || 0 == filePathList.size()){
    		return;
    	}
    		
    	for( String filePath : filePathList ){
    			
    		o = loader.open( new File( filePath ) );
    			
            if( o instanceof ROP ){
    			builder.add( (ROP) o);
    		}else if( o instanceof EOP ){
    			builder.add( (EOP)o );
    		}else if( o instanceof IL ){
    			builder.add( (IL)o );
    		}else{
    			JOptionPane.
    				showMessageDialog(dialogReferenceFrame,
    								  "Unknown object in " + filePath,
    								  "Problem",
    								  JOptionPane.ERROR_MESSAGE);
    		}
    	}
    }
    
    private void buildSpecificationSynthes() {
    	
    	ModuleSubject module = null;
    	
    	loadFiles();
    	
    	module = builder.getSpecificationSynthesisOutput();
    	
    	openWithIDE(module);
    }
    
    private void buildRelationsFromROP(){
    	
    	File file = null;
    	ModuleSubject module = null;
    	
    	loadFiles();
    	module = builder.getDOPtoEFAOutput();
    	
    	//create temporary file
    	try{
            file = File.createTempFile("dop_relation_extract", WATER_MODULE_EXTENSION);
        }catch( IOException e ){
        	;
        }
        
        //Save module to file
        try
		{
			JAXBModuleMarshaller marshaller = 
				new JAXBModuleMarshaller(factory, 
						                 CompilerOperatorTable.getInstance());	
			marshaller.marshal(module, file);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
        openFileWithIDE( file );
            
    	//delete temporary file
        if( null != file){
        	file.delete();
        }	
    	
    	
    }
    
    private void buildOperationsFromEOP(){
    	
    	File file = null;
    	ModuleSubject module = null;
    	
    	loadFiles();
    	module = builder.getEOPtoEFAOutput();
    	
    	//create temporary file
    	try{
            file = File.createTempFile("eop_build_efa", WATER_MODULE_EXTENSION);
        }catch( IOException e ){
        	;
        }
        
        //Save module to file
        try
		{
			JAXBModuleMarshaller marshaller = 
				new JAXBModuleMarshaller(factory, 
						                 CompilerOperatorTable.getInstance());	
			marshaller.marshal(module, file);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		//Open with IDE
        openFileWithIDE( file );
            
    	//delete temporary file
        if( null != file){
        	file.delete();
        }	
    	
    	
    }
    
    
    
    /*-------------------------------------------------------------------------
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     *------------------------------------------------------------------------*/
    private void buildTogether(){
    	
    	File file = null;
    	ModuleSubject module = null;
    	
    	List<ModuleSubject> moduleList = new LinkedList<ModuleSubject>();
    	
    	//Update files
    	loadFiles();
    	
    	//
    	moduleList.add( builder.getSpecificationSynthesisOutput() );
    	moduleList.add( builder.getEOPtoEFAOutput() );
    	moduleList.add( builder.getDOPtoEFAOutput() );
    	
    	module = builder.mergeModules( moduleList );
    	
    	module.setName("Together");
    	module.setComment("DOP to EFA output\n" + 
    			          "DOP to EFA output\n" + 
    			          "EOP to EFA output\n" + 
    			          "Specification synthesis output\n");
    	
    	//create temporary file
    	try{
            file = File.createTempFile("build_all_togheter", WATER_MODULE_EXTENSION);
        }catch( IOException e ){
        	;
        }
        
        try
		{
			JAXBModuleMarshaller marshaller = new JAXBModuleMarshaller(factory, CompilerOperatorTable.getInstance());	
			marshaller.marshal(module, file);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
  
        openFileWithIDE( file );
            
    	//delete temporary file
        if( null != file){
        	file.delete();
        }	
    }
    
    
    
    
    
    private boolean checkInputToConvertAndInformUser(){
    	
    	String noFileTitle = "No files";
    	String noFileMessage = "No files to convert";
    	
    	/*
    	 * Do we have some files
    	 */
    	if( tablePane.getMarkedFilePathList() == null ||
    	    tablePane.getMarkedFilePathList().size() == 0){
    		
    		if( dialogReferenceFrame == null ){
    			System.out.println( noFileMessage );
    		}else{
    			JOptionPane.
    				showMessageDialog( dialogReferenceFrame,
    								   noFileMessage,
    								   noFileTitle,
    								   JOptionPane.ERROR_MESSAGE );
    		}
    		return false;
    	}
    	return true;
    }
    
    private void buildSynthes() {
    	File file = null;
    	ModuleSubject module = null;
    	
    	List<ModuleSubject> moduleList = new LinkedList<ModuleSubject>();
    	
    	//Update files
    	loadFiles();
    	
    	//
    	moduleList.add( builder.getSpecificationSynthesisOutput() );
    	moduleList.add( builder.getEOPtoEFAOutput() );
    	moduleList.add( builder.getDOPtoEFAOutput() );
    	
    	module = builder.createSupervisor( moduleList );
    	
    	module.setName("Synthes");
    	module.setComment("DOP to EFA output\n" + 
    			          "DOP to EFA output\n" + 
    			          "EOP to EFA output\n" + 
    			          "Specification synthesis output\n");
    	
    	//create temporary file
    	try{
            file = File.createTempFile("sync_all_togheter", WATER_MODULE_EXTENSION);
        }catch( IOException e ){
        	;
        }
        
        try
		{
			JAXBModuleMarshaller marshaller = new JAXBModuleMarshaller(factory, CompilerOperatorTable.getInstance());	
			marshaller.marshal(module, file);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
  
        openFileWithIDE( file );
            
    	//delete temporary file
        if( null != file){
        	file.delete();
        }	
    }
    
    private void relationExtraction() {
    	List<ROP> copList;
    	
    	loadFiles();
    	
    	copList = builder.getRelationExtractionOutput();
    	
    	if(null == copList || 0 == copList.size() ){
    		System.out.println("No COP:s");
    		return;
    	}
    	
    	if(null != container){
    		
    		for(ROP rop : copList){
    			container.insertResource(rop, null);
    		}
    	}
    	
    	
    }
    
    /**
     * Enables the user to save document to a file
     * @param document
     */
    private void saveDocument( Document document ){
    	
    	int returnVal = JFileChooser.CANCEL_OPTION;
    	
    	//Create a file chooser
		JFileChooser fc = new JFileChooser();
        
		//set selection mode
        fc.setFileSelectionMode( JFileChooser.FILES_ONLY );
        fc.setFileFilter( new SOCFileFilter( XML_EXTENSION ) );

    	returnVal = fc.showOpenDialog( this );
        if( returnVal == JFileChooser.APPROVE_OPTION ) {
            
        	File file = fc.getSelectedFile();
            
        	if(file.isFile()){
        		saveDocument( document, file );	
        	}
        } else {
        	;
        }
    }
    
    private void saveAdaptedSpecifications(){
    	
    	final String PATH;
    	
    	String fileName = "";
    	
    	Loader loader = new Loader();
    	File file;
    	
    	loadFiles();
    	
    	if(fcOutput == null){
    		fcOutput = new JFileChooser();
    	}
    	
    	fcOutput.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    	
        if(JFileChooser.APPROVE_OPTION == fcOutput.showOpenDialog(this)){
            file = fcOutput.getSelectedFile();
            PATH = file.getAbsolutePath() + "//";
        }else{
        	return;
        }
    	
    	List<EOP> eopList = builder.getAdaptedEOPList();
    	for(int i = 0; i < eopList.size(); i++){
    		
    		fileName = "eop_" + i + ".xml";
    		file = new File(PATH + fileName);
    		loader.saveEOP(eopList.get(i), file );
    	}
    	
    	List<IL> ilList = builder.getAdaptedILList();
    	for(int i = 0; i < ilList.size(); i++){
    		
    		fileName = "il_" + i + ".xml";
    		file = new File(PATH + fileName);
    		loader.saveIL(ilList.get(i), file);
    	}
    	
    	List<ROP> ropList = builder.getAdaptedROPList();
    	for(int i = 0; i < ropList.size(); i++){
    		
    		fileName = "rop_" + i + ".xml";
    		file = new File(PATH + fileName);
    		loader.save(ropList.get(i), file);
    	}
    }
    
    /**
     * Opens the document in IDE if possible.
     * @param document the document to be opened
     */
    private void openDocument( Document document ){
    	
    	IDE ide = null;
    	File file = null;
    	
    	//Sanity check
    	if( null == container ){
    	    return;    
    	}
    	
    	ide = container.getIDE();
    	
    	//Sanity check
    	if( null == ide ){
            return;
        }
    	
    	//create temporary file
    	try{
            file = File.createTempFile("specification_synthesis", ".xml");
        }catch( IOException e ){
        	;
        }
        	
        saveDocument( document, file );
        	
        //open file in IDE
    	ide.getDocumentContainerManager().openContainer( file );
    	ide.setVisible(true);
    	    
    	//delete temporary file
        if( null != file){
        	file.delete();
        }	
    }
    
    private void openFileWithIDE( File file ){
    	
    	IDE ide = null;
    	
    	//Sanity check
    	if( null == file ){
    		return;
    	}
    	
    	//Sanity check
    	if( null == container ){
    	    return;    
    	}
    	
    	ide = container.getIDE();
    	
    	//Sanity check
    	if( null == ide ){
            return;
        }
    	
    	//open file in IDE
    	ide.getDocumentContainerManager().openContainer( file );
    	ide.setVisible(true);
    }
    
    private void openWithIDE(ModuleSubject modsub){
    	IDE ide = null;
    	
    	//Sanity check
    	if( null == modsub){
    		return;
    	}
    	
    	if( null == container ){
    	    return;    
    	}
    	
    	ide = container.getIDE();
    	
    	//Sanity check
    	if( null == ide ){
            return;
        }
    	
    	ide.getDocumentContainerManager().newContainer(modsub);
    	ide.setVisible(true);
    }
    
    /**
     * 
     * @param document
     * @param filePath
     */
    private void saveDocument( Document document, File file ){
    	
		try{
			XMLOutputter outp = new XMLOutputter();
			outp.setFormat( org.jdom.output.Format.getPrettyFormat() );

			FileOutputStream fileStream = new FileOutputStream( file.getAbsolutePath() );

			outp.output( document, fileStream );
		}
		catch ( FileNotFoundException e ) {
			System.out.println( "No file" );
		}
		catch ( IOException e ) {
			;
		}
	}
    
    
    
    
    
    
    
    /*=========================================================================
	/* MouseListenere
	/*=======================================================================*/
	public void mouseClicked(MouseEvent e){
		
		//table clicked?
		if( e.getSource() instanceof JTable ){
			
			//table double clicked?
		    if( 2 == e.getClickCount() ){
		    	int row;
		    	String filePath;
		    	
		    	//Get row and file path
		    	row = tablePane.rowAtPoint( e.getPoint() );
		    	filePath = tablePane.getFilePathAtRow( row );
		    	
		    	//Open file in SOC
		    	if( null != filePath && 0 != filePath.length()){
		    		container.insertResource(new File(filePath));
		    	}
		    }
		}
	}
    public void mouseEntered(MouseEvent e){};
    public void mouseExited(MouseEvent e){};
    public void mousePressed(MouseEvent e){};
    public void mouseReleased(MouseEvent e){};
}

