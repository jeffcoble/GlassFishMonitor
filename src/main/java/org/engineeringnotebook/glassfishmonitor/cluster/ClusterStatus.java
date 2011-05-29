
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

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.engineeringnotebook.glassfishmonitor.status.Status;

/**
 *
 * Holds the status of an single cluster, including a list of the instances 
 * that are part of the cluster.  
 * 
 * @author Jeffrey Coble <jeffrey.a.coble@gmail.com> http://engineeringnotebook.org
 *  
 */
public class ClusterStatus implements Status{
    private static final Logger logger = Logger.getLogger(ClusterStatus.class.getName());
    private String clusterName = null;
    private String clusterStatus = null;
    private ArrayList<String> instanceList = new ArrayList();
    
   
    public void setClusterName(String cName) {
        this.clusterName = cName;
    }
    
    public void setClusterStatus(String cStatus) {
        this.clusterStatus = cStatus;
    }
    
    public String getClusterName() {
        return this.clusterName;
    }
    
    public String getClusterStatus() {
        return this.clusterStatus;
    }
    
    /**
     * Add the name of one of the instances that belongs to this cluster
     * 
     * @param instanceName The name of the instance
     */
    public void addInstance(String instanceName) {
        instanceList.add(instanceName);
    }
    
    /**
     * 
     * @return The set of instances that belong to this cluster
     */
    public ArrayList<String> getInstanceList() {
        return instanceList;
    }
    
    /**
     * Returns a human-readable status string for the cluster
     * 
     * @return A string representation of the cluster status
     */
    public String getStatusString() {  
        
        logger.log(Level.FINEST, "Cluster Name = {0},  -- Status =  {1}", new Object[]{this.clusterName, this.clusterStatus});
        
        return("Cluster Name = " + this.clusterName + " -- Status = " + this.clusterStatus);
    }
    
}
