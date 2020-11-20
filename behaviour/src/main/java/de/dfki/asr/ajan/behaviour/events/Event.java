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

package de.dfki.asr.ajan.behaviour.events;

public interface Event {
	String getName();
	void setName(final String name);
	String getUrl();
	void setUrl(final String url);

	boolean isRegistered(final Listener listener);
	void register(final Listener listener);
	void delete(final Listener listener);
	void notifyListeners();
	void notifyListeners(final String id);
	void notifyListeners(final Producer producer);

	void setEventInformation(final Object information);
	void setEventInformation(final String id, final Object information);
	void setEventInformation(final Producer producer, final Object information);
}
