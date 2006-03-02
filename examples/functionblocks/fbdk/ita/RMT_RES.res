<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE ResourceType SYSTEM "http://www.holobloc.com/xml/LibraryElement.dtd" >
<ResourceType xmlns="http://www.holobloc.com/xml/LibraryElement" Name="RMT_RES" Comment="Resource for Remote Device Management" >
  <Identification Standard="61499 ITA" Description="Resource for Remote Device Management" />
  <VersionInfo Organization="Rockwell Automation" Version="0.0" Author="JHC" Date="2001-01-08" />
  <CompilerInfo header="package fb.rt;" >
    <Compiler Language="Java" Vendor="IBM" Product="VisualAge" Version="3.0" />
  </CompilerInfo>
  <VarDeclaration Name="MGR_ID" Type="WSTRING" InitialValue="&#34;localhost:61499&#34;" Comment="Device manager socket ID" />
  <FBNetwork >
    <FB Name="START" Type="E_RESTART" x="129.4118" y="11.7647" />
    <FB Name="MGR_FF" Type="E_SR" x="729.4117" y="11.7647" />
    <FB Name="KERNEL" Type="DM_KRNL" x="1264.7058" y="11.7647" />
    <EventConnections>
      <Connection Source="START.COLD" Destination="MGR_FF.S" dx1="29.4118" dx2="47.0588" dy="-70.5882" />
      <Connection Source="START.WARM" Destination="MGR_FF.S" dx1="64.7059" dx2="76.4706" dy="-188.2353" />
      <Connection Source="START.STOP" Destination="MGR_FF.R" dx1="117.6471" dx2="100" dy="-311.7647" />
      <Connection Source="MGR_FF.EO" Destination="KERNEL.INIT" dx1="52.9412" />
    </EventConnections>
    <DataConnections>
      <Connection Source="MGR_FF.Q" Destination="KERNEL.QI" dx1="47.0588" />
      <Connection Source="MGR_ID" Destination="KERNEL.ID" dx1="1100" />
    </DataConnections>
  </FBNetwork>
</ResourceType>
