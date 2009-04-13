package org.supremica.external.processeditor.tools.copextractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.swing.SwingWorker;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.supremica.external.avocades.COPBuilder;

class RelationExtractionWorker
                           extends
                                SwingWorker<List<Document>, Void>
{

	private COPBuilder builder;
	private File directory;

	//constructors
	RelationExtractionWorker(COPBuilder builder, File directory){
		this.builder = builder;
		this.directory = directory;
	}

	@Override
	public List<Document> doInBackground() {
		
		List<Document> list;
		
		System.out.println( "Relation extraction ..." );
		list = builder.getRelationExtractionOutput(); 
		System.out.println( "Done: Relation extraction" );

		return list;
	}

	@Override
	public void done() {
		
		List<Document> copList = null;

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
		if (null == copList || 0 == copList.size() ){
			System.out.println( "No COP:s" );
			return;
		}
		
		saveToFolder(copList);
	}
	
	private void saveToFolder(List<Document> ropList){
		File file = null;
		String fileName = "";
		String path = "";
		
		//Sanity check
		if(null == directory || !directory.isDirectory() ){
			return;
		}
		
		path = directory.getPath();
		
		for(int i = 0; i < ropList.size(); i++){	
    		fileName = "cop_" + i + ".xml";
    		file = new File(path + File.separator +  fileName);
    		saveDocument(ropList.get(i), file);
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
	
	
	
	
	
	
	
}
