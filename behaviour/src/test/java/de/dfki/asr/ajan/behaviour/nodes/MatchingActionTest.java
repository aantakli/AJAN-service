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

package de.dfki.asr.ajan.behaviour.nodes;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.common.TripleDataBase;
import java.net.URI;
import java.util.ArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.eclipse.rdf4j.repository.Repository;
import org.mockito.Mockito;
import org.testng.annotations.Test;

public class MatchingActionTest {

	@Test
	public void failsWhenTheOptionsQueryYieldsNoOptions() {
		Repository repo = Mockito.mock(Repository.class);
		TripleDataBase serviceTDB = Mockito.mock(TripleDataBase.class);
		Mockito.when(serviceTDB.getInitializedRepository()).thenReturn(repo);
		AgentTaskInformation info = Mockito.mock(AgentTaskInformation.class);
		Mockito.when(info.getServiceTDB()).thenReturn(serviceTDB);
		BehaviorSelectQuery options = Mockito.mock(BehaviorSelectQuery.class);
		Mockito.when(options.getOriginBase()).thenReturn(URI.create(AJANVocabulary.SERVICE_KNOWLEDGE.stringValue()));
		Mockito.when(options.getResult(repo)).thenReturn(new ArrayList<>());

		MatchingAction action = new MatchingAction();
		action.setOptionsQ(options);
		new BehaviorTree<>(action, info).step();

		assertThat(action.getStatus(), is(Task.Status.FAILED));
		assertThat(action.getChildCount(), is(0));
	}
}
