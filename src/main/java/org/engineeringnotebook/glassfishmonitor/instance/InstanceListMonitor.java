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
package org.engineeringnotebook.glassfishmonitor.instance;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.util.ArrayList;
import org.engineeringnotebook.glassfishmonitor.status.StatusMonitor;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.engineeringnotebook.glassfishmonitor.Subject;
import org.engineeringnotebook.glassfishmonitor.Observer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Iterator;
import org.engineeringnotebook.glassfishmonitor.status.Status;
import org.engineeringnotebook.glassfishmonitor.util.StatusUtilities;

/**
 * Monitors the status of a set of instances associated with a cluster.
 * 
 * Implements the StatusMonitor interface, as all monitors must.
 * Implements the Subject interface to participate in the (GoF) Observer pattern.
 *
 * @author Jeffrey Coble <jeffrey.a.coble@gmail.com> http://engineeringnotebook.org
 */
public class InstanceListMonitor implements StatusMonitor, Subject, Runnable {
    private static final Logger logger = Logger.getLogger(InstanceListMonitor.class.getName());
    private static String restURL;
    private Client restClient = null;
    private long pollingRate;
    private String clusterName;
    private CopyOnWriteArrayList<Observer> observerList = new CopyOnWriteArrayList();
    private ArrayList<Status> instanceListStatus;
    /**
     * 
     * @param clusterName The name of the cluster associated with these instances
     * @param baseURL The server URL for the rest call
     */
    public InstanceListMonitor(String clusterName, String baseURL) {
        this.clusterName = clusterName;
        this.restURL = baseURL + "/management/domain/clusters/cluster/";
        this.instanceListStatus = null;
    }
    
    public void run() {
         
      while(true) {
         instanceListStatus = this.queryGFStatus();
         notifyObservers(instanceListStatus);
         logger.log(Level.INFO, "Sleeping for {0} msecs", new Object[]{pollingRate});
         try {
            Thread.sleep(pollingRate);
         } catch(InterruptedException ie) {
             System.out.println(ie);
         }
      }
    }

    /**
     * Send the latest status to all observers
     * 
     * @param statusList 
     */    
    public void notifyObservers(ArrayList<Status> statusList) {
        for (Iterator<Observer> i=observerList.iterator(); i.hasNext(); ) {
            Observer o = i.next();
            try {
                o.update(statusList);
            }
            catch (RuntimeException e) {
                logger.log(Level.INFO, "Unexpected exception in listener  {0}", new Object[]{e});
                i.remove();
            }
        }
    }
   
    /**
     * 
     * @param obs The observer to add
     */    
    public void addObserver(Observer obs) {
        observerList.add(obs);        
    }
 
    /**
     * 
     * @param obs The observer to remove 
     */    
    public void removeObserver(Observer obs) {
        observerList.remove(obs);        
    }
    
    /**
     * 
     * @param client The Jersey rest client
     */    
    public void setClient(Client client) {
        this.restClient = client;
    } 
    
    /**
     * 
     * @param milliseconds The rate at which the GF server will be queried
     */    
    public void setPollingRate(long milliseconds) {
        this.pollingRate = milliseconds;
        
    }
    
    /**
     * 
     * @return The status reported by the GF server
     */    
    public ArrayList<Status> queryGFStatus() {
        ArrayList<Status> statusList = null;
        
        if(restClient != null) {
            String response = getInstanceListStatus(restClient);
            statusList = parseInstanceResponse(response);
        }
        
        return statusList;
    }

    /**
     * Performs the GET operation on http://baseURL/management/domain/clusters/cluster/{cluster-name}/list-instances
     * 
     * @param client The Jersey rest client
     * 
     * @return The XML text
     */    
    private String getInstanceListStatus(Client client) {
        
        
        WebResource webResource = client.resource(restURL + this.clusterName + "/list-instances");
        
        ClientResponse response = webResource.accept("application/xml").get(ClientResponse.class);
        
        int status = response.getStatus();
        logger.log(Level.FINEST, "list-instances Status =  {0}", new Object[]{status});
        String textEntity = response.getEntity(String.class);

        logger.log(Level.FINEST, "list-instances Response =  {0}", new Object[]{textEntity});
        StatusUtilities.writeResponseToFile(textEntity, this.clusterName + "-instance-status.xml");
        
        return textEntity;
    }
    
    /**
     * Parses the XML response received from the GF server and returns the
     * instance list status
     * 
     * @param xmlData XML data received from the rest GET
     * 
     * @return A list of status objects, one for each instance
     */    
    private ArrayList<Status> parseInstanceResponse(String xmlData) {
        ArrayList<Status> statusList = null;
        
        InstanceListStatusHandler handler = new InstanceListStatusHandler();
        
        statusList = handler.parseXMLData(xmlData);
        
        return statusList;
    }   

    /**
     * 
     * @return The current instance list status
     */
    public ArrayList<Status> getStatusList() {
        return this.instanceListStatus;
    }  
}
