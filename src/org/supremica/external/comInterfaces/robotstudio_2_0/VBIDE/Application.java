package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// interface Application Declaration
public interface Application extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x0002E158,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public String getVersion() throws com.inzoom.comjni.ComJniException;
}
