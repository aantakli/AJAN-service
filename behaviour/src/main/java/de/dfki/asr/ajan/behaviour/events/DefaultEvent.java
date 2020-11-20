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

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEvent implements Event {

	private String name;
	private String url;
	protected final List<Listener> listeners = new ArrayList();
	protected Object information = new Object();
	private static final Logger LOG = LoggerFactory.getLogger(Event.class);

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public void setUrl(final String url) {
		this.url = url;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public void register(final Listener listener) {
		listeners.add(listener);
	}

	@Override
	public void delete(final Listener listener) {
		listeners.remove(listener);
	}

	@Override
	public boolean isRegistered(final Listener listener) {
		return listeners.contains(listener);
	}

	@Override
	public void notifyListeners() {
		listeners.stream().forEach((listener) -> {
			new Thread(() -> listener.setEventInformation(information)).start();
		});
	}

	@Override
	public void notifyListeners(final String id) {
		listeners.stream().forEach((listener) -> {
			LOG.info(id);
			new Thread(() -> listener.setEventInformation(id, information)).start();
		});
	}

	@Override
	public void notifyListeners(final Producer producer) {
		listeners.stream().forEach((listener) -> {
			new Thread(() -> listener.setEventInformation(producer, information)).start();
		});
	}

	@Override
	public void setEventInformation(final Object information) {
		this.information = information;
		notifyListeners();
	}

	@Override
	public void setEventInformation(final String id, final Object information) {
		this.information = information;
		notifyListeners(id);
	}

	@Override
	public void setEventInformation(final Producer producer, final Object information) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
