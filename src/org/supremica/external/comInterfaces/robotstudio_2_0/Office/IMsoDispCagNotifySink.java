package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface IMsoDispCagNotifySink Declaration
public interface IMsoDispCagNotifySink extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0359,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public void insertClip(com.inzoom.comjni.IUnknown pClipMoniker,com.inzoom.comjni.IUnknown pItemMoniker) throws com.inzoom.comjni.ComJniException;
  public void windowIsClosing() throws com.inzoom.comjni.ComJniException;
}
