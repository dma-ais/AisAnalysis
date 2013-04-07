/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.ais.analysis.common.web;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Embedded Jetty web server and servlet container using a web app context  
 */
public class WebServer {
    
    private final Server server = new Server();
    
    public WebServer(WebServerConfiguration conf) {
        SocketConnector connector = new SocketConnector();
        connector.setPort(conf.getPort());
        server.setConnectors(new Connector[] { connector });        
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
