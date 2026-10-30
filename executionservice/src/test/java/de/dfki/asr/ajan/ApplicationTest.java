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

package de.dfki.asr.ajan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ApplicationTest {

	/**
	 * Every launcher that loads the application class (main, @SpringBootTest, IDE runners) must
	 * run with RESTEasy's built-in ExceptionMapper switched off; see the comment in Application.
	 */
	@Test
	void loadingTheApplicationClassDisablesTheRestEasyDefaultExceptionMapper() throws ClassNotFoundException {
		Class.forName(Application.class.getName(), true, getClass().getClassLoader());
		assertEquals("false", System.getProperty("dev.resteasy.exception.mapper"));
	}
}
