package org.jgrafchart;

import com.nwoods.jgo.*;
import java.awt.*;
import java.util.*;

public class ParallelJoin extends GrafcetObject implements GCIdent
{

  public JGoStroke myTopLine = null;
  public JGoStroke myBottomLine = null;
  public JGoStroke myInline1 = null;
  public JGoStroke myInline2 = null;
  public JGoStroke myOutline = null;
  public GCTransitionInPort myInPort1 = null;
  public GCTransitionInPort myInPort2 = null;
  public GCStepOutPort myOutPort = null;


  public ParallelJoin(){
      super();
  }

  public ParallelJoin(Point loc)
  {
    super();
    setSize(280, 20);    
    setSelectable(true);
    setGrabChildSelection(true);    
    setDraggable(true);
    setResizable(false);
    myTopLine = new JGoStroke();
    myTopLine.addPoint(0,0);
    myTopLine.addPoint(280,0);
    myTopLine.setSelectable(false);
    myBottomLine = new JGoStroke();
    myBottomLine.addPoint(0,0);
    myBottomLine.addPoint(280,0);
    myBottomLine.setSelectable(false);

    myInline1 = new JGoStroke();
    myInline1.addPoint(10,0);
    myInline1.addPoint(10,10);
    myInline1.setSelectable(false);
    myInline2 = new JGoStroke();
    myInline2.addPoint(10,0);
    myInline2.addPoint(10,10);
    myInline2.setSelectable(false);
    myOutline = new JGoStroke();
    myOutline.addPoint(20,0);
    myOutline.addPoint(20,10);
    myOutline.setSelectable(false);


    myInPort1 = new GCTransitionInPort();
    myInPort1.setSize(10,10);
    myInPort1.setToSpot(JGoObject.TopCenter);
    myInPort1.setPen(JGoPen.Null);
    myInPort1.setBrush(JGoBrush.Null);

    myInPort2 = new GCTransitionInPort();
    myInPort2.setSize(10,10);
    myInPort2.setToSpot(JGoObject.TopCenter);
    myInPort2.setPen(JGoPen.Null);
    myInPort2.setBrush(JGoBrush.Null);

    myOutPort = new GCStepOutPort();
    myOutPort.setSize(10,10);
    myOutPort.setFromSpot(JGoObject.BottomCenter);
    myOutPort.setPen(JGoPen.Null);
    myOutPort.setBrush(JGoBrush.Null);


    addObjectAtTail(myTopLine);
    addObjectAtTail(myBottomLine);
    addObjectAtTail(myInline1);
    addObjectAtTail(myInline2);
    addObjectAtTail(myOutline);
    addObjectAtTail(myInPort1);
    addObjectAtTail(myInPort2);
    addObjectAtTail(myOutPort);
    setLocation(loc);
    layoutChildren();
  }

  public JGoObject copyObject(JGoCopyEnvironment env)
  {
    ParallelJoin newobj = (ParallelJoin)super.copyObject(env); 
    return newobj;
  }

  public void copyChildren(JGoArea newarea,JGoCopyEnvironment env) {

    ParallelJoin newobj = (ParallelJoin)newarea;
    newobj.myTopLine = (JGoStroke)myTopLine.copyObject(env);
    newobj.addObjectAtTail(newobj.myTopLine);
    newobj.myBottomLine = (JGoStroke)myBottomLine.copyObject(env);
    newobj.addObjectAtTail(newobj.myBottomLine);
    newobj.myInline1 = (JGoStroke)myInline1.copyObject(env);
    newobj.addObjectAtTail(newobj.myInline1);
    newobj.myInline2 = (JGoStroke)myInline2.copyObject(env);
    newobj.addObjectAtTail(newobj.myInline2);
    newobj.myOutline = (JGoStroke)myOutline.copyObject(env);
    newobj.addObjectAtTail(newobj.myOutline);
    newobj.myInPort1 = (GCTransitionInPort)myInPort1.copyObject(env);
    newobj.addObjectAtTail(newobj.myInPort1);
    newobj.myInPort2 = (GCTransitionInPort)myInPort2.copyObject(env);
    newobj.addObjectAtTail(newobj.myInPort2);
    newobj.myOutPort = (GCStepOutPort)myOutPort.copyObject(env);
    newobj.addObjectAtTail(newobj.myOutPort);


  }

