package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IIOs Declaration
public interface IIOs extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x484EA46F,(short)0xEF59,(short)0x11D3,new char[]{0x80,0xF6,0x00,0xC0,0x4F,0x60,0xF7,0x8D});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IIO item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IIO add() throws com.inzoom.comjni.ComJniException;
}
