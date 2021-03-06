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

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Properties;

import org.apache.commons.codec.binary.Base32;
import org.json.simple.JSONObject;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

/**
 * Třída pro správu databáze uživatelů
 * 
 * @author Petr Michalík
 * @see org.restlet.resource.ServerResource
 *
 */
public class Users extends ServerResource {

	Properties p = Data.load();
	JSONObject msg = new JSONObject();

	// HTTP kódy
	Status error = new Status(404);
	Status conflict = new Status(409);
	Status success = new Status(200);
	Status created = new Status(201);

	/**
	 * Nastaví zprávu
	 */
	public void doInit() {
		// Pokud metoda nezmění stav, stala se chyba
		msg = new JSONObject();
		msg.put("status", "error");
	}

	/**
	 * Vytvoří nový záznam a vrátí index záznamu a klíč
	 * 
	 * @return JSON
	 */
	@Post
	public String create(String user) {
		this.setStatus(error);
		if (user == null) {
			msg.put("message", "No username");
		} else if (user.matches("^[a-zA-Z0-9_]+$") == false) {
			// Uživatelské jméno smí obsahovat pouze písmena, čísla nebo
			// podtržítka
			msg.put("message", "Data should be '[a-zA-Z0-9_]+'");
		} else {
			// Vygeneruju pseudonáhodné heslo
			String secret = generateSecret();
			// Pokud existuje uživatelské jméno
			if (!Data.add(user, secret, false)) {
				this.setStatus(conflict);
				msg.put("message", "Username already exists");
			} else {
				this.setStatus(created);
				this.setLocationRef("./users/" + user);
				msg.put("status", "ok");
				msg.put("username", user);
				msg.put("secret", secret);
			}
		}
		return msg.toJSONString();
	}

	/**
	 * Vrátí počet uživatelů
	 * 
	 * @return JSON
	 */
	@Get
	public String getUsers() {
		// Zpráva pro klienta je JSON
		this.setStatus(success);
		p = Data.load();
		msg.put("status", "ok");
		msg.put("users", String.valueOf(p.size()));
		return msg.toJSONString();
	}

	/**
	 * Vymaže všechny záznamy
	 */
	@Delete
	public StringRepresentation delete() {
		Data.deleteAll();
		msg.put("status", "ok");
		return new StringRepresentation(msg.toJSONString());
	}

	/**
	 * Vygenerovat klíč pro generování TOTP hesla
	 * 
	 * @return base32 řetězec
	 */
	public static String generateSecret() {
		// Chci klíč o velikosti 20 bytů
		int maxBits = 20 * 8 - 1;
		SecureRandom rand = new SecureRandom();
		byte[] val = new BigInteger(maxBits, rand).toByteArray();
		return new Base32().encodeToString(val);
	}

}
