package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface Scripts Implementation
public class ScriptsJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw._IMsoDispObjJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.Scripts {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.Scripts getScriptsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ScriptsJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.Scripts getScriptsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ScriptsJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.Scripts getScriptsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ScriptsJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.Scripts convertComPtrToScripts(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ScriptsJCW(comPtr,true,releaseComPtr); }
  protected ScriptsJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ScriptsJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Scripts.IID); }
  protected ScriptsJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected ScriptsJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Scripts.IID); }
  protected ScriptsJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected ScriptsJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Scripts.IID,releaseComPtr);}
  protected ScriptsJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Scripts.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getCount() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Scripts.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IUnknown)null,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Scripts.IID);
    com.inzoom.comjni.IUnknown rv = com.inzoom.comjni.jcw.IUnknownJCW.getIUnknownFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Script item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Index),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Scripts.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Script rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ScriptJCW.getScriptFromComPtr(_v[1].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Script add(com.inzoom.comjni.IDispatch Anchor,int Location,int Language,String Id,String Extended,String ScriptText) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Anchor,false),
      new com.inzoom.comjni.Variant(Location,false),
      new com.inzoom.comjni.Variant(Language,false),
      new com.inzoom.comjni.Variant(Id,false),
      new com.inzoom.comjni.Variant(Extended,false),
      new com.inzoom.comjni.Variant(ScriptText,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Scripts.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Script rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ScriptJCW.getScriptFromComPtr(_v[6].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Script add(com.inzoom.comjni.IDispatch Anchor,int Location,int Language,String Id,String Extended) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Anchor,false),
      new com.inzoom.comjni.Variant(Location,false),
      new com.inzoom.comjni.Variant(Language,false),
      new com.inzoom.comjni.Variant(Id,false),
      new com.inzoom.comjni.Variant(Extended,false),
      new com.inzoom.comjni.Variant((String)"",false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Scripts.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Script rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ScriptJCW.getScriptFromComPtr(_v[6].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Script add(com.inzoom.comjni.IDispatch Anchor,int Location,int Language,String Id) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Anchor,false),
      new com.inzoom.comjni.Variant(Location,false),
      new com.inzoom.comjni.Variant(Language,false),
      new com.inzoom.comjni.Variant(Id,false),
      new com.inzoom.comjni.Variant((String)"",false),
      new com.inzoom.comjni.Variant((String)"",false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Scripts.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Script rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ScriptJCW.getScriptFromComPtr(_v[6].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Script add(com.inzoom.comjni.IDispatch Anchor,int Location,int Language) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Anchor,false),
      new com.inzoom.comjni.Variant(Location,false),
      new com.inzoom.comjni.Variant(Language,false),
      new com.inzoom.comjni.Variant((String)"",false),
      new com.inzoom.comjni.Variant((String)"",false),
      new com.inzoom.comjni.Variant((String)"",false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Scripts.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Script rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ScriptJCW.getScriptFromComPtr(_v[6].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Script add(com.inzoom.comjni.IDispatch Anchor,int Location) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Anchor,false),
      new com.inzoom.comjni.Variant(Location,false),
      new com.inzoom.comjni.Variant((int)2,false),
      new com.inzoom.comjni.Variant((String)"",false),
      new com.inzoom.comjni.Variant((String)"",false),
      new com.inzoom.comjni.Variant((String)"",false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Scripts.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Script rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ScriptJCW.getScriptFromComPtr(_v[6].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Script add(com.inzoom.comjni.IDispatch Anchor) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Anchor,false),
      new com.inzoom.comjni.Variant((int)2,false),
      new com.inzoom.comjni.Variant((int)2,false),
      new com.inzoom.comjni.Variant((String)"",false),
      new com.inzoom.comjni.Variant((String)"",false),
      new com.inzoom.comjni.Variant((String)"",false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Scripts.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Script rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ScriptJCW.getScriptFromComPtr(_v[6].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Script add() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,false),
      new com.inzoom.comjni.Variant((int)2,false),
      new com.inzoom.comjni.Variant((int)2,false),
      new com.inzoom.comjni.Variant((String)"",false),
      new com.inzoom.comjni.Variant((String)"",false),
      new com.inzoom.comjni.Variant((String)"",false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Scripts.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Script rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ScriptJCW.getScriptFromComPtr(_v[6].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void delete() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.Scripts.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
