package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface _IPseDynClassInstInternal Declaration
public interface _IPseDynClassInstInternal extends com.inzoom.comjni.IUnknown {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x0CFDD9BA,(short)0xA7CC,(short)0x11D3,new char[]{0x80,0xBD,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public void releaseResources() throws com.inzoom.comjni.ComJniException;
  public void releaseProxyMappings() throws com.inzoom.comjni.ComJniException;
  public void setCompositeObject(com.inzoom.comjni.IDispatch pDisp) throws com.inzoom.comjni.ComJniException;
  public void getCompositeObject(com.inzoom.comjni.IDispatch[] ppDisp) throws com.inzoom.comjni.ComJniException;
}
