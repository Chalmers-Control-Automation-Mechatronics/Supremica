package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface ICommandBarButtonEvents Declaration
public interface ICommandBarButtonEvents extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x55F88890,(short)0x7708,(short)0x11D1,new char[]{0xAC,0xEB,0x00,0x60,0x08,0x96,0x1D,0xA5});
  public void click(org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarButton Ctrl,boolean[] CancelDefault) throws com.inzoom.comjni.ComJniException;
}
