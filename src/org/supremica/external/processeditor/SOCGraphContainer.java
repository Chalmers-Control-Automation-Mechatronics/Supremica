package org.supremica.external.processeditor;

import java.awt.image.*;
import javax.imageio.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.*;

import org.supremica.external.processeditor.processgraph.*;
import org.supremica.external.processeditor.processgraph.opcell.*;
import org.supremica.external.processeditor.processgraph.resrccell.*;
import org.supremica.external.processeditor.xml.ConvertAutomatas;
import org.supremica.external.processeditor.xml.Loader;
import org.supremica.external.processeditor.xml.XlsConverter;
import org.supremica.external.processeditor.xml.XmlConverter;
import org.supremica.external.processeditor.xml.SDPConverter;

import org.supremica.external.processeditor.xgraph.Graph;
import org.supremica.external.processeditor.xgraph.GraphCell;
import org.supremica.external.processeditor.xgraph.Selection;

import org.supremica.external.processeditor.processgraph.opcell.OperationCell;
import org.supremica.external.processeditor.processgraph.resrccell.ResourceCell;
import org.supremica.external.processeditor.tools.db.DBInterface;

import org.supremica.manufacturingTables.xsd.processeditor.ObjectFactory;
import org.supremica.manufacturingTables.xsd.processeditor.ROP;
import org.supremica.manufacturingTables.xsd.processeditor.RelationType;

import org.supremica.external.processeditor.xml.Converter;
//import org.supremica.external.relationExtraction.Algorithms.*;
import org.supremica.external.relationExtraction.Gui.*;

import org.supremica.external.processeditor.xgraph.*;

import org.supremica.external.processeditor.tools.db.DBInterface;

/**
 * The most central GUI class of the <code>org.soc</code> package. 
 * <p>
 * The class receive all user action events via the menu items.
 * Since <code>SOCGraphContainer</code> extends the 
 * <code>JDesktop</code> class,
 * it manages all the internal frames within the SOC an therefore carries out
 * the user instruction to the internal frames and their underlaying graphs.
 */
