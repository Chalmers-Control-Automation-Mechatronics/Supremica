package org.supremica.external.processeditor.processgraph.eopcell;

import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.event.MouseEvent;

import org.supremica.external.processeditor.xgraph.*;
import org.supremica.external.processeditor.processgraph.*;
import org.supremica.manufacturingTables.xsd.eop.*;


public class ExecutionOfOperationCell 
						extends 
							GraphCell
{
	EOP eop = null;
	
	public ExecutionOfOperationCell(){
		super("EOP");
		setLayout(null);
		
		setBorder(BorderFactory.createLineBorder(Color.black));
		
		eop = (new ObjectFactory()).createEOP();
	}
	
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() == 2){
	    	EOPInfoWindow ilInfoWin = new EOPInfoWindow(eop);
	    	ilInfoWin.setVisible(true);
		}else{
			super.mouseClicked(e);
		}
	}
}
