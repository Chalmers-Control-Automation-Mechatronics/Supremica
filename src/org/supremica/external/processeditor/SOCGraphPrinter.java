package org.supremica.external.processeditor;

import java.awt.*;
import java.awt.print.*;
import javax.swing.*;
import java.lang.Math.*;

/**
 * Prints out graphs and selected objects.
 */
public class SOCGraphPrinter implements Printable, Pageable
{

    private final double INCH = 72;   
    private JPanel panel;
    private PageFormat mPageFormat;
    private double xPrintSize , yPrintSize;    

    // Number between 0 and 1 the scales the pic between full pagesize and zero
    private double scaleFactor = 0.25;           
    
    private int numPages; // How many pages in the document
    
    private double printX, printY; // coordinates of upper-left of print area
    
    private double printWidth; // Width of the printable area
    
    private double printHeight; // Height of the printable area
    
    //Rectangle drawRect; // The rectangle in which the document is painted

    /**
     * Prints the panel.    
     * 
     * @param p the panel to print
     */
    public SOCGraphPrinter(JPanel p) 
    {
	this(p, new PageFormat());
    }
    /**
     * Prints the panel with specified page format.    
     * 
     * @param p the panel to print
     * @param m describing the size and orientation
     */
    public SOCGraphPrinter(JPanel p, PageFormat m) {	
	this.panel = p; 

	PrinterJob printJob = PrinterJob.getPrinterJob();	
	if(m == null){
	    this.mPageFormat = printJob.defaultPage();		   	
	}
	else{
	    this.mPageFormat = m;
	}  
	this.printX = this.mPageFormat.getImageableX();
	this.printY = this.mPageFormat.getImageableY();
	this.printWidth = this.mPageFormat.getImageableWidth();
	this.printHeight = this.mPageFormat.getImageableHeight();  
	this.numPages = this.getNumberOfPages();	
  
	printJob.setPrintable(this,mPageFormat);
	printJob.setPageable(this);	
    
	if (printJob.printDialog()) { 	
	    try {
		printJob.print();
		System.out.println("SOCGraphPrinter try printjob.print()");
	    } 
	    catch (Exception PrintException) {
		PrintException.printStackTrace();
	    }
	}
    }    
    /**
     * Prints the page at the specified index into the specified 
     * <code>Graphics</code> context in the specified format.
     * 
     * @param g the context into which the page is drawn
     * @param pageFormat the size and orientation of the page being drawn
     * @param pageIndex the zero based index of the page to be drawn
     */
    public int print(Graphics g, PageFormat pageFormat, int pageIndex)throws PrinterException 
    {
	if(pageIndex > this.getNumberOfPages())
	    {return Printable.NO_SUCH_PAGE;}
	
	disableDoubleBuffering(panel); 

	Graphics2D g2 = (Graphics2D) g;
	//  shift Graphic to line up with beginning of print-imageable region  
	g2.translate( mPageFormat.getImageableX(), 
		      mPageFormat.getImageableY());	
	//  shift Graphic to line up with beginning of next page to print 
	g2.translate(0f, -pageIndex * this.printHeight); 
	//  scale the page so the width fits... 
	double scale = this.printWidth / panel.getSize().width;      
	g2.scale(scale, scale); 
 
	panel.paint(g2);  

	enableDoubleBuffering(panel);

	System.out.println("GraphPrint pageIndex  "+ pageIndex);
	
	return Printable.PAGE_EXISTS;	
    }
    /**
     * Returns the number of pages in the set.
     *
     * @return the number of pages
     */
    public int getNumberOfPages() 
    {
	return ((int)Math.ceil(((JPanel)this.panel).getSize().height/this.printHeight));
    }
    /**
     * Returns the <code>Printable</code> instance responsible for rendering
     * the page specified by <code>pageIndex</code>.
     *
     * @param pagenum the zero based index of the page whose
     * <code>Printable</code> is being requested
     * @return the <code>Printable</code> that renders the page
     */
    public Printable getPrintable(int pagenum) {
	return this;
    }
    /**
     * Returns the <code>PageFormat</code> of the page specified by 
     * <code>pageIndex</code>.
     * 
     * @param pagenum the zero based index of the page whose
     * <code>PageFormat</code> is being requested
     * @return describing the size and orientation
     */
    public PageFormat getPageFormat(int pagenum) {
	return mPageFormat;
    }
    public static void disableDoubleBuffering(Component c) {
	RepaintManager currentManager = RepaintManager.currentManager(c);
	currentManager.setDoubleBufferingEnabled(false);
    }        
    public static void enableDoubleBuffering(Component c) {
	RepaintManager currentManager = RepaintManager.currentManager(c);
	currentManager.setDoubleBufferingEnabled(true);
    }
}	