  public Point getLocation(Point p)
  {
      return myTopLine.getSpotLocation(TopCenter, p);
  }

  public void setLocation(int x, int y)
  {
    myTopLine.setSpotLocation(TopCenter, x, y);
    layoutChildren();
  }

  public void layoutChildren()
  {
    if (myTopLine == null) return;
    if (myBottomLine != null) {
    Point p = myTopLine.getSpotLocation(BottomCenter);
    myBottomLine.setSpotLocation(TopCenter, (int)p.getX(), (int)p.getY() + 4);
    }
    if (myInline1 != null) {
    Point p =  myTopLine.getSpotLocation(TopCenter); 
    myInline1.setSpotLocation(Bottom, (int)p.getX() - 110,  (int)p.getY());
    }
    if (myInline2 != null) {
    Point p =  myTopLine.getSpotLocation(TopCenter); 
    myInline2.setSpotLocation(Bottom, (int)p.getX() + 110,  (int)p.getY());
    }
    if (myOutline != null) {
     Point  p =  myBottomLine.getSpotLocation(BottomCenter); 
     myOutline.setSpotLocation(Top,myBottomLine, BottomCenter);
    }
    if (myInPort1 != null) {
      myInPort1.setSpotLocation(TopCenter, myInline1, Top);}
    if (myInPort2 != null) {
      myInPort2.setSpotLocation(TopCenter, myInline2, Top);}
    if (myOutPort != null) {
      myOutPort.setSpotLocation(BottomCenter, myOutline, Bottom);}

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

  public boolean isTransition()
  { return true; }

  public boolean isStep()
  { return true; }

  public void compileUpwards(GCTransition t) {

    JGoListPosition pos = myInPort1.getFirstLinkPos();
    while (pos != null) {
      JGoLink l = myInPort1.getLinkAtPos(pos);
      GrafcetObject gO = (GrafcetObject)l.getFromPort().getParent();
      if (gO instanceof GCStep) {
	GCStep s = (GCStep) gO;
	t.addPrecedingStep(s);
	  }
      if (gO instanceof MacroStep) {
	MacroStep ms = (MacroStep)gO;
        t.addPrecedingStep(ms);
	GCDocument doc = ms.myContentDocument;
        JGoListPosition pos1 = doc.getFirstObjectPos();
        JGoObject obj = doc.getObjectAtPos(pos1);
        while (obj != null && pos1 != null) {
	  if (obj instanceof ExitStep) {
	    ExitStep ex = (ExitStep)obj;
	    t.addPrecedingStep(ex);
	  }
         pos1 = doc.getNextObjectPos(pos1);
         obj = doc.getObjectAtPos(pos1);
	}
      }
      if (gO instanceof ParallelJoin) {
	ParallelJoin pj = (ParallelJoin)gO;
	pj.compileUpwards(t);
      }
      pos = myInPort1.getNextLinkPos(pos);
    }
    pos = myInPort2.getFirstLinkPos();
    while (pos != null) {
      JGoLink l = myInPort2.getLinkAtPos(pos);
      GrafcetObject gO = (GrafcetObject)l.getFromPort().getParent();
      if (gO instanceof GCStep) {
	GCStep s = (GCStep) gO;
	t.addPrecedingStep(s);
	  }
      if (gO instanceof MacroStep) {
	MacroStep ms = (MacroStep)gO;
        t.addPrecedingStep(ms);
	GCDocument doc = ms.myContentDocument;
        JGoListPosition pos1 = doc.getFirstObjectPos();
        JGoObject obj = doc.getObjectAtPos(pos1);
        while (obj != null && pos1 != null) {
	  if (obj instanceof ExitStep) {
	    ExitStep ex = (ExitStep)obj;
	    t.addPrecedingStep(ex);
	  }
         pos1 = doc.getNextObjectPos(pos1);
         obj = doc.getObjectAtPos(pos1);
	}
      }
      if (gO instanceof ParallelJoin) {
	ParallelJoin pj = (ParallelJoin)gO;
	pj.compileUpwards(t);
      }
      pos = myInPort2.getNextLinkPos(pos);
    }
  }

}
