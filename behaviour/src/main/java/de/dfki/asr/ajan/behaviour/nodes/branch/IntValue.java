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
import java.math.BigInteger;
import static java.math.BigInteger.ZERO;
import java.net.URISyntaxException;
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
	private Integer value;

	@RDF("bt:query")
	@Getter @Setter
	private BehaviorSelectQuery query;

	public BigInteger getIntValue(final AgentTaskInformation info) throws SelectEvaluationException {
		try {
			if (query == null) {
				return returnValue();
			}
			return getIntValue(BTUtil.getInitializedRepository(info, query.getOriginBase()));
		} catch (URISyntaxException ex) {
			throw new SelectEvaluationException("IntValue not defined as Integer", ex);
		}
	}

	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	public BigInteger getIntValue(final Repository repo) throws SelectEvaluationException {
		try {
			if (query == null) {
				return returnValue();
			} else {
				List<BindingSet> result = query.getResult(repo);
				if (result.isEmpty()) {
					return ZERO;
				}
				BindingSet bindings = result.get(0);
				Value strValue = bindings.getValue("intValue");
				if (strValue == null) {
					return ZERO;
				} else {
					return new BigInteger(strValue.stringValue().replace(" ",""));
				}
			}
		} catch (QueryEvaluationException ex) {
			throw new SelectEvaluationException("IntValue not defined as Integer", ex);
		}
	}

	private BigInteger returnValue() {
		if (value == null) {
			return BigInteger.valueOf(0);
		} else {
			return BigInteger.valueOf(value);
		}
	}
}