public class SOCGraphContainer
							extends 
								JDesktopPane 
									implements 
										SOCGraphFrameListener,
										SOCToolBarListener {
    private PageFormat mPageFormat = null;   
    
    private int numOfNewSheetToDay = 0;    

    public SOCToolBar toolbar = new SOCToolBar();
    public SOCMenuBar menubar = new SOCMenuBar(this);
    
    private SOCDOPtoEFAFrame dopToEfaFrame = null; 
    
    private Object memory;
    private Object numOfCopies = 0;

    private File cDir = null;
    private SOCFileFilter xmlFilter = new SOCFileFilter(".xml");
    private SOCFileFilter wmodFilter = new SOCFileFilter(".wmod");
    private SOCFileFilter xlsFilter = new SOCFileFilter(".xls");
    private SOCFileFilter bmpFilter = new SOCFileFilter(".bmp");
    private SOCFileFilter jpgFilter = new SOCFileFilter(".jpg");
    
    static public final int COMPLETE = 1;
    static public final int BASIC = 2;
    static public final int SIMPLIFIED = 3;
    static public final int ERWD = 0;
    static public final int SDP_STATION = 1;
    static public final int SDP_DATABASE = 2;    

    private ObjectFactory objectFactory = new ObjectFactory();

    /**
    * Creates a new instance of the class.
    * <p>
    * Add all the menu items and mangage the user action events.
    */
    public SOCGraphContainer() {	
    	super();
    	setBackground(Color.gray);
    	toolbar.addSOCToolBarListener(this);
    }   
    /**
     * Adds a new worksheet to the graph container pane.
     * <p>
     * Makes sure that new internal frame gets a unique title.
     * Update the the Windows menubar.
     *
     * @param newFrame the internal frame to be added
     *
     * @return the component argument
     */
    public Component add(SOCGraphFrame newFrame) {
	//DEBUG
	//System.out.println("SOCGraphContainer.add()");
	//END DEUBG		
	if(newFrame.getTitle().equals("Sheet")) {
	    newFrame.setTitle("Sheet "+(++numOfNewSheetToDay));
	}else {
	    setUniqueFrameTitle(newFrame);
	}
	super.add(newFrame);	
	newFrame.addInternalFrameListener(menubar);

	newFrame.addSOCGraphFrameListener(this);		       
	newFrame.setSelected(true);	
	
	menubar.addFrame(newFrame.getTitle());
	menubar.setState("Multi Mode", true);

	newFrame.setMaximum(true);

	return newFrame;	
    }
    /**
     * Sets the title of the frame unique.
     *
     * @param frame the frame to give unique title
     */        
    private void setUniqueFrameTitle(JInternalFrame frame) {
	String title = frame.getTitle();
	int numOfEquals = 0;
	while(!isUniqueFrameTitle(frame.getTitle())) {
	    numOfEquals++;
	    frame.setTitle(title+" ("+numOfEquals+")");
	}
    }
    /**
     * Check whether <code>title</code> is unique or exists as a title
     * in the existing frames.
     *
     * @param title the title to check
     * @return returns <code>true</code> if <code>title</code> exist as a title
     * in existing frames, otherwise <code>false</code>
     */
    private boolean isUniqueFrameTitle(String title) {
	JInternalFrame[] frames = getAllFrames();
	for(int i = 0; i < frames.length; i++) {
	    if(title.equals(frames[i].getTitle())) {
		return false;
	    }
	}
	return true;
    }
    /**
     * Rebuilds the graphs for all worksheets.
     * <p>
     * Compare to <code>org.copgraph.NestedCell</code> method 
     * <code>rebuild</code>
     */
    public void rebuildAll() {
	JInternalFrame[] frames = getAllFrames();
	for(int i = 0; i < frames.length; i++) {
	    if(frames[i] instanceof SOCGraphFrame) {
		for(int j = 0; j < ((SOCGraphFrame)frames[i]).getGraph().
			cells.length; j++) {
		    if(((SOCGraphFrame)frames[i]).getGraph().cells[i] 
		       instanceof NestedCell) {
			((SOCGraphFrame)frames[i]).getGraph().
			    removeSelection();
			((NestedCell)((SOCGraphFrame)frames[i]).getGraph().
			 cells[i]).rebuild();
		    }
		}
	    }
	}
    }   
    /**
     * Returns the program MenuBar.
     *
     * @return the MenuBar
     */
    public JMenuBar getMenuBar() {	
	return menubar;
    }        
    /**
     * Returns the program ToolBar. 
     * 
     * @return the ToolBar
     */
    public JToolBar getToolBar() {
	return toolbar;
    } 
    /**
     * Organize the internal windows in a cascade.         
     */
    public void cascade() {
	JInternalFrame[] frames = getAllFrames();
	int stepX = 20; int stepY = 29;
	int tmpLocX = stepX*(frames.length-1); 
	int tmpLocY = stepY*(frames.length-1);		
	for(int i = 0; i < frames.length; i++) {
	    ((SOCGraphFrame)frames[i]).setMaximum(false);
	    frames[i].moveToBack();
	    frames[i].setBounds(new Rectangle(tmpLocX, tmpLocY,
					      getBounds().width,
					      getBounds().height));
	    tmpLocX -= stepX;
	    tmpLocY -= stepY;	  
	}
	((SOCGraphFrame)frames[0]).setSelected(true);
    }  
    /**
     * Sets the multi mode on or off.
     * <p>
     * If the graphical representation of the relations, i.e. the light blue
     * rectangles enclosing one or more operation and/or other relation 
     * components, is unwanted they can be removed by turning 
     * the mulit mode off.
     *
     * @param set true if multi mode should be on, false otherwise
     */    
    public void setMultiModeView(boolean set) {
	//DEBUG
	//System.out.println("SOCGraphContainer.setMultiModeView()");	
	//END DEBUG
	getSelectedFrame().setMultiModeView(set);
    }
    /**
     * Updates the menubar and toobar by setting concerned items of 
     * this graph container enable or not.
     * <p>
     * Is invoked each time the selection is changed. 
     *
     * @param s the <code>Selection</code> object
     */
    public void selectionChanged(Selection s) {
	//DEBUG
	//System.out.println("SOCGraphContainer.selectionChanged()");
	//END DEBUG
	if(getSelectedFrame() != null) {
	    getSelectedFrame().getGraph().repaint();
	}
	menubar.selectionChanged();
    }  
    /**
     * Adds a new worksheet.
     * <p>
     * Adds a new sheet, i.e. new interal frame, to the container.
     */
    public void newSheet() {
	  SOCGraphFrame newFrame = new SOCGraphFrame("Sheet");
	  add(newFrame);
    }
    /**
     * Enables the open file dialog. 
     * <p>
     * Restricts enable file format possible 
     * to open to .xml files.      
     */
    public void open() {
	JFileChooser fc;
	if(cDir != null) {
	    fc = new JFileChooser(cDir);
	}else {
	    fc = new JFileChooser();
	}
	fc.addChoosableFileFilter(xmlFilter);
	fc.setFileFilter(xmlFilter);
	fc.setAcceptAllFileFilterUsed(false);
	int result = fc.showOpenDialog(this);       
	if(result == JFileChooser.APPROVE_OPTION) {
	    cDir = fc.getSelectedFile().getParentFile();
	    Loader loader = new Loader();	   
	    Object newObject = loader.open(fc.getSelectedFile());
	    SOCGraphFrame newFrame = new SOCGraphFrame(fc.getSelectedFile().
						       getName(), 
						       newObject, 
						       fc.getSelectedFile()); 
	    add(newFrame);
	    newFrame.setMaximum(true);
	    
	}else if(result == JFileChooser.CANCEL_OPTION) {	    
	}else if(result == JFileChooser.ERROR_OPTION) {}	    	
    }
    
    public void insertResource(Object o, File file) {
    	String name = "New ROP";
    	
    	if(file == null){
    		if(o instanceof ROP){
    			name = ((ROP)o).getMachine();
    		}
    	}else{
    		name = file.getName();
    	}
    	
    	SOCGraphFrame newFrame = new SOCGraphFrame(name, o, file);
    	add(newFrame);
    	newFrame.setMaximum(true);
    }
    
    /**
     * Closes the active worksheet.
     */
    public void close() {
	try {
	    getSelectedFrame().setClosed(true);
	}catch(Exception ex) {
	    System.out.println("ERROR! in SOCGraphContainer."+
			       "closeSelectedFrame()");
	}
    }
    /**
     * Enables the open file dialog. 
     * <p>
     * Restricts enable file format possible 
     * to import to .xls files.
     *
     * @param type type of file to import     
     */
    public void importFromFile(int type) {
	JFileChooser fc;
	if(cDir != null) {
	    fc = new JFileChooser(cDir);
	}else {
	    fc = new JFileChooser();
	}
	fc.addChoosableFileFilter(xlsFilter);
	fc.setFileFilter(xlsFilter);
	fc.setAcceptAllFileFilterUsed(false);
	int result = fc.showOpenDialog(this);
	if(result == JFileChooser.APPROVE_OPTION) {
	    System.out.println(fc.getSelectedFile().getAbsolutePath());
	    cDir = fc.getSelectedFile().getParentFile();
	    XlsConverter xlsConverter = new XlsConverter();
	    SDPConverter sdpConverter = new SDPConverter();
	    if(type == ERWD) {
		SOCGraphFrame newFrame = 
		    new SOCGraphFrame(fc.getSelectedFile().getName(), 
				      xlsConverter.open(fc.getSelectedFile(), 
							new XmlConverter()));
		add(newFrame);
		newFrame.setMaximum(true);
	    }else if(type == SDP_STATION) {			    
		sdpConverter.create(fc.getSelectedFile(), new XmlConverter());
	    }else if(type == SDP_DATABASE) {
		
	    }	    
	}else if(result == JFileChooser.CANCEL_OPTION) {
	}else if(result == JFileChooser.ERROR_OPTION) {}	
    }
    /**
     * Performs custom save to the selected resource.
     * <p>
     * The method exclude all unwanted xml features from the saved file.      
     */
    protected void saveWithoutSubelements() {
	//DEBUG
	System.out.println("SOCGraphContainer.saveWithoutSubelements()");
	//END DEBUG	
	ResourceCell resrcCell = getSelectedResourceCell();       	
	if(resrcCell != null && resrcCell.getROP() != null) {	  	   
	    Object objectToSave = Converter.clone(resrcCell.getFunction());
	    JCheckBox[] typesToSave = menubar.typesToSave;
	    for(int i = 0; i < typesToSave.length; i++) {
		if(typesToSave[i] != null) {
		    if(typesToSave[i].getName().equals("Comment")) {
			if(!typesToSave[i].isSelected()) {
			    Converter.removeComment(objectToSave);
			}			    
		    }else if(typesToSave[i].
			     getName().equals("Algebraic")) {
			if(!typesToSave[i].isSelected()) {
			    Converter.removeAlgebraic(objectToSave);
			}else {
			    if(!typesToSave[i+1].isSelected()) {
				Converter.
				    removeAlgebraicUnextended(objectToSave);
			    }
			}
		    }else if(typesToSave[i].
			     getName().equals("Precondition")) {
			if(!typesToSave[i].isSelected()) {
			    Converter.removePrecondition(objectToSave);
			}
		    }else if(typesToSave[i].
			     getName().equals("Properties")) {
			if(!typesToSave[i].isSelected()) {
			    Converter.removeProperties(objectToSave);
			}else {
			    if(!typesToSave[i+1].isSelected()) {
				Converter.removeAttribute(objectToSave);
			    }else {
				boolean rmUpperIndicator = !typesToSave[i+2].
				    isSelected();
				boolean rmLowerIndicator = !typesToSave[i+3].
				    isSelected();
				boolean rmInvisible = !typesToSave[i+4].
				    isSelected();
				if(rmUpperIndicator ||
				   rmLowerIndicator ||
				   rmInvisible) {
				    Converter.removeAttributeCharacteristics(objectToSave, rmUpperIndicator, rmLowerIndicator, rmInvisible);
				}
			    }
			    if(!typesToSave[i+5].isSelected()) {
				Converter.removeActivityUnextended(objectToSave);
			    }
			}
		    }		
		}		     
	    }
	    File file = saveObjectAs(objectToSave);
	    if(file != null) {
		resrcCell.setFile(file);
	    }
	}	
    }
    /**
     * Saves selected resource. 
     * <p>
     * If selected resource already has been saved once,
     * any changes made to the resource will be saved to URL.
     * If not the <code>saveAs</code> method will be invoked instead.
     */
    public void save() {
	ResourceCell resrcCell = getSelectedResourceCell();
	if(resrcCell != null && resrcCell.getROP() != null) {
	    if(resrcCell.getFile() != null) {
		Loader loader = new Loader();			       
		loader.save(resrcCell.getFunction(), resrcCell.getFile()); 
	    }else {
		saveAs();
	    }
	}
    }	
    /**
     * Saves selected resource to the URL chosen by the user.
     */
    public void saveAs() {
	//DEBUG
	//System.out.println("SOCGraphContainer.save()");
	//END DEBUG
	ResourceCell resrcCell = getSelectedResourceCell();
	if(resrcCell != null && resrcCell.getROP() != null) {
	    File file = saveObjectAs(resrcCell.getFunction());
	    if(file != null) {
		resrcCell.setFile(file);
	    }	    
	}
    }	
    /**
     * Show the save dialog and saves the object to the chosen URL.
     *
     * @param o object to save
     *
     * @return the URL where the object was saved. <code>Null</code> if the 
     * assignment failed.
     */
    protected File saveObjectAs(Object o) {
	if(o instanceof ROP) {	    
	    JFileChooser fc;
	    if(cDir != null) {
		fc = new JFileChooser(cDir);
	    }else {
		fc = new JFileChooser();
	    }
	    fc.addChoosableFileFilter(xmlFilter);			    
	    fc.setAcceptAllFileFilterUsed(true);
	    fc.setFileFilter(xmlFilter);
	    int result = fc.showSaveDialog(this);
	    if(result == JFileChooser.APPROVE_OPTION) {			
		cDir = fc.getSelectedFile().getParentFile();
		File file = fc.getSelectedFile();		
		if(!fc.getFileFilter().accept(file)) {		
		    file = new File(file.getAbsolutePath()+
				    fc.getFileFilter().getDescription()); 
		}
		if(xmlFilter.accept(file)) {
		    Loader loader = new Loader();			   
		    Converter.printROP(o);
		    loader.save(o, file);    	
		    return file;
		}
	    }else if(result == JFileChooser.CANCEL_OPTION) {	   
	    }else if(result == JFileChooser.ERROR_OPTION) {		    
	    }		
	}
	return null;	
    }
    /**
     * Exports the active worksheet to .bmp image file.
     */
    public void exportAsImage() {
	//DEBUG
	//System.out.println("SOCGraphContainer.exportAsImage()");
	//END DEBUG
	Component backgroundComp = null;		
	if(getSelectedFrame() != null) {
	    backgroundComp = getSelectedFrame().getGraph();	    
	}
    
	if(backgroundComp instanceof Component) {
	    JFileChooser fc;
	    if(cDir != null) {
		fc = new JFileChooser(cDir);
	    }else {
		fc = new JFileChooser();
	    }
	    fc.addChoosableFileFilter(bmpFilter);
	    fc.addChoosableFileFilter(jpgFilter);
	    fc.setAcceptAllFileFilterUsed(true);
	    fc.setFileFilter(bmpFilter);
	    int result = fc.showSaveDialog(this);
	    if(result == JFileChooser.APPROVE_OPTION) {			     
		File file = fc.getSelectedFile();		
		if(!fc.getFileFilter().accept(file)) {		
		    file = new File(file.getAbsolutePath()+
				    fc.getFileFilter().getDescription());    
		    BufferedImage image = 
			new BufferedImage(((Component)backgroundComp).
					  getWidth(),
					  ((Component)backgroundComp).
					  getHeight(), 
					  BufferedImage.TYPE_INT_RGB);
		    Graphics2D g = image.createGraphics();		      
		    ((Component)backgroundComp).paint(g);		    
		    try {		
			if(bmpFilter.accept(file)) {		    
			    ImageIO.write((RenderedImage)image, "bmp", file);
			}else if(jpgFilter.accept(file)) {
			    ImageIO.write((RenderedImage)image, "jpg", file);
			}
		    }catch(Exception ex) {
			//DEBUG
			System.out.println("ERROR SOCGraphContainer."+
					   "exportAsImage()");
			//END DEBUG
		    }
		}else if(result == JFileChooser.CANCEL_OPTION) {	   
		}else if(result == JFileChooser.ERROR_OPTION) {		    
		}
	    }
	}
    }
    /**
     * Exports the selected resource to Excel spreadsheet, i.e. .xls file
     */
    public void export() {	
	//DEBUG
	//System.out.println("SOCGraphContainer.export()");
	//END DEBUG	
	ResourceCell resrcCell = getSelectedResourceCell();	       
	if(resrcCell != null && resrcCell.getROP() != null) {	    
	    JFileChooser fc;
	    if(cDir != null) {
		fc = new JFileChooser(cDir);
	    }else {
		fc = new JFileChooser();
	    }
	    fc.addChoosableFileFilter(xlsFilter);
	    fc.setFileFilter(xlsFilter);
	    fc.setAcceptAllFileFilterUsed(true);	    
	    int result = fc.showSaveDialog(this);
	    if(result == JFileChooser.APPROVE_OPTION) {			       
		File file = fc.getSelectedFile();
		if((!file.getName().endsWith(".xls"))&&
		   fc.isFileSelectionEnabled()) {		       
		    file = new File(file.getAbsolutePath()+".xls");	  
		}	    	
		XmlConverter converter = new XmlConverter();
		converter.save(file, 
			       resrcCell.getFunction(), 
			       new XlsConverter());
	    }else if(result == JFileChooser.CANCEL_OPTION) {	   
	    }else if(result == JFileChooser.ERROR_OPTION) {		    
	    }		
	}

    }
    
    public void openDBConnection(){
    	DBInterface dbInterface = new DBInterface();
    	dbInterface.setGraphContainer(this);
    }
    
    /**
     * Creates a automata model and generates a supervisor to that model.
     * <p>
     * Creates a autoamta model of the selected resoruce files according to 
     * <code>Supremica</code> file standard.
     */
    public void synthesis() {
	JFileChooser fc;
	if(cDir != null) {
	    fc = new JFileChooser(cDir);
	}else {
	    fc = new JFileChooser();
	}
	fc.addChoosableFileFilter(xmlFilter);			    
	fc.setAcceptAllFileFilterUsed(true);
	fc.setFileFilter(xmlFilter);
	fc.setMultiSelectionEnabled(true);
	int result = fc.showDialog(this, "Create");
	if(result == JFileChooser.APPROVE_OPTION) {	     	     
	    cDir = fc.getSelectedFiles()[0].getParentFile();
	    ConvertAutomatas converter = new ConvertAutomatas();	      
	    converter.basic(fc.getSelectedFiles());	    
	    File modelFile = new File(cDir.getAbsolutePath()+
				      "//automatasBasicModel.xml");	   
	    if(modelFile.exists()) {		
		converter.supervisor(modelFile, SIMPLIFIED);
	    }
	    File supervisorFile = new File(cDir.getAbsolutePath()+
					   "//supervisor.xml");
	    converter.viewAutomata(supervisorFile);
	}else if(result == JFileChooser.CANCEL_OPTION) {	   
	}else if(result == JFileChooser.ERROR_OPTION) {		    
	}			
    }
    /**
     * Creates a automata model.
     * <p>
     * Creates a automata model of the selected resource files according to 
     * <code>Supremica</code> file standard. Generates a supervisor based on 
     * the automata model.
     *         
     * @param type decide which type of model used
     */    
    public void createAutomatas(int type) {
	//DEBUG
	//System.out.println("SOCGraphContainter.createAutomatas()");
	//END DEBUG
	 JFileChooser fc;
	 if(cDir != null) {
	     fc = new JFileChooser(cDir);
	 }else {
	     fc = new JFileChooser();
	 }
	 fc.addChoosableFileFilter(xmlFilter);			    
	 fc.setAcceptAllFileFilterUsed(true);
	 fc.setFileFilter(xmlFilter);
	 fc.setMultiSelectionEnabled(true);
	 int result = fc.showDialog(this, "Create");
	 if(result == JFileChooser.APPROVE_OPTION) {	     	     
	     cDir = fc.getSelectedFiles()[0].getParentFile();
	     ConvertAutomatas converter = new ConvertAutomatas();
	     if(type == COMPLETE) {
		 converter.complete(fc.getSelectedFiles());	    	     
	     }else if(type == BASIC) {
		 converter.basic(fc.getSelectedFiles());
	     }else if(type == SIMPLIFIED) {
		 converter.simplified(fc.getSelectedFiles());
	     }
	 }else if(result == JFileChooser.CANCEL_OPTION) {	   
	 }else if(result == JFileChooser.ERROR_OPTION) {		    
	 }			
    }
    /**
     * Generates a supervior.
     * <p>
     * Generates a supervisor based on the selected automata model.
     *
     * @param type not in use
     */
    public void generateSupervisor(int type) {
	//DEBUG
	//System.out.println("SOCGraphContainer.generateSupervisor()");
	//END DEBUG
	 JFileChooser fc;
	 if(cDir != null) {
	     fc = new JFileChooser(cDir);
	 }else {
	     fc = new JFileChooser();
	 }
	 fc.addChoosableFileFilter(xmlFilter);			    
	 fc.setAcceptAllFileFilterUsed(true);
	 fc.setFileFilter(xmlFilter);	 
	 int result = fc.showDialog(this, "Create");
	 if(result == JFileChooser.APPROVE_OPTION) {	     	     
	     cDir = fc.getSelectedFile().getParentFile();
	     ConvertAutomatas converter = new ConvertAutomatas();	     
	     converter.supervisor(fc.getSelectedFile(), type);	    	       
	 }else if(result == JFileChooser.CANCEL_OPTION) {	   
	 }else if(result == JFileChooser.ERROR_OPTION) {		    
	 }			
    }    
     /**
     * Perform solution extraction.
     * <p>
     * Perform solution extraction to the selected supervisor.
     *
     * @param type not in use
     */  
    /*
    public void solutionExtraction(int type) {
	JFileChooser fc;
	if(cDir != null) {
	    fc = new JFileChooser(cDir);
	}else {
	    fc = new JFileChooser();
	}
	fc.addChoosableFileFilter(xmlFilter);			    
	fc.setAcceptAllFileFilterUsed(true);
	fc.setFileFilter(xmlFilter);	
	int result = fc.showDialog(this, "Create");
	if(result == JFileChooser.APPROVE_OPTION) {	     	     
	    cDir = fc.getSelectedFile().getParentFile();
	    ConvertAutomatas converter = new ConvertAutomatas();	     
	    converter.solutionExtraction(fc.getSelectedFile(), type);          
	}else if(result == JFileChooser.CANCEL_OPTION) {	   
	}else if(result == JFileChooser.ERROR_OPTION) {		    
	}		
    }
    **/
    /**
     * Views automata.
     * <p>
     * Views the automata of the selected model or supervisor.
     */
    public void viewAutomata() {
	JFileChooser fc;
	if(cDir != null) {
	    fc = new JFileChooser(cDir);
	}else {
	    fc = new JFileChooser();
	}
	fc.addChoosableFileFilter(xmlFilter);			    
	fc.setAcceptAllFileFilterUsed(true);
	fc.setFileFilter(xmlFilter);	 
	int result = fc.showDialog(this, "Create");
	if(result == JFileChooser.APPROVE_OPTION) {	     	     
	    cDir = fc.getSelectedFile().getParentFile();
	    ConvertAutomatas converter = new ConvertAutomatas();	     
	    converter.viewAutomata(fc.getSelectedFile());	    	       
	}else if(result == JFileChooser.CANCEL_OPTION) {	   
	}else if(result == JFileChooser.ERROR_OPTION) {		    
	}		
    }
    
    public void viewDOPtoEFAFrame() {
    
    	/*
    	 * Create frame
    	 */
    	if(dopToEfaFrame == null){
    		dopToEfaFrame = new SOCDOPtoEFAFrame();
    	}
    	
    	//show frame
    	dopToEfaFrame.setVisible(true);
    	
    	/*
    	 * Set input file chooser
    	 */
    	JFileChooser fcInput;
    	
    	if(cDir != null) {
    	    fcInput = new JFileChooser(cDir);
    	}else {
    	    fcInput = new JFileChooser();
    	}
    	
    	fcInput.addChoosableFileFilter(xmlFilter);
    	fcInput.setFileFilter(xmlFilter);
    	fcInput.setAcceptAllFileFilterUsed(false);
    	
    	dopToEfaFrame.setInputFileChooser(fcInput);
    	
    	/*
    	 * Set output file chooser
    	 */
    	JFileChooser fcOutput;
    	
    	if(cDir != null) {
    	    fcOutput = new JFileChooser(cDir);
    	}else {
    	    fcOutput = new JFileChooser();
    	}
    	
    	fcOutput.setAcceptAllFileFilterUsed(true);
    	fcOutput.addChoosableFileFilter(wmodFilter);
    	fcOutput.setFileFilter(wmodFilter);
    	
    	dopToEfaFrame.setOutputFileChooser(fcOutput);
    }
    
    /**
     * Page and print setup
     */
    protected void pageSetup() {
	PrinterJob pj = PrinterJob.getPrinterJob();
	if(mPageFormat == null) {
	    mPageFormat = pj.defaultPage();
	}
	mPageFormat = pj.pageDialog(mPageFormat);
    }
    /**
     * Prints function.
     * <p>
     * Prints the active worksheet or its selected operation/relation.
     */
    public void print() {
	if(getSelectedFrame() != null) {
	    if(getSelectedFrame().getGraph().getSelection().hasSelected()) {
		SOCGraphPrinter p = new SOCGraphPrinter(getSelectedFrame().
							getGraph().
							getSelection().
							getSelectedAt(0),
							mPageFormat);	     
	    }else {
		SOCGraphPrinter p = new SOCGraphPrinter(getSelectedFrame().
							getGraph(),
							mPageFormat);	  
	    }
	}
    }
    /**
     * Adds a new resource.
     * <p>
     * Adds a new resource to the active worksheet by
     * calling the method <code>newResource</code> at 
     * <code>org.soc.SOCGraphFrame</code>.
     * 
     */
    public void newResource() {
	//DEBUG
	//System.out.println("newResource");
	//END DEBUG
	if(getSelectedFrame() != null) {
	    getSelectedFrame().newResource();
	}	
    }
    /**
     * Adds a new operation.
     * <p>
     * Adds a new operation to the active worksheet by 
     * calling the method <code>newOperation</code> at 
     * <code>org.soc.SOCGraphFrame</code>.
     */
    public void newOperation() {
	if(getSelectedFrame() != null) {	    
	    getSelectedFrame().newOperation();
	}    
    }
    /**
     * Adds a new relation.
     * <p>
     * Adds a new relation to the active worksheet by 
     * calling the method <code>newRelation</code> at
     * <code>org.soc.SOCGraphFrame</code>.
     *
     * @param relationType can be either be "Sequence", "Alternative",
     * "Parallel" or "Arbitrary".
     */
    public void newRelation(RelationType relationType) {
    	if(getSelectedFrame() != null) {
    		getSelectedFrame().newRelation(relationType);
    	}
    }
    /**
     * Adds a new relation.
     * <p>
     * Adds a new relation to the active worksheet.
     * The relation will be defined by the user input to 
     * the dialog window shown.
     */
    public void newAlgebraic() {
	if(getSelectedFrame() != null) {
	    getSelectedFrame().
		newAlgebraic(JOptionPane.showInputDialog("New Algebraic")); 
	}
    }
    /**
     * Inserts a new resource to the active worksheet.
     * <p>
     * Same as the <code>open</code> method but instead of putting the
     * resource in a new worksheet the resource is put in the active one.
     */
    public void insertResource() {		
	JFileChooser fc;
	if(cDir != null) {
	    fc = new JFileChooser(cDir);
	}else {
	    fc = new JFileChooser();
	}
	fc.addChoosableFileFilter(xmlFilter);
	fc.setFileFilter(xmlFilter);
	fc.setAcceptAllFileFilterUsed(false);
	int result = fc.showOpenDialog(this);
	if(result == JFileChooser.APPROVE_OPTION) {
	    cDir = fc.getSelectedFile().getParentFile();
	    Loader loader = new Loader();	   
	    Object newObject = loader.open(fc.getSelectedFile());
	    Converter.printROP(newObject);
	    if(getSelectedFrame() != null) {
		getSelectedFrame().insertResource(newObject, 
						  fc.getSelectedFile()); 
	    }else {
		add(new SOCGraphFrame(fc.getSelectedFile().getName(), 
				      newObject, 
				      fc.getSelectedFile())); 	    
	    }	    
	}else if(result == JFileChooser.CANCEL_OPTION) {
	    
	}else if(result == JFileChooser.ERROR_OPTION) {
	    
	}   	
    }
    /**
     * Creates a outer relation.
     * <p>
     * Creates a outer relation for the selected operation/relation.     
     */
    public void createOuterRelation() {
	//DEBUG
	//System.out.println("SOCGraphContainer.createOuterRelation()");
	//END DEBUG
	NestedCell nestedCell = getSelectedNestedCell();
	if(nestedCell != null) {
	    nestedCell.createOuterRelation();
	    removeAllSelected();
	}
	OperationCell opCell = getSelectedOperationCell();
	if(opCell != null) {
	    opCell.createOuterRelation();
	    removeAllSelected();
	}
    }
    /**
     * Removes outer relation.
     * <p>
     * Removes outer relation for the selected operation/relation.
     */
    public void removeOuterRelation() {
	//DEBUG
	//System.out.println("SOCGraphContainer.removeOuterRelation()");
	//END DEBUG
	NestedCell nestedCell = getSelectedNestedCell();
	if(nestedCell != null) {
	    nestedCell.removeOuterRelation();
	    removeAllSelected();
	}
	OperationCell opCell = getSelectedOperationCell();
	if(opCell != null) {
	    opCell.removeOuterRelation();
	    removeAllSelected();
	}
    }
    /**
     * Cuts the selection.
     * <p>
     * Cuts the selection, i.e. copy and delete the selection.
     */
    public void cut() {
	copy();
	delete();
    }
    /**
     * Copies the selection.         
     */
    public void copy() {
	NestedCellListener nestedCellList = getSelectedNestedCellListener();
	if(nestedCellList != null) {
	    memory = nestedCellList;
	    //jmiPaste.setEnabled(true);
	    menubar.setEnabled("Paste",true);
	    toolbar.setPasteEnabled(true);
	}
    }
    /**
     * Pastes the copied object into the selected object. 
     */
    public void paste() {
	//DEBUG
	//System.out.println("SOCGraphContainer.paste()");
	//END DEBUG
	NestedCellListener nestedCellList = getSelectedNestedCellListener();
	if(nestedCellList != null) {
	    nestedCellList.paste(((NestedCellListener)memory).copy());	      
	    removeAllSelected();		
	}else {	       
	    getSelectedFrame().paste(((NestedCellListener)memory).copy()); 
	}	
    }
    /**
     * Deletes the selection.
     */
    public void delete() {	
	NestedCellListener nestedCellList = getSelectedNestedCellListener();
	if(nestedCellList != null) {
	    nestedCellList.delete();
	    removeAllSelected();	    
	}	
    }
    /**
     * Changes relation type of the selected relation.
     *
     * @param type changes to "Sequence", "Alternative", 
     * "Parallel" or "Arbitrary"
     */
    public void changeRelationType(RelationType type) {       
    	NestedCell nestedCell = getSelectedNestedCell();
    	if(nestedCell != null && nestedCell.getRelation() != null) {	    
    		nestedCell.getRelation().setType(type);
    		nestedCell.rebuild();
    		removeAllSelected();	       	   
    	}	
    }   
    /**
     * Sets the algebraic expression mode on or off.
     * <p>
     * Sets the algebraic expression mode on or off for the selected relation.
     *
     * @param set true if algebraic mode should be on, false otherwise.
     */
    public void setAlgebraic(boolean set) {
	NestedCell nestedCell = getSelectedNestedCell();
	if(nestedCell != null && nestedCell.getRelation() != null) {
	    try {
		nestedCell.getRelation().getAlgebraic().setCompressed(set);
	    }catch(Exception ex) {
		try {
		    nestedCell.getRelation().setAlgebraic(objectFactory.
							  createAlgebraic());
		    nestedCell.getRelation().getAlgebraic().setCompressed(set);
		  }catch(Exception ex2) {}
	    }       	
	    nestedCell.rebuild();
	    removeAllSelected();
	}
    }
    /**
     * Adds all the attributes of the specified attribute type.
     * <p>
     * Adds all the attributes of the specified attribute type if a resource
     * is selected. If the attribute value is not a number the attribute value
     * will be treated as zero.
     *
     * @param type define the attribute type to sum up.
     */
    public void sumAttribute(String type) {
	ResourceCell resrcCell = getSelectedResourceCell();
	if(resrcCell != null) {
	    JOptionPane.showMessageDialog(this, 
					  type+": "+
					  Float.toString(Converter.
							 sumAttribute(resrcCell.getFunction(), 
type)));	
	}
    }
    /** 
     * Shows the resource info window.
     * <p>
     * Shows the resource info window for the selected resource.
     */
    public void resourceInfo() {
	ResourceCell resrcCell = getSelectedResourceCell();
	if(resrcCell != null) {
	    resrcCell.resourceInfo();
	}
    }
    /**
     * Shows the operation info window.
     * <p>
     * Shows the operation info window for the selected opertion.
     */
    public void operationInfo() {
	OperationCell opCell = getSelectedOperationCell();
	if(opCell != null) {
	    opCell.operationInfo();
	}
    }    
    /**
     * Returns the active internal frame.
     * 
     * @return the active internal frame.
     */
    public SOCGraphFrame getSelectedFrame() {	
	    return (SOCGraphFrame)super.getSelectedFrame();	
    }
    /**
     * Counts the number of internal frames (or worksheets).
     *
     * @return the number of internal frames.
     */
    public int getFrameCount() {
	return getAllFrames().length;
    }
    /**
     * Returns all the graphs.
     * <p>
     * Returns all the graphs from all the worksheets.
     *
     * @return an array with all the graphs.
     */
    public Graph[] getAllGraphs() {
	JInternalFrame[] frames = getAllFrames();
	Graph[] graphs = new Graph[frames.length];
	for(int i = 0; i < frames.length; i++) {
	    graphs[i] = ((SOCGraphFrame)frames[i]).getGraph();
	}
	return graphs;
    } 
    /**
     * Returns the selected cell specified by the selection index.
     * <p>
     * Returns the selected cell specified by the selection index from this 
     * active graph frame.
     * This method will return <code>null</code> if there is no active frame 
     * or if no cells are selected. 
     *
     * @param index the selection index
     * @return the cell that corresponds to the selection index
     */
    public GraphCell getSelectedAt(int index) {
	if(getSelectedFrame() != null) {
	    return getSelectedFrame().getGraph().getSelection().getSelectedAt(index);
	}else {
	    return null;
	}
    }
    /**
     * Returns the number of selected cells.
     * <p>
     * Returns the number of selected cells in this active graph frame.
     * This method will return <code>0</code> if there is no active frame or 
     * if no cells are selected.
     *
     * @return number of selected cells
     */
    public int getSelectedCount() {
	if(getSelectedFrame() != null) {
	    return getSelectedFrame().getGraph().getSelection().
		getSelectedCount();
	}else {
	    return 0;
	}
    }
    /**
     * Removes all selected cells from this active graph frame.
     */
    public void removeAllSelected() {
	if(getSelectedFrame() != null) {
	    getSelectedFrame().getGraph().getSelection().removeAll();
	}
    }
    /**
     * Returns the first selected <code>GraphCell</code>.
     * <p>
     * Returns the first selected <code>GraphCell</code> from this active 
     * graph frame. 
     * If the first selected 
     * cell is not an instance of the <code>GraphCell</code> class this 
     * method will return
     * <code>null</code>
     * 
     * @return the selected graph cell
     */
    public GraphCell getSelectedGraphCell() {	
	if(getSelectedAt(0) instanceof GraphCell) {
	    return (GraphCell)getSelectedAt(0);
	}else {
	    return null;
	}
    }
     /**
     * Returns the first selected <code>OperationCell</code>.
     * <p>
     * Returns the first selected <code>OperationCell</code> from this 
     * active graph frame. 
     * If the first selected 
     * cell is not an instance of the <code>OperationCell</code> class this 
     * method will return
     * <code>null</code>
     * 
     * @return the selected operation cell
     */
    public OperationCell getSelectedOperationCell() {
	if(getSelectedAt(0) instanceof OperationCell) {
	    return (OperationCell)getSelectedAt(0);
	}else {
	    return null;
	}
    }
     /**
     * Returns the first selected <code>ResourceCell</code>.
     * <p>
     * Returns the first selected <code>ResourceCell</code> from this 
     * active graph frame. 
     * If the first selected 
     * cell is not an instance of the <code>ResourceCell</code> class this 
     * method will return
     * <code>null</code>
     * 
     * @return the selected resource cell
     */
    public ResourceCell getSelectedResourceCell() {
	if(getSelectedAt(0) instanceof ResourceCell) {
	    return (ResourceCell)getSelectedAt(0);
	}else {
	    return null;
	}
    }
     /**
     * Returns the first selected <code>NestedCell</code>.
     * <p>
     * Returns the first selected <code>NestedCell</code> from this 
     * active graph frame. 
     * If the first selected 
     * cell is not an instance of the <code>NestedCell</code> class this 
     * method will return
     * <code>null</code>
     * 
     * @return the selected nested cell
     */
    public NestedCell getSelectedNestedCell() {
	if(getSelectedAt(0) instanceof NestedCell) {
	    return (NestedCell)getSelectedAt(0);
	}else {
	    return null;
	}
    }   
     /**
     * Returns the first selected <code>NestedCellListener</code>.
     * <p>
     * Returns the first selected <code>NestedCellListener</code> from this 
     * active graph frame. 
     * If the first selected 
     * cell is not an instance of the <code>NestedCellListener</code> class 
     * this method will return
     * <code>null</code>
     * 
     * @return the selected nested cell listener
     */
    public NestedCellListener getSelectedNestedCellListener() {
	if(getSelectedAt(0) instanceof NestedCellListener) {
	    return (NestedCellListener)getSelectedAt(0);
	}else {
	    return null;
	}
    }    
}
