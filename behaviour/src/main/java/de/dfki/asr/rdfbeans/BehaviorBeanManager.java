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

package de.dfki.asr.rdfbeans;

import de.dfki.asr.ajan.behaviour.events.AJANGoal;
import de.dfki.asr.ajan.behaviour.nodes.common.Bound;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.*;
import de.dfki.asr.ajan.behaviour.events.JsonEvent;
import de.dfki.asr.ajan.behaviour.events.ModelEvent;
import de.dfki.asr.ajan.behaviour.service.impl.*;
import de.dfki.asr.ajan.behaviour.nodes.query.*;
import de.dfki.asr.ajan.behaviour.nodes.*;
import de.dfki.asr.ajan.behaviour.nodes.branch.*;
import de.dfki.asr.ajan.behaviour.nodes.common.Bindings;
import de.dfki.asr.ajan.behaviour.nodes.common.Variable;
import de.dfki.asr.ajan.behaviour.nodes.event.EventProducer;
import de.dfki.asr.ajan.behaviour.nodes.event.GoalProducer;
import de.dfki.asr.ajan.behaviour.nodes.event.HandleModelEvent;
import de.dfki.asr.ajan.behaviour.nodes.event.HandleModelQueueEvent;
import de.dfki.asr.ajan.behaviour.nodes.messages.*;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.cyberborean.rdfbeans.exceptions.RDFBeanValidationException;
import org.cyberborean.rdfbeans.reflect.RDFBeanInfo;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

@SuppressWarnings("PMD.CouplingBetweenObjects")
public class BehaviorBeanManager extends RDFBeanManager {

	protected static final Map<IRI, Class<?>> TYPE_MAP = new ConcurrentHashMap<>();

	static {
		try {
			initTypeMap();
			actionTypeMap();
			eventTypeMap();
		} catch (RDFBeanValidationException ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	public BehaviorBeanManager(final RepositoryConnection conn) {
		super(conn);
		setDatatypeMapper(new XSDDatatypeMapper());
	}

	public BehaviorBeanManager(final RepositoryConnection conn, final List<NodeExtension> extensions) throws RDFBeanValidationException {
		super(conn);
		setDatatypeMapper(new XSDDatatypeMapper());
		addTypes(extensions);
	}

	private void addTypes(final List<NodeExtension> extensions) throws RDFBeanValidationException {
		for (NodeExtension extension : extensions) {
			registerType(extension.getClass());
		}
	}

	@Override
	protected Class<?> getBindingClassForType(final IRI rdfType) throws RDFBeanException, RepositoryException {
		Class<?> binding = super.getBindingClassForType(rdfType);
		if (binding == null) {
			binding = TYPE_MAP.get(rdfType);
		}
		return binding; // might still be null, i.e. no binding available
	}

	private static void initTypeMap() throws RDFBeanValidationException {
		registerType(BTRoot.class);
		registerType(Condition.class);
		registerType(Evaluate.class);
		registerType(BehaviorQuery.class);
		registerType(BehaviorAskQuery.class);
		registerType(BehaviorConstructQuery.class);
		registerType(BehaviorSelectQuery.class);
		registerType(BehaviorUpdateQuery.class);
		registerType(Executor.class);
		registerType(Repeater.class);
		registerType(IntValue.class);
		registerType(Update.class);
		registerType(Message.class);
		registerType(SyncMessage.class);
		registerType(QueryDomain.class);
		registerType(LoadBehavior.class);
		registerType(Write.class);
		registerType(Variable.class);
		registerType(Bound.class);
		registerType(Bindings.class);
	}

	private static void actionTypeMap() throws RDFBeanValidationException {
		registerType(Action.class);
		registerType(MatchingAction.class);
		registerType(AbstractActionDefinition.class);
		registerType(ServiceActionDefinition.class);
		registerType(PluginActionDefinition.class);
		registerType(SelectQueryTemplate.class);
		registerType(Consumable.class);
		registerType(Producible.class);
		registerType(HttpBinding.class);
		registerType(ActnPayload.class);
		registerType(HttpHeader.class);
		registerType(ActionVariable.class);
	}

	private static void eventTypeMap() throws RDFBeanValidationException {
		registerType(EventProducer.class);
		registerType(GoalProducer.class);
		registerType(HandleModelEvent.class);
		registerType(HandleModelQueueEvent.class);
		registerType(ModelEvent.class);
		registerType(JsonEvent.class);
		registerType(AJANGoal.class);
	}

	protected static void registerType(final Class<?> aClass) throws RDFBeanValidationException {
		try {
			TYPE_MAP.put(RDFBeanInfo.get(aClass).getRDFType(), aClass);
		} catch (RDFBeanValidationException ex) {
			throw new RDFBeanValidationException("You tried to register a type "
					+ "which does not conform to RDFBeans requirements",
					aClass,
					ex);
		}
	}
}
