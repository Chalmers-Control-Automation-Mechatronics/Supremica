package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface _CommandBarComboBox Implementation
public class _CommandBarComboBoxJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox get_CommandBarComboBoxFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _CommandBarComboBoxJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox get_CommandBarComboBoxFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _CommandBarComboBoxJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox get_CommandBarComboBoxFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new _CommandBarComboBoxJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox convertComPtrTo_CommandBarComboBox(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _CommandBarComboBoxJCW(comPtr,true,releaseComPtr); }
  protected _CommandBarComboBoxJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected _CommandBarComboBoxJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox.IID); }
  protected _CommandBarComboBoxJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected _CommandBarComboBoxJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox.IID); }
  protected _CommandBarComboBoxJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected _CommandBarComboBoxJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox.IID,releaseComPtr);}
  protected _CommandBarComboBoxJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public void addItem(String Text,com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Text,false),
      createVTblVArg(Index)
    };
    vtblCall(332,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void addItem(String Text) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Text,false),
      createVTblVArg(noParam)
    };
    vtblCall(332,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void clear() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(336,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getDropDownLines() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(340,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setDropDownLines(int pcLines) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pcLines,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(344,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getDropDownWidth() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(348,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setDropDownWidth(int pdx) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pdx,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(352,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getList(int Index) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Index,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(356,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox.IID);
    String rv = _v[1].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setList(int Index,String pbstrItem) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Index,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant(pbstrItem,false)
    };
    vtblCall(360,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getListCount() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(364,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getListHeaderCount() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(368,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setListHeaderCount(int pcItems) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pcItems,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(372,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getListIndex() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(376,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setListIndex(int pi) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pi,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(380,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void removeItem(int Index) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Index,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(384,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getStyle() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(388,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setStyle(int pstyle) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pstyle,false)
    };
    vtblCall(392,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getText() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(396,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setText(String pbstrText) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pbstrText,false)
    };
    vtblCall(400,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
