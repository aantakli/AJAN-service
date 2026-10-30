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
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.mockito.Mockito;
import org.testng.annotations.Test;

/**
 * RDF literals without an explicit datatype ({@code bt:intValue 1}) are mapped to
 * {@code xsd:integer}, i.e. {@link BigInteger}, not {@link Integer}.
 */
public class ExecutorTest {

	private static class CountingLeaf extends LeafTask<AgentTaskInformation> {
		private int runs;

		@Override
		public Status execute() {
			runs++;
			return Status.SUCCEEDED;
		}

		@Override
		protected Task<AgentTaskInformation> copyTo(final Task<AgentTaskInformation> task) {
			return task;
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private IntValue xsdIntegerOrder(final long... order) {
		List raw = new ArrayList();
		for (long index : order) {
			raw.add(BigInteger.valueOf(index));
		}
		IntValue value = new IntValue();
		value.setValue(raw);
		return value;
	}

	@Test
	public void sequenceRunsAllChildrenOfXsdIntegerOrder() {
		CountingLeaf first = new CountingLeaf();
		CountingLeaf second = new CountingLeaf();
		Executor executor = new Executor();
		executor.setSequence(true);
		executor.setSelectedChild(xsdIntegerOrder(1, 0));
		executor.setChildren(Arrays.asList(first, second));
		BehaviorTree<AgentTaskInformation> tree = new BehaviorTree<>(executor, Mockito.mock(AgentTaskInformation.class));
		tree.step();
		assertThat(first.runs, is(1));
		assertThat(second.runs, is(1));
		assertThat(executor.getStatus(), is(Task.Status.SUCCEEDED));
	}

	@Test
	public void singleXsdIntegerSelectsOnlyThatChild() {
		CountingLeaf first = new CountingLeaf();
		CountingLeaf second = new CountingLeaf();
		Executor executor = new Executor();
		executor.setSelectedChild(xsdIntegerOrder(1));
		executor.setChildren(Arrays.asList(first, second));
		BehaviorTree<AgentTaskInformation> tree = new BehaviorTree<>(executor, Mockito.mock(AgentTaskInformation.class));
		tree.step();
		assertThat(first.runs, is(0));
		assertThat(second.runs, is(1));
	}
}
