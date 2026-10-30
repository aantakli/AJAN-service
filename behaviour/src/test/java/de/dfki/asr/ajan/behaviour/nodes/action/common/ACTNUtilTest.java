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

package de.dfki.asr.ajan.behaviour.nodes.action.common;

import java.util.Collections;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.testng.annotations.Test;

public class ACTNUtilTest {
	private static final String DEFAULT_MIME_TYPE = "text/turtle";

	@Test
	public void missingHeadersDefaultToTurtle() {
		assertThat(ACTNUtil.getMimeTypeFromHeaders(null), is(DEFAULT_MIME_TYPE));
	}

	@Test
	public void emptyHeadersDefaultToTurtle() {
		assertThat(ACTNUtil.getMimeTypeFromHeaders(Collections.emptyList()), is(DEFAULT_MIME_TYPE));
	}
}
