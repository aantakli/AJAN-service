/*
 * Copyright (C) 2020 Andre Antakli (German Research Center for Artificial Intelligence, DFKI).
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

package org.cyberborean.rdfbeans.datatype;

import java.text.ParseException;

import org.cyberborean.rdfbeans.datatype.DateUtils;

import junit.framework.TestCase;

/**
 * @author alex
 *
 */
public class DateUtilsTest extends TestCase {

	public void test()  {
		try {
			DateUtils.parseDate("2014-04-10T23:00:00.000Z");
			DateUtils.parseDate("2014-04-10T23:00:00Z");
			DateUtils.parseDate("2014-04-10T23:00Z");
			DateUtils.parseDate("2014-04-10T23:00:00-04:00");
		}
		catch (ParseException e) {
			fail(e.getMessage());
		}
	}
}
