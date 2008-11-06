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

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import java.util.Date;

import javax.xml.bind.JAXBException;

import org.jdom.Document;
import org.jdom.output.XMLOutputter;
import org.jdom.input.SAXBuilder;
import org.jdom.JDOMException;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.ModularSupervisor;
import org.supremica.automata.Supervisor;

import org.supremica.automata.Project;
import org.supremica.automata.IO.SupremicaMarshaller;
import org.supremica.automata.IO.SupremicaUnmarshaller;
import org.supremica.external.avocades.relationextraction.Extractor;
import org.supremica.external.avocades.specificationsynthesis.ConverterILtoAutomata;
import org.supremica.external.avocades.specificationsynthesis.SpecificationSynthesInputBuilder;

import org.supremica.manufacturingTables.xsd.processeditor.ROP;
import org.supremica.manufacturingTables.xsd.processeditor.Activity;
import org.supremica.manufacturingTables.xsd.processeditor.Attribute;
import org.supremica.manufacturingTables.xsd.processeditor.UpperIndicator;
import org.supremica.manufacturingTables.xsd.processeditor.LowerIndicator;
import org.supremica.manufacturingTables.xsd.processeditor.ObjectFactory;
import org.supremica.manufacturingTables.xsd.processeditor.OperationReferenceType;
import org.supremica.manufacturingTables.xsd.processeditor.Relation;
import org.supremica.manufacturingTables.xsd.processeditor.RelationType;
import org.supremica.manufacturingTables.xsd.eop.EOP;
import org.supremica.manufacturingTables.xsd.eop.Action;
import org.supremica.manufacturingTables.xsd.eop.ZoneState;

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
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.automata.algorithms.SynthesizerOptions;

import org.supremica.automata.IO.ProjectBuildFromWaters;


//-----------------------------------------------------------------------------
//
//Imported constants
//
//-----------------------------------------------------------------------------

import static org.supremica.external.avocades.AutomataNames.EVENT_MACHINE_SEPARATOR;
import static org.supremica.external.avocades.dop2efa.DOPnative.RESOURCE;

import static org.supremica.external.avocades.dop2efa.DOPnative.BOOK;
import static org.supremica.external.avocades.dop2efa.DOPnative.FREE;


/**
 * <code>COPBuilder</code> is the main class in the <code>avocades</code>
 * package.
 * 
 * @author       David Millares
 * @version      %G%
 */
public class COPBuilder {
	
	private List<ROP> ropList = null;
	private List<EOP> eopList = null;
	private List<IL>  ilList  = null;
	
	private DocumentManager mDocumentManager = null;
	private final ProductDESImporter mImporter;
	
	private SynchronizationOptions synchronizationOptions;
	private SynthesizerOptions synthesizerOptions;
	
