package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface ICommandBarControlEventSink Declaration
public interface ICommandBarControlEventSink extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xB32ED570,(short)0x9D90,(short)0x11D3,new char[]{0x80,0xB9,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public void advise(com.inzoom.comjni.IUnknown pUnk) throws com.inzoom.comjni.ComJniException;
  public void unAdvise() throws com.inzoom.comjni.ComJniException;
}
