package org.supremica.external.avocades;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.StringReader;
import java.io.StringWriter;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;


import org.jdom.Document;
import org.jdom.output.XMLOutputter;
import org.jdom.input.SAXBuilder;
import org.jdom.JDOMException;

import org.supremica.automata.Automaton;
import org.supremica.automata.Supervisor;

import org.supremica.automata.Project;
import org.supremica.automata.IO.SupremicaMarshaller;
import org.supremica.automata.IO.SupremicaUnmarshaller;
import org.supremica.external.avocades.relationextraction.Extractor;
import org.supremica.external.avocades.specificationsynthesis.ConverterILtoAutomata;
import org.supremica.external.avocades.specificationsynthesis.SpecificationSynthesInputBuilder;

import org.supremica.manufacturingTables.xsd.processeditor.ROP;
import org.supremica.manufacturingTables.xsd.processeditor.Activity;
import org.supremica.manufacturingTables.xsd.processeditor.Relation;
import org.supremica.manufacturingTables.xsd.eop.EOP;
import org.supremica.manufacturingTables.xsd.eop.Action;
import org.supremica.manufacturingTables.xsd.il.IL;
import org.supremica.manufacturingTables.xsd.il.Term;
import org.supremica.manufacturingTables.xsd.il.OperationCheck;

import org.supremica.external.avocades.xml.Converter;

import org.supremica.external.avocades.dop2efa.DOPtoEFA;
import org.supremica.external.avocades.eop2efa.EOPtoEFA;
import org.xml.sax.SAXException;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;

import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;

import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

import org.supremica.automata.algorithms.AutomataSynchronizer;
import org.supremica.automata.algorithms.AutomataSynthesizer;

import org.supremica.automata.IO.ProjectBuildFromWaters;


//-----------------------------------------------------------------------------
//
//Imported constants
//
//-----------------------------------------------------------------------------

