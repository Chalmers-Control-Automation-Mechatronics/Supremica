package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass UserOptions
public class UserOptions extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IUserOptions2JCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions2 {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xC61FA141,(short)0x9BF9,(short)0x11D3,new char[]{0xAC,0xFC,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static UserOptions getUserOptionsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new UserOptions(comPtr,bAddRef); }
  public static UserOptions getUserOptionsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new UserOptions(comPtr); }
  public static UserOptions getUserOptionsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new UserOptions(unk); }
  public static UserOptions convertComPtrToUserOptions(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new UserOptions(comPtr,true,releaseComPtr); }
  protected UserOptions(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected UserOptions(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected UserOptions(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected UserOptions(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
