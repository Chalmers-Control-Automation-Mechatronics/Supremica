package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Views
public class Views extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IViewsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IViews {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x006C2A61,(short)0xEDC0,(short)0x11D3,new char[]{0x80,0xC5,0x00,0xC0,0x4F,0x60,0xF7,0x93});
  public static Views getViewsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Views(comPtr,bAddRef); }
  public static Views getViewsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Views(comPtr); }
  public static Views getViewsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Views(unk); }
  public static Views convertComPtrToViews(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Views(comPtr,true,releaseComPtr); }
  protected Views(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Views(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Views(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Views(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
