package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface ShapeNodes Declaration
public interface ShapeNodes extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0319,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNode item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public void delete(int Index) throws com.inzoom.comjni.ComJniException;
  public void insert(int Index,int SegmentType,int EditingType,float X1,float Y1,float X2,float Y2,float X3,float Y3) throws com.inzoom.comjni.ComJniException;
  public void insert(int Index,int SegmentType,int EditingType,float X1,float Y1,float X2,float Y2,float X3) throws com.inzoom.comjni.ComJniException;
  public void insert(int Index,int SegmentType,int EditingType,float X1,float Y1,float X2,float Y2) throws com.inzoom.comjni.ComJniException;
  public void insert(int Index,int SegmentType,int EditingType,float X1,float Y1,float X2) throws com.inzoom.comjni.ComJniException;
  public void insert(int Index,int SegmentType,int EditingType,float X1,float Y1) throws com.inzoom.comjni.ComJniException;
  public void setEditingType(int Index,int EditingType) throws com.inzoom.comjni.ComJniException;
  public void setPosition(int Index,float X1,float Y1) throws com.inzoom.comjni.ComJniException;
  public void setSegmentType(int Index,int SegmentType) throws com.inzoom.comjni.ComJniException;
}
