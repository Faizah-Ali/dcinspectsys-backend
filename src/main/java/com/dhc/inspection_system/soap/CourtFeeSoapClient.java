package com.dhc.inspection_system.soap;

import com.dhc.inspection_system.dto.CourtFeeQueryResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPPart;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Jakarta SAAJ client for SHCIL EcfSWS (legacy PaymentGatewayAction lock/query).
 */
@Component
public class CourtFeeSoapClient {

    private static final Logger log = LoggerFactory.getLogger(CourtFeeSoapClient.class);

    private static final String ECF_NAMESPACE = "http://ecfws/";
    private static final String MSG_VERSION = "ECFSHCIL001";
    private static final String RECEIPT_NOT_LOCKED = "Receipt not locked";
    private static final String VALID_COURT_FEE = "VALID COURT FEE";

    @Value("${inspection.courtfee.endpoint}")
    private String endpoint;

    @Value("${inspection.courtfee.username}")
    private String username;

    @Value("${inspection.courtfee.password}")
    private String password;

    @Value("${inspection.courtfee.lock-user}")
    private String lockUser;

    /**
     * Builds LOCKRQ XML, invokes ecflock, returns RPSTATUS (legacy lockcourtFee).
     */
    public String lockCourtFee(String diaryNo, String diaryYr, String receiptNo) throws Exception {
        String lockRequestXml = buildLockRequestXml(diaryNo, diaryYr, receiptNo);
        String soapResponseXml = invokeSoapOperation("ecflock", "v_Ecf_Lock_Req", lockRequestXml);
        Map<String, String> resultMap = processResults(soapResponseXml);
        if (resultMap == null) {
            return null;
        }
        return resultMap.get("RPSTATUS");
    }

    /**
     * Legacy PaymentGatewayAction.getcourtFeeDetails — CERTRQ / ecfreq.
     * Does not throw; network/XML failures return error("NETWORK ISSUE").
     */
    public CourtFeeQueryResult queryCourtFee(String receiptNo) {
        if (receiptNo == null || receiptNo.isBlank()) {
            return CourtFeeQueryResult.skip();
        }

        try {
            String certRequestXml = buildCertRequestXml(receiptNo.trim());
            String soapResponseXml = invokeSoapOperation("ecfreq", "v_Ecf_Req", certRequestXml);
            Map<String, String> resultMap = processResults(soapResponseXml);

            if (resultMap == null) {
                return CourtFeeQueryResult.error("NETWORK ISSUE");
            }

            String rpStatus = resultMap.get("RPSTATUS");
            if (rpStatus == null) {
                rpStatus = "";
            }

            String status = resultMap.get("STATUS");

            if (rpStatus.equalsIgnoreCase("SUCCESS")) {
                if (status != null && status.equalsIgnoreCase(RECEIPT_NOT_LOCKED)) {
                    String amount = resultMap.get("CFAMT");
                    return CourtFeeQueryResult.success(amount, false, VALID_COURT_FEE);
                }
                return CourtFeeQueryResult.error(status);
            }

            return CourtFeeQueryResult.error(status);
        } catch (Exception ex) {
            log.error("PAYMENT TOKEN ERROR :-{}", ex.toString(), ex);
            return CourtFeeQueryResult.error("NETWORK ISSUE");
        }
    }

    String buildLockRequestXml(String diaryNo, String diaryYr, String receiptNo) throws Exception {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHH:mm:ss");
        String strDate = sdfDate.format(new Date());

        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        fact.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder parser = fact.newDocumentBuilder();
        Document doc = parser.newDocument();

        Node root = doc.createElement("ECFWTXN");
        doc.appendChild(root);

        Node header = doc.createElement("TXNHDR");
        root.appendChild(header);

        Node msgver = doc.createElement("MSGVER");
        msgver.appendChild(doc.createTextNode(MSG_VERSION));
        header.appendChild(msgver);

        Node msgtp = doc.createElement("MSGTP");
        msgtp.appendChild(doc.createTextNode("LOCKRQ"));
        header.appendChild(msgtp);

        Node sendtm = doc.createElement("SENDTM");
        sendtm.appendChild(doc.createTextNode(strDate));
        header.appendChild(sendtm);

        Node lckusr = doc.createElement("LCKUSR");
        lckusr.appendChild(doc.createTextNode(lockUser));
        header.appendChild(lckusr);

        Node dirno = doc.createElement("DIRNO");
        dirno.appendChild(doc.createTextNode(diaryNo));
        header.appendChild(dirno);

        Node certtot = doc.createElement("CERTTOT");
        certtot.appendChild(doc.createTextNode("1"));
        header.appendChild(certtot);

        Node locktxn = doc.createElement("LOCKTXN");
        root.appendChild(locktxn);

        Node lockrqdtdl = doc.createElement("LOCKRQDTL");
        locktxn.appendChild(lockrqdtdl);

        Node srno = doc.createElement("SRNO");
        srno.appendChild(doc.createTextNode("1"));
        lockrqdtdl.appendChild(srno);

        Node rcptno = doc.createElement("RCPTNO");
        rcptno.appendChild(doc.createTextNode(receiptNo.trim()));
        lockrqdtdl.appendChild(rcptno);

        Node diryear = doc.createElement("DIRYEAR");
        diryear.appendChild(doc.createTextNode(diaryYr));
        lockrqdtdl.appendChild(diryear);

        return documentToString(doc);
    }

