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

import java.util.logging.Logger;
import java.util.logging.Level;
import org.engineeringnotebook.glassfishmonitor.status.Status;

/**
 *
 * @author Jeffrey Coble <jeffrey.a.coble@gmail.com> http://engineeringnotebook.org
 */
public class InstanceStatus implements Status {
  private static final Logger logger = Logger.getLogger(InstanceStatus.class.getName());
  private String instanceName = null;
  private String instanceStatus = null;
  private String instanceUptime = null;


  public void setInstanceName(String cName) {
      this.instanceName = cName;
  }

  public void setInstanceStatus(String cStatus) {
      this.instanceStatus = cStatus;
  }

  public void setInstanceUptime(String uptime) {
      this.instanceUptime = uptime;
  }

  public String getInstanceName() {
      return this.instanceName;
  }

  public String getInstanceStatus() {
      return this.instanceStatus;
  }

  public String getInstanceUptime() {
      return this.instanceUptime;
  }
    
 /**
 * Returns a human-readable status string for the instance
 * 
 * @return A string representation of the instance status
 */   
  public String getStatusString() {  
      logger.log(Level.FINEST, "Instance Name = {0},  -- Status =  {1}, -- Uptime = {2}", new Object[]{this.instanceName, this.instanceStatus, this.instanceUptime});
      return("Instance Name = " + this.instanceName + " -- Status = " + this.instanceStatus + " -- Uptime = " + this.instanceUptime);
  }
    
}
