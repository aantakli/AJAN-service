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

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.mockito.Mockito;
import org.testng.annotations.Test;

public class ExecutorTest {
	private static final String FIRST = "first";
	private static final String SECOND = "second";

	private Executor runExecutor(final boolean sequence, final IntValue order, final List<String> log) {
		Executor executor = new Executor();
		executor.setSequence(sequence);
		executor.setSelectedChild(order);
		executor.setChildren(Arrays.asList(new RecordingLeaf(FIRST, log), new RecordingLeaf(SECOND, log)));
		new BehaviorTree<>(executor, Mockito.mock(AgentTaskInformation.class)).step();
		return executor;
	}

	@Test
	public void sequenceRunsChildrenInXsdIntegerOrder() {
		List<String> log = new ArrayList<>();
		Executor executor = runExecutor(true, RecordingLeaf.xsdIntegerValue(1, 0), log);
		assertThat(log, is(Arrays.asList(SECOND, FIRST)));
		assertThat(executor.getStatus(), is(Task.Status.SUCCEEDED));
	}

	@Test
	public void singleXsdIntegerSelectsOnlyThatChild() {
		List<String> log = new ArrayList<>();
		runExecutor(false, RecordingLeaf.xsdIntegerValue(1), log);
		assertThat(log, is(Arrays.asList(SECOND)));
	}
}
