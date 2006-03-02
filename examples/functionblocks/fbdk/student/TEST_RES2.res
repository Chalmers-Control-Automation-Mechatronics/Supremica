<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE ResourceType SYSTEM "http://www.holobloc.com/xml/LibraryElement.dtd" >
<ResourceType xmlns="http://www.holobloc.com/xml/LibraryElement" Name="TEST_RES2" Comment="Like TEST_RES but with PUBLISH, SUBSCRIBE" >
  <Identification Standard="61499-2" />
  <VersionInfo Organization="Rockwell Automation" Version="0.0" Author="JHC" Date="2004-06-08" />
  <CompilerInfo header="package fb.rt.student; import fb.rt.net.*; import fb.rt.math.*;" >
  </CompilerInfo>
  <VarDeclaration Name="INPUT_ID" Type="WSTRING" InitialValue="&#34;225.0.0.1:1025&#34;" Comment="UDP hostport for inputs" />
  <VarDeclaration Name="OUTPUT_ID" Type="WSTRING" InitialValue="&#34;225.0.0.1:1026&#34;" Comment="UDP hostport for output" />
  <FBTypeName Name="FB_ADD_REAL" />
  <FBNetwork >
    <FB Name="START" Type="E_RESTART" x="72.22222" y="11.111111" />
    <FB Name="INPUTS" Type="SUBSCRIBE_2" x="716.6666" y="11.111111" />
    <FB Name="ADDER" Type="FB_ADD_REAL" x="1411.1111" y="205.55554" />
    <FB Name="OUTPUT" Type="PUBLISH_1" x="2338.889" y="11.111111" />
    <EventConnections>
      <Connection Source="START.COLD" Destination="INPUTS.INIT" dx1="38.888885" />
      <Connection Source="START.WARM" Destination="INPUTS.INIT" dx1="66.666664" />
      <Connection Source="INPUTS.INITO" Destination="OUTPUT.INIT" dx1="127.77777" />
      <Connection Source="INPUTS.IND" Destination="ADDER.REQ" dx1="211.1111" />
      <Connection Source="ADDER.CNF" Destination="OUTPUT.REQ" dx1="66.666664" />
    </EventConnections>
    <DataConnections>
      <Connection Source="1" Destination="INPUTS.QI" dx1="650.0" />
      <Connection Source="1" Destination="OUTPUT.QI" dx1="2272.2222" />
      <Connection Source="INPUTS.RD_1" Destination="ADDER.IN1" dx1="200.0" />
      <Connection Source="INPUTS.RD_2" Destination="ADDER.IN2" dx1="227.77777" />
      <Connection Source="ADDER.OUT" Destination="OUTPUT.SD_1" dx1="33.333332" />
      <Connection Source="INPUT_ID" Destination="INPUTS.ID" dx1="538.88885" />
      <Connection Source="OUTPUT_ID" Destination="OUTPUT.ID" dx1="2127.7776" />
    </DataConnections>
  </FBNetwork>
</ResourceType>
