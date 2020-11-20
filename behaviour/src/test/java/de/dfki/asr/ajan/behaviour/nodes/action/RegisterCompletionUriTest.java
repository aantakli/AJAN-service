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

package de.dfki.asr.ajan.behaviour.nodes.action;

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.InputModel;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.service.RegisterCompletionUri;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.Succeed;
import de.dfki.asr.ajan.behaviour.service.impl.HttpConnection;
import de.dfki.asr.ajan.behaviour.service.impl.IConnection;
import java.net.URISyntaxException;
import java.util.UUID;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.Test;

public class RegisterCompletionUriTest extends AbstractTaskStepTest {

	private RegisterCompletionUri registerUriStep;
	private InputModel input;

	@Mock
	private IConnection connection;

	@Mock
	private AgentTaskInformation taskInfoMock;

	@Override
	protected void initTestObjects() throws URISyntaxException {
		registerUriStep = new RegisterCompletionUri(new Succeed());
		UriGenerator gen = Mockito.mock(UriGenerator.class);
		Mockito.doReturn(taskInfoMock).when(action).getObject();
		Mockito.doReturn(gen).when(taskInfoMock).getUriGenerator();
		UUID conId = UUID.randomUUID();
		UUID actId = UUID.randomUUID();
		Mockito.doReturn("localhost:8091/ajan/agents/testAgent/" + conId + "/completeAction/" + actId).when(gen).getCompletionUri(conId,actId);
		Mockito.doReturn(actId).when(action).getId();
		Mockito.doReturn(conId).when(connection).getId();
		input = new InputModel();
		Mockito.doReturn(input).when(context).get(InputModel.class);
		Mockito.doReturn(connection).when(context).get(HttpConnection.class);
	}

	@Test
	public void shouldAddUriToModel() {
		ValueFactory factory = SimpleValueFactory.getInstance();
		IRI completionUri = factory.createIRI("localhost:8091/ajan/agents/testAgent/" + connection.getId() + "/completeAction/" + action.getId());
		registerUriStep.execute(context);
		assertThat(input.objects(), hasItem(completionUri));
	}
}
