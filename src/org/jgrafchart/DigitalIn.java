
package org.jgrafchart;

import com.nwoods.jgo.*;
import java.awt.*;
import java.util.*;
import se.lth.control.realtime.*;

/**
 * A DigitalIn is an Area containing a JGoStroke and two text labels
 */
public class DigitalIn extends JGoArea implements Readable {

  static protected int digitalInputCounter = 0;
  protected JGoStroke myBorder = null;
  public JGoText myIntext = null;
  public JGoText myChanFixed = null;
  protected JGoText myChannel = null;
  protected int channel = -1;
  protected JGoText myValue = null;
  public boolean oldval = false;
  public boolean val = false;
  static Color red = new Color(1.0f,0f,0f);
  static Color green = new Color(0f,1.0f,0f);
  static JGoBrush redSolidBrush = new JGoBrush(JGoBrush.SOLID, red);
  static JGoBrush greenSolidBrush = new JGoBrush(JGoBrush.SOLID, green);
  static JGoBrush noFill = new JGoBrush();
  static JGoPen redPen = new JGoPen(JGoPen.SOLID,2,red);
  static JGoPen greenPen = new JGoPen(JGoPen.SOLID,2,green);
  static JGoPen standardPen = new JGoPen(JGoPen.SOLID,2,new Color(0.0F,0.0F,0.0F));
  public se.lth.control.realtime.DigitalIn digIn = null;

  public DigitalIn() {
    super();
  }

  public DigitalIn(Point loc) {

    super();
    setSize(80,60);
    setSelectable(true);
    setGrabChildSelection(false);
    setDraggable(true);
    setResizable(false);

    myBorder = new JGoStroke();
    myBorder.setPen(new JGoPen(JGoPen.SOLID,2,new Color(0.0F,0.0F,0.0F)));
    myBorder.addPoint(0,0);
    myBorder.addPoint(80,0);
    myBorder.addPoint(80,60);
    myBorder.addPoint(0,60);
    myBorder.addPoint(20,30);
    myBorder.addPoint(0,0);
    myBorder.setSelectable(false);
    myBorder.setDraggable(false);
    myBorder.setPen(redPen);


    setLocation(loc);

    digitalInputCounter++;
    myIntext =  new JGoText("DIn"+digitalInputCounter);
    myIntext.setSelectable(true);
    myIntext.setEditable(true);
    myIntext.setEditOnSingleClick(true);
    myIntext.setDraggable(false);
    myIntext.setAlignment(JGoText.ALIGN_LEFT);
    myIntext.setTransparent(true);
    myChanFixed = new JGoText("Chan:");
    myChanFixed.setSelectable(false);
    myChanFixed.setEditable(false);
    myChanFixed.setDraggable(false);
    myChanFixed.setAlignment(JGoText.ALIGN_LEFT);
    myChanFixed.setTransparent(true);
    myChannel =  new JGoText(""+digitalInputCounter);
    myChannel.setSelectable(true);
    myChannel.setEditable(true);
    myChannel.setEditOnSingleClick(true);
    myChannel.setDraggable(false);
    myChannel.setAlignment(JGoText.ALIGN_LEFT);
    myChannel.setTransparent(true);
    myValue = new JGoText("0");
    myValue.setSelectable(true);
    myValue.setEditable(true);
    myValue.setEditOnSingleClick(true);
    myValue.setDraggable(false);
    myValue.setAlignment(JGoText.ALIGN_LEFT);
    myValue.setTransparent(true);

    addObjectAtHead(myBorder);
    addObjectAtTail(myIntext);
    addObjectAtTail(myChannel);
    addObjectAtTail(myChanFixed);
    addObjectAtTail(myValue);
    layoutChildren();
  }

  public JGoObject copyObject(JGoCopyEnvironment env)
  {
    DigitalIn newobj = (DigitalIn)super.copyObject(env); 
    return newobj;
  }
   
