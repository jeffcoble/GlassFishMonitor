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
 * Monitors the status of the GF clusters.
 * 
 * Implements the StatusMonitor interface, as all monitors must.
 * Implements the Subject interface to participate in the (GoF) Observer pattern.
 * 
 * @author Jeffrey Coble <jeffrey.a.coble@gmail.com> http://engineeringnotebook.org
 */
public class ClusterListMonitor implements StatusMonitor, Subject, Runnable {
    private static final Logger logger = Logger.getLogger(ClusterListMonitor.class.getName());
    private static String restURL;
    private Client restClient = null;
    private long pollingRate;
    private CopyOnWriteArrayList<Observer> observerList = new CopyOnWriteArrayList();
    private ArrayList<Status> clusterStatusList;
    
    /**
     * 
     * @param baseURL The server URL for the rest call
     */
    public ClusterListMonitor(String baseURL) {
        this.restURL = baseURL + "/management/domain/clusters/list-clusters";
    }
    
    /**
     * ToDo: probably need to provide an exit condition for the thread loop, 
     * rather than an infinite loop
     */
    public void run() {
         while(true) {
             //query the GF rest interface for the cluster status
             clusterStatusList = this.queryGFStatus();
             //notify the observers of the new status
             notifyObservers(clusterStatusList);
             //sleeping for the user-specified polling rate
             try {
                Thread.sleep(pollingRate);
             } catch(InterruptedException ie) {
                 logger.log(Level.INFO, "Cluster Monitor Thread Problem: {0}", new Object[]{ie});
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
     * Allows observers to query the current status
     * 
     * @return The current cluster status
     */
    public ArrayList<Status> getStatusList() {
        return this.clusterStatusList;
    }
    
    /**
     * Poll the server for status and parse the XML response
     * 
     * @return The status reported by the GF server
     */
    public ArrayList<Status> queryGFStatus() {
        ArrayList<Status> statusList = null;
        
        if(restClient != null) {
            String response = queryStatusOfClusters(restClient);
            if(response != null)
              statusList = parseClusterStatusResponse(response);
        }
        
        return statusList;
    }
 
    /**
     * Performs the GET operation on http://baseURL/management/domain/clusters/list-clusters
     * 
     * @param client The Jersey rest client
     * 
     * @return The XML text
     */
    private String queryStatusOfClusters(Client client) {
        
        //Construct the resource and perform the GET operation
        WebResource webResource = client.resource(restURL);
        ClientResponse response = webResource.accept("application/xml").get(ClientResponse.class);
        
        int status = response.getStatus();
        logger.log(Level.FINEST, "list-clusters Status =  {0}", new Object[]{status});
        
        String textEntity = response.getEntity(String.class);
        if(textEntity != null) {
          logger.log(Level.FINEST, "list-clusters Response =  {0}", new Object[]{textEntity});
          StatusUtilities.writeResponseToFile(textEntity, "cluster-status.xml");
        }
        else 
          logger.log(Level.FINEST, "list-clusters Response =  No Clusters");
        
        return textEntity;
    }
    
    /**
     * Parses the XML response received from the GF server and returns the
     * cluster list status
     * 
     * @param xmlData XML data received from the rest GET
     * 
     * @return A list of status objects, one for each cluster
     */
    private ArrayList<Status> parseClusterStatusResponse(String xmlData) {
        ArrayList<Status> clusterStatusList = null;
        
        ClusterListStatusHandler handler = new ClusterListStatusHandler();
        
        clusterStatusList = handler.parseXMLData(xmlData);
        
        return clusterStatusList;
    } 
    
}
