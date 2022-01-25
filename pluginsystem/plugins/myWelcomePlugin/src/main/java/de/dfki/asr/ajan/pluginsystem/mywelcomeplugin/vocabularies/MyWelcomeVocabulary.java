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

package de.dfki.asr.ajan.pluginsystem.mywelcomeplugin.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class MyWelcomeVocabulary {
	private static final ValueFactory FACTORY = SimpleValueFactory.getInstance();
	public final static IRI LOGGING = FACTORY.createIRI("http://www.ajan.de/myWelcome-ns#Logging");
	public final static IRI WELCOME_MESSAGE = FACTORY.createIRI("http://www.ajan.de/myWelcome-ns#Message");

	// Logging Levels
	public final static IRI LOG_INFO = FACTORY.createIRI("http://www.ajan.de/myWelcome-ns/logging#Info");
	public final static IRI LOG_WARN = FACTORY.createIRI("http://www.ajan.de/myWelcome-ns/logging#Warn");
	public final static IRI LOG_ERROR = FACTORY.createIRI("http://www.ajan.de/myWelcome-ns/logging#Error");
	public final static IRI LOG_FATAL = FACTORY.createIRI("http://www.ajan.de/myWelcome-ns/logging#Fatal");
	public final static IRI LOG_DEBUG = FACTORY.createIRI("http://www.ajan.de/myWelcome-ns/logging#Debug");
	public final static IRI LOG_SUMMARY = FACTORY.createIRI("http://www.ajan.de/myWelcome-ns/logging#Summary");
	public final static IRI LOG_NONE = FACTORY.createIRI("http://www.ajan.de/myWelcome-ns/logging#None");
}
