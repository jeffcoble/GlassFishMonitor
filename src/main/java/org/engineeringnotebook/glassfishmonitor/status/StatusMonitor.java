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
package org.engineeringnotebook.glassfishmonitor.status;

import com.sun.jersey.api.client.Client;
import java.util.ArrayList;

/**
 * A separate monitoring thread is instantiated for each aspect of the GF server
 * that we choose to monitor.  Each monitor class must implement the 
 * StatusMonitor interface.
 * 
 * @author Jeffrey Coble <jeffrey.a.coble@gmail.com> http://engineeringnotebook.org
 * 
 */
public interface StatusMonitor {
 
    //Set the Jersey rest client reference
    public void setClient(Client client);
    
    //Set the thread's polling rate to query the GF server for status
    public void setPollingRate(long milliseconds);    
    
    //Every monitor must retrieve some status from the GF server
    public ArrayList<Status> queryGFStatus();
    
    //Returns the latest status retrieved by the monitor from the GF server
    public ArrayList<Status> getStatusList();
    
}
