package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IControllerEventSink Declaration
public interface IControllerEventSink extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xCF19EBAE,(short)0xA80D,(short)0x11D3,new char[]{0x80,0xB8,0x00,0xC0,0x4F,0x60,0xFA,0xB6});
  public void adviseVCI(com.inzoom.comjni.IUnknown pUnk) throws com.inzoom.comjni.ComJniException;
  public void unAdviseVCI() throws com.inzoom.comjni.ComJniException;
  public void adviseS4API(com.inzoom.comjni.IUnknown pUnk) throws com.inzoom.comjni.ComJniException;
  public void unAdviseS4API() throws com.inzoom.comjni.ComJniException;
  public void adviseRIM(com.inzoom.comjni.IUnknown pUnk) throws com.inzoom.comjni.ComJniException;
  public void unAdviseRIM() throws com.inzoom.comjni.ComJniException;
}
