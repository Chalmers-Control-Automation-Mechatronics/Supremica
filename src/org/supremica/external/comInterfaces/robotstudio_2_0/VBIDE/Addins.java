package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// coclass Addins
public class Addins extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw._AddInsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._AddIns {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xDA936B63,(short)0xAC8B,(short)0x11D1,new char[]{0xB6,0xE5,0x00,0xA0,0xC9,0x0F,0x27,0x44});
  public static Addins getAddinsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Addins(comPtr,bAddRef); }
  public static Addins getAddinsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Addins(comPtr); }
  public static Addins getAddinsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Addins(unk); }
  public static Addins convertComPtrToAddins(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Addins(comPtr,true,releaseComPtr); }
  protected Addins(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Addins(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Addins(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Addins(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public Addins(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._AddIns.IID,Context),false);
  }
  public Addins() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._AddIns.IID),false);
  }
}
