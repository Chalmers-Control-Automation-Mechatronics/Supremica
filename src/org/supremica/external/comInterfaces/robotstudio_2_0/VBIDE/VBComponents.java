package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// coclass VBComponents
public class VBComponents extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw._VBComponentsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponents {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xBE39F3D7,(short)0x1B13,(short)0x11D0,new char[]{0x88,0x7F,0x00,0xA0,0xC9,0x0F,0x27,0x44});
  public static VBComponents getVBComponentsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new VBComponents(comPtr,bAddRef); }
  public static VBComponents getVBComponentsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new VBComponents(comPtr); }
  public static VBComponents getVBComponentsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new VBComponents(unk); }
  public static VBComponents convertComPtrToVBComponents(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new VBComponents(comPtr,true,releaseComPtr); }
  protected VBComponents(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected VBComponents(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected VBComponents(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected VBComponents(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public VBComponents(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponents.IID,Context),false);
  }
  public VBComponents() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponents.IID),false);
  }
}
