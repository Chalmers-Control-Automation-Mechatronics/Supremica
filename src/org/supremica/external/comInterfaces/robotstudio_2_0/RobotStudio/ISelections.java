package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface ISelections Declaration
public interface ISelections extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x78A4FEBA,(short)0xA30E,(short)0x11D3,new char[]{0xAD,0x05,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public com.inzoom.comjni.IDispatch item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsFilter getFilter() throws com.inzoom.comjni.ComJniException;
  public void setFilter(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsFilter pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject add(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject RsObject) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject remove(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject RsObject) throws com.inzoom.comjni.ComJniException;
  public void removeAll() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2 getParent() throws com.inzoom.comjni.ComJniException;
}
