package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base;

import java.lang.*;
import java.awt.*;
import java.awt.font.TextAttribute;

import javax.swing.*;
import javax.swing.border.*;

import java.nio.*;

public class InfoPanel extends JPanel{
    
    private static Font textFont = null;
    private static Font opFont = null;
    
    private String text = "";
    final static int PADDING = 4;
    
    public int text_hgt = 0;
    public int text_adv = 0;
    
    private Dimension size = new Dimension(1,1);
    private InfoPanelListener infoPanelListener = null;
    
    //input safe characters
    //operands.length must be 6
    String[] operands = new String[]{"->", "+","--","*","<",">"};
    
    //special characters for PPN
    private static char seq = '\u2192'; //RIGHTWARDS ARROW
    private static char alt = '\u002B'; //PLUS SIGN
    private static char par = '\u2294'; //SQUARE CUP
    private static char arb = '\u2295'; //CIRCLED PLUS
    
    private static char sta = '\u2191'; //RIGHTWARDS ARROW
    private static char sto = '\u2193';  //DOWNWARDS ARROW
    
    public InfoPanel(){
		super();
		this.setLayout(null);
		
		initalizeFonts();
		
        setText(text);
        setBounds(0,0,size.height, size.width);
    }
    
    private static void initalizeFonts(){
    	
    	if(opFont == null){
    		//find a font that can display special char
    		GraphicsEnvironment ge = GraphicsEnvironment.
    		getLocalGraphicsEnvironment();
		
    		Font[] fonts = ge.getAllFonts();
		
    		for(int i =0; i< fonts.length; i++){
    			if(fonts[i].canDisplayUpTo(""+seq) == -1 &&
    			   fonts[i].canDisplayUpTo(""+alt) == -1 &&
    			   fonts[i].canDisplayUpTo(""+par) == -1 &&
    			   fonts[i].canDisplayUpTo(""+arb) == -1 &&
    			   fonts[i].canDisplayUpTo(""+sta) == -1 &&
    			   fonts[i].canDisplayUpTo(""+sto) == -1){
    				opFont = new Font(fonts[i].getFontName(), Font.PLAIN, 15);
    			}
    		}
		
    		//if no font found use 
    		if(opFont == null){
    			opFont = new Font("Serif", Font.PLAIN, 12);
        
    		}
    	}
    	
    	
		if(textFont == null){
			textFont = new Font("Serif", Font.PLAIN, 12);
    	}
        
    }
    
    public static void setTextFont(Font newFont){
    	textFont = newFont;
    }
    
    public static void setOpFont(Font newFont){
    	opFont = newFont;
    }
    
	public InfoPanel(String text){
		this();
		setText(text);
	}
    
    public void setText(String nm){
		if(!text.equals(nm)){
			text = nm;
        	repaint();
			textChanged();
		}
    }
    
	public String getText(){
        return text;
    }
    public void addInfoPanelListener(InfoPanelListener l){
        infoPanelListener = l;
    }
    
    public void paintComponent(Graphics g) {
    	
        String exp = text;
        String tmp;
        
        int x = PADDING/2;
        int y = (getSize().height-PADDING+text_hgt)/2; 
        
        setSize(evaluateSize(g));
        
        g.setColor(Color.BLACK);
        
        while(exp.length() > 0){
        	
        	//draw special operand
        	tmp = getOperand(exp);
        	if(tmp.length() > 0){
        		exp = exp.substring(tmp.length());
        		
        		//change to special char
            	if(tmp.equals(operands[0])){
            		tmp = ""+seq;
            	}else if(tmp.equals(operands[1])){
            		tmp = ""+alt;
            	}else if(tmp.equals(operands[2])){
            		tmp = ""+par;
            	}else if(tmp.equals(operands[3])){
            		tmp = ""+arb;
            	}else if(tmp.equals(operands[4])){
            		tmp = ""+sta;
            	}else if(tmp.equals(operands[5])){
            		tmp = ""+sto;
            	}
            	
            	g.setFont(opFont);
            	g.drawString(tmp,x,y);
            	
            	//advance
            	x = x + g.getFontMetrics(opFont).stringWidth(tmp);
        		
        	}else{
        		//draw text
        		tmp = getText(exp);
        		if(tmp.length() > 0){
        			exp = exp.substring(tmp.length());
        		
        			g.setFont(textFont);
        			g.drawString(tmp,x,y);
            	
        			//advance
        			x = x + g.getFontMetrics(textFont).stringWidth(tmp);
        		}
        	}
        }
    }
    
