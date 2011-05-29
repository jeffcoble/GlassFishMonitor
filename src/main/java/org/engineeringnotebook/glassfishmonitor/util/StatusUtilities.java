/*
 * Copyright 2011 jcoble70.
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
package org.engineeringnotebook.glassfishmonitor.util;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 *
 * @author Jeffrey Coble <jeffrey.a.coble@gmail.com> http://engineeringnotebook.org
 *
 */
public class StatusUtilities {
    
    private static final Logger logger = Logger.getLogger(StatusUtilities.class.getName());
    
    public static void writeResponseToFile(String xmlData, String fileName) {
     
        try{
        // Create file
        FileWriter fstream = new FileWriter(fileName);
            BufferedWriter out = new BufferedWriter(fstream);
        out.write(xmlData);
        //Close the output stream
        out.close();
        }catch (Exception e){//Catch exception if any
          logger.log(Level.INFO, "Error writing to file =  {0}", new Object[]{e.getMessage()});
        }    
        
    }
    
    public PropertiesMap readConfigurationProperties() {
      String pollingRate;
      String baseURL;
      String responseWaitTime;
      PropertiesMap propertiesMap = new PropertiesMap();

      
      logger.log(Level.INFO, "***Creating Input Stream***");
      //load the config file from the root of the JAR file
      InputStream is = StatusUtilities.class.getClassLoader().getResourceAsStream("config.xml");  
      if(is == null)
        logger.log(Level.INFO, "***Could not read configuration file***");
      
      Document doc = createDOM(is);
      
      //parse the config values from the XML doc
      pollingRate = getPollingRate(doc);
      baseURL = getBaseURL(doc);
      responseWaitTime = getResponseWaitTime(doc);
      
      //construct a properties map with the config values
      propertiesMap.setPollingRate(Long.valueOf(pollingRate).longValue());
      propertiesMap.setResponseWaitTime(Long.valueOf(responseWaitTime).longValue());
      propertiesMap.setBaseURL(baseURL);
      
      return propertiesMap;
    }
    
     private Document createDOM(InputStream configFileInputStream){
        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc = null;

        try {
                //Factory returns an instance of document builder
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.parse(configFileInputStream);
        }catch(ParserConfigurationException pce) {
            logger.log(Level.INFO, "***Error reading config.xml file***");
            System.exit(0);
        }catch(SAXException se) {
            logger.log(Level.INFO, "***Error reading config.xml file***");
            System.exit(0);
        }catch(IOException ioe) {
            logger.log(Level.INFO, "***Error reading config.xml file***");
            System.exit(0);
        }
        
        return doc;
    }

   /**
    * Extracts the polling rate from the XML config doc
    * @param dom
    * @return 
    */
    private String getPollingRate(Document dom) {
      String pollingRateValue = null;
      
      //get the root element
      Element element = dom.getDocumentElement();
      
      //get a nodelist of elements
    
      NodeList nl = element.getElementsByTagName("pollingrate");
      if(nl != null && nl.getLength() > 0) {
        for(int i = 0 ; i < nl.getLength();i++) {
          //get the list element
          Element pollingrate = (Element)nl.item(i);
          pollingRateValue = pollingrate.getTextContent();
          }
      }
      
      return pollingRateValue;
    }
    
    /**
     * Extracts the base URL from the XML config doc.  The base URL is used for 
     * REST calls.
     * 
     * @param dom
     * @return 
     */
    private String getBaseURL(Document dom) {
      String baseURLValue = null;
      
      //get the root element
      Element element = dom.getDocumentElement();
      
      //get a nodelist of elements
    
      NodeList nl = element.getElementsByTagName("baseURL");
      if(nl != null && nl.getLength() > 0) {
        for(int i = 0 ; i < nl.getLength();i++) {
          //get the list element
          Element baseURL = (Element)nl.item(i);
          baseURLValue = baseURL.getTextContent();
          }
      }
      
      return baseURLValue;
    }
    
    /**
     * Extracts the response wait time from the XML config doc.  
     * 
     * @param dom
     * @return 
     */
    private String getResponseWaitTime(Document dom) {
      String responseWaitTimeValue = null;
      
      //get the root element
      Element element = dom.getDocumentElement();
      
      //get a nodelist of elements
    
      NodeList nl = element.getElementsByTagName("responsewaittime");
      if(nl != null && nl.getLength() > 0) {
        for(int i = 0 ; i < nl.getLength();i++) {
          //get the list element
          Element responseWaitTime = (Element)nl.item(i);
          responseWaitTimeValue = responseWaitTime.getTextContent();
          }
      }
      
      return responseWaitTimeValue;
    }
    
}
