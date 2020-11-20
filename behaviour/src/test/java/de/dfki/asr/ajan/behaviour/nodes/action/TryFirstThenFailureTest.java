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

import com.badlogic.gdx.ai.btree.Task.Status;
import static com.badlogic.gdx.ai.btree.Task.Status.RUNNING;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.Failure;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.Succeed;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.TryFirstThenOnFailure;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import org.testng.annotations.Test;

@SuppressWarnings("PMD.SignatureDeclareThrowsException")
public class TryFirstThenFailureTest extends AbstractTaskStepTest {

	@Test
	public void shouldNotCallSecondOnSuccess() {
		TaskStep second = mock(TaskStep.class);
		new TryFirstThenOnFailure(new Succeed(), second).execute(context);
		verify(second, times(0)).execute(context);
	}

	@Test
	public void shouldCallSecondOnFailure() {
		TaskStep second = mock(TaskStep.class);
		new TryFirstThenOnFailure(new Failure(), second).execute(context);
		verify(second, times(1)).execute(context);
	}

	@Test
	public void shouldLeveOtherStatusAlone() {
		TaskStep first = mock(TaskStep.class);
		when(first.execute(context)).thenReturn(RUNNING);
		TaskStep second = mock(TaskStep.class);
		Status result = new TryFirstThenOnFailure(first, second).execute(context);
		verify(second, times(0)).execute(context);
		assertThat(result, is(RUNNING));
	}

	@Test
	public void shouldReturnOtherStatus() {
		Status result = new TryFirstThenOnFailure(new Failure(), new Succeed()).execute(context);
		assertThat(result, is(Status.SUCCEEDED));
	}

	@Override
	protected void initTestObjects() throws Exception {
		// nothing to do here
	}
}
