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

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.exception.SelectEvaluationException;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.Repository;

@RDFBean("bt:IntValue")
@Data
public class IntValue {

	@RDF("bt:intValue")
	@Getter @Setter
	private List<Integer> value;

	@RDF("bt:query")
	@Getter @Setter
	private BehaviorSelectQuery query;

	public List<Integer> getIntValue(final AgentTaskInformation info) throws SelectEvaluationException {
		try {
			if (query == null || query.getSparql().equals("")) {
				return returnValue();
			}
			return getIntValue(BTUtil.getInitializedRepository(info, query.getOriginBase()));
		} catch (URISyntaxException ex) {
			throw new SelectEvaluationException("IntValue not defined as Integer", ex);
		}
	}

	@SuppressWarnings({"PMD.AvoidCatchingGenericException","PMD.AvoidInstantiatingObjectsInLoops"})
	public List<Integer> getIntValue(final Repository repo) throws SelectEvaluationException {
		try {
			if (query == null) {
				return returnValue();
			} else {
				List<BindingSet> result = query.getResult(repo);
				if (result.isEmpty()) {
					return returnValue();
				}
				Value strValue;
				List<Integer> values = new ArrayList<>();
				for (BindingSet set: result) {
					strValue = set.getValue("intValue");
					if (strValue != null) {
						values.add(Integer.getInteger(strValue.stringValue().replace(" ","")));
					}
				}
				return values;
			}
		} catch (QueryEvaluationException ex) {
			throw new SelectEvaluationException("IntValue not defined as Integer", ex);
		}
	}

	private List<Integer> returnValue() {
		List<Integer> values = new ArrayList<>();
		if (value == null) {
			values.add(0);
			return values;
		} else {
			return value;
		}
	}
}
