/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.ais.analysis.common.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Embedded Jetty web server and servlet container using a web app context  
 */
public class WebServer {
    
    private final Server server;
    
    public WebServer(WebServerConfiguration conf) {
        server = new Server(conf.getPort());
        // Sets setReuseAddress
        ((ServerConnector) server.getConnectors()[0]).setReuseAddress(true);
        WebAppContext bb = new WebAppContext();
        bb.setServer(server);
        bb.setContextPath(conf.getContextPath());
        bb.setWar(conf.getWebappPath());
        server.setHandler(bb);
    }
    
    public void start() throws Exception {        
        server.start();
    }
    
    public void stop() throws Exception {
        server.stop();
    }

}
