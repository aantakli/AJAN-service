package de.dfki.asr.ajan.behaviour;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@SuppressWarnings("PMD.MoreThanOneLogger")
public class AJANLogger {
	private final String agentInfo;

	public AJANLogger(final String id) {
		this.agentInfo = "agent(" + id + ") -> ";
	}

	public void info(final Class<?> clazz, final String message) {
		Logger log = LoggerFactory.getLogger(clazz);
		log.info(agentInfo + message);
	}

	public void info(final Class<?> clazz, final String message, final Throwable thrwbl) {
		Logger log = LoggerFactory.getLogger(clazz);
		log.info(agentInfo + message, thrwbl);
	}
}
