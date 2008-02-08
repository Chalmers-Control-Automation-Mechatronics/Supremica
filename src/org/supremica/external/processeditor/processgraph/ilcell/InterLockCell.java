package org.supremica.external.processeditor.processgraph.ilcell;

import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.event.MouseEvent;

import org.supremica.external.processeditor.xgraph.*;
import org.supremica.external.processeditor.processgraph.*;
import org.supremica.manufacturingTables.xsd.il.IL;
import org.supremica.manufacturingTables.xsd.il.ObjectFactory;


public class InterLockCell 
						extends 
							GraphCell
{
	IL il = null;
	
	public InterLockCell(){
		super("IL");
		setLayout(null);
		setBorder(BorderFactory.createLineBorder(Color.black));
		il = (new ObjectFactory()).createIL();
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
