package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Position
public class Position extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPositionJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x86593370,(short)0x7B03,(short)0x11D3,new char[]{0xAC,0xD3,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Position getPositionFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Position(comPtr,bAddRef); }
  public static Position getPositionFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Position(comPtr); }
  public static Position getPositionFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Position(unk); }
  public static Position convertComPtrToPosition(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Position(comPtr,true,releaseComPtr); }
  protected Position(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Position(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Position(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Position(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public Position(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition.IID,Context),false);
  }
  public Position() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition.IID),false);
  }
}
