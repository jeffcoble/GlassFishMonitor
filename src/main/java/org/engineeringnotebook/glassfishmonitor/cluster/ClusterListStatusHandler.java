/*
 * Copyright 2011 Jeffrey Coble <jeffrey.a.coble@gmail.com>.
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
package org.engineeringnotebook.glassfishmonitor.cluster;

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
import org.engineeringnotebook.glassfishmonitor.status.StatusHandler;

/**
 *
 * @author Jeffrey Coble <jeffrey.a.coble@gmail.com> http://engineeringnotebook.org
 * 
 * Uses a DOM parser to extract meaningful status information from the GF
 * server's XML response to the rest GET query
 */
public class ClusterListStatusHandler implements StatusHandler {
    private static final Logger logger = Logger.getLogger(ClusterListStatusHandler.class.getName());
    
    /**
     * 
     * @param xmlData The xml data received from the GF server
     * @return The status for each cluster
     */
    public ArrayList<Status> parseXMLData(String xmlData) {
        ArrayList<Status> clusterStatusList = null;
        
        Document dom = createDOM(xmlData);
        clusterStatusList = parseDOM(dom);
        
        return clusterStatusList; 
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
        ArrayList<Status> clusterList = null;
        
        //get the root element
        Element element = dom.getDocumentElement();

        //get a nodelist of elements
    
        NodeList nl = element.getElementsByTagName("entry");
        if(nl != null && nl.getLength() > 0) {
            for(int i = 0 ; i < nl.getLength();i++) {
                //get the entry element
                Element entry = (Element)nl.item(i);
                if(entry.hasAttribute("key")) {
                    Attr attr = entry.getAttributeNode("key");
                    logger.log(Level.FINEST, "Attr Name = {0}, Attr Value = {1}", new Object[]{attr.getName(), attr.getValue()});
                    if(attr.getValue().equals("properties")) {
                        clusterList = getClusters(entry);
                        break;
                    }
                }
            }
        }
        return clusterList;
    }
    
    private ArrayList<Status> getClusters(Element entry) {
        ArrayList<Status> clusterList = null;
        NodeList nl = entry.getElementsByTagName("map");
        if(nl != null && nl.getLength() > 0) {
            for(int i = 0 ; i < nl.getLength();i++) {
                Element map = (Element)nl.item(i);
                clusterList = processMapEntity(map);
                break;
            }
        }
        return clusterList;
    }
    
    private ArrayList<Status> processMapEntity(Element map) {
        ArrayList<Status> clusterList = new ArrayList();
        NodeList nl = map.getElementsByTagName("entry");
        if(nl != null && nl.getLength() > 0) {
            for(int i = 0 ; i < nl.getLength();i++) {  
                Element entry = (Element)nl.item(i);
                ClusterStatus cStatus = processEntryEntity(entry);
                clusterList.add(cStatus);
            }
        }
        
        return clusterList;
    }
    
    private ClusterStatus processEntryEntity(Element entry) {
        
        ClusterStatus cStatus = new ClusterStatus();
        if(entry.hasAttribute("key")) {
            Attr attr = entry.getAttributeNode("key");
            logger.log(Level.FINEST, "Key = {0}", attr.getValue());
            cStatus.setClusterName(attr.getValue());
        }
        if(entry.hasAttribute("value")) {
            Attr attr = entry.getAttributeNode("value");
            logger.log(Level.FINEST, "Value = {0}", attr.getValue());
            cStatus.setClusterStatus(attr.getValue());
        }  
        
        return cStatus;
    }
    
}
