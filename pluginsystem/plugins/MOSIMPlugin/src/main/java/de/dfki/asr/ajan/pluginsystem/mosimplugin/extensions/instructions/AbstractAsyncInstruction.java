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

package de.dfki.asr.ajan.pluginsystem.mosimplugin.extensions.instructions;

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.events.ModelEvent;
import de.dfki.asr.ajan.behaviour.exception.ConditionSimulationException;
import de.dfki.asr.ajan.behaviour.exception.LoadPredicateException;
import de.dfki.asr.ajan.behaviour.nodes.Action;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.ActionVariable;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.InputModel;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.ResultModel;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorAskQuery;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.endpoint.MAJANServiceHandler;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.endpoint.ThriftAction;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.endpoint.ThriftPluginServer;
import de.mosim.mmi.cosim.MCoSimulationAccess;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andre Antakli
 */
public abstract class AbstractAsyncInstruction extends ThriftAction {

	@Getter @Setter
    private ModelEvent event;
	private Thread thread;
    private MAJANServiceHandler handler;
    private AbstractAsyncInstruction currObject;

	protected String instID;
	protected String url;
	protected abstract String getSPARQLConsumes();
	protected abstract String getSPARQLProduces();

    @Override
    public Action.Communication getCommunication() {
        return Action.Communication.ASYNCHRONOUS;
    }

	@Override
    public abstract List<ActionVariable> getVariables();

    @Override
    public BehaviorAskQuery getConsumable() {
        BehaviorAskQuery consumable = new BehaviorAskQuery();
        consumable.setSparql(getSPARQLConsumes());
        return consumable;
    }

    @Override
    public BehaviorAskQuery getProducible() {
        BehaviorAskQuery producible = new BehaviorAskQuery();
        producible.setSparql(getSPARQLProduces());
        return producible;
    }
	
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractAsyncInstruction.class);
    protected final ValueFactory vf = SimpleValueFactory.getInstance();

    @Override
    public ResultModel run(InputModel inputModel, AgentTaskInformation info, String url) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run(InputModel inputModel, ModelEvent _event, UUID id, AgentTaskInformation info, String url) {
		this.url = url;
        LOG.info("Run " + this.getLable() + " with ID: " + id.toString());
        InstructionParameters parameters = readInput(inputModel, info);
        setEvent(_event);
        currObject = this;
        thread = new Thread() {
            @Override
            public void run() {
                try {
                    handler = ThriftPluginServer.getHandler();
                    handler.register(id.toString(), currObject);
                    executeOperation(id, info, parameters);
                } catch (URISyntaxException | ConditionSimulationException | LoadPredicateException | NullPointerException ex) {
                    LOG.error(toString() + " FAILED due to query evaluation error OR handler object is null. Cannot register Event.");
                }
            }
        };
        thread.start();
    }

	protected abstract InstructionParameters readInput(final InputModel inputModel, final AgentTaskInformation info);

    protected void executeOperation(final UUID id, final AgentTaskInformation info, final InstructionParameters parameters) throws ConditionSimulationException, URISyntaxException, LoadPredicateException {
        try {
            try (TTransport transport = new TSocket(parameters.getCosimHost(),parameters.getCosimPort())) {
				transport.open();
				TProtocol protocol = new TCompactProtocol(transport);
				MCoSimulationAccess.Client client = new MCoSimulationAccess.Client(protocol);
				performOperation(client, id.toString(), parameters);
				updateInfo(id,info);
				transport.close();
			}
        } catch (TException ex) {
            LOG.info("Cannot connect to MCoSimulationAccess", ex);
        }
    }

	protected void updateInfo(final UUID id, final AgentTaskInformation info) {
		Model addModel = getAddModel(id);
		Model removeModel = getRemoveModel(id);
		info.getAgentBeliefs().update(addModel, removeModel, false);
	}

	protected abstract boolean performOperation(final MCoSimulationAccess.Client client, final String actionID, final InstructionParameters parameters) throws TException;
	protected abstract Model getAddModel(final UUID id);
	protected abstract Model getRemoveModel(final UUID id);
	
    @Override
    public ResultModel abort(InputModel inputModel) {
        LOG.info("Abort ThriftAsyncAction");
		instID = "";
        thread.interrupt();
        ResultModel model = new ResultModel();
        model.add(vf.createBNode(), vf.createIRI("http://nullinger.de/abort"), vf.createBNode());
        return model;
    }

    @Override
    public void setResponse(final String id, final int response) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

	@Override
	public abstract void setResponse(final String id, final Object response);
}