    protected Dimension evaluateSize(Graphics g){
    	
    	String exp = text;
        String tmp;
    	
        // get metrics from the graphics
        FontMetrics metricsText = g.getFontMetrics(textFont);
        FontMetrics metricsOp = g.getFontMetrics(opFont);
        
        // get the height of a line of text in this font and render context
        if(metricsText.getHeight() > metricsOp.getHeight()){
        	text_hgt = metricsText.getHeight();
        }else{
        	text_hgt = metricsOp.getHeight();
        }
        
        // get the advance of my text in this font and render context
        text_adv = 0;
       
        
        while(exp.length() > 0){
        	
        	//draw text
        	tmp = getText(exp);
        	if(tmp.length() > 0){
        		exp = exp.substring(tmp.length());
        	}
        	
        	//advance
        	text_adv = text_adv + metricsText.stringWidth(tmp);
        	
        	//draw special operand
        	tmp = getOperand(exp);
        	if(tmp.length() > 0){
        		exp = exp.substring(tmp.length());
        	}
        	
        	//change to special char
        	if(tmp.equals(operands[0])){
        		tmp = ""+seq;
        	}else if(tmp.equals(operands[1])){
        		tmp = ""+alt;
        	}else if(tmp.equals(operands[2])){
        		tmp = ""+par;
        	}else if(tmp.equals(operands[3])){
        		tmp = ""+arb;
        	}else if(tmp.equals(operands[4])){
        		tmp = ""+sta;
        	}else if(tmp.equals(operands[5])){
        		tmp = ""+sto;
        	}
        	
        	text_adv = text_adv + metricsOp.stringWidth(tmp) + 1 ;
        }
        
        // calculate the size of a box to hold the text with some padding.
        size = new Dimension(text_adv+PADDING, text_hgt+PADDING);
        
        return size;
    }
	
	public void setSize(Dimension size){
		if(!size.equals(getSize())){
            super.setSize(size);
            sizeChanged();
        }
	}
	
	protected void sizeChanged(){
		if(infoPanelListener != null){
			infoPanelListener.sizeChanged();
		}
	}
    
    protected void textChanged(){
		if(infoPanelListener != null){
			infoPanelListener.textChanged();
		}
	}
    
    
    
    
    
    
    private String getText(String exp){
    	
    	String tmp = "";
    	
    	if(!containOperand(exp)){
    		return exp;
    	}
    	
    	//find operand
    	while(exp.length() > 0){
			//add first char to tmp
			tmp = tmp.concat(exp.substring(0,1));
			
			//remove first char from exp
			exp = exp.substring(1);
			
			//check if we have a operation
			for(int i = 0; i < operands.length; i++){
				if(exp.startsWith(operands[i])){
					return tmp;
				}
			}
		}
    	
    	//something is wrong
    	return "";
    }
    
    private String getOperand(String exp){
    	
    	if(exp == null){
    		return "";
    	}
    	
    	//first must be operand
    	for(int i = 0; i < operands.length; i++){
    		if(exp.startsWith(operands[i])){
    			return operands[i];
			}
		}
    	
    	//something is wrong
    	return "";
    }
    
    
    private boolean containOperand(String exp){
    	
    	if(exp == null){
    		return false;
    	}
    	
    	for(int i= 0; i < operands.length; i++){
    		if(exp.contains(operands[i])){
    			return true;
    		}
    		
    	}
    	return false;
    }
    
}


  
