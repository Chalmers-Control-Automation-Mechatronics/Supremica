package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface FreeformBuilder Declaration
public interface FreeformBuilder extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0315,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public void addNodes(int SegmentType,int EditingType,float X1,float Y1,float X2,float Y2,float X3,float Y3) throws com.inzoom.comjni.ComJniException;
  public void addNodes(int SegmentType,int EditingType,float X1,float Y1,float X2,float Y2,float X3) throws com.inzoom.comjni.ComJniException;
  public void addNodes(int SegmentType,int EditingType,float X1,float Y1,float X2,float Y2) throws com.inzoom.comjni.ComJniException;
  public void addNodes(int SegmentType,int EditingType,float X1,float Y1,float X2) throws com.inzoom.comjni.ComJniException;
  public void addNodes(int SegmentType,int EditingType,float X1,float Y1) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape convertToShape() throws com.inzoom.comjni.ComJniException;
}
