package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// coclass VBComponent
public class VBComponent extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw._VBComponentJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponent {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xBE39F3DA,(short)0x1B13,(short)0x11D0,new char[]{0x88,0x7F,0x00,0xA0,0xC9,0x0F,0x27,0x44});
  public static VBComponent getVBComponentFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new VBComponent(comPtr,bAddRef); }
  public static VBComponent getVBComponentFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new VBComponent(comPtr); }
  public static VBComponent getVBComponentFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new VBComponent(unk); }
  public static VBComponent convertComPtrToVBComponent(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new VBComponent(comPtr,true,releaseComPtr); }
  protected VBComponent(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected VBComponent(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected VBComponent(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected VBComponent(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public VBComponent(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponent.IID,Context),false);
  }
  public VBComponent() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponent.IID),false);
  }
}
