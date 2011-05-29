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
package org.engineeringnotebook.glassfishmonitor.instance;

import org.engineeringnotebook.glassfishmonitor.Observer;
import org.engineeringnotebook.glassfishmonitor.status.Status;
import org.engineeringnotebook.glassfishmonitor.Subject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Date;

/**
 * This class implements the observer portion of the (GoF) Observer pattern and 
 * listens for updates from the InstanceListMonitor (subject).
 * The listener will wait a specific amount of time for an update from the 
 * monitor.  If it doesn't receive an update, the thread will terminate.  
 * 
 * @author Jeffrey Coble <jeffrey.a.coble@gmail.com> http://engineeringnotebook.org
 */
public class InstanceListStatusObserver implements Observer, Runnable {
    private static final Logger logger = Logger.getLogger(InstanceListStatusObserver.class.getName());
    private long lastUpdateTime;
    private Date date;
    private long statusWaitTime;
 
    /**
     * @param milliseconds The time the thread will wait for the monitor to provide a response
     */    
    public InstanceListStatusObserver(long milliseconds) {
        this.statusWaitTime = milliseconds;
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
    
    public void setStatusWaitTime(long milliseconds) {
      this.statusWaitTime = milliseconds;
    }
    
   /**
     * Called by the InstanceListMonitor (subject) to provide the status update for 
     * the set of instances.
     * 
     * @param statusList The status of all instances
   */   
    public void update(ArrayList<Status> statusList) {
        for (Iterator<Status> i=statusList.iterator(); i.hasNext(); ) {
            Status o = i.next();
            try {
                logger.log(Level.INFO, "InstanceList Status Listener: {0}", new Object[]{o.getStatusString()});
                lastUpdateTime = date.getTime();
            }
            catch (RuntimeException e) {
                logger.log(Level.INFO, "Unexpected exception in listener  {0}", new Object[]{e});
                i.remove();
            }
        }  
    }

    /**
     * Registers this Observer with the Subject
     * 
     * @param subject 
     */    
    public void registerListener(Subject subject) {
        subject.addObserver(this);
    }
    
}