	/**
	 * Constructor
	 */
    public COPBuilder()
    	throws JAXBException, SAXException
    {
        ropList = new ArrayList<ROP>();
        eopList = new LinkedList<EOP>();
        ilList = new LinkedList<IL>();
        
        //Default options
        synchronizationOptions = SynchronizationOptions.
        							getDefaultSynthesisOptions();
        synthesizerOptions = SynthesizerOptions.
        						getDefaultMonolithicCNBSynthesizerOptions();
        
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
     * Add new EOP to COPBuilder
     * @param eop - the EOP instance to be added
     */
    public void add( EOP eop ){
    	
    	//Sanity check
    	if( null == eop || eopList.contains( eop ) ){
    		return;
    	}
    	
    	eopList.add( eop );
    }
    
    /**
     * Add new IL to COPBuilder
     * @param il - the IL instance to be added
     */
    public void add( IL il ){
    	
    	//Sanity check
    	if( null == il || ilList.contains( il ) ){
    		return;
    	}
    	
    	ilList.add( il );
    }
    
    
    /**
     * Sets the synchronization options to be used by synchronizations.
     * @param synchronizationOptions
     */
    public void setSynchronizationOptions
    (
    	SynchronizationOptions synchronizationOptions
    )
    {
    	this.synchronizationOptions = synchronizationOptions;
    }
    
    /**
     * Sets the synthesis options.
     * @param synthesizerOptions
     */
    public void setSynthesizerOptions(SynthesizerOptions synthesizerOptions){
    	this.synthesizerOptions = synthesizerOptions;
    }
    
    
    //-----------------------------------------------------------------------//
    // get
    //-----------------------------------------------------------------------//
    
    /**
     * Get the synthesis options
     * @param synthesizerOptions
     * @return the synthesizer options
     */
    public SynthesizerOptions getSynthesizerOptions
    (
        SynthesizerOptions synthesizerOptions
    )
    {
    	return synthesizerOptions;
    }
    
    
    /**
     * Get the synchronization options.
     * @return the synchronization options
     */
    public SynchronizationOptions getSynchronizationOptions(){
    	return synchronizationOptions;
    }
    
    
    public ModuleSubject getSpecificationSynthesisOutput(){
    	return getSpecificationSynthesisOutput( getAdaptedEOPList(),
    			                                getAdaptedILList() );
    }
    
    private ModuleSubject getSpecificationSynthesisOutput
    (
    	final List<EOP> adaptedEOPList,
    	final List<IL> adaptedILList
    )
    {
    	
    	final String NEWLINE = "\n";
    	final ConverterILtoAutomata convAut;
    	final SpecificationSynthesInputBuilder builder;
    	
    	String comment = "";
    	
    	File tmpFile = null;
    	ModuleSubject module = null;
 
    	builder = new SpecificationSynthesInputBuilder();
    	
    	comment = "EOP" + NEWLINE;
    	
    	//Add all EOP:s
    	for( EOP eop : adaptedEOPList ){
    		builder.add( eop );
    		comment = comment + eop.getId() +", ";
    	}
    	comment = comment + NEWLINE;
    	
    	
    	comment = comment + "IL" + NEWLINE;
    	
    	//Add all IL:s
    	for( IL il : adaptedILList ){
    		builder.add( il );
    		comment = comment + il.getId() +", ";
    	}
    	
    	convAut = new ConverterILtoAutomata();
		convAut.convertILtoAutomata( builder.getDoc() );
/*		
		//debug code
		try{
    		//create debug file
            tmpFile = new File("eopsils.xml");
            
            //save JDOM document to file
            saveDocument( builder.getDoc(), tmpFile );
            
            System.out.println("Saved debug file: " +
            		               tmpFile.getAbsolutePath());
            
        }catch( Exception e ){
        	tmpFile.delete();
        	return null;
        }
		//end debug code
*/			
    	try{
    		//create temporary file
            tmpFile = File.createTempFile("tmpSpecificationSynthes", ".xml");
            
            //save JDOM document to file
            saveDocument( convAut.getDoc(), tmpFile );
            
        }catch( IOException e ){
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
        module.setName( "SpecificationSynthes" );
        module.setComment( comment );
        
        return module;
    }
    
    
    public List<EOP> getAdaptedEOPList(){
    	
    	List<EOP> tmpEOPList = new LinkedList<EOP>();
    	EOP tmpEOP;
    	
    	for(EOP eop: eopList){
    		for(ROP rop : ropList){
    			
    			if( eopIsUsedByROP(eop, rop) ){
    				
    				//Copy EOP
    				tmpEOP = Converter.copy( eop );
    				tmpEOP.setId(eop.getId() + 
    						     EVENT_MACHINE_SEPARATOR + 
    						     rop.getMachine());
    				tmpEOPList.add(tmpEOP);
    			}
    		}
    	}
    	
    	return tmpEOPList;
    }
    
    
    /**
     * Returns a list of IL renamed to match the ROPs and EOPs added
     * to COPBuilder.
     *  
     * @return
     */
    public List<IL> getAdaptedILList(){
    	
    	final List<IL> tmpILList = new LinkedList<IL>();
    	
    	for(IL il : ilList){
    		
    		/*
    		 * Create new il
    		 */
    		IL tmpIL = Converter.copy( il );
    		
    		/*
    		 * Rename Operations in Operation list
    		 */
    		renameILOperationInList( tmpIL.getILStructure().getOperations().getOperation() );
    		
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
    	
    	for(ROP rop : ropList){
    		ROP tmpROP = Converter.copy( rop );
    		
    		addZoneBookingFromEOP( tmpROP );
    		
    		tmpROPList.add( tmpROP );
    	}
    	
    	return tmpROPList;
    }
    
    
    //-------------------------------------------------------------------------
    //
    //	Zone booking code
    //
    //-------------------------------------------------------------------------
    
    private void addZoneBookingFromEOP(ROP rop){
    	List<String> preBookedZones = new LinkedList<String>();
    	addZoneBookingFromEOP(rop.getRelation(), preBookedZones);
    }
    
    private List<String> addZoneBookingFromEOP(Relation relation, List<String> preBookedZones){
    	
    	if (RelationType.SEQUENCE.equals( relation.getType() )){
    		
    		for(Object o : relation.getActivityRelationGroup() ){
    			if (o instanceof Activity){
    				preBookedZones = addZoneBookingFromEOP( (Activity)o, preBookedZones);
        		} else if (o instanceof Relation){
        			preBookedZones = addZoneBookingFromEOP( (Relation)o , preBookedZones);
        		}
    		}
    		
    		
    	} else {
    		
    		List<String> tmpPreBookedZones1 = new LinkedList<String>();
    		List<String> tmpPreBookedZones2 = new LinkedList<String>();
    		
    		//First object
    		Object o = relation.getActivityRelationGroup().get(0);
			if (o instanceof Activity){
				tmpPreBookedZones1 = addZoneBookingFromEOP( (Activity)o, preBookedZones);
    		} else if (o instanceof Relation){
    			tmpPreBookedZones1 = addZoneBookingFromEOP( (Relation)o , preBookedZones);
    		}
			
			//The rest of the objects
    		for(int i = 1; i < relation.getActivityRelationGroup().size(); i++ ){
    			o = relation.getActivityRelationGroup().get(i);
    			
    			if (o instanceof Activity){
    				tmpPreBookedZones2 = addZoneBookingFromEOP( (Activity)o, preBookedZones);
        		} else if (o instanceof Relation){
        			tmpPreBookedZones2 = addZoneBookingFromEOP( (Relation)o , preBookedZones);
        		}
    			
    			/*
    			 * Remove zones that not are prebooked by all paths
    			 */
    			for(String zone : tmpPreBookedZones2){
    				if ( !tmpPreBookedZones1.contains( zone ) ){
    					tmpPreBookedZones1.remove( zone );
    				}
    			}
    		}
    		
    		/*
    		 * If there where prebooked zones before all zones needs to be prebooked
    		 * again to still be prebooked. 
    		 */
    		if (0 == preBookedZones.size() ){
    			preBookedZones = tmpPreBookedZones1;
    		} else {
    			
    			/*
    			 * Remove zones that not are prebooked by all paths
    			 */
    			for(String zone : tmpPreBookedZones1){
    				if ( !preBookedZones.contains( zone ) ){
    					preBookedZones.remove( zone );
    				}
    			}
    			
    		}
    		
    	}
    	
    	return preBookedZones;
    }
    
    private List<String> addZoneBookingFromEOP(Activity activity,
    		                                   List<String> preBookedZones)
    {	
    	//Sanity check
    	if(null == activity){
    		return preBookedZones;
    	}
    	
    	//Find EOP
    	EOP eop = getFirstMatchingEOPfromList(activity, eopList);
    	
    	//No EOP found
    	if(null == eop){
    		return preBookedZones;
    	}
    	
    	addZonesFromEOPtoActivity(eop, activity);
    	
    	//Zone booking from initial state
    	for (ZoneState zoneState : eop.getInitialState().getZoneState()){
    		
			//Booking of zones
    		if (BOOK.equals( zoneState.getState() )){
    			
    			//is it prebooked?
    			if ( !preBookedZones.contains( zoneState.getZone() ) ){
    				
    				for ( Attribute att : activity.getProperties().getAttribute() ){
						
						if ( att.getAttributeValue().equals(zoneState.getZone()) ){
							
							//look for zone/resource attribute
							if ( att.getType().equals(RESOURCE) ){
								
								//book the zone
    							att.getUpperIndicator().setIndicatorValue(true);
    							preBookedZones.add( zoneState.getZone() );
    							
    						}
						}
					}			
    			}
    			
    		}else if(FREE.equals( zoneState.getState() )){
    			//do nothing
    		}
    	}
    	
    	
    	//Zone booking from action
    	for( Action action : eop.getAction() ){
    		
    		for (ZoneState zoneState : action.getZoneState()){
    			
    			//Booking of zones
    			if (BOOK.equals( zoneState.getState() )){
    			
    				//is it prebooked?
    				if ( !preBookedZones.contains( zoneState.getZone() ) ){
    				
    					for ( Attribute att : activity.getProperties().getAttribute()){
    						
    						if ( att.getAttributeValue().equals(zoneState.getZone()) ){
    							if ( att.getType().equals(RESOURCE) ){
    								
    								//book the zone
        							att.getUpperIndicator().setIndicatorValue(true);
        							preBookedZones.add( zoneState.getZone() );
        							
        						}
    						}
    					}//end for
    				}		
    			}
    		}//end for
    	}
    	
    	if ( 0 == eop.getAction().size() ){
    		return preBookedZones;
    	}
    	
    	Action lastAction = eop.getAction().get( eop.getAction().size() - 1 );
    	for ( ZoneState zoneState : lastAction.getZoneState() ){
			
			//Booking of zones
			if ( FREE.equals( zoneState.getState() ) ){
				
				for ( Attribute att : activity.getProperties().getAttribute() ){
					
					if ( att.getAttributeValue().equals( zoneState.getZone() ) ){
						if ( att.getType().equals(RESOURCE) ){
							//unbook the zone
    						att.getLowerIndicator().setIndicatorValue(true);			
    					}
					}
				}//end for
				
				if( !preBookedZones.remove( zoneState.getZone() ) ){
					System.out.println("Warning");
					System.out.println("Zone: " + zoneState.getZone());
					System.out.println("Unbooked without being bocked");
				}
			}//end if
		}//end for
    	
    	return preBookedZones;
    }
    
    
    
    private static EOP getFirstMatchingEOPfromList(Activity activity,
    		                                       List<EOP> eopList)
    {	
    	//Find EOP
    	for(EOP eop : eopList){
    		if (eop.getId().trim().equals( activity.getOperation().trim() )){
    			return eop;
    		}
    	}
    	
    	return null;
    }
    
    private static void addZonesFromEOPtoActivity(EOP eop, Activity activity){
    	
    	//Sanity check
    	if(null == activity || null == eop){
    		return;
    	}
    	
    	if (null == eop.getZones() ||
    	    null == eop.getZones().getZone() ||
    	    0 == eop.getZones().getZone().size() )
    	{
    		return;
    	}
    	//end sanity check
    	
    	//add properties if needed
    	if ( null == activity.getProperties() ){
			activity.setProperties((new ObjectFactory()).createProperties());
		}
    	
    	//create and add zone attribute
    	for(String zone :  eop.getZones().getZone()){
			Attribute att = createZoneAttribute( zone );
			
			if ( !contains(activity.getProperties().getAttribute(), att) ){
				activity.getProperties().getAttribute().add(att);
			}
    	}
    }
    
    private static boolean contains(List<Attribute> attributeList,
    		                                   Attribute attribute)
    {
    	
    	for (Attribute att : attributeList){
    		
    		//Same value
    		if ( attribute.getAttributeValue().trim().equals(att.
    				getAttributeValue().trim()))
    		{
    			//Same type
    			if(attribute.getType().trim().equals(att.getType().trim())){
    				return true;
    			}
    		}
    	}
    	
    	return false;
    }
    
    
    /**
     * Creates a zone attribute
     * @param zoneName - the zone name
     * @return an attribute 
     */
    private static Attribute createZoneAttribute(String zoneName){
    	
    	ObjectFactory factory = new ObjectFactory();
    	
    	Attribute attribute = factory.createAttribute();
		
    	attribute.setInvisible(false);
		
		UpperIndicator uppInd = factory.createUpperIndicator();
		LowerIndicator lowInd = factory.createLowerIndicator();
		
		uppInd.setIndicatorValue(false);
		lowInd.setIndicatorValue(false);
		
		attribute.setUpperIndicator(uppInd);
		attribute.setLowerIndicator(lowInd);
		
		attribute.setAttributeValue( zoneName );
		attribute.setType(RESOURCE);
		
		return attribute;
    }
    
    private void renameILOperationInList(List<String> operationList){
    	
    	/*
    	 * The renamed EOP list contains EOP:s with operation name and machine.
    	 * They are derived based on the ROP list.
    	 */
    	final List<EOP> tmpEOPList = getAdaptedEOPList();
    	
    	
    	final List<String> machineList = new LinkedList<String>();
    	
    	for(EOP eop : tmpEOPList ){
    		machineList.add( eop.getId() );
		}
    	
    	renameStringsFromMachineStrings(operationList, machineList);
    }
    
    private void renameStringsFromMachineStrings(List<String> operationList,
    		                               final List<String> machineList)
    {
    	
    	final List<String> renamedOperationList = new LinkedList<String>();
    	final List<String> operationsToAddList = new LinkedList<String>();
    	
    	String operation;
    	boolean operationAdded = false;
    	
    	/*
		 * Rename Operations in Operation list
		 */
    	for(String op : operationList){
    		
    		operationAdded = false;
    		
    		for( String opm : machineList ){
    			operation = removeMachineString(opm);
    			
    			if ( op.trim().equals(operation.trim()) ){
    				
    				operationsToAddList.add( opm );
    				
    				if (!operationAdded){
    					renamedOperationList.add( op );
    					operationAdded = true;
    				}
    			}
			}
		}

    	//remove renamed operations
    	for(String op : renamedOperationList){
    		operationList.remove(op);
    	}

    	
    	operationList.addAll(operationsToAddList);	
    }
    
    /**
     * Removes all after EVENT_MACHINE_SEPARATOR from a string
     * [String1][EVENT_MACHINE_SEPARATOR][String2]
     * returns String2
     * Ex.
     * 
     * EVENT_MACHINE_SEPARATOR = ::
     * 
     * Op2::Machine1 => Op2
     *
     * @param str the string to be parsed
     * @return a substring from first index to last index of the
     *         EVENT_MACHINE_SEPARATOR string.
     */
    private String removeMachineString(String str){
    	
    	if (!str.contains(EVENT_MACHINE_SEPARATOR)){
    		return str;
    	}
    	
    	return str.substring(0, str.indexOf(EVENT_MACHINE_SEPARATOR));
    }
    
    
    private void renameOperationsInTermList( IL il ){
    	
    	final List<String> machineList = il.getILStructure().getOperations().getOperation();
    	
    	/*
		 * Rename Operation in term
		 */
		for(Term term : il.getILStructure().getTerm() ){
			for(OperationCheck operationCheck : term.getOperationCheck()){
				renameStringsFromMachineStrings(
						operationCheck.getNotOngoing().getOperation(),
						machineList);
				
				renameStringsFromMachineStrings(
						operationCheck.getNotStarted().getOperation(),
						machineList);
			}
		}
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

			FileOutputStream fileStream;
			fileStream = new FileOutputStream( file.getAbsolutePath() );

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
    	return getDOPtoEFAOutput( getAdaptedROPList() );
    }
    private ModuleSubject getDOPtoEFAOutput(final List<ROP> adaptedROPList){
    	
    	final ModuleSubject module;
    	final String moduleName = "DOP to EFA output";
    	
    	String comment = "Derived from following DOP(s):" + "\n";
    	for(ROP rop : adaptedROPList){
    		comment = comment.concat( rop.getMachine() + ", ");
    	}
    	
    	module = DOPtoEFA.buildModuleFromROP( adaptedROPList,
    			                              moduleName, false ).getModule();
    	module.setComment(comment);
    	
    	return module;
    }
    
    public ModuleSubject getEOPtoEFAOutput(){
    	return getEOPtoEFAOutput( getAdaptedEOPList() );
    }
    private ModuleSubject getEOPtoEFAOutput(final List<EOP> adaptedEOPList){
    	
    	final ModuleSubject module;
    	final String moduleName = "EOP to EFA output";
    	final EOPtoEFA builder = new EOPtoEFA();
    	
    	String comment = "";
    	
    	//Sanity check
    	if(null == adaptedEOPList || 0 == adaptedEOPList.size() ){
    		return null;
    	}
    	
    	comment = "Derived from following(renamed) EOP(s):" + "\n";
    	for(EOP eop :  adaptedEOPList ){
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
    			if( eop.getId().trim().equals( ((Activity)o).getOperation().trim() ) ){
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
    
    public List<ROP> getRelationExtractionOutput(){
    	
    	Date start;
    	Date stop;
    	
    	Long diff;
    	
    	start = new Date();
    	//do this once!
    	final List<ROP> adaptedROPList = getAdaptedROPList();
    	final List<EOP> adaptedEOPList = getAdaptedEOPList();
    	final List<IL>  adaptedILList  = getAdaptedILList();
    	stop = new Date();
    	
    	diff =  stop.getTime() - start.getTime();
    	System.out.println("All list adapted in " + diff + " ms");
    	
    	final Extractor extractor = new Extractor();
    	
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
    	
    	System.out.println("Create automatas");
    	start = new Date();
    	moduleList.add( getSpecificationSynthesisOutput(adaptedEOPList, adaptedILList) ); 
    	moduleList.add( getEOPtoEFAOutput( adaptedEOPList ) );
    	moduleList.add( getDOPtoEFAOutput( adaptedROPList ) );
    	stop = new Date();
    	diff =  stop.getTime() - start.getTime();
    	System.out.println("Done: Create automatas in " + diff + " ms");
    	
    	System.out.println("Create supervisor");
    	start = new Date();
    	module = createSupervisor( moduleList );
    	stop = new Date();
    	diff =  stop.getTime() - start.getTime();
    	System.out.println("Done: Create supervisor in " + diff + " ms");
    	
    	//System can not be started
    	if( null == module ){
    		System.out.println("No supervisor, system can not be started");
    		return null;
    	}
    	
    	supDoc = watersModuleToProjectDocument( module );
    	
    	
    	/*
    	 * 2. Extract relation 
    	 */
    	tmpROPList = new ArrayList<Document>();
    	for(ROP rop : adaptedROPList){
    		tmpROPList.add( Converter.convertToDocument( rop ) );
    	}
    	
    	/*
    	 * Observe that the automaton representation of the supervisor must be
    	 * such that the states of the operation models are unique, and
    	 * separated by a dot in the supervisor states; 
    	 */
    	
    	System.out.println("Extract restrictions");
    	start = new Date();
    	
    	tmpROPList = extractor.extractRestrictions( supDoc , tmpROPList );
    	
    	stop = new Date();
    	diff =  stop.getTime() - start.getTime();
    	System.out.println("Done: Extract restrictions in " + diff + "ms");
    	
		/*
		 * 3. Fill COP list
		 */
    	copList = new ArrayList<ROP>();
    	
    	System.out.println("Convert COPs");
		start = new Date();
		
		for( Iterator<Document> cIter = tmpROPList.iterator(); cIter.hasNext(); ){
			Document cop = (Document) cIter.next();
			
			ROP rop = Converter.convertToROP( cop );
			
			if ( null != rop ){	
				removeMachineNameFromActivitiesInROP( rop );
				removeDuplicatesOfPreconditionsInOperations( rop.getRelation() );
				copList.add( rop );
			}
		}
		
		stop = new Date();
    	diff =  stop.getTime() - start.getTime();
    	System.out.println("Done: Convert " +copList.size() + 
    			           "st COP:s in " + diff + "ms");
    	
		return copList;
    }
    
    private void removeMachineNameFromActivitiesInROP(final ROP rop){
    	
    	final String strMachineName;
    	
    	//Sanity check
    	if(null == rop){
    		return;
    	}
    	
    	//String to be removed 
    	strMachineName = EVENT_MACHINE_SEPARATOR + rop.getMachine();
    	
    	removeMachineNameFromActivitiesInRelation( rop.getRelation(), strMachineName);
    }
    
    private void removeMachineNameFromActivitiesInRelation(
    		final Relation relation,
    		final String strMachineName)
    {
    	
    	for(Object o : relation.getActivityRelationGroup()){
    		if(o instanceof Activity){
    			
    			//Base case
    			removeMachineNameFromActivity( (Activity)o, strMachineName );
    		}else if(o instanceof Relation){
    			
    			//Recursion
    			removeMachineNameFromActivitiesInRelation( (Relation)o, strMachineName );
    		}
    	}
    }
    
    private void removeMachineNameFromActivity(
    		final Activity activity, 
    		String strMachineName )
    {
    	String strOperation;
    	
    	//Sanity check
    	if(null == activity || null == strMachineName){
    		return;
    	}
    	
    	strOperation = activity.getOperation();
    	
    	//---------------------------------------------------------------------
    	//	1. Remove machine name in Operation name
    	//---------------------------------------------------------------------
    	if( null != strOperation && 0 != strOperation.length() ){
    		activity.setOperation( strOperation.replace( strMachineName, "" ) );
    	}
    	
    	
    	//---------------------------------------------------------------------
    	//	2. Remove machine name in Preconditions
    	//---------------------------------------------------------------------
    	if( null != activity.getPrecondition() ){
    		for(OperationReferenceType opRef : activity.getPrecondition().getPredecessor()){
    		
    			strOperation = opRef.getOperation();
    			strMachineName = EVENT_MACHINE_SEPARATOR + opRef.getMachine();
    			
    			/*
        	 	* Replaces machine name in operation name
        	 	*/
        		opRef.setOperation( strOperation.replace( strMachineName, "" ) );
    		}
    	}
    }
    
    private void removeDuplicatesOfPreconditionsInOperations(final Relation relation){
    	
    	//Sanity check
    	if( null == relation){
    		return;
    	}
    	
    	for(Object o : relation.getActivityRelationGroup()){
    		if (o instanceof Activity){
    			if (null != ((Activity)o).getPrecondition() ){
    				//base case
        			removeDuplicates( ((Activity)o).
        					             getPrecondition().
        					                   getPredecessor() );
    			}
    		} else if(o instanceof Relation){
    			//recursion
    			removeDuplicatesOfPreconditionsInOperations( (Relation)o );
    		}
    	}
    }
    
    private void removeDuplicates(final List<OperationReferenceType> opRefList){
    	
    	final Set<OperationReferenceType> opRefSet; 
    	
    	//Sanity check
    	if (null == opRefList || 0 == opRefList.size()){
    		return;
    	}
    	
    	opRefSet = new TreeSet<OperationReferenceType>(new OperationReferenceComparator());
    	opRefSet.addAll(opRefList);
    	
    	if(opRefList.size() != opRefSet.size()){
    		opRefList.clear();
    		opRefList.addAll(opRefSet);
    	}
    }
    
    /**
     * Internal class to compare OperationReferenceType
     * @author david.millares
     *
     */
    private class OperationReferenceComparator 
                                         implements 
                                             Comparator<OperationReferenceType>
    {	
    	public int compare(final OperationReferenceType opRef1,
    			           final OperationReferenceType opRef2)
    	{
    		final int operationCompare = opRef1.getOperation().compareTo( opRef2.getOperation() );
    		final int machineCompare   = opRef1.getMachine().compareTo( opRef2.getMachine() );
    		
    		return operationCompare + machineCompare;
    	}
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
        	automaton = AutomataSynchronizer.
        					synchronizeAutomata(supremicaProject,
        			                            synchronizationOptions);
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
    	if(null == moduleList || 0 == moduleList.size()){
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
            AutomataSynthesizer synthesizer;
            Automata result;
            
            synthesizer = new AutomataSynthesizer(supremicaProject,
            		                              synchronizationOptions,
            		                              synthesizerOptions);
            result = synthesizer.execute();
            
            if( null == result || !result.hasInitialState() ){
            	return null;
            } 
            
            supervisor = new ModularSupervisor(result);
        	
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
