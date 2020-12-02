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

package de.dfki.asr.ajan.pluginsystem.mosimplugin.vocabularies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class MOSIMVocabulary {
	private static final ValueFactory FACTORY = SimpleValueFactory.getInstance();
	public final static IRI AVATAR = FACTORY.createIRI("http://www.dfki.de/mosim-ns#Avatar");
	public final static IRI MMU_DESCRIPTION = FACTORY.createIRI("http://www.dfki.de/mosim-ns#MMUDescription");
	public final static IRI M_SCENE_OBJECT = FACTORY.createIRI("http://www.dfki.de/mosim-ns#MSceneObject");
	public final static IRI M_TRANSFORM = FACTORY.createIRI("http://www.dfki.de/mosim-ns#MTransform");
	public final static IRI M_COLLIDER = FACTORY.createIRI("http://www.dfki.de/mosim-ns#MCollider");
	public final static IRI M_WALKPOINT = FACTORY.createIRI("http://www.dfki.de/mosim-ns#MWalkPoint");
	public final static IRI M_PART = FACTORY.createIRI("http://www.dfki.de/mosim-ns#Part");
	public final static IRI M_FINAL_LOCATION = FACTORY.createIRI("http://www.dfki.de/mosim-ns#FinalLocation");
	public final static IRI CO_SIMULATOR = FACTORY.createIRI("http://www.dfki.de/mosim-ns#CoSimulator");
	public final static IRI INSTRUCTION = FACTORY.createIRI("http://www.dfki.de/mosim-ns#Instruction");

	public final static IRI CONTAINS = FACTORY.createIRI("http://www.dfki.de/mosim-ns#contains");
	public final static IRI HAS_ACTION_ID = FACTORY.createIRI("http://www.dfki.de/mosim-ns#actionID");
	public final static IRI HAS_ID = FACTORY.createIRI("http://www.dfki.de/mosim-ns#id");
	public final static IRI HAS_TRANSFORM = FACTORY.createIRI("http://www.dfki.de/mosim-ns#transform");
	public final static IRI HAS_COLLIDER = FACTORY.createIRI("http://www.dfki.de/mosim-ns#collider");
	public final static IRI HAS_PATH_LENGTH = FACTORY.createIRI("http://www.dfki.de/mosim-ns#pathLength");
	public final static IRI HAS_POS_X = FACTORY.createIRI("http://www.dfki.de/mosim-ns#posX");
	public final static IRI HAS_POS_Y = FACTORY.createIRI("http://www.dfki.de/mosim-ns#posY");
	public final static IRI HAS_POS_Z = FACTORY.createIRI("http://www.dfki.de/mosim-ns#posZ");
	public final static IRI HAS_ROT_X = FACTORY.createIRI("http://www.dfki.de/mosim-ns#rotX");
	public final static IRI HAS_ROT_Y = FACTORY.createIRI("http://www.dfki.de/mosim-ns#rotY");
	public final static IRI HAS_ROT_Z = FACTORY.createIRI("http://www.dfki.de/mosim-ns#rotZ");
	public final static IRI HAS_ROT_W = FACTORY.createIRI("http://www.dfki.de/mosim-ns#rotW");
	public final static IRI HAS_MMU = FACTORY.createIRI("http://www.dfki.de/mosim-ns#mmu");
	public final static IRI HAS_NAME = FACTORY.createIRI("http://www.dfki.de/mosim-ns#name");
	public final static IRI HAS_MOTION_TYPE = FACTORY.createIRI("http://www.dfki.de/mosim-ns#motionType");
	public final static IRI HAS_HAND = FACTORY.createIRI("http://www.dfki.de/mosim-ns#hand");
	public final static IRI HAS_INITIAL_LOCATION = FACTORY.createIRI("http://www.dfki.de/mosim-ns#initialLocation");
	public final static IRI HAS_FINAL_LOCATION = FACTORY.createIRI("http://www.dfki.de/mosim-ns#finalLocation");
	public final static IRI IS_LOCATED_AT = FACTORY.createIRI("http://www.dfki.de/mosim-ns#isLocatedAt");
	public final static IRI HAS_INSTRUCTION = FACTORY.createIRI("http://www.dfki.de/mosim-ns#instruction");
	public final static IRI HAS_FINISHED = FACTORY.createIRI("http://www.dfki.de/mosim-ns#finished");
	public final static IRI HAS_MMU_PROPERTIES = FACTORY.createIRI("http://www.dfki.de/mosim-ns#mmuProperties");
	public final static IRI HAS_OBJECTS = FACTORY.createIRI("http://www.dfki.de/mosim-ns#objects");
	public final static IRI HAS_ACTION_NAME = FACTORY.createIRI("http://www.dfki.de/mosim-ns#actionName");
	public final static IRI HAS_START_CONDITION = FACTORY.createIRI("http://www.dfki.de/mosim-ns#startCondition");
	public final static IRI HAS_END_CONDITION = FACTORY.createIRI("http://www.dfki.de/mosim-ns#endCondition");

	public final static IRI HAS_EVENT_TYPE = FACTORY.createIRI("http://www.dfki.de/mosim-ns#eventType");
	public final static IRI HAS_HOST = FACTORY.createIRI("http://www.dfki.de/mosim-ns#host");
	public final static IRI HAS_PORT = FACTORY.createIRI("http://www.dfki.de/mosim-ns#port");
	public final static IRI HAS_CALLBACK = FACTORY.createIRI("http://www.dfki.de/mosim-ns#callback");
}