import static org.supremica.external.avocades.AutomataNames.EVENT_MACHINE_SEPARATOR;

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
    	
    	final String NEWLINE = "\n";
    	
    	String comment = "";
    	
    	File tmpFile = null;
    	ModuleSubject module = null;
    	
    	ConverterILtoAutomata convAut;
    	
    	SpecificationSynthesInputBuilder builder;
    	builder = new SpecificationSynthesInputBuilder();
    	
    	comment = "EOP" + NEWLINE;
    	
    	//Add all EOP:s
    	for( EOP eop : getAdaptedEOPList() ){
    		builder.add( eop );
    		comment = comment + eop.getId() +", ";
    	}
    	comment = comment + NEWLINE;
    	
    	
    	comment = comment + "IL" + NEWLINE;
    	//Add all IL:s
    	for( IL il : getAdaptedILList() ){
    		builder.add( il );
    		comment = comment + il.getId() +", ";
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
        
        //Debug info
        module.setName("SpecificationSynthes");
        module.setComment(comment);
        
        return module;
    }
    
    
    /**
     * Returns a list of IL renamed to match the ROPs and EOPs added
     * to COPBuilder.
     *  
     * @return
     */
    public List<IL> getAdaptedILList(){
    	
    	List<IL> tmpILList = new LinkedList<IL>();
    	IL tmpIL;
    	
    	for(IL il : ilList){
    		
    		/*
    		 * Create new il
    		 */
    		tmpIL = copy( il );
    		
    		/*
    		 * Rename Operations in Operation list
    		 */
    		renameILOperationList( tmpIL.getILStructure().getOperations().getOperation() );
    		
    		/*
    		 * Rename Operation in term
    		 */
    		renameOperationsInTermList( tmpIL );
    		
    		/*
    		 * Add renamed IL to list
    		 */
    		tmpILList.add( tmpIL );
    		
    	}//end for ilList
    	
    	return tmpILList;
    }
    
    public List<ROP> getAdaptedROPList(){
    	
    	final List<ROP> tmpROPList = new LinkedList<ROP>();
    	
    	ROP tmpROP;
    	
    	for(ROP rop : ropList){
    		tmpROP = copy( rop );
    		tmpROPList.add( tmpROP );
    	}
    	
    	
    	
    	return tmpROPList;
    }
    
    private ROP copy(final ROP rop){
    	
    	org.supremica.manufacturingTables.xsd.processeditor.ObjectFactory factory;
    	factory = new org.supremica.manufacturingTables.xsd.processeditor.ObjectFactory();
    	
    	ROP newROP = factory.createROP();
    	newROP.setComment(rop.getComment());
    	newROP.setId(rop.getId());
    	newROP.setMachine(rop.getMachine());
    	newROP.setRelation(rop.getRelation());
    	newROP.setType(rop.getType());
    	 
      	return newROP;
    }
    
    
    
    private void renameILOperationList(List<String> operationList){
    	
    	/*
    	 * The renamed EOP list contains EOP:s with operation name and machine.
    	 * They are derived based on the ROP list.
    	 */
    	final List<EOP> tmpEOPList = getAdaptedEOPList();
    	
    	final List<String> tmpStrList = new LinkedList<String>();
    	
    	/*
		 * Rename Operations in Operation list
		 */
		for( String op : operationList ){
			
			for(EOP eop : tmpEOPList){
				
				
				if(eop.getId().contains(EVENT_MACHINE_SEPARATOR)){
					
					if( eop.getId().indexOf(EVENT_MACHINE_SEPARATOR) ==  op.length() && 
						eop.getId().substring(0, op.length()).equals( op ) )
					{
						tmpStrList.add( eop.getId() );
					}
					
				}else{
					if( eop.getId().equals(op) ){
						tmpStrList.add( eop.getId() );
					}
				}
    		}
		}
		
		//clear list
		operationList.clear();
		
		//add new operations
		for(String str : tmpStrList){
			operationList.add(str);
		}
    }
    
    
    private void renameOperationsInTermList( IL il ){
    	
    	List<String> tmpStrList = new LinkedList<String>();
    	
    	/*
		 * Rename Operation in term
		 */
		for(Term term : il.getILStructure().getTerm() ){
			for(OperationCheck operationCheck : term.getOperationCheck()){
				
				/*
				 * Rename not ongoing operations
				 */
				for(String operationNotOngoing : operationCheck.getNotOngoing().getOperation()){
					
					/*
					 * find operation
					 */
    				for( String operation : il.getILStructure().getOperations().getOperation() ){
    					
    					if( operation.startsWith( operationNotOngoing ) ){
    						//add
    						tmpStrList.add( operation );
    					}
    					
        			}
				}
				
				//clear list
				operationCheck.getNotOngoing().getOperation().clear();
				
				//add new operations
				for(String str : tmpStrList){
					operationCheck.getNotOngoing().getOperation().add(str);
				}
				tmpStrList.clear();
				
				
				/*
				 * Rename not started operations
				 */
				for(String operationNotStarted : operationCheck.getNotStarted().getOperation()){
    				
					/*
					 * find operation
					 */
    				for( String operation : il.getILStructure().getOperations().getOperation() ){
    					if( operation.startsWith( operationNotStarted ) ){
    						//add
    						tmpStrList.add( operation );
    					}
        			}
				}
				
				//clear list
				operationCheck.getNotStarted().getOperation().clear();
				
				//add new operations
				for(String str : tmpStrList){
					operationCheck.getNotStarted().getOperation().add(str);
				}
				tmpStrList.clear();
			}
		}
    }
    
    private IL copy( IL il ){
    	
    	org.supremica.manufacturingTables.xsd.il.ObjectFactory factory;
    	factory = new org.supremica.manufacturingTables.xsd.il.ObjectFactory();
    	
    	IL newIL = factory.createIL();
    	
    	newIL.setActuator(il.getActuator());
    	newIL.setComment(il.getComment());
    	newIL.setId(il.getId());
    	newIL.setILStructure(il.getILStructure()); //Ok??
    	newIL.setOperation(il.getOperation());
    	newIL.setType(il.getType());
    	
    	return il;
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
    
    private Document openDocument( File file ){
    	Document doc = null;
		try{
			SAXBuilder inp = new SAXBuilder();
			inp.setExpandEntities(true);
			doc = inp.build(file);
		}
		catch ( JDOMException e ) {
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		return doc;
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
    	
    	comment = "Derived from following(renamed) EOP(s):" + "\n";
    	for(EOP eop : getAdaptedEOPList() ){
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
    
    
    
    public List<EOP> getAdaptedEOPList(){
    	
    	List<EOP> tmpEOPList = new LinkedList<EOP>();
    	EOP tmpEOP;
    	
    	for(EOP eop: eopList){
    		for(ROP rop : ropList){
    			
    			if( eopIsUsedByROP(eop, rop) ){
    				
    				//Copy EOP
    				tmpEOP = copy( eop );
    				tmpEOP.setId(eop.getId() + 
    						     EVENT_MACHINE_SEPARATOR + 
    						     rop.getMachine());
    				tmpEOPList.add(tmpEOP);
    			}
    		}
    	}
    	
    	return tmpEOPList;
    }
    
    private boolean eopIsUsedByROP(EOP eop, ROP rop){
    	
    	if(null == rop){
    		return false;
    	}
    	
    	return relationContainsEOP(eop, rop.getRelation());
    }
    
    private boolean relationContainsEOP(EOP eop, Relation r){
    	
    	//Sanity check
    	if( null == eop || null == eop.getId() || 0 == eop.getId().length() ){
    		return false;
    	}else if( null == r ){
    		return false;
    	}
    	
    	for(Object o : r.getActivityRelationGroup()){
    		
    		if(o instanceof Activity){
    			
    			/*
    			 * if Activity Operation string equals EOP id. 
    			 * 
    			 */
    			if( eop.getId().equals( ((Activity)o).getOperation() ) ){
    				return true;
    			}
    		}else if(o instanceof Relation){
    			
    			//recursion
    			if( relationContainsEOP(eop, (Relation)o ) ){
    				return true;
    			}
    		}
    	}
    	
    	return false;
    }
    
    private EOP copy(EOP eop){
    	
    	org.supremica.manufacturingTables.xsd.eop.ObjectFactory factory;
    	factory = new org.supremica.manufacturingTables.xsd.eop.ObjectFactory();
    	
    	EOP tmpEOP;
    	
    	//Sanity check
    	if(null == eop){
    		return null;
    	}
    	
    	//Copy EOP
		tmpEOP = factory.createEOP();
		
		tmpEOP.setId(eop.getId());
		tmpEOP.setType(eop.getType());
		tmpEOP.setComment(eop.getComment());
		
		tmpEOP.setInitialState(eop.getInitialState());
		
		for(Action a : eop.getAction()){
			tmpEOP.getAction().add(a);
		}
		
		tmpEOP.setInternalComponents(eop.getInternalComponents());
		tmpEOP.setExternalComponents(eop.getExternalComponents());
		
		tmpEOP.setZones(eop.getZones());
		
    	return tmpEOP;
    }
    
    //TODO: Make this work, unfinished
    public List<ROP> getRelationExtractionOutput(){
    	
    	Extractor extractor = null;
    	Document supDoc = null;
    	ArrayList<Document> tmpROPList = null;
    	List<ROP> copList = null;
    	
    	ModuleSubject module = null;
    	List<ModuleSubject> moduleList = null;
    	
    	/*
    	 * 1. Create Supervisor document.
    	 */
    	supDoc = new Document();
   
    	moduleList = new LinkedList<ModuleSubject>();
    	
    	moduleList.add( getSpecificationSynthesisOutput() );
    	moduleList.add( getEOPtoEFAOutput() );
    	moduleList.add( getDOPtoEFAOutput() );
    	
    	module = createSupervisor( moduleList );
    	
    	supDoc = watersModuleToProjectDocument( module );
    	
    	/*
    	 * 2. Extract relation 
    	 */
    	
    	tmpROPList = new ArrayList<Document>();
    	for(ROP rop : ropList){
    		tmpROPList.add( Converter.convertToDocument( rop ) );
    	}
    	
    	/*
    	 * Observe that the automaton representation of the supervisor must be
    	 * such that the states of the operation models are unique, and
    	 * separated by a dot in the supervisor states; 
    	 */
    	extractor = new Extractor();
    	tmpROPList = extractor.extractRestrictions( supDoc , tmpROPList );

		/*
		 * 3. Fill COP list
		 */
		copList = new ArrayList<ROP>();
		for( Iterator cIter = tmpROPList.iterator(); cIter.hasNext(); ){
			Document cop = (Document) cIter.next();
			
			ROP rop = Converter.convertToROP( cop );
			
			if(null != rop){	
				removeMachineNameFromOperation(rop);
				copList.add( rop );
			}
		}
		return copList;
    }
    
    private void removeMachineNameFromOperation(ROP rop){
    	;//Gör klart denna funktion snart klar
    }
    
    
    
    
    
    
    private Document watersModuleToProjectDocument(ModuleSubject module){
    	
    	File file = null;
    	Document doc = null;
    	
    	ProjectBuildFromWaters supremicaProjBuilderFromWatersModule = null;
    	supremicaProjBuilderFromWatersModule = new ProjectBuildFromWaters(new DocumentManager());
    	
    	SupremicaMarshaller supremicaMarshaller = new SupremicaMarshaller();
    	
    	//create temporary file
    	try{
            file = File.createTempFile("watersModuleToDOMDocument",
            		supremicaMarshaller.getDefaultExtension() );
        }catch( IOException e ){
        	;
        }
        
        //Save module to file
        try
		{
        	Project project = supremicaProjBuilderFromWatersModule.build( module );
        	supremicaMarshaller.marshal( project, file);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		doc = openDocument( file );
		
		if(null != file){
			//file.delete();
		}
		
    	return doc;
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
    
    public ModuleSubject synchronizeModules(List<ModuleSubject> moduleList){
    	
    	ModuleSubject module = null;
    	Automaton automaton = null; 
    	
    	ProjectBuildFromWaters supremicaProjBuilderFromWatersModule = null;
        Project supremicaProject = null;
        
    	//Sanity check
    	if(null == moduleList || 0 == moduleList.size() ){
    		return module;
    	}
    	
    	//Merge all modules to one module
    	module = mergeModules( moduleList );
    	
    	supremicaProjBuilderFromWatersModule = new ProjectBuildFromWaters(new DocumentManager());
    	
        try{
        	supremicaProject = supremicaProjBuilderFromWatersModule.build(module);
        }catch(EvalException e){
        	e.printStackTrace();
        }
        
        //Synchronize
        try{
        	automaton = AutomataSynchronizer.synchronizeAutomata(supremicaProject);
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        //Clear supremica project
        supremicaProject.clear();
        
        //Add synchronized automaton 
        supremicaProject.addAutomaton(automaton);
        
        //Convert to waters module
        module = (ModuleSubject) mImporter.importModule( supremicaProject );
        
        return module;
    }
    
    
    public ModuleSubject createSupervisor(List<ModuleSubject> moduleList){
    	
    	ModuleSubject module = null;
    	Supervisor supervisor = null; 
    	
    	ProjectBuildFromWaters supremicaProjBuilderFromWatersModule = null;
        Project supremicaProject = null;
        
    	//Sanity check
    	if(null == moduleList || 0 == moduleList.size() ){
    		return module;
    	}
    	
    	//Merge all modules to one module
    	module = mergeModules( moduleList );
    	
    	supremicaProjBuilderFromWatersModule = new ProjectBuildFromWaters(new DocumentManager());
    	
        try{
        	supremicaProject = supremicaProjBuilderFromWatersModule.build(module);
        }catch(EvalException e){
        	e.printStackTrace();
        }
        
        //Supervisor
        try{
        	supervisor = AutomataSynthesizer.synthesizeControllableNonblocking( supremicaProject );
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        //Clear supremica project
        supremicaProject.clear();
        
        //Add synchronized automaton 
        supremicaProject.addAutomata(supervisor.getAsAutomata());
        
        //Convert to waters module
        module = (ModuleSubject) mImporter.importModule( supremicaProject );
        
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
