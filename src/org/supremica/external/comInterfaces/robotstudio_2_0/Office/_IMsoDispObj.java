package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface _IMsoDispObj Declaration
public interface _IMsoDispObj extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0300,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.IDispatch getApplication() throws com.inzoom.comjni.ComJniException;
  public int getCreator() throws com.inzoom.comjni.ComJniException;
}
