package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IViews Declaration
public interface IViews extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x006C2A62,(short)0xEDC0,(short)0x11D3,new char[]{0x80,0xC5,0x00,0xC0,0x4F,0x60,0xF7,0x93});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2 getParent() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView add(String Name) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView getWindowFromPoint(int x,int y) throws com.inzoom.comjni.ComJniException;
}
