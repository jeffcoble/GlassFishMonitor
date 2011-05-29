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
package org.engineeringnotebook.glassfishmonitor.client;

import org.engineeringnotebook.glassfishmonitor.cluster.ClusterListMonitor;
import org.engineeringnotebook.glassfishmonitor.cluster.ClusterListStatusObserver;
import com.sun.jersey.api.client.Client;
import org.engineeringnotebook.glassfishmonitor.util.AdminConfiguration;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.engineeringnotebook.glassfishmonitor.util.StatusUtilities;
import org.engineeringnotebook.glassfishmonitor.util.PropertiesMap;

/**
 * This is a sample client.  The expectation is that developers using the
 * GlassFish Monitor API will create their own (graphical) client.
 * 
 * @author Jeffrey Coble <jeffrey.a.coble@gmail.com> http://engineeringnotebook.org
 *
 */
public class MonitoringClient {
    private static final Logger logger = Logger.getLogger("org.engineeringnotebook.glassfishmonitor");
    private static String baseURL = "http://localhost:4848";
    Client client = null;
    PropertiesMap propertiesMap;
    
    public void MonitoringClient() {
      
      logger.setLevel(Level.INFO);
      
    }
    
    /**
     * Get the config properties and do some housekeeping with the GF admin 
     * REST interface
     */ 
    private void initialize() {
      
      logger.log(Level.INFO, "Initializing Logging Threads...");
      
      propertiesMap = new PropertiesMap();
        
      StatusUtilities statusUtilities = new StatusUtilities();  
      propertiesMap = statusUtilities.readConfigurationProperties(); 
      
      logger.log(Level.INFO, "Polling Rate = {0}, baseURL = {1}, Response Wait Time = {2}", new Object[]{propertiesMap.getPollingRate(), propertiesMap.getbaseURL(), propertiesMap.getResponseWaitTime()});

      AdminConfiguration aConfig = new AdminConfiguration(baseURL);

      client = Client.create();

      //initialize the server output for pretty printing
      aConfig.configureAdminServer(client);         

    }
    
    /**
     * Set up two threads - a monitor thread to monitor the cluster and the 
     * second to listen for status from the monitor thread
     */     
    private void getClusterStatus() {

        logger.log(Level.INFO, "Starting Cluster Status Threads...");
        
        //Start a cluster list monitor thread
        ClusterListMonitor clusterListMonitor = new ClusterListMonitor(propertiesMap.getbaseURL());
        clusterListMonitor.setClient(client);
        clusterListMonitor.setPollingRate(propertiesMap.getPollingRate());
        (new Thread(clusterListMonitor)).start();
        
        //Start a cluster status listener thread
        ClusterListStatusObserver csObserver = new ClusterListStatusObserver(client, propertiesMap.getResponseWaitTime(), propertiesMap.getbaseURL());
        csObserver.registerObserver(clusterListMonitor);
        (new Thread(csObserver)).start();
    }
    
    public static void main(String[] args) {
        MonitoringClient mClient = new MonitoringClient();
        mClient.initialize();
        mClient.getClusterStatus();

    }

    
}
