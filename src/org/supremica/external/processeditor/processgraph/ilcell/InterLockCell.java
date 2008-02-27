package org.supremica.external.processeditor.processgraph.ilcell;

import javax.swing.BorderFactory;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.io.File;

import org.supremica.external.processeditor.processgraph.eopcell.EOPInfoWindow;
import org.supremica.external.processeditor.xgraph.*;
import org.supremica.manufacturingTables.xsd.eop.EOP;
import org.supremica.manufacturingTables.xsd.il.IL;
import org.supremica.manufacturingTables.xsd.il.ObjectFactory;


public class InterLockCell 
						extends 
							GraphCell 
{
	private IL il = null;
	private ILInfoWindow ilInfoWin = null;
	private File ilFile = null;
	
	private int x = 40;
	
	public InterLockCell(){
		super("IL");
		il = (new ObjectFactory()).createIL();
		//setBorder(BorderFactory.createLineBorder(Color.red));
		this.setSize(x,x);
	}     
	
	public void paintComponent(Graphics g) {
		int diff = 4;
		int x,y;
		
		String txt = "IL";
		
		Graphics2D g2 = (Graphics2D) g;
		
		g2.setColor(new Color(100,0,0,50));
		g2.fillOval(-diff, -diff, getHeight()+diff*2, getWidth()+diff*2);
		
		g2.setColor(Color.black);
		
		x = (getWidth() - g2.getFontMetrics().stringWidth(txt))/2;
		y = getHeight()/2 + g2.getFontMetrics().getHeight()/4;
		
		g2.drawString(txt,x,y);
		
	}
	
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() == 2){
			
			if(null == ilInfoWin){
				ilInfoWin = new ILInfoWindow(this);
			}else{
				ilInfoWin.setIL(il);
			}
	    	ilInfoWin.setVisible(true);
		}else{
			super.mouseClicked(e);
		}
	}
	
	public IL getIL(){
		return il;
	}
	
	public void setIL(IL il){
		this.il = il;
	}
}
