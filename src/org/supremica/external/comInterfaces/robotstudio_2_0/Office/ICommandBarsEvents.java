package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface ICommandBarsEvents Declaration
public interface ICommandBarsEvents extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x55F88892,(short)0x7708,(short)0x11D1,new char[]{0xAC,0xEB,0x00,0x60,0x08,0x96,0x1D,0xA5});
  public void onUpdate() throws com.inzoom.comjni.ComJniException;
}
