
package org.jgrafchart;

import com.nwoods.jgo.*;
import java.awt.*;
import java.util.*;

public abstract class InternalVariable extends JGoArea implements Readable, Writable  {

  protected JGoRectangle myBorder = null;
  protected JGoText myValue = null;
//  private JGoText myTag = null;
  protected JGoText myName = null;
  protected InternalVariable redirect = null;


  public InternalVariable() {
    super();
  }

  public InternalVariable(Point loc) {

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
    myValue =  new JGoText("");
    myValue.setSelectable(false);
    myValue.setEditable(false);
    myValue.setEditOnSingleClick(false);
    myValue.setDraggable(false);
    myValue.setAlignment(JGoText.ALIGN_LEFT);
    myValue.setTransparent(true);
    addObjectAtHead(myBorder);
    addObjectAtTail(myName);
    addObjectAtTail(myValue);
    layoutChildren();
  }

  public JGoObject copyObject(JGoCopyEnvironment env)
  {
    InternalVariable newobj = (InternalVariable)super.copyObject(env); 
    return newobj;
  }

  public void copyChildren(JGoArea newarea,JGoCopyEnvironment env) {

   InternalVariable  newobj = (InternalVariable)newarea;
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
    if (myName != null) {
      myName.setSpotLocation(TopCenter, myBorder, BottomCenter);
    }
    if (myValue != null) {
      myValue.setSpotLocation(LeftCenter, myBorder, Center);
    }
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

  public boolean isBoolean() {
    return false;
  }

  public boolean isInteger() { return false;}
  public boolean isString() {return false;}

  public String getName() {return myName.getText();}

  public boolean getBoolVal() {return false;}
  public boolean getOldBoolVal() {return false;}

  public void setStoredStringAction(String s) {}

  public String getStringVal() {return new String("");}
  public String getOldStringVal() {return new String("");}

  public void initializeDisplay() {}

  public void setRedirect(InternalVariable iv) {
    redirect = iv;
    initializeDisplay();
  }

}
