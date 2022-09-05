
package org.alicebot.ab;

import java.util.Set;
import org.alicebot.ab.AIMLProcessor;
import org.alicebot.ab.AIMLProcessorExtension;
import org.alicebot.ab.Contact;
import org.alicebot.ab.ParseState;
import org.alicebot.ab.Utilities;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PCAIMLProcessorExtension
implements AIMLProcessorExtension {
    public Set<String> extensionTagNames = Utilities.stringSet("contactid", "multipleids", "displayname", "dialnumber", "emailaddress", "contactbirthday", "addinfo");

    @Override
    public Set<String> extensionTagSet() {
        return this.extensionTagNames;
    }

    private String newContact(Node node, ParseState ps) {
        NodeList childList = node.getChildNodes();
        String emailAddress = "unknown";
        String displayName = "unknown";
        String dialNumber = "unknown";
        String emailType = "unknown";
        String phoneType = "unknown";
        String birthday = "unknown";
        for (int i = 0; i < childList.getLength(); ++i) {
            if (childList.item(i).getNodeName().equals("birthday")) {
                birthday = AIMLProcessor.evalTagContent(childList.item(i), ps, null);
            }
            if (childList.item(i).getNodeName().equals("phonetype")) {
                phoneType = AIMLProcessor.evalTagContent(childList.item(i), ps, null);
            }
            if (childList.item(i).getNodeName().equals("emailtype")) {
                emailType = AIMLProcessor.evalTagContent(childList.item(i), ps, null);
            }
            if (childList.item(i).getNodeName().equals("dialnumber")) {
                dialNumber = AIMLProcessor.evalTagContent(childList.item(i), ps, null);
            }
            if (childList.item(i).getNodeName().equals("displayname")) {
                displayName = AIMLProcessor.evalTagContent(childList.item(i), ps, null);
            }
            if (!childList.item(i).getNodeName().equals("emailaddress")) continue;
            emailAddress = AIMLProcessor.evalTagContent(childList.item(i), ps, null);
        }
        System.out.println("Adding new contact " + displayName + " " + phoneType + " " + dialNumber + " " + emailType + " " + emailAddress + " " + birthday);
        Contact contact = new Contact(displayName, phoneType, dialNumber, emailType, emailAddress, birthday);
        return "";
    }

    private String contactId(Node node, ParseState ps) {
        String displayName = AIMLProcessor.evalTagContent(node, ps, null);
        String result = Contact.contactId(displayName);
        return result;
    }

    private String multipleIds(Node node, ParseState ps) {
        String contactName = AIMLProcessor.evalTagContent(node, ps, null);
        String result = Contact.multipleIds(contactName);
        return result;
    }

    private String displayName(Node node, ParseState ps) {
        String id = AIMLProcessor.evalTagContent(node, ps, null);
        String result = Contact.displayName(id);
        return result;
    }

    private String dialNumber(Node node, ParseState ps) {
        NodeList childList = node.getChildNodes();
        String id = "unknown";
        String type = "unknown";
        for (int i = 0; i < childList.getLength(); ++i) {
            if (childList.item(i).getNodeName().equals("id")) {
                id = AIMLProcessor.evalTagContent(childList.item(i), ps, null);
            }
            if (!childList.item(i).getNodeName().equals("type")) continue;
            type = AIMLProcessor.evalTagContent(childList.item(i), ps, null);
        }
        String result = Contact.dialNumber(type, id);
        return result;
    }

    private String emailAddress(Node node, ParseState ps) {
        NodeList childList = node.getChildNodes();
        String id = "unknown";
        String type = "unknown";
        for (int i = 0; i < childList.getLength(); ++i) {
            if (childList.item(i).getNodeName().equals("id")) {
                id = AIMLProcessor.evalTagContent(childList.item(i), ps, null);
            }
            if (!childList.item(i).getNodeName().equals("type")) continue;
            type = AIMLProcessor.evalTagContent(childList.item(i), ps, null);
        }
        String result = Contact.emailAddress(type, id);
        return result;
    }

    private String contactBirthday(Node node, ParseState ps) {
        String id = AIMLProcessor.evalTagContent(node, ps, null);
        String result = Contact.birthday(id);
        return result;
    }

    @Override
    public String recursEval(Node node, ParseState ps) {
        try {
            String nodeName = node.getNodeName();
            if (nodeName.equals("contactid")) {
                return this.contactId(node, ps);
            }
            if (nodeName.equals("multipleids")) {
                return this.multipleIds(node, ps);
            }
            if (nodeName.equals("dialnumber")) {
                return this.dialNumber(node, ps);
            }
            if (nodeName.equals("addinfo")) {
                return this.newContact(node, ps);
            }
            if (nodeName.equals("displayname")) {
                return this.displayName(node, ps);
            }
            if (nodeName.equals("emailaddress")) {
                return this.emailAddress(node, ps);
            }
            if (nodeName.equals("contactbirthday")) {
                return this.contactBirthday(node, ps);
            }
            return AIMLProcessor.genericXML(node, ps);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }
}

