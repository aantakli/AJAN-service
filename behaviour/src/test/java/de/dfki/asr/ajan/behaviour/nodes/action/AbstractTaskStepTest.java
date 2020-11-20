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
import de.dfki.asr.ajan.behaviour.nodes.Action;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.TaskContext;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeClass;

@SuppressWarnings("PMD.SignatureDeclareThrowsException")
public abstract class AbstractTaskStepTest {

	@Mock
	protected Action action;

	@Mock
	protected AgentTaskInformation taskInfo;

	@Spy
	protected TaskContext context;

	protected abstract void initTestObjects() throws Exception;

	@BeforeClass
	public void setupTest() throws Exception {
		MockitoAnnotations.initMocks(this);
		Mockito.doReturn(action).when(context).get(Action.class);
		Mockito.doReturn(UUID.randomUUID()).when(action).getId();
		Mockito.doReturn(taskInfo).when(action).getObject();
		Mockito.doReturn(new ConcurrentHashMap<>()).when(taskInfo).getConnections();
		initTestObjects();
	}
}
