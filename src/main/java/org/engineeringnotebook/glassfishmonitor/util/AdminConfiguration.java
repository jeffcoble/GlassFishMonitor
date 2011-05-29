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
package org.engineeringnotebook.glassfishmonitor.util;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.core.MultivaluedMap;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 *
 * @author Jeffrey Coble <jeffrey.a.coble@gmail.com> http://engineeringnotebook.org
 */
public class AdminConfiguration {
    private static final Logger logger = Logger.getLogger(AdminConfiguration.class.getName());
    private String restURL;
   
    public AdminConfiguration(String baseURL) {
        this.restURL = baseURL + "/management/domain/";
    }
    
    public void configureAdminServer(Client client) {
        initializeAdminConfig(client);
        setAdminParms(client);
    }
    
    /**
     * For reasons unknown, this must be called at least once before using the 
     * rest-config resource to configure admin parms
     * 
     * @param client 
     */
    private void initializeAdminConfig(Client client) {
        
        WebResource webResource = client.resource(restURL + "configs/config/server-config/_set-rest-admin-config");
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("indentLevel", "4");
        ClientResponse response = webResource.queryParams(queryParams).accept("application/xml").post(ClientResponse.class);
        //ClientResponse response = webResource.accept("application/xml").get(ClientResponse.class);

        int status = response.getStatus(); 
        logger.log(Level.FINEST, "_set-rest-admin-config Status = {0}", new Object[]{status});
        
        String textEntity = response.getEntity(String.class);
        logger.log(Level.FINEST, "_set-rest-admin-config Response = {0}", new Object[]{textEntity});
    }

    /**
     * Need to call this to set up pretty printing of the XML responses
     * 
     * @param client 
     */
    private void setAdminParms(Client client) {
        
        WebResource webResource = client.resource(restURL + "configs/config/server-config/rest-config");
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("indentLevel", "4");
        ClientResponse response = webResource.queryParams(queryParams).accept("application/xml").post(ClientResponse.class);
        
        int status = response.getStatus();
        logger.log(Level.FINEST, "rest-config Status = {0}", new Object[]{status});
        
        String textEntity = response.getEntity(String.class);
        logger.log(Level.FINEST, "rest-config Status Response = {0}", new Object[]{textEntity});
    }    
}
