package org.supremica.external.processeditor.tools.copextractor;

import java.util.List;

import javax.swing.SwingWorker;

import org.supremica.external.avocades.COPBuilder;
import org.supremica.external.processeditor.SOCGraphContainer;
import org.supremica.manufacturingTables.xsd.processeditor.ROP;

class RelationExtractionWorker
                           extends
                                SwingWorker<List<ROP>, Void>
{

	private COPBuilder builder;
	private SOCGraphContainer container;

	//constructors
	RelationExtractionWorker(COPBuilder builder, SOCGraphContainer container){
		this.builder = builder;
		this.container = container;
	}

	@Override
	public List<ROP> doInBackground() {
		
		List<ROP> list;
		
		System.out.println("Relation extraction ...");
		list = builder.getRelationExtractionOutput(); 
		System.out.println("Done: Relation extraction");

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
			
			System.err.println("Error: " + why);
		}


		if(null == copList || 0 == copList.size() ){
			System.out.println("No COP:s");
			return;
		}
		
		if ( null != container ){

			for(ROP rop : copList){
				container.insertResource(rop, null);
			}

		} else {
			System.out.println("No SOC");
		}
	}
}
