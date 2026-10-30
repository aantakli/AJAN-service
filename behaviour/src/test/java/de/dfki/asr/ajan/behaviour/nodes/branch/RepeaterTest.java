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
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.mockito.Mockito;
import org.testng.annotations.Test;

public class RepeaterTest {
	private static final String CHILD = "child";

	@Test
	public void repeatsChildXsdIntegerTimes() {
		List<String> log = new ArrayList<>();
		Repeater repeater = new Repeater();
		repeater.setIntValue(RecordingLeaf.xsdIntegerValue(3));
		repeater.setChild(new RecordingLeaf(CHILD, log));
		new BehaviorTree<>(repeater, Mockito.mock(AgentTaskInformation.class)).step();
		assertThat(log, is(Arrays.asList(CHILD, CHILD, CHILD)));
	}
}
