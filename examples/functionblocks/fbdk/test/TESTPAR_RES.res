<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE ResourceType SYSTEM "http://www.holobloc.com/xml/LibraryElement.dtd" >
<ResourceType xmlns="http://www.holobloc.com/xml/LibraryElement" Name="TESTPAR_RES" Comment="Test Java generation of FB parameters in a resource type." >
  <Identification Standard="61499-1" />
  <VersionInfo Organization="Rockwell Automation" Version="0.0" Author="JHC" Date="2004-12-16" />
  <CompilerInfo header="package fb.rt.test; import fb.rt.math.*; import fb.rt.net.*;" >
    <Compiler Language="Java" Vendor="Sun" Product="JDK" Version="1.4.2" />
  </CompilerInfo>
  <VarDeclaration Name="SUBSC_ID" Type="WSTRING" />
  <VarDeclaration Name="PUBL_ID" Type="WSTRING" />
  <FBNetwork >
    <FB Name="START" Type="E_RESTART" x="72.22222" y="11.111111" />
    <FB Name="ADD1" Type="FB_ADD_REAL" x="1311.1111" y="122.22221" >
      <Parameter Name="IN2" Value="5.0" />
    </FB>
    <FB Name="SUBSC" Type="SUBSCRIBE_1" x="683.3333" y="11.111111" >
      <Parameter Name="QI" Value="1" />
    </FB>
    <FB Name="PUBL" Type="PUBLISH_1" x="2094.4443" y="11.111111" >
      <Parameter Name="QI" Value="1" />
    </FB>
    <EventConnections>
      <Connection Source="START.COLD" Destination="SUBSC.INIT" dx1="33.333332" />
      <Connection Source="SUBSC.IND" Destination="ADD1.REQ" dx1="111.11111" />
      <Connection Source="ADD1.CNF" Destination="PUBL.REQ" dx1="50.0" />
      <Connection Source="SUBSC.INITO" Destination="PUBL.INIT" dx1="122.22221" />
    </EventConnections>
    <DataConnections>
      <Connection Source="SUBSC_ID" Destination="SUBSC.ID" dx1="494.44443" />
      <Connection Source="SUBSC.RD_1" Destination="ADD1.IN1" dx1="27.777777" />
      <Connection Source="PUBL_ID" Destination="PUBL.ID" dx1="1927.7777" />
      <Connection Source="ADD1.OUT" Destination="PUBL.SD_1" dx1="27.777777" />
    </DataConnections>
  </FBNetwork>
</ResourceType>
