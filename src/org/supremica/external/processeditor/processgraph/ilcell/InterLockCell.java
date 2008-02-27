package org.supremica.external.processeditor.processgraph.ilcell;

import javax.swing.BorderFactory;

import java.awt.Color;
import java.awt.event.MouseEvent;

import org.supremica.external.processeditor.xgraph.*;
import org.supremica.manufacturingTables.xsd.il.IL;
import org.supremica.manufacturingTables.xsd.il.ObjectFactory;


public class InterLockCell 
						extends 
							GraphCell 
{
	private IL il = null;
	
	public InterLockCell(){
		super("IL");
		il = (new ObjectFactory()).createIL();
		setBorder(BorderFactory.createLineBorder(Color.red));
	}     
	
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() == 2){
	    	ILInfoWindow ilInfoWin = new ILInfoWindow(il);
	    	ilInfoWin.setVisible(true);
		}else{
			super.mouseClicked(e);
		}
	}
}
