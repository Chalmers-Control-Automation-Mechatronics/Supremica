package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// coclass CodeModule
public class CodeModule extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw._CodeModuleJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x0002E170,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public static CodeModule getCodeModuleFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CodeModule(comPtr,bAddRef); }
  public static CodeModule getCodeModuleFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CodeModule(comPtr); }
  public static CodeModule getCodeModuleFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new CodeModule(unk); }
  public static CodeModule convertComPtrToCodeModule(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CodeModule(comPtr,true,releaseComPtr); }
  protected CodeModule(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected CodeModule(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected CodeModule(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected CodeModule(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public CodeModule(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID,Context),false);
  }
  public CodeModule() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID),false);
  }
}
