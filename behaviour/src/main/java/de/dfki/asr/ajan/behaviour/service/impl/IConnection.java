/*
 * Copyright (C) 2020 see AJAN-service/AUTHORS.txt (German Research Center for Artificial Intelligence, DFKI).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package de.dfki.asr.ajan.behaviour.service.impl;

import de.dfki.asr.ajan.behaviour.events.Event;
import java.io.IOException;
import java.util.UUID;

public interface IConnection {
	String BASE_URI = "http://www.ajan.de";

	boolean addProcessId(final String id);
	boolean removeProcessId(final String id);

	UUID getId();
	Event getEvent();
	void setPayload(final String payload);
	void setEvent(final Event event);

	Object execute() throws IOException;
	void response(final Object info);
	void response(final Object info, final String id);
}
