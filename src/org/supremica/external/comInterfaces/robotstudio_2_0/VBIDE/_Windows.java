package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// interface _Windows Declaration
public interface _Windows extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._Windows_old {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xF57B7ED0,(short)0xD8AB,(short)0x11D1,new char[]{0x85,0xDF,0x00,0xC0,0x4F,0x98,0xF4,0x2C});
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window createToolWindow(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.AddIn AddInInst,String ProgId,String Caption,String GuidPosition,com.inzoom.comjni.IDispatch[] DocObj) throws com.inzoom.comjni.ComJniException;
}
