package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Markup
public class Markup extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IMarkUpJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMarkUp {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x51E3B62B,(short)0x3160,(short)0x11D4,new char[]{0x80,0xEE,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public static Markup getMarkupFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Markup(comPtr,bAddRef); }
  public static Markup getMarkupFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Markup(comPtr); }
  public static Markup getMarkupFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Markup(unk); }
  public static Markup convertComPtrToMarkup(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Markup(comPtr,true,releaseComPtr); }
  protected Markup(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Markup(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Markup(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Markup(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
