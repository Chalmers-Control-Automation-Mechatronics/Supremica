package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IWires Declaration
public interface IWires extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xA4D0E5EC,(short)0x87AB,(short)0x11D3,new char[]{0x8B,0xA0,0x00,0xC0,0x4F,0x68,0xDF,0x58});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWire item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getParent() throws com.inzoom.comjni.ComJniException;
}
