package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// coclass LinkedWindows
public class LinkedWindows extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw._LinkedWindowsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._LinkedWindows {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x0002E187,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public static LinkedWindows getLinkedWindowsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new LinkedWindows(comPtr,bAddRef); }
  public static LinkedWindows getLinkedWindowsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new LinkedWindows(comPtr); }
  public static LinkedWindows getLinkedWindowsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new LinkedWindows(unk); }
  public static LinkedWindows convertComPtrToLinkedWindows(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new LinkedWindows(comPtr,true,releaseComPtr); }
  protected LinkedWindows(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected LinkedWindows(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected LinkedWindows(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected LinkedWindows(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public LinkedWindows(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._LinkedWindows.IID,Context),false);
  }
  public LinkedWindows() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._LinkedWindows.IID),false);
  }
}
