package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface DocumentProperties Implementation
public class DocumentPropertiesJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperties, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperties getDocumentPropertiesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new DocumentPropertiesJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperties getDocumentPropertiesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new DocumentPropertiesJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperties getDocumentPropertiesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new DocumentPropertiesJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperties convertComPtrToDocumentProperties(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new DocumentPropertiesJCW(comPtr,true,releaseComPtr); }
  protected DocumentPropertiesJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected DocumentPropertiesJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperties.IID); }
  protected DocumentPropertiesJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected DocumentPropertiesJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperties.IID); }
  protected DocumentPropertiesJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected DocumentPropertiesJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperties.IID,releaseComPtr);}
  protected DocumentPropertiesJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public void getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperties.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperty getItem(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Index),
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.I4,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IUnknown)null,true)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperties.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperty rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.DocumentPropertyJCW.getDocumentPropertyFromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getCount() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperties.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperty add(String Name,boolean LinkToContent,com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Value,com.inzoom.comjni.Variant LinkSource) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Name,false),
      new com.inzoom.comjni.Variant(LinkToContent,false),
      createVTblVArg(Type),
      createVTblVArg(Value),
      createVTblVArg(LinkSource),
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.I4,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IUnknown)null,true)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperties.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperty rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.DocumentPropertyJCW.getDocumentPropertyFromComPtr(_v[6].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperty add(String Name,boolean LinkToContent,com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Value) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Name,false),
      new com.inzoom.comjni.Variant(LinkToContent,false),
      createVTblVArg(Type),
      createVTblVArg(Value),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.I4,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IUnknown)null,true)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperties.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperty rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.DocumentPropertyJCW.getDocumentPropertyFromComPtr(_v[6].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperty add(String Name,boolean LinkToContent,com.inzoom.comjni.Variant Type) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Name,false),
      new com.inzoom.comjni.Variant(LinkToContent,false),
      createVTblVArg(Type),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.I4,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IUnknown)null,true)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperties.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperty rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.DocumentPropertyJCW.getDocumentPropertyFromComPtr(_v[6].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperty add(String Name,boolean LinkToContent) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Name,false),
      new com.inzoom.comjni.Variant(LinkToContent,false),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.I4,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IUnknown)null,true)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperties.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperty rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.DocumentPropertyJCW.getDocumentPropertyFromComPtr(_v[6].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IUnknown)null,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperties.IID);
    com.inzoom.comjni.IUnknown rv = com.inzoom.comjni.jcw.IUnknownJCW.getIUnknownFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.IDispatch getApplication() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperties.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getCreator() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.DocumentProperties.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
