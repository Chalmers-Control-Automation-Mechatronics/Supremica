package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IRSExtTarget Declaration
public interface IRSExtTarget extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x57C7286E,(short)0xA355,(short)0x11D3,new char[]{0x80,0xBB,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException;
}
