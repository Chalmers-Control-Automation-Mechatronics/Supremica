package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface LanguageSettings Implementation
public class LanguageSettingsJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw._IMsoDispObjJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.LanguageSettings {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.LanguageSettings getLanguageSettingsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new LanguageSettingsJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.LanguageSettings getLanguageSettingsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new LanguageSettingsJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.LanguageSettings getLanguageSettingsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new LanguageSettingsJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.LanguageSettings convertComPtrToLanguageSettings(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new LanguageSettingsJCW(comPtr,true,releaseComPtr); }
  protected LanguageSettingsJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected LanguageSettingsJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LanguageSettings.IID); }
  protected LanguageSettingsJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected LanguageSettingsJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LanguageSettings.IID); }
  protected LanguageSettingsJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected LanguageSettingsJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LanguageSettings.IID,releaseComPtr);}
  protected LanguageSettingsJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public int getLanguageID(int Id) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Id,false),
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LanguageSettings.IID);
    int rv = _v[1].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getLanguagePreferredForEditing(int lid) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(lid,false),
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LanguageSettings.IID);
    boolean rv = _v[1].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
