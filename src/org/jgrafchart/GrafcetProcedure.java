package org.jgrafchart;

import com.nwoods.jgo.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;

public class GrafcetProcedure extends GrafcetObject implements Readable {

  public GrafcetProcedure(){
      super();
  }

  public  GrafcetProcedure(Point loc, String labeltext)
  {
    super();
    setSize(60, 60);
    setSelectable(true);
    setGrabChildSelection(false);
    setDraggable(true);
    setResizable(false);

    myRectangle = new JGoRectangle(getTopLeft(), getSize());
    myRectangle.setPen(new JGoPen(JGoPen.SOLID,2,new Color(0.0F,0.0F,0.0F)));
    myRectangle.setSelectable(false);
    myRectangle.setDraggable(false);

    setLocation(loc);

    myRectangle1  = new JGoRectangle(new Rectangle(7,7));
    myRectangle1.setSelectable(false);
    myRectangle1.setDraggable(false);
    myRectangle2  = new JGoRectangle(new Rectangle(7,7));
    myRectangle2.setSelectable(false);
    myRectangle2.setDraggable(false);
    myRectangle3  = new JGoRectangle(new Rectangle(7,7));
    myRectangle3.setSelectable(false);
    myRectangle3.setDraggable(false);

    myStroke1 = new JGoStroke();
    myStroke1.addPoint(0,0);
    myStroke1.addPoint(0,7);
    myStroke1.setSelectable(false);
    myStroke1.setDraggable(false);
    myStroke2 = new JGoStroke();
    myStroke2.addPoint(0,0);
    myStroke2.addPoint(0,7);
    myStroke2.setSelectable(false);
    myStroke2.setDraggable(false);

    if (labeltext != null) {
      myLabel = new JGoText(labeltext);
      myLabel.setSelectable(true);
      myLabel.setEditable(true);
      myLabel.setEditOnSingleClick(true);
      myLabel.setDraggable(false);
      myLabel.setAlignment(JGoText.ALIGN_CENTER);
      myLabel.setTransparent(true);
    }
    else {
      myLabel = new JGoText("P1");
      myLabel.setSelectable(true);
      myLabel.setEditable(true);
      myLabel.setEditOnSingleClick(true);
      myLabel.setDraggable(false);
      myLabel.setAlignment(JGoText.ALIGN_LEFT);
      myLabel.setTransparent(true);
    };

    addObjectAtHead(myRectangle);
    addObjectAtTail(myRectangle1);
    addObjectAtTail(myRectangle2);
    addObjectAtTail(myRectangle3);
    addObjectAtTail(myStroke1);
    addObjectAtTail(myStroke2);
    if (myLabel != null){
      addObjectAtTail(myLabel);
    }


    myContentDocument = new GCDocument();

    layoutChildren();
  }
    

  public JGoObject copyObject(JGoCopyEnvironment env)
  {
    GrafcetProcedure newobj = (GrafcetProcedure)super.copyObject(env); 

    return newobj;
  }

  public void copyChildren(JGoArea newarea,JGoCopyEnvironment env) {

    GrafcetProcedure newobj = (GrafcetProcedure)newarea;
    if (myRectangle != null) {
      newobj.myRectangle = (JGoRectangle)myRectangle.copyObject(env);
      newobj.addObjectAtHead(newobj.myRectangle);
    }
    if (myRectangle1 != null) {
      newobj.myRectangle1 = (JGoRectangle)myRectangle1.copyObject(env);
      newobj.addObjectAtTail(newobj.myRectangle1);
    }
    if (myRectangle2 != null) {
      newobj.myRectangle2 = (JGoRectangle)myRectangle2.copyObject(env);
      newobj.addObjectAtTail(newobj.myRectangle2);
    }
    if (myRectangle3 != null) {
      newobj.myRectangle3 = (JGoRectangle)myRectangle3.copyObject(env);
      newobj.addObjectAtTail(newobj.myRectangle3);
    }
    if (myStroke1 != null) {
      newobj.myStroke1 = (JGoStroke)myStroke1.copyObject(env);
      newobj.addObjectAtTail(newobj.myStroke1);
    }
    if (myStroke2 != null) {
      newobj.myStroke2 = (JGoStroke)myStroke2.copyObject(env);
      newobj.addObjectAtTail(newobj.myStroke2);
    }

    if (myLabel != null) {
      newobj.myLabel = (JGoText)myLabel.copyObject(env);
      newobj.addObjectAtTail(newobj.myLabel);
    }

    if (myContentDocument != null) {
      newobj.myContentDocument = new GCDocument();
      newobj.myContentDocument.copyFromCollection(myContentDocument);
    }

  }

  public Point getLocation(Point p)
  {
    if (myRectangle != null)
      return myRectangle.getSpotLocation(TopCenter, p);
    else
      return getSpotLocation(TopCenter, p);
  }

  public void setLocation(int x, int y)
  {
    if (myRectangle != null) {
      myRectangle.setSpotLocation(TopCenter, x, y);
    } else {
      setSpotLocation(TopCenter, x, y);
    }
    layoutChildren();
  }

  public void layoutChildren()
  {
    if (myRectangle == null) return;
    if (myRectangle1 != null) {
      Point p = myRectangle.getSpotLocation(TopCenter);
      myRectangle1.setSpotLocation(TopCenter, (int)p.getX(), (int)p.getY() + 10);
    }
    if (myStroke1 != null) {
      myStroke1.setSpotLocation(TopCenter, myRectangle1, BottomCenter);
    }
    if (myRectangle2 != null) {
      myRectangle2.setSpotLocation(TopCenter, myStroke1, BottomCenter);
    }
    if (myStroke2 != null) {
      myStroke2.setSpotLocation(TopCenter, myRectangle2, BottomCenter);
    }
    if (myRectangle3 != null) {
      myRectangle3.setSpotLocation(TopCenter, myStroke2, BottomCenter);
    }

    if (myLabel != null) {
      Point p = myRectangle.getSpotLocation(BottomCenter);
     myLabel.setSpotLocation(TopCenter,(int)p.getX(),(int)p.getY() + 10);
//     myLabel.setSpotLocation(LeftCenter,60,60);
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

  public JGoPen getPen()
  {
    return myRectangle.getPen();
  }

  public void setPen(JGoPen p)
  {
    myRectangle.setPen(p);
  }

  public JGoBrush getBrush()
  {
    return myRectangle.getBrush();
  }

  public void setBrush(JGoBrush b)
  {
    myRectangle.setBrush(b);
  }

  public JGoRectangle myRectangle = null;
  public JGoRectangle myRectangle1 = null;
  public JGoRectangle myRectangle2 = null;
  public JGoRectangle myRectangle3 = null;
  public JGoStroke myStroke1 = null;
  public JGoStroke myStroke2 = null;
  public  JGoText myLabel = null;

  public GCDocument myContentDocument = null;
  public JInternalFrame frame = null; 
  public GCView parentView = null;
  public GCView view = null;

  public int stepCounterInt = 2;
  
  public String getName() {return myLabel.getText();}
  public boolean getBoolVal() {return false;}
  public boolean getOldBoolVal() {return false;}
  public int getIntVal() {return 0;}
  public int getOldIntVal() {return 0;}
  public String getStringVal() {return new String("");}
  public String getOldStringVal() {return new String("");}



  }
