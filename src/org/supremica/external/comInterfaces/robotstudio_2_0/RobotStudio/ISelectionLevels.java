package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface ISelectionLevels Declaration
public interface ISelectionLevels extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xC02A25D8,(short)0xE9C2,(short)0x11D3,new char[]{0xAD,0x4F,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ISelectionLevel item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getParent() throws com.inzoom.comjni.ComJniException;
  public void add(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ISelectionLevel SelectionLevel) throws com.inzoom.comjni.ComJniException;
  public void remove(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ISelectionLevel pDisp) throws com.inzoom.comjni.ComJniException;
}
