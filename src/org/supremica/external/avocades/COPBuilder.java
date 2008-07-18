package org.supremica.external.avocades;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import javax.xml.bind.JAXBException;

import org.jdom.Document;
import org.jdom.output.XMLOutputter;

import org.supremica.automata.Project;
import org.supremica.automata.IO.SupremicaMarshaller;
import org.supremica.automata.IO.SupremicaUnmarshaller;
import org.supremica.external.avocades.relationextraction.Extractor;
import org.supremica.external.avocades.specificationsynthesis.ConverterILtoAutomata;
import org.supremica.external.avocades.specificationsynthesis.SpecificationSynthesInputBuilder;

import org.supremica.manufacturingTables.xsd.processeditor.ROP;
import org.supremica.manufacturingTables.xsd.eop.EOP;
import org.supremica.manufacturingTables.xsd.il.IL;

import org.supremica.external.avocades.xml.Converter;

import org.supremica.external.avocades.dop2efa.DOPtoEFA;
import org.supremica.external.avocades.eop2efa.EOPtoEFA;
import org.xml.sax.SAXException;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;

import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;


public class COPBuilder {
	
	private List<ROP> ropList = null;
	private List<EOP> eopList = null;
	private List<IL>  ilList  = null;
	
	private DocumentManager mDocumentManager = null;
	private final ProductDESImporter mImporter;
	
	/**
	 * Constructor
	 */
    public COPBuilder()
    	throws JAXBException, SAXException
    {
        ropList = new ArrayList<ROP>();
        eopList = new LinkedList<EOP>();
        ilList = new LinkedList<IL>();
        
     // Set up document manager ...
        mDocumentManager = new DocumentManager();
        final ModuleProxyFactory factory = ModuleSubjectFactory.getInstance();
        final OperatorTable opTable = CompilerOperatorTable.getInstance();
        
        final JAXBModuleMarshaller moduleMarshaller =
            new JAXBModuleMarshaller(factory, opTable);
        final ProxyUnmarshaller<Project> supremicaUnmarshaller =
            new SupremicaUnmarshaller(factory);
        
        // Add unmarshallers in order of importance ...
        mDocumentManager.registerUnmarshaller(moduleMarshaller);
        mDocumentManager.registerUnmarshaller(supremicaUnmarshaller);
        
        mImporter = new ProductDESImporter(factory);
    }
    
    //-----------------------------------------------------------------------//
    // add
    //-----------------------------------------------------------------------//
    
    /**
     * Add new ROP to COPBuilder
     * @param rop - the ROP instance to be added
     */
    public void add( ROP rop ){
    	
    	//Sanity check
    	if( null == rop || ropList.contains( rop ) ){
    		return;
    	}
    	
    	ropList.add( rop );
    }
    
    /**
     * 
     * Add new EOP to COPBuilder
     * @param eop - the EOP instance to be added
     * 
     */
    public void add( EOP eop ){
    	
    	//Sanity check
    	if( null == eop || eopList.contains( eop ) ){
    		return;
    	}
    	
    	eopList.add( eop );
    }
    
    /**
     * 
     * Add new IL to COPBuilder
     * @param il - the IL instance to be added
     * 
     */
    public void add( IL il ){
    	
    	//Sanity check
    	if( null == il || ilList.contains( il ) ){
    		return;
    	}
    	
    	ilList.add( il );
    }
    
    //-----------------------------------------------------------------------//
    // get
    //-----------------------------------------------------------------------//
    public ModuleSubject getSpecificationSynthesisOutput(){
    	
    	File tmpFile = null;
    	ModuleSubject module = null;
    	
    	ConverterILtoAutomata convAut = null;
    	SpecificationSynthesInputBuilder builder = null;
  
    	builder = new SpecificationSynthesInputBuilder();
    	
    	//Add all EOP:s
    	for( EOP eop : eopList ){
    		builder.add( eop );
    	}
    	
    	//Add all IL:s
    	for( IL il : ilList ){
    		builder.add( il );
    	}
    	
    	convAut = new ConverterILtoAutomata();
		convAut.convertILtoAutomata( builder.getDoc() );
		
    	try{
    		//create temporary file
            tmpFile = File.createTempFile("tmpSpecificationSynthes", ".xml");
            
            //save JDOM document to file
            saveDocument( convAut.getDoc(), tmpFile );
            
        }catch( IOException e ){
        	tmpFile.delete();
        	return null;
        }
        
        final DocumentProxy doc = load(tmpFile.toURI());
        
        if(doc instanceof Project){
        	module = (ModuleSubject) mImporter.importModule( (Project)doc );
        }
        
        if(null != tmpFile){
        	tmpFile.delete();
        }
        
        return module;
    }
    
