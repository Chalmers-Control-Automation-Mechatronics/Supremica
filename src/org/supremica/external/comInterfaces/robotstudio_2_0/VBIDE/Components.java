package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// coclass Components
public class Components extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw._ComponentsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._Components {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xBE39F3D6,(short)0x1B13,(short)0x11D0,new char[]{0x88,0x7F,0x00,0xA0,0xC9,0x0F,0x27,0x44});
  public static Components getComponentsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Components(comPtr,bAddRef); }
  public static Components getComponentsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Components(comPtr); }
  public static Components getComponentsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Components(unk); }
  public static Components convertComPtrToComponents(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Components(comPtr,true,releaseComPtr); }
  protected Components(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Components(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Components(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Components(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public Components(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._Components.IID,Context),false);
  }
  public Components() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._Components.IID),false);
  }
}
