package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface GroupShapes Declaration
public interface GroupShapes extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0316,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
}