    String buildCertRequestXml(String receiptNo) throws Exception {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHH:mm:ss");
        String strDate = sdfDate.format(new Date());

        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        fact.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder parser = fact.newDocumentBuilder();
        Document doc = parser.newDocument();

        Node root = doc.createElement("ECFWTXN");
        doc.appendChild(root);

        Node header = doc.createElement("TXNHDR");
        root.appendChild(header);

        Node msgver = doc.createElement("MSGVER");
        msgver.appendChild(doc.createTextNode(MSG_VERSION));
        header.appendChild(msgver);

        Node msgtp = doc.createElement("MSGTP");
        msgtp.appendChild(doc.createTextNode("CERTRQ"));
        header.appendChild(msgtp);

        Node sendtm = doc.createElement("SENDTM");
        sendtm.appendChild(doc.createTextNode(strDate));
        header.appendChild(sendtm);

        Node certrqdtdl = doc.createElement("CERTRQDTL");
        root.appendChild(certrqdtdl);

        Node rcptno = doc.createElement("RCPTNO");
        rcptno.appendChild(doc.createTextNode(receiptNo));
        certrqdtdl.appendChild(rcptno);

        return documentToString(doc);
    }

    private String invokeSoapOperation(
            String operationLocalName,
            String thirdParamLocalName,
            String thirdParamValue
    ) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage request = messageFactory.createMessage();
        SOAPPart soapPart = request.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("ecf", ECF_NAMESPACE);

        SOAPBody body = envelope.getBody();
        SOAPElement operation = body.addChildElement(operationLocalName, "ecf");

        // Axis WSDL params use empty namespace (unqualified children).
        SOAPElement vUsr = operation.addChildElement("v_Usr", "", "");
        vUsr.addTextNode(username);

        SOAPElement vPwd = operation.addChildElement("v_Pwd", "", "");
        vPwd.addTextNode(password);

        SOAPElement third = operation.addChildElement(thirdParamLocalName, "", "");
        third.addTextNode(thirdParamValue);

        request.saveChanges();

        SOAPConnectionFactory connectionFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection connection = connectionFactory.createConnection();
        try {
            log.debug("Invoking SHCIL {} at {}", operationLocalName, endpoint);
            SOAPMessage response = connection.call(request, endpoint);
            return extractReturnString(response);
        } finally {
            connection.close();
        }
    }

    private String extractReturnString(SOAPMessage response) throws Exception {
        if (response.getSOAPBody().hasFault()) {
            String fault = response.getSOAPBody().getFault().getFaultString();
            throw new IllegalStateException("SHCIL SOAP fault: " + fault);
        }

        SOAPBody body = response.getSOAPBody();
        Iterator<?> children = body.getChildElements();
        while (children.hasNext()) {
            Object next = children.next();
            if (!(next instanceof SOAPElement element)) {
                continue;
            }
            Iterator<?> inner = element.getChildElements();
            while (inner.hasNext()) {
                Object innerNext = inner.next();
                if (!(innerNext instanceof SOAPElement child)) {
                    continue;
                }
                String localName = child.getElementQName() != null
                        ? child.getElementQName().getLocalPart()
                        : child.getLocalName();
                if ("return".equals(localName)
                        || "ecflockReturn".equals(localName)
                        || "ecfreqReturn".equals(localName)) {
                    String value = child.getValue();
                    if (value == null || value.isBlank()) {
                        value = child.getTextContent();
                    }
                    return value;
                }
            }
            // Some stacks put the string directly under the response wrapper.
            String text = element.getTextContent();
            if (text != null && !text.isBlank()) {
                return text.trim();
            }
        }

        throw new IllegalStateException("SHCIL SOAP response did not contain a return value");
    }

    /**
     * Legacy PaymentGatewayAction.processResults — flattens element text by tag name.
     */
    Map<String, String> processResults(String result) {
        Map<String, String> returnMap = new HashMap<>();
        if (result == null) {
            return null;
        }

        String sanitized = result.replace("&", "AND");
        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            fact.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder parser = fact.newDocumentBuilder();
            Document doc = parser.parse(new ByteArrayInputStream(sanitized.getBytes()));
            Element root = doc.getDocumentElement();
            NodeList nodeLst = root.getChildNodes();
            if (nodeLst != null) {
                for (int counter = 0; counter < nodeLst.getLength(); counter++) {
                    visitRecursively(nodeLst.item(counter), returnMap);
                }
            }
        } catch (Exception ex) {
            log.error("ERROR processResults() {}", ex.toString(), ex);
            return null;
        }
        return returnMap;
    }

    private void visitRecursively(Node node, Map<String, String> mp) {
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node childNode = list.item(i);
            if (childNode.getNodeName() != null) {
                String text = childNode.getTextContent();
                mp.put(childNode.getNodeName(), text == null ? "" : text.trim());
            }
            visitRecursively(childNode, mp);
        }
    }

    private String documentToString(Document doc) throws Exception {
        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
        transformer.transform(domSource, result);
        return writer.toString();
    }
}
