

package org.jgrafchart;

import com.nwoods.jgo.*;
import java.awt.*;
import java.util.*;

public class DigitalOut1 extends DigitalOut  {

  public JGoEllipse myCircle = null;

  public DigitalOut1() {
    super();
  }

  public DigitalOut1(Point loc, String labeltext) {
    super();
    setSize(80,60);
    setSelectable(true);
    setGrabChildSelection(false);
    setDraggable(true);
    setResizable(false);
    val = true;

    myBorder = new JGoStroke();
    myBorder.setPen(new JGoPen(JGoPen.SOLID,2,new Color(0.0F,0.0F,0.0F)));
    myBorder.addPoint(0,0);
    myBorder.addPoint(60,0);
    myBorder.addPoint(80,30);
    myBorder.addPoint(60,60);
    myBorder.addPoint(0,60);
    myBorder.addPoint(0,0);
    myBorder.setSelectable(false);
    myBorder.setDraggable(false);
    myBorder.setPen(redPen);


    setLocation(loc);

    digitalOutputCounter++;
    myOuttext =  new JGoText("DOut"+digitalOutputCounter);
    myOuttext.setSelectable(true);
    myOuttext.setEditable(true);
    myOuttext.setEditOnSingleClick(true);
    myOuttext.setDraggable(false);
    myOuttext.setAlignment(JGoText.ALIGN_LEFT);
    myOuttext.setTransparent(true);
    myChanFixed = new JGoText("Chan:");
    myChanFixed.setSelectable(false);
    myChanFixed.setEditable(false);
    myChanFixed.setDraggable(false);
    myChanFixed.setAlignment(JGoText.ALIGN_LEFT);
    myChanFixed.setTransparent(true);
    myChannel =  new JGoText(""+digitalOutputCounter);
    myChannel.setSelectable(true);
    myChannel.setEditable(true);
    myChannel.setEditOnSingleClick(true);
    myChannel.setDraggable(false);
    myChannel.setAlignment(JGoText.ALIGN_LEFT);
    myChannel.setTransparent(true);
    myValue = new JGoText(labeltext);
    myValue.setSelectable(false);
    myValue.setEditable(false);
    myValue.setDraggable(false);
    myValue.setAlignment(JGoText.ALIGN_LEFT);
    myValue.setTransparent(true);
    myCircle = new JGoEllipse();
    myCircle.setSize(6,6);
    myCircle.setSelectable(false);
    myCircle.setDraggable(false);
    myCircle.setPen(greenPen);
    addObjectAtHead(myBorder);
    addObjectAtTail(myOuttext);
    addObjectAtTail(myChannel);
    addObjectAtTail(myChanFixed);
    addObjectAtTail(myValue);
    addObjectAtTail(myCircle);
    layoutChildren();
  }



  public JGoObject copyObject(JGoCopyEnvironment env)
  {
    DigitalOut1 newobj = (DigitalOut1)super.copyObject(env); 
    return newobj;
  }

  public void copyChildren(JGoArea newarea,JGoCopyEnvironment env) {

    DigitalOut1 newobj = (DigitalOut1)newarea;
    if (myBorder != null) {
      newobj.myBorder = (JGoStroke)myBorder.copyObject(env);
      newobj.addObjectAtHead(newobj.myBorder);
    }
    if (myOuttext != null) {
      newobj.myOuttext = (JGoText)myOuttext.copyObject(env);
      digitalOutputCounter++;
      newobj.myOuttext.setText("DOut"+digitalOutputCounter);
      newobj.addObjectAtTail(newobj.myOuttext);
    }
    if (myChannel != null) {
      newobj.myChannel = (JGoText)myChannel.copyObject(env);
      newobj.myChannel.setText(""+digitalOutputCounter);
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
    if (myCircle != null) {
      newobj.myCircle = (JGoEllipse)myCircle.copyObject(env);
      newobj.addObjectAtTail(newobj.myCircle);
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
    if (myOuttext != null) {
      Point p = myBorder.getSpotLocation(BottomCenter);
      myOuttext.setSpotLocation(TopCenter,(int)p.getX() - 5,(int)p.getY() + 7);}
    if (myChanFixed != null) {
      myChanFixed.setSpotLocation(TopRight, myBorder, TopCenter);
    }
    if (myChannel != null) {
      myChannel.setSpotLocation(LeftCenter, myChanFixed, RightCenter);}
    if (myValue != null) {
      myValue.setSpotLocation(Center, myBorder, Center);}
    if (myCircle != null) {
      myCircle.setSpotLocation(LeftCenter, myBorder, RightCenter); }
  }



  public void setStoredBoolAction(boolean newval) {
    val = newval;
    if (newval) {

        myValue.setText("1");
        myBorder.setPen(greenPen);
	myCircle.setPen(redPen);

      if (!((GCDocument)getDocument()).isSimulating()) {
	digOut.set(false);
//  	System.out.println("Digout 1");
        }
    } else {

        myValue.setText("0");
        myBorder.setPen(redPen);
	myCircle.setPen(greenPen);
      if  (!((GCDocument)getDocument()).isSimulating()) {
	digOut.set(true);
//	System.out.println("DigOut 0");
      }
    }
//    System.out.println("DigitalIn.setStoredAction");
//    System.out.println(newval);
    layoutChildren();
  }







    public void effectuateNormalActions() {
//      System.out.println("DigitalOut.effectuateNormalActions");
      if (setLow && !setHigh) {

        myValue.setText("0");
	myBorder.setPen(redPen);
	myCircle.setPen(greenPen);
	if (!((GCDocument)getDocument()).isSimulating()) {
	  digOut.set(true);
//	  System.out.println("DigOut 1");
	}
	val = false;
	layoutChildren();
      } else {
	if (setHigh) {

	  myValue.setText("1");
	  myBorder.setPen(greenPen);
	  myCircle.setPen(redPen);
	  if (!((GCDocument)getDocument()).isSimulating()) { 
	    digOut.set(false);
//	    System.out.println("DigOut 0");
	  }
	  val = true;
	  layoutChildren();
	}
      }
      setLow = false;
      setHigh = false;
    }

}
