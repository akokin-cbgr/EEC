import net.sf.saxon.s9api.*;

import java.io.*;
import java.util.*;

class Scratch {
    public static void main(String[] args) {
        new Scratch().go();
    }

    private void go() {
        Processor processor = new Processor(false);
        DocumentBuilder builder = processor.newDocumentBuilder();
        try {

            XdmNode node = builder.build(new File("D:\\projects\\eek\\scratch_xml\\P.DS.06.TRN.001_MSG.001.xml"));

            XPathCompiler compiler = processor.newXPathCompiler();
            getNamespaces().forEach(compiler::declareNamespace);

            String xpath = " every $x in //fpcdo:AntiDumpingDutyDetails " +
                    "satisfies $x/fpcdo:GenericDistributableAntiDumpingDutyDetails[fpsdo:TotalAmountIndicator= '1']/fpsdo:AntiDumpingGenericDistributableDutyAmount/xs:decimal(text()) " +
                    "= sum($x/fpcdo:GenericDistributableAntiDumpingDutyDetails[fpsdo:TotalAmountIndicator= '0']/fpsdo:AntiDumpingGenericDistributableDutyAmount/xs:decimal(text()))";

            /*
            String xpath = "not((year-from-date(xs:date(//csdo:DocValidityDate))-year-from-date(xs:date(//csdo:DocStartDate)))>=1 and " +
                    "((month-from-date(xs:date(//csdo:DocValidityDate))-month-from-date(xs:date(//csdo:DocStartDate)))>=1 or " +
                    "((month-from-date(xs:date(//csdo:DocValidityDate))-month-from-date(xs:date(//csdo:DocStartDate)))=0 and " +
                    "(day-from-date(xs:date(//csdo:DocValidityDate))-day-from-date(xs:date(//csdo:DocStartDate)))>=1))) ";
                     */
//            String xpath = "every $x in //ccdo:EDocHeader " +
//                    "scratch_xmlatisfies matches($x/csdo:EDocDateTime/text(),'\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{1,3}Z')";

            //String xpath = " //ctcdo:TurnoverGoodsDetails/ctsdo:GTIN[text()='51378436717868']/text()";


            /*String ex = " every $x in //fpcdo:PaymentImportDutyInfoReportDetails " +
                    " satisfies $x[" +
                    " boolean( " +
                    "(if (boolean($x/fpsdo:EndCarryOverDutyAmount))" +
                    " then xs:double($x/fpsdo:EndCarryOverDutyAmount)" +
                    " else 0) " +
                    " =" +
                    " (round(((if (boolean($x/fpsdo:BeginCarryOverDutyAmount)) " +
                    " then  xs:double($x/fpsdo:BeginCarryOverDutyAmount) " +
                    " else 0)" +
                    " + (if(boolean($x/fpsdo:CollectedDutyAmount))" +
                    " then xs:double($x/fpsdo:CollectedDutyAmount)" +
                    " else 0)" +
                    " - (if(boolean($x/fpcdo:DocumentDutyDetails/fpsdo:DocumentDutyAmount))" +
                    " then xs:double($x/fpcdo:DocumentDutyDetails/fpsdo:DocumentDutyAmount)" +
                    " else 0 )" +
                    " - (if(boolean($x/fpsdo:RefundDutyAmount))" +
                    " then xs:double($x/fpsdo:RefundDutyAmount)" +
                    " else 0 ))*1000) div 1000)" +
                    ")" +
                    "]";*/

            //String ex = "xs:date(//fpcdo:PaymentImportDutyInfoReportDetails/fpsdo:ReportDate)>xs:date(//fpcdo:PaymentImportDutyInfoReportDetails/csdo:EventDate)" ;

            //String ex = "if(count(//trcdo:MeasurementStandardDetails)=1) then (count((//trcdo:MeasurementStandardDetails/trcdo:MetrologicalCharacteristicDetails/trsdo:MeasurementUnitCode/text(),//trcdo:MeasurementStandardDetails/trcdo:MetrologicalCharacteristicDetails/trsdo:MeasurementUnitName/text()))=1) else true()";
            //String ex = "if(count(//trcdo:MeasurementStandardDetails)=1) then (every $x in //trcdo:MeasurementsStandardDetails//csdo:UnifiedCountryCode satisfies if(count($x/text())=1) then string-length(normalize-space($x/@codeListId))>0 else true()) else true()";
            //String ex = "every $x in //csdo:UnifiedCountryCode satisfies if(count($x/text())=1) then if(count( $x/@codeListId)=1) then true() else false() else true()";
            //String ex = "//cacdo:RegisterCustomsCarrierDetails[1]/casdo:RegisterCountryCode/text()=//cacdo:RegisterCustomsCarrierDetails[2]/casdo:RegisterCountryCode/text() and  //cacdo:RegisterCustomsCarrierDetails[1]/cacdo:RegisterDocumentDetails/casdo:RegistrationNumberIdentifier/text()=//cacdo:RegisterCustomsCarrierDetails[2]/cacdo:RegisterDocumentDetails/casdo:RegistrationNumberIdentifier/text()";
            //String ex = "every $x in //cacdo:RegisterCustomsCarrierDetails[1] satisfies count($x/casdo:RegisterCountryCode/text())=1";
            //String ex = "count(//*[name()!='casdo:RegisterCountryCode' and name()!='casdo:RegistrationNumberIdentifier' and text()=//casdo:RegisterCountryCode/text() and text()=//casdo:RegistrationNumberIdentifier/text()])=0 ";
            //String ex = "every $x in //csdo:EndDateTime satisfies $x[count($x)=0]";
            //String ex = "xs:date(format-dateTime(//csdo:StartDateTime, '[Y0001]-[M01]-[D01]'))";
            //String ex = "xs:date(//casdo:StartActivityDate)";
            //String ex = "//csdo:StartDateTime/xs:dateTime(text())";
            //String ex = "//csdo:StartDateTime/xs:date(xs:dateTime(text()))";
            //String ex = "every $x in //csdo:StartDateTime satisfies $x[string-length(normalize-space($x))>0 and xs:date(xs:dateTime($x/text()))=xs:date(//casdo:StartActivityDate)]";
            //String ex = "every $x in //hccdo:AccreditationCertificateDetails satisfies count($x/csdo:DescriptionText/text())>0";
            //String ex = "//hcsdo:ResearchKindCode[text()=('01','02','03')]/parent::*/hccdo:AuthorizedPartySubdivisionDetails/hccdo:AccreditationCertificateDetails/* ";
            //String ex = "//hcsdo:ResearchKindCode[text()=('04','05')]/parent::*/hccdo:OfficialDocDetails";

            //String ex = "every $x in //hccdo:AuthorizedPartyDetails | //hccdo:AuthorizedPartyManagerDetails satisfies count($x//ccdo:CommunicationDetails)>=2 ";
            //String ex = "if( every $x in //hccdo:AuthorizedPartyDetails | //hccdo:AuthorizedPartyManagerDetails satisfies count($x/*)>0 ) then (every $y in $x/*/ccdo:CommunicationDetails satisfies count($y/*) ) else(false())";


            //String ex = "every $x in //ccdo:SubjectAddressDetails/csdo:AddressKindCode satisfies $x=('1','2','3')";
            //String ex =  "//ccdo:SubjectAddressDetails/csdo:UnifiedCountryCode/text()";
            //String ex =  "count(//ccdo:SubjectAddressDetails/csdo:UnifiedCountryCode/text())=1";

            XdmValue evaluated = compiler
                    .evaluate(xpath, node);
            System.out.println(evaluated);
        } catch (SaxonApiException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> getNamespaces() {
        return new HashMap<String, String>() {{

            //75
            put("[]", "http://www.w3.org/XML/1998/namespace");
            put("xml", "http://www.w3.org/XML/1998/namespace");
            put("xmlns", "http://www.w3.org/2000/xmlns/");
            put("fn", "http://www.w3.org/2005/xpath-functions");
            put("wsa", "http://www.w3.org/2005/08/addressing");
            put("csdo", "urn:EEC:M:SimpleDataObjects:v0.4.4");
            put("ccdo", "urn:EEC:M:ComplexDataObjects:v0.4.4");
            put("doc", "urn:EEC:R:ProcessingResultDetails:v0.4.4");
            put("fpcdo", "urn:EEC:M:FP:ComplexDataObjects:v1.0.0");
            put("fpsdo", "urn:EEC:M:FP:SimpleDataObjects:v1.0.0");
            put("addr", "urn:EEC:R:FP:DS:06:AntiDumpingDutyReport:v1.0.0");
            put("vp", "urn:EEC:R:FP:DS:01:VerificationProtocol:v1.0.0");



        }};
    }

}