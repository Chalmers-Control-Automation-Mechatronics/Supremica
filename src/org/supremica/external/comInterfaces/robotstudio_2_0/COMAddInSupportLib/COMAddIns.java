package org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib;

// coclass COMAddIns
public class COMAddIns extends org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.jcw.IPseColCOMAddInsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x5052EF13,(short)0x5688,(short)0x11D3,new char[]{0x80,0xD2,0x00,0x50,0x04,0x29,0xD1,0x09});
  public static COMAddIns getCOMAddInsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new COMAddIns(comPtr,bAddRef); }
  public static COMAddIns getCOMAddInsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new COMAddIns(comPtr); }
  public static COMAddIns getCOMAddInsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new COMAddIns(unk); }
  public static COMAddIns convertComPtrToCOMAddIns(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new COMAddIns(comPtr,true,releaseComPtr); }
  protected COMAddIns(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected COMAddIns(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected COMAddIns(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected COMAddIns(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public COMAddIns(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns.IID,Context),false);
  }
  public COMAddIns() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns.IID),false);
  }
}
