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

package de.dfki.asr.ajan.model;

import de.dfki.asr.ajan.behaviour.events.Event;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.service.impl.IConnection;
import de.dfki.asr.ajan.knowledge.AgentBeliefBase;
import java.net.URI;
import java.util.Map;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import static lombok.AccessLevel.NONE;
import org.eclipse.rdf4j.model.Resource;

@Data
public class Agent {
	private final String url;
	private final String id;
        private final Resource template;
        private final SingleRunBehavior initialBehavior;
        private final SingleRunBehavior finalBehavior;
	private final Map<Resource,Behavior> behaviors;
        private final boolean overwrite;
	private final AgentBeliefBase beliefs;
	private final Map<URI,Event> events;
	private final Map<String,Endpoint> endpoints;
	private final Map<URI,IConnection> connections;

	@Getter(NONE) @Setter(NONE)
	private final Object LOCK = new Object();

	public void setEndpointEvent(final String capability, final Object input) {
		if (endpoints.containsKey(capability)) {
			Endpoint endpoint = endpoints.get(capability);
			Event event = endpoint.getEvent();
			event.setEventInformation(input);
		}
	}

	public void execute() {
		behaviors.forEach((k,v) -> this.execute(k));
	}

	public void stop() {
		behaviors.forEach((k,v) -> this.stop(k));
                if (finalBehavior != null) {
                    BTRoot finalBT = finalBehavior.getBehaviorTree();
                    finalBT.run();
                }
	}

	public void execute(final Resource btResc) {
		if (behaviors.containsKey(btResc)) {
			behaviors.get(btResc).getBehaviorTree().run();
		}
	}

	public void stop(final Resource btResc) {
		synchronized (LOCK) {
			if (behaviors.containsKey(btResc)) {
				BehaviorTree<AgentTaskInformation> behavior = behaviors.get(btResc).getBehaviorTree();
				behavior.cancel();
				behavior.removeListeners();
			}
		}
	}
}
