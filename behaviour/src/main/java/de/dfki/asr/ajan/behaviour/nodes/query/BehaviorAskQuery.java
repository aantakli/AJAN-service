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

package de.dfki.asr.ajan.behaviour.nodes.query;

import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.BTVocabulary;
import de.dfki.asr.ajan.common.SPARQLUtil;
import static de.dfki.asr.ajan.common.SPARQLUtil.getTupleExpr;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RDFBean("bt:AskQuery")
@Data
@SuppressWarnings("Duplicates")
public class BehaviorAskQuery implements BehaviorQuery {

	private String id;
	private URI originBase;
	private String sparql;
	private Boolean result;

	private Boolean reset = false;
	private List<BindingSet> bindings;
	private final ValueFactory vf = SimpleValueFactory.getInstance();
	private static final Logger LOG = LoggerFactory.getLogger(BehaviorAskQuery.class);

	@Override
	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	public Boolean getResult(final Repository repo) {
		reset = false;
		TupleExpr tupleExpr = getTupleExpr(sparql);
		try (RepositoryConnection conn = repo.getConnection()) {
			try {
				String queryString = SPARQLUtil.getSelectQuery(tupleExpr);
				TupleQueryResult list = conn.prepareTupleQuery(queryString).evaluate();
				readResult(list);
			} catch (Exception ex) {
				LOG.info("No binding results available.");
			}
			result = conn.prepareBooleanQuery(sparql).evaluate();
			return result;
		} catch (Exception ex) {
			return false;
		}
	}

	private void readResult(final TupleQueryResult list) {
		bindings = new ArrayList();
		while (list.hasNext()) {
			bindings.add(list.next());
		}
	}

	@Override
	public void setResultModel(final Resource resource, final Resource type, final Model model) {
		if (result != null && !reset) {
			Resource resultNode = BTUtil.setQueryResultModel(resource, model);
			model.add(resultNode, RDF.TYPE, BTVocabulary.BOOLEAN_RESULT);
			model.add(resultNode, RDF.TYPE, type);
			model.add(resultNode, BTVocabulary.HAS_RESULT, vf.createLiteral(result));
			if (bindings != null && !bindings.isEmpty()) {
				BTUtil.setQueryBindingModel(bindings, resource, model);
			}
		}
	}
}
