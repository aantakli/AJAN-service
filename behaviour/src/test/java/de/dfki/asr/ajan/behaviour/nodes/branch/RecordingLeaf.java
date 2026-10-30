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

package de.dfki.asr.ajan.behaviour.nodes.branch;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/** Test helper: succeeds immediately and records its name into a shared run log. */
public final class RecordingLeaf extends LeafTask<AgentTaskInformation> {
	private final String name;
	private final List<String> log;

	public RecordingLeaf(final String name, final List<String> log) {
		this.name = name;
		this.log = log;
	}

	@Override
	public Status execute() {
		log.add(name);
		return Status.SUCCEEDED;
	}

	@Override
	protected Task<AgentTaskInformation> copyTo(final Task<AgentTaskInformation> task) {
		return task;
	}

	/**
	 * RDF literals without an explicit datatype ({@code bt:intValue 1}) are mapped to
	 * {@code xsd:integer}, i.e. {@link BigInteger}, not {@link Integer}.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static IntValue xsdIntegerValue(final long... values) {
		List raw = new ArrayList();
		for (long value : values) {
			raw.add(BigInteger.valueOf(value));
		}
		IntValue intValue = new IntValue();
		intValue.setValue(raw);
		return intValue;
	}
}
