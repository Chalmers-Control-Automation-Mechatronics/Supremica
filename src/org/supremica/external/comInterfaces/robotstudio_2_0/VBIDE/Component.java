package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// coclass Component
public class Component extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw._ComponentJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._Component {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xBE39F3D8,(short)0x1B13,(short)0x11D0,new char[]{0x88,0x7F,0x00,0xA0,0xC9,0x0F,0x27,0x44});
  public static Component getComponentFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Component(comPtr,bAddRef); }
  public static Component getComponentFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Component(comPtr); }
  public static Component getComponentFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Component(unk); }
  public static Component convertComPtrToComponent(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Component(comPtr,true,releaseComPtr); }
  protected Component(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Component(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Component(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Component(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public Component(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._Component.IID,Context),false);
  }
  public Component() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._Component.IID),false);
  }
}
