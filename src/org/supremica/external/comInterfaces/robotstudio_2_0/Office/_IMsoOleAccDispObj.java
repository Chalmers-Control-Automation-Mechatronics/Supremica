package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface _IMsoOleAccDispObj Declaration
public interface _IMsoOleAccDispObj extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.IAccessible {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0301,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.IDispatch getApplication() throws com.inzoom.comjni.ComJniException;
  public int getCreator() throws com.inzoom.comjni.ComJniException;
}
