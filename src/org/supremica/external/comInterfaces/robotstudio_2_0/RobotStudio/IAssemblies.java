package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IAssemblies Declaration
public interface IAssemblies extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x3941368A,(short)0x7D48,(short)0x11D3,new char[]{0xAC,0xD5,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAssembly item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getParent() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAssembly add(com.inzoom.comjni.Variant Object) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAssembly add() throws com.inzoom.comjni.ComJniException;
}
