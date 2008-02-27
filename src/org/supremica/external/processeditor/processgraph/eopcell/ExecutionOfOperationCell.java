package org.supremica.external.processeditor.processgraph.eopcell;

import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.Dimension;
import java.awt.Point;

import org.supremica.external.processeditor.xgraph.*;
import org.supremica.external.processeditor.processgraph.*;
import org.supremica.manufacturingTables.xsd.eop.*;

import java.io.File;


public class ExecutionOfOperationCell 
						extends 
							GraphCell
{
	EOP eop = null;
	EOPInfoWindow eopInfoWin = null;
	File eopFile = null;
	
	private int x = 40;
	
	public ExecutionOfOperationCell(){
		super("EOP");
		setLayout(null);
		
		//setBorder(BorderFactory.createLineBorder(Color.black));
		
		eop = (new ObjectFactory()).createEOP();
		
		setSize(x,x);
	}
	
	public void paintComponent(Graphics g) {
		int diff = 4;
		int x,y;
		
		String txt = "EOP";
		
		Graphics2D g2 = (Graphics2D) g;
		
		g2.setColor(new Color(0,0,100,50));
		g2.fillOval(-diff, -diff, getHeight()+diff*2, getWidth()+diff*2);
		
		g2.setColor(Color.black);
		
		x = (getWidth() - g2.getFontMetrics().stringWidth(txt))/2;
		y = getHeight()/2 + g2.getFontMetrics().getHeight()/4;
		
		g2.drawString("EOP",x,y);
		
	} 
	
	public void setEOP(EOP eop){
		this.eop = eop;
	}
	
	public EOP getEOP(){
		return eop;
	}
	
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() == 2){
			
			if(null == eopInfoWin){
				eopInfoWin = new EOPInfoWindow(this);
			}else{
				eopInfoWin.setEOP(eop);
			}
			
			eopInfoWin.setVisible(true);
			
		}else{
			super.mouseClicked(e);
		}
	}
}