  public void copyChildren(JGoArea newarea,JGoCopyEnvironment env) {

    DigitalIn newobj = (DigitalIn)newarea;
    if (myBorder != null) {
      newobj.myBorder = (JGoStroke)myBorder.copyObject(env);
      newobj.addObjectAtHead(newobj.myBorder);
    }
    if (myIntext != null) {
      newobj.myIntext = (JGoText)myIntext.copyObject(env);
      digitalInputCounter++;
      newobj.myIntext.setText("DIn"+digitalInputCounter);
      newobj.addObjectAtTail(newobj.myIntext);
    }
    if (myChannel != null) {
      newobj.myChannel = (JGoText)myChannel.copyObject(env);
      newobj.myChannel.setText(""+digitalInputCounter);
      newobj.addObjectAtTail(newobj.myChannel);
    }
    if (myChanFixed != null) {
      newobj.myChanFixed = (JGoText)myChanFixed.copyObject(env);
      newobj.addObjectAtTail(newobj.myChanFixed);
    }
    if (myValue != null) {
      newobj.myValue = (JGoText)myValue.copyObject(env);
      newobj.addObjectAtTail(newobj.myValue);
    }
  }

  public Point getLocation(Point p)
  {
      return getSpotLocation(Center, p);
  }

  public void setLocation(int x, int y) {

     myBorder.setSpotLocation(Center, x, y); 
     layoutChildren();
  }

  public void layoutChildren() {

    if (myBorder == null) return;
    if (myIntext != null) {
      Point p = myBorder.getSpotLocation(BottomCenter);
      myIntext.setSpotLocation(TopCenter,(int)p.getX(),(int)p.getY() + 7);
    }
    if (myChanFixed != null) {
      myChanFixed.setSpotLocation(TopCenter, myBorder, TopCenter);
    }
    if (myChannel != null) {
      myChannel.setSpotLocation(LeftCenter, myChanFixed, RightCenter);}
    if (myValue != null) {
      myValue.setSpotLocation(Center, myBorder, Center);}
  }

  public void geometryChange(Rectangle prevRect)
  {
    // see if this is just a move and not a scale
    if ((prevRect.width == getWidth()) &&
        (prevRect.height == getHeight())) {
      // let the default JGoArea implementation do the work
      super.geometryChange(prevRect);
    } 
    layoutChildren();
  }

  public void compile() {
    if (!((GCDocument)getDocument()).isSimulating()) {
      int newChan =  Integer.parseInt(myChannel.getText());
      if ((channel != newChan) || (digIn == null)) {
	channel = newChan;
	try {
	  digIn = new se.lth.control.realtime.DigitalIn(channel);
	} catch (Exception x) {
	  System.out.println(x.getMessage());
	  x.printStackTrace();}
      }
    }
  }


  public void readInput() {
    oldval = val;
    if (((GCDocument)getDocument()).isSimulating()) {
    String s = myValue.getText();
    if (s.compareTo("0") == 0) {
     val = false;
     if (oldval) {myBorder.setPen(redPen);}
    } else {
     val = true;
     if (!oldval) {myBorder.setPen(greenPen);}
    }
    } else {
       val = digIn.get();
//       System.out.println("Digin "+ val);
       if (!val) {myBorder.setPen(redPen);myValue.setText("0");}
       else {myBorder.setPen(greenPen);myValue.setText("1");}
    }
  }

  public void initialize() {
    compile();
    if (((GCDocument)getDocument()).isSimulating()) {
    String s = myValue.getText();
    if (s.compareTo("0") == 0) {
     val = false;
     myBorder.setPen(redPen);
    } else {
     val = true;
     myBorder.setPen(greenPen);
    }
    oldval = val;
    } else {
       val = digIn.get();
       if (!val) {myBorder.setPen(redPen);myValue.setText("0");}
       else {myBorder.setPen(greenPen);myValue.setText("1");}
       oldval = val;
    }
  }

  public void stop() {
//    myBorder.setPen(standardPen);
  }

  public boolean doMouseDblClick(int mod, java.awt.Point dc, java.awt.Point vc,JGoView view) {
    String s = myValue.getText();
    if (s.compareTo("0") == 0) {
      myValue.setText("1");
    } else {myValue.setText("0");}
    return true;
  }

  public String getName() {return myIntext.getText();}
  public boolean getBoolVal(){return val;}
  public boolean getOldBoolVal(){return oldval;}
  public int getIntVal(){return 0;}
  public int getOldIntVal(){return 0;}
  public String getStringVal() {return new String("");}
  public String getOldStringVal() {return new String("");}

}
