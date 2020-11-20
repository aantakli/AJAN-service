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
import de.dfki.asr.ajan.behaviour.nodes.action.impl.Failure;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.Succeed;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.testng.annotations.Test;

@SuppressWarnings("PMD.SignatureDeclareThrowsException")
public class SimpleStepsTest extends AbstractTaskStepTest {
	@Test
	public void succeedReturnsSucceeded() {
		Status status = new Succeed().execute(context);
		assertThat(status, is(Status.SUCCEEDED));
	}

	@Test
	public void failureReturnsFailed() {
		Status status = new Failure().execute(context);
		assertThat(status, is(Status.FAILED));
	}

	@Override
	protected void initTestObjects() throws Exception {
		// nothing to do here
	}
}
