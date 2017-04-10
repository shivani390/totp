/*
   Copyright 2017 Petr Michalík

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package cz.alej.michalik.totp.server;

import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;

/**
 * Server imlementující REST API
 * 
 * @author Petr Michalík
 *
 */
public class Serve {

	// HTTP Port
	private static int port = 8080;
	// Časový interval generování TOTP hesla
	public static int step = 30;

	/**
	 * Vytvoří nový server
	 * 
	 * @param args
	 *            číslo portu (nepovinné)
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// Nastaví případně port
		if (args.length > 0) {
			port = Integer.valueOf(args[0]);
		}
		Component c = new Component();
		c.getServers().add(Protocol.HTTP, port);
		c.getDefaultHost().attach("", createInboundRoot());

		c.start();
	}

	/**
	 * Nastaví metody pro zpracování HTTP požadavků
	 * 
	 * @return router
	 */
	public static Restlet createInboundRoot() {
		Router router = new Router();
		router.attach("/users", Users.class);
		router.attach("/users/{id}", User.class);

		return router;
	}

	/**
	 * Vrátí port serveru
	 * 
	 * @return port
	 */
	public static int getPort() {
		return port;
	}
}
