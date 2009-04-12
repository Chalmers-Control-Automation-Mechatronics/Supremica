package org.supremica.external.processeditor.tools.copextractor;

import java.io.File;
import java.util.List;

import javax.swing.SwingWorker;

import org.supremica.external.avocades.COPBuilder;
import org.supremica.external.processeditor.SOCGraphContainer;
import org.supremica.external.processeditor.xml.Loader;
import org.supremica.manufacturingTables.xsd.processeditor.ROP;

class RelationExtractionWorker
                           extends
                                SwingWorker<List<ROP>, Void>
{

	private COPBuilder builder;
	private SOCGraphContainer container;
	private Loader loader;

	//constructors
	RelationExtractionWorker(COPBuilder builder, SOCGraphContainer container){
		this.loader = new Loader();
		
		this.builder = builder;
		this.container = container;
	}

	@Override
	public List<ROP> doInBackground() {
		
		List<ROP> list;
		
		System.out.println( "Relation extraction ..." );
		list = builder.getRelationExtractionOutput(); 
		System.out.println( "Done: Relation extraction" );

		return list;
	}

	@Override
	public void done() {
		
		List<ROP> copList = null;

		try {
			copList = get();
		} catch (InterruptedException ignore) {
			
		} catch (java.util.concurrent.ExecutionException e) {
			String why = null;
			Throwable cause = e.getCause();
			
			if (cause != null) {
				why = cause.getMessage();
			} else {
				why = e.getMessage();
			}
			
			System.err.println( "Error: " + why );
		}


		//Sanity check
		if(null == copList || 0 == copList.size() ){
			System.out.println( "No COP:s" );
			return;
		}
		
		if ( null != container ){
			saveToFolder(copList);
			openInProcessEditor(copList);
		} else {
			System.out.println( "No SOC" );
		}
	}
	
	private void openInProcessEditor(List<ROP> list){
		
		//Sanity check
		if ( null == container || null == list ){
			return;
		}
		
		for(ROP rop : list){
			container.insertResource(rop, null);
		}
		
		container.setVisible(true);
	}
	
	
	private void saveToFolder(List<ROP> ropList){
		File file = null;
		String fileName = "";
		String PATH = "";
		
		for(int i = 0; i < ropList.size(); i++){	
    		fileName = "rop_" + i + ".xml";
    		file = new File(PATH + fileName);
    		loader.save(ropList.get(i), file);
    	}
	}
	
	
	
	
}
