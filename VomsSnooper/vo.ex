
<IDCard CIC_ID="1530" Name="BLAH.org" Serial="158" Status="Production" Alias="BLAH.org" GridId="">
    <EnrollmentUrl>https://voms.gridpp.ac.uk:8443/voms/BLAH.org/StartRegistration.do</EnrollmentUrl>
    <Description>The T2K experiment is the first super-beam neutrino oscillation experiment which hopes to measure the third and final lepton mixing angle.</Description>
    <Middlewares ARC="0" gLite="1" UNICORE="0" GLOBUS="0"/>
    <gLiteConf>
        <FQANs>
            <FQAN IsGroupUsed="1" GroupType="Software Manager">
                <FqanExpr>/BLAH.org/Role=lcgadmin</FqanExpr>
                <Description>Software manager </Description>
                <ComputingShare>10</ComputingShare>
            </FQAN>
            <FQAN IsGroupUsed="1" GroupType="Production Manager">
                <FqanExpr>/BLAH.org/Role=production</FqanExpr>
                <Description>Production team responsible for major processing</Description>
                <ComputingShare>90</ComputingShare>
            </FQAN>
        </FQANs>
        <VOMSServers>
            <VOMS_Server HttpsPort="8443" VomsesPort="15003" IsVomsAdminServer="1" MembersListUrl="https://voms.gridpp.ac.uk:8443/voms/BLAH.org/services/VOMSAdmin?method=listMembers">
                <hostname>voms.gridpp.ac.uk</hostname>
                <X509Cert>
                    <DN>/C=UK/O=eScience/OU=Manchester/L=HEP/CN=voms.gridpp.ac.uk</DN>
                    <CA_DN>/C=UK/O=eScienceCA/OU=Authority/CN=UK e-Science CA 2B</CA_DN>
                    <X509PublicKey>-----BEGIN CERTIFICATE-----
ANY OLD RUBBISH
uqXrOG5Jg1y1ibOnVhjf5r5xpFSs3g==
-----END CERTIFICATE-----
</X509PublicKey>
                    <SerialNumber>34339</SerialNumber>
                </X509Cert>
            </VOMS_Server>
        </VOMSServers>
    </gLiteConf>
</IDCard>




