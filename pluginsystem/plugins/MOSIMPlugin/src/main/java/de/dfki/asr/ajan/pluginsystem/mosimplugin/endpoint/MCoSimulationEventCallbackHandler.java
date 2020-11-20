/*
 * Copyright (C) 2020 Andre Antakli (German Research Center for Artificial Intelligence, DFKI).
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

package de.dfki.asr.ajan.pluginsystem.mosimplugin.endpoint;

import de.mosim.mmi.avatar.MAvatarPostureValues;
import de.mosim.mmi.cosim.MCoSimulationEventCallback;
import de.mosim.mmi.cosim.MCoSimulationEvents;
import java.util.Map;
import org.apache.thrift.TException;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author Andre Antakli
 */
@Service
public class MCoSimulationEventCallbackHandler implements MCoSimulationEventCallback.Iface {

	private final Map<String, ThriftAction> actions;
	protected static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MAJANServiceHandler.class);

	public MCoSimulationEventCallbackHandler(final Map<String, ThriftAction> actions) {
		this.actions = actions;
	}

	@Override
	public void OnEvent(MCoSimulationEvents event) throws TException {
		event.getEvents().forEach((simEvent) -> {
			String actionID = simEvent.Name;
			LOG.info("OnEvent name: " + simEvent.Name);
			LOG.info("OnEvent type: " + simEvent.Type);
			LOG.info("OnEvent reference: " + simEvent.Reference);
			if (simEvent.Type.equals("end")
					|| simEvent.Type.equals("initError")
					|| simEvent.Type.equals("PositioningFinished")) {
				actions.forEach((k,v) -> {
					v.setResponse(k, simEvent);
				});
			}
		});
	}

	@Override
	public void OnFrameEnd(MAvatarPostureValues newPosture) throws TException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}
