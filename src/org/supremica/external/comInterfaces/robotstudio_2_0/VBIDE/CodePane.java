package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// coclass CodePane
public class CodePane extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw._CodePaneJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePane {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x0002E178,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public static CodePane getCodePaneFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CodePane(comPtr,bAddRef); }
  public static CodePane getCodePaneFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CodePane(comPtr); }
  public static CodePane getCodePaneFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new CodePane(unk); }
  public static CodePane convertComPtrToCodePane(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CodePane(comPtr,true,releaseComPtr); }
  protected CodePane(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected CodePane(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected CodePane(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected CodePane(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public CodePane(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePane.IID,Context),false);
  }
  public CodePane() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePane.IID),false);
  }
}
