package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IArgumentValues Declaration
public interface IArgumentValues extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xCE7D7779,(short)0x9C61,(short)0x11D5,new char[]{0xBC,0xA6,0x00,0xD0,0xB7,0xE6,0x41,0x75});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IArgumentValue item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IActionInstructionRef getParent() throws com.inzoom.comjni.ComJniException;
  public void setParent(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IActionInstructionRef ppPar) throws com.inzoom.comjni.ComJniException;
}
