package org.jgrafchart;

import com.nwoods.jgo.*;
import java.awt.*;
import java.util.*;

public class IntegerVariable extends InternalVariable {

  private JGoText myTag = null;
  public int val = 0;
  public int oldval = 0;

  public IntegerVariable() {
    super();
  }

  public IntegerVariable(Point loc) {

    super();
    setSize(65,45);
    setSelectable(true);
    setGrabChildSelection(false);
    setDraggable(true);
    setResizable(false);

    myBorder = new JGoRectangle(getTopLeft(), getSize());
    myBorder.setPen(new JGoPen(JGoPen.SOLID,2,new Color(0.0F,0.0F,0.0F)));
    myBorder.setSelectable(false);
    myBorder.setDraggable(false);

    setLocation(loc);

    myName =  new JGoText("Var");
    myName.setSelectable(true);
    myName.setEditable(true);
    myName.setEditOnSingleClick(true);
    myName.setDraggable(false);
    myName.setAlignment(JGoText.ALIGN_CENTER);
    myName.setTransparent(true);

    myValue =  new JGoText("" + val);
    myValue.setSelectable(true);
    myValue.setEditable(true);
    myValue.setEditOnSingleClick(true);
    myValue.setDraggable(false);
    myValue.setAlignment(JGoText.ALIGN_LEFT);
    myValue.setTransparent(true);
    myTag = new JGoText("Int ");
    myTag.setSelectable(false);
    myTag.setEditable(false);
    myTag.setEditOnSingleClick(false);
    myTag.setDraggable(false);
    myTag.setTransparent(true);
    myValue.setAlignment(JGoText.ALIGN_LEFT);
    addObjectAtHead(myBorder);
    addObjectAtTail(myName);
    addObjectAtTail(myValue);

    addObjectAtTail(myTag);
    layoutChildren();
  }

  public JGoObject copyObject(JGoCopyEnvironment env)
  {
    IntegerVariable newobj = (IntegerVariable)super.copyObject(env); 
    return newobj;
  }

  public void copyChildren(JGoArea newarea,JGoCopyEnvironment env) {

   IntegerVariable  newobj = (IntegerVariable)newarea;
   if (myBorder != null) {
      newobj.myBorder = (JGoRectangle)myBorder.copyObject(env);
      newobj.addObjectAtHead(newobj.myBorder);
    }
   if (myName != null) {
      newobj.myName = (JGoText)myName.copyObject(env);
      newobj.addObjectAtTail(newobj.myName);
    }    
   if (myValue != null) {
      newobj.myValue = (JGoText)myValue.copyObject(env);
      newobj.addObjectAtTail(newobj.myValue);
    }
   if (myTag != null) {
      newobj.myTag = (JGoText)myTag.copyObject(env);
      newobj.addObjectAtTail(newobj.myTag);
    }
  }

  public void layoutChildren() {

    if (myBorder == null) return;
    if (myName != null) {
      myName.setSpotLocation(TopCenter, myBorder, BottomCenter);
    }
    if (myTag != null) {
      myTag.setSpotLocation(RightCenter, myBorder, Center);
    }
    if (myValue != null) {
      myValue.setSpotLocation(LeftCenter, myTag, RightCenter);
    }
  }

  public boolean isInteger() {
    return true;
  }

  public boolean getBoolVal() {return false;}
  public boolean getOldBoolVal() {return false;}

  public int getIntVal() {
    if (redirect == null) {
      return val;
    } else {
      int i = redirect.getIntVal();
      myValue.setText("" + i);
      return i;
    }
  }

  public int getOldIntVal() {
    if (redirect == null) {
      return oldval;
    } else {
      int i = redirect.getOldIntVal();
      return i;
    }
  }

  public String getStringVal() {return new String("");}
  public String getOldStringVal(){return new String("");}

  public void setStoredIntAction(int n) {
    myValue.setText("" + n);
    if (redirect == null) {
      oldval = val;
      val = n;
    } else {
      redirect.setStoredIntAction(n);
    }
  }

  public void setStoredBoolAction(boolean b) {}

  public void setStoredStringAction(String s) {}

  public void initializeDisplay() {getIntVal();}
}  
