package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface ShapeNode Declaration
public interface ShapeNode extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0318,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public int getEditingType() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getPoints() throws com.inzoom.comjni.ComJniException;
  public int getSegmentType() throws com.inzoom.comjni.ComJniException;
}
