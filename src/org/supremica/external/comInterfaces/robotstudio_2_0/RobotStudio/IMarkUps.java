package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IMarkUps Declaration
public interface IMarkUps extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x91052CFC,(short)0x9F3E,(short)0x11D4,new char[]{0x81,0xB2,0x00,0xC0,0x4F,0x60,0xF7,0x91});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMarkUp item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getParent() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMarkUp add(String Name,String Text,int Type) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMarkUp add(String Name,String Text) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMarkUp add(String Name) throws com.inzoom.comjni.ComJniException;
}
