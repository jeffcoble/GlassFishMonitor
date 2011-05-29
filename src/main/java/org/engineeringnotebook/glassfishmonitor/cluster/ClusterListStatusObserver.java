/*
 * Copyright 2011 Jeffrey Coble <jeffrey.a.coble@gmail.com> http://engineeringnotebook.org.
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

import org.engineeringnotebook.glassfishmonitor.Observer;
import org.engineeringnotebook.glassfishmonitor.status.Status;
import org.engineeringnotebook.glassfishmonitor.Subject;
import org.engineeringnotebook.glassfishmonitor.instance.InstanceListMonitor;
import org.engineeringnotebook.glassfishmonitor.instance.InstanceListStatusObserver;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Date;
import com.sun.jersey.api.client.Client;

/**
 * This class implements the observer portion of the (GoF) Observer pattern and 
 * listens for updates from the ClusterListMonitor (subject).
 * The listener will wait a specific amount of time for an update from the 
 * monitor.  If it doesn't receive an update, the thread will terminate.  
 * 
 * The ClusterListStatusListener starts an InstanceListMonitor and an
 * InstanceListStatusListener for each cluster's instance list. 
 * 
 * @author Jeffrey Coble <jeffrey.a.coble@gmail.com> http://engineeringnotebook.org
 */
public class ClusterListStatusObserver implements Observer, Runnable {
    private static final Logger logger = Logger.getLogger(ClusterListStatusObserver.class.getName());
    private long lastUpdateTime;
    private Date date;
    //milliseconds
    private long statusWaitTime;
    private ArrayList<String> clusterNames = new ArrayList();
    private ArrayList<InstanceListMonitor> ilMonitorList = new ArrayList();
    private ArrayList<InstanceListStatusObserver> instanceStatusListenerList = new ArrayList();
    private Client restClient;
    private String baseURL;
    
    /**
     * 
     * @param client The Jersey rest client
     * @param milliseconds The time the thread will wait for the monitor to provide a response
     * @param baseURL The URL for the GF server, which will be used to construct restful resources
     */
    public ClusterListStatusObserver(Client client, long statusWaitTime, String baseURL) {
        this.restClient = client;
        this.statusWaitTime = statusWaitTime;
        this.baseURL = baseURL;
        date = new Date();
        lastUpdateTime = date.getTime();
    }
    
    public void run() {
        long currentTime;
        long timeSinceLastStatusUpdate;
        
         while(true) {
             currentTime = date.getTime(); 
             timeSinceLastStatusUpdate = currentTime - this.lastUpdateTime;
             if(timeSinceLastStatusUpdate > this.statusWaitTime) {
                 logger.log(Level.INFO, "Configured server wait time '{0}' exceeds blocking time '{1}'", new Object[]{this.lastUpdateTime, timeSinceLastStatusUpdate});
                 break;
             }
         }
    }
    
    public void setStatusWaitTime(long statusWaitTime) {
      this.statusWaitTime = statusWaitTime;
    }
    
    /**
     * Called by the ClusterListMonitor (subject) to provide the status update 
     * for the list of clusters.
     * 
     * @param statusList The status of all clusters
     */
    public void update(ArrayList<Status> statusList) { 
      if(statusList != null) {
        for(Iterator<Status> i=statusList.iterator(); i.hasNext(); ) {
          Status status = i.next();
          updateClusterList(status);
          try {
            updateStatus(status);
          }
          catch (RuntimeException e) {
            i.remove();
            logger.log(Level.INFO, "Removing Cluster Status From List");
          }
        } 
      }
      else {
        logger.log(Level.INFO, "Cluster Status List Is Empty");
      }
      lastUpdateTime = date.getTime();
    }
    
    
    /**
     * Since it is possible for clusters to be added at any point, this method
     * checks to see if the cluster is new, and if so starts an instance list
     * monitor for it.
     * 
     * @param clusterStatus The status for a single cluster
     */
    private void updateClusterList(Status clusterStatus) {

        if(!clusterNames.contains(((ClusterStatus)clusterStatus).getClusterName())) {
            clusterNames.add(((ClusterStatus)clusterStatus).getClusterName());
            startInstanceListMonitor(((ClusterStatus)clusterStatus).getClusterName());
        }
       
    }
    
    /**
     * Starts an InstanceListMonitor and InstanceListStatusListener for the 
     * instances belonging to a newly-discovered cluster
     * 
     * @param clusterName The cluster that with instances to be monitored
     */
    private void startInstanceListMonitor(String clusterName) {
        InstanceListMonitor ilMonitor = new InstanceListMonitor(clusterName, baseURL);
        
        ilMonitor.setClient(restClient);
        ilMonitor.setPollingRate(5000);   
        this.ilMonitorList.add(ilMonitor);
        (new Thread(ilMonitor)).start();    
        
        InstanceListStatusObserver ilsListener = new InstanceListStatusObserver(10000);
        ilsListener.registerListener(ilMonitor);
        this.instanceStatusListenerList.add(ilsListener);        
        (new Thread(ilsListener)).start();
    }
    
     /**
     * We'd normally want to do more than log the status. This is where the 
     * status would be used to update a GUI display.
     * 
     * @param status 
     */
    private void updateStatus(Status status) {
      try {
            logger.log(Level.INFO, "Cluster Status Listener: {0}", new Object[]{status.getStatusString()});
          }
          catch (RuntimeException e) {
            logger.log(Level.INFO, "Unexpected exception in listener  {0}", new Object[]{e});
          }
    }
    
    /**
     * Registers this Observer with the Subject
     * 
     * @param subject 
     */
    public void registerObserver(Subject subject) {
        subject.addObserver(this);
    }
    
}
