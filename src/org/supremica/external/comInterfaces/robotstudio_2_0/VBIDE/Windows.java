package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// coclass Windows
public class Windows extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw._WindowsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._Windows {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x0002E185,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public static Windows getWindowsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Windows(comPtr,bAddRef); }
  public static Windows getWindowsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Windows(comPtr); }
  public static Windows getWindowsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Windows(unk); }
  public static Windows convertComPtrToWindows(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Windows(comPtr,true,releaseComPtr); }
  protected Windows(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Windows(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Windows(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Windows(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public Windows(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._Windows.IID,Context),false);
  }
  public Windows() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._Windows.IID),false);
  }
}
