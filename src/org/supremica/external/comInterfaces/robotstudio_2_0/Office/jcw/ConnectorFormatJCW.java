package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface ConnectorFormat Implementation
public class ConnectorFormatJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw._IMsoDispObjJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.ConnectorFormat {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ConnectorFormat getConnectorFormatFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ConnectorFormatJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ConnectorFormat getConnectorFormatFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ConnectorFormatJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ConnectorFormat getConnectorFormatFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ConnectorFormatJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ConnectorFormat convertComPtrToConnectorFormat(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ConnectorFormatJCW(comPtr,true,releaseComPtr); }
  protected ConnectorFormatJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ConnectorFormatJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ConnectorFormat.IID); }
  protected ConnectorFormatJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected ConnectorFormatJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ConnectorFormat.IID); }
  protected ConnectorFormatJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected ConnectorFormatJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ConnectorFormat.IID,releaseComPtr);}
  protected ConnectorFormatJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ConnectorFormat.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void beginConnect(org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape ConnectedShape,int ConnectionSite) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)ConnectedShape,false),
      new com.inzoom.comjni.Variant(ConnectionSite,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ConnectorFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void beginDisconnect() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ConnectorFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void endConnect(org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape ConnectedShape,int ConnectionSite) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)ConnectedShape,false),
      new com.inzoom.comjni.Variant(ConnectionSite,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ConnectorFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void endDisconnect() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ConnectorFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getBeginConnected() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ConnectorFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape getBeginConnectedShape() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ConnectorFormat.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ShapeJCW.getShapeFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getBeginConnectionSite() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ConnectorFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getEndConnected() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ConnectorFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape getEndConnectedShape() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ConnectorFormat.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ShapeJCW.getShapeFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getEndConnectionSite() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ConnectorFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getType() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ConnectorFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setType(int Type) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Type,false)
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ConnectorFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