    private DocumentProxy load(final URI uri){
        try {
            // The documentmanager does the loading, by extension.
            return mDocumentManager.load(uri);
        }catch (final WatersUnmarshalException exception){
            return null;
        }catch (final IOException exception){
            return null;
        }
    }
    
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
			e.printStackTrace();
		}
	}
    
    /**
     * Converts all ROP:s to EFA:s
     * @return a waters ModuleSubject object to be opened with Supremica
     */
    public ModuleSubject getDOPtoEFAOutput(){
    	ModuleSubject module;
    	
    	String moduleName = "DOP to EFA output";
    	
    	String comment = "Derived from following DOP(s):" + "\n";
    	for(ROP rop : ropList){
    		comment = comment.concat( rop.getMachine() + ", ");
    	}
    	
    	module = DOPtoEFA.buildModuleFromROP(ropList, moduleName, false).getModule();
    	module.setComment(comment);
    	
    	return module;
    }
    
    public ModuleSubject getEOPtoEFAOutput(){
    	ModuleSubject module;
    	
    	String moduleName = "EOP to EFA output";
    	String comment = "";
    	
    	EOPtoEFA builder = new EOPtoEFA();
    	
    	comment = "Derived from following EOP(s):" + "\n";
    	for(EOP eop : eopList){
    		comment = comment.concat(eop.getId() + ", ");
    		builder.add(eop);
    	}
    	
    	//Create module
    	module = builder.getModule().getModule();
    	
    	//Set name and comment
    	module.setName(moduleName);
    	module.setComment(comment);
    	
    	return  module;
    }
    
    //TODO: Make this work, unfinished
    public List<ROP> getRelationExtractionOutput(){
    	
    	Extractor extractor = null;
    	Document supDoc = null;
    	ArrayList<ROP> ropList = null;
    	List<ROP> copList = null;
    	
    	/*
    	 * 1. Create Supervisor document.
    	 */
    	supDoc = new Document();
    	
    	/*
    	 * 2. Extract relation 
    	 */
    	
    	/*
    	 * Observe that the automaton representation of the supervisor must be
    	 * such that the states of the operation models are unique, and
    	 * separated by a dot in the supervisor states; 
    	 */
    	extractor = new Extractor();
		ropList = extractor.extractRestrictions( supDoc , ropList );

		/*
		 * 3. Fill COP list
		 */
		copList = new ArrayList<ROP>();
		for( Iterator cIter = ropList.iterator(); cIter.hasNext(); ){
			Document cop = (Document) cIter.next();
			copList.add( Converter.convertJDomCOPDocumentToJAXBCOPObject( cop ) );
		}
		
		return copList;
    }
    
    
    
    /**
     * Merge a List of waters modules by adding all components to one module.
     * @param moduleList
     * @return
     */
    public ModuleSubject mergeModules(List<ModuleSubject> moduleList){
    	ModuleSubject module;
    	
    	//Sanity check
    	if(null == moduleList || 0 == moduleList.size() ){
    		return null;
    	}
    	
    	module = new ModuleSubject( "", null );
    	for(ModuleSubject mod : moduleList){
    		
    		//Add event declarations from module
    		for(EventDeclSubject sub : mod.getEventDeclListModifiable()){
    			if( !containsEvent( module, sub.getName() ) ){
    				module.getEventDeclListModifiable().add(sub.clone());
    			}
    		}
    	
    		//Add components from module
    		for(AbstractSubject sub : mod.getComponentListModifiable()){
    			module.getComponentListModifiable().add(sub.clone());
    		}
    	}
    	
    	return module;
    }
    
    
    /**
     * Checks whether the module contains an event with the
     * given name.
     */
    boolean containsEvent(final ModuleSubject module, final String name){
        for (final EventDeclProxy decl : module.getEventDeclList()) {
            if (decl.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    
    
    
    
    
    
}
