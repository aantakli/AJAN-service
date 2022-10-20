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

package de.dfki.asr.ajan.behaviour.nodes.common;

import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.AJANLogger;
import lombok.Data;

/**
 *
 * @author Andre Antakli (German Research Center for Artificial Intelligence,
 * DFKI)
 */
@Data
@SuppressWarnings({"PMD.NullAssignment", "PMD.ExcessiveParameterList"})
public class NodeStatus {
	private final Task.Status status;
	private final AJANLogger logger;
	private final Class<?> clazz;
	private final String label;
	private final Throwable cause;

	public NodeStatus(final Task.Status status, final AJANLogger logger, final Class<?> clazz, final String label) {
		this.status = status;
		this.label = label;
		this.clazz = clazz;
		this.logger = logger;
		this.cause = null;
	}

	public NodeStatus(final Task.Status status, final AJANLogger logger, final Class<?> clazz, final String label, final Throwable cause) {
		this.status = status;
		this.label = label;
		this.clazz = clazz;
		this.logger = logger;
		this.cause = cause;
	}

	public void log() {
		if (cause == null) {
			logger.info(clazz, label);
		} else {
			logger.info(clazz, label, cause);
		}
	}
}
