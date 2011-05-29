/*
 * Copyright 2011 Jeffrey Coble <jeffrey.a.coble@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.engineeringnotebook.glassfishmonitor.instance;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Attr;
import org.xml.sax.InputSource;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.engineeringnotebook.glassfishmonitor.status.Status;

/**
 * Uses a DOM parser to extract meaningful status information from the GF
 * server's XML response to the rest GET query
 * 
 * @author Jeffrey Coble <jeffrey.a.coble@gmail.com> http://engineeringnotebook.org
 */
public class InstanceListStatusHandler {
    
    private static final Logger logger = Logger.getLogger(InstanceListStatusHandler.class.getName());
    
    /**
     * 
     * @param xmlData The xml data received from the GF server
     * @return The status for each cluster
     */    
    public ArrayList<Status> parseXMLData(String xmlData) {
        ArrayList<Status> instanceList = null;
        Document dom = createDOM(xmlData);
        instanceList = parseDOM(dom);
        
        return instanceList;   
    }
    
    private Document createDOM(String xmlData){
        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document dom = null;

        try {

                //Factory returns an instance of document builder
                DocumentBuilder db = dbf.newDocumentBuilder();
                InputSource is = new InputSource(new StringReader(xmlData));
        
                //Creat DOM representation of the XML data
                dom = db.parse(is);


        }catch(ParserConfigurationException pce) {
            pce.printStackTrace();
        }catch(SAXException se) {
            se.printStackTrace();
        }catch(IOException ioe) {
            ioe.printStackTrace();
        }
        
        return dom;
    }
    
    /**
     * This method is the entry point for parsing the specific XML structure 
     * of the server response message
     * 
     * @param dom The document object model
     * @return An ArrayList of status objects for the clusters
     */    
    private ArrayList<Status> parseDOM(Document dom){
        ArrayList<Status> instanceList = null;
        
        //get the root element
        Element element = dom.getDocumentElement();

        //get a nodelist of elements
    
        NodeList nl = element.getElementsByTagName("list");
        if(nl != null && nl.getLength() > 0) {
            for(int i = 0 ; i < nl.getLength();i++) {
                //get the list element
                Element entry = (Element)nl.item(i);
                instanceList = getInstances(entry);
            }
        }
        return instanceList;
    }
    
    private ArrayList<Status> getInstances(Element entry) {
        ArrayList<Status> instanceList = null;
        NodeList nl = entry.getElementsByTagName("map");
        if(nl != null && nl.getLength() > 0) {
            for(int i = 0 ; i < nl.getLength();i++) {
                Element map = (Element)nl.item(i);
                instanceList = processMapEntity(map);
                break;
            }
        }
        return instanceList;
    }
    
    private ArrayList<Status> processMapEntity(Element map) {
        ArrayList<Status> instanceList = new ArrayList();
        InstanceStatus iStatus = new InstanceStatus(); 
        instanceList.add(iStatus);
        NodeList nl = map.getElementsByTagName("entry");
        if(nl != null && nl.getLength() > 0) {
            for(int i = 0 ; i < nl.getLength();i++) {  
                Element entry = (Element)nl.item(i);
                processEntryEntity(entry, iStatus); 
            }
        }
        
        return instanceList;
    }
    
    private InstanceStatus processEntryEntity(Element entry, InstanceStatus iStatus) {
        
        if(entry.hasAttribute("key")) {
            Attr keyAttr = entry.getAttributeNode("key");
            logger.log(Level.FINEST, "Key = {0}", new Object[]{keyAttr.getValue()});
            if("status".equals(keyAttr.getValue())) {
                processInstanceStatus(entry, iStatus);
            }
            else if("name".equals(keyAttr.getValue())) {
                processInstanceName(entry, iStatus);
            }
            else if("uptime".equals(keyAttr.getValue())) {
                processInstanceUptime(entry, iStatus);
            }
        }

        return iStatus;
    }
    
    private void processInstanceStatus(Element entry, InstanceStatus iStatus) {
        if(entry.hasAttribute("value")) {
            Attr valueAttr = entry.getAttributeNode("value");
            logger.log(Level.FINEST, "Value = {0}", new Object[]{valueAttr.getValue()});
            iStatus.setInstanceStatus(valueAttr.getValue());
        }                   
    }

    private void processInstanceName(Element entry, InstanceStatus iStatus) {
        if(entry.hasAttribute("value")) {
            Attr valueAttr = entry.getAttributeNode("value");
            logger.log(Level.FINEST, "Value = {0}", new Object[]{valueAttr.getValue()});
            iStatus.setInstanceName(valueAttr.getValue());
        }                   
    }

    private void processInstanceUptime(Element entry, InstanceStatus iStatus) {
        
        NodeList nl = entry.getElementsByTagName("number");
        if(nl != null && nl.getLength() > 0) {
            for(int i = 0 ; i < nl.getLength();i++) {  
                Element number = (Element)nl.item(i);
                logger.log(Level.FINEST, "Value = {0}", new Object[]{number.getTextContent()});
                iStatus.setInstanceUptime(number.getTextContent());                 
            }
        }              
    }
}
