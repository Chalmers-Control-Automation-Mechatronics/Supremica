package org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib;

// coclass COMAddIn
public class COMAddIn extends org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.jcw.IPseCOMAddInJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x5052EF15,(short)0x5688,(short)0x11D3,new char[]{0x80,0xD2,0x00,0x50,0x04,0x29,0xD1,0x09});
  public static COMAddIn getCOMAddInFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new COMAddIn(comPtr,bAddRef); }
  public static COMAddIn getCOMAddInFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new COMAddIn(comPtr); }
  public static COMAddIn getCOMAddInFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new COMAddIn(unk); }
  public static COMAddIn convertComPtrToCOMAddIn(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new COMAddIn(comPtr,true,releaseComPtr); }
  protected COMAddIn(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected COMAddIn(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected COMAddIn(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected COMAddIn(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public COMAddIn(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID,Context),false);
  }
  public COMAddIn() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID),false);
  }
}
