/*
 *Test of JGo for Grafchart.
 *
 */
package org.jgrafchart;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import com.nwoods.jgo.*;



public class GCPalette extends JGoView {

  public GCPalette()
  {
    super();
    setBorder(new TitledBorder("Palette"));
    getDocument().setModifiable(false);
    setHorizontalScrollBar(null);
//    setVerticalScrollBar(null);
//    setAutoscrolls(false);
    setSize(250,500);
  }



  public GCPalette(JGoDocument doc) {
    super(doc);
    setBorder(new TitledBorder("Palette"));
    getDocument().setModifiable(false);
    setHorizontalScrollBar(null);
//    setVerticalScrollBar(null);
//    setAutoscrolls(false);
    setSize(250,500);
  }

  public JGoDocument getDoc() {
    return (JGoDocument)getDocument();
  }

  // limit the dimensions of this palette window
  public Dimension getPreferredSize() { 
      return new Dimension(130,400); 
  }

  public Dimension getMinimumSize() { 
      return new Dimension(130, 400); 
  }

//  public void autoscroll(Point location)
//  {
    // don't do anything
//  }
}

