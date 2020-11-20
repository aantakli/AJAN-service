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
import static de.dfki.asr.ajan.behaviour.nodes.common.BTUtil.setQueryBindingModel;
import de.dfki.asr.ajan.behaviour.nodes.common.BTVocabulary;
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
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

@RDFBean("bt:SelectQuery")
@Data
public class BehaviorSelectQuery implements BehaviorQuery {

	private String id;
	private URI originBase;
	private String sparql;
	private List<BindingSet> bindings;

	private final ValueFactory vf = SimpleValueFactory.getInstance();
	private Boolean reset = false;

	@Override
	public List<BindingSet> getResult(final Repository repo) {
		reset = false;
		try (RepositoryConnection conn = repo.getConnection()) {
			TupleQuery query = conn.prepareTupleQuery(sparql);
			TupleQueryResult result = query.evaluate();
			bindings = new ArrayList();
			while (result.hasNext()) {
				bindings.add(result.next());
			}
			return bindings;
		}
	}

	@Override
	public void setResultModel(final Resource resource, final Resource type, final Model model) {
		if (!reset && bindings != null) {
			Resource resultNode = BTUtil.setQueryResultModel(resource, model);
			model.add(resultNode, RDF.TYPE, type);
			model.add(resultNode, RDF.TYPE, BTVocabulary.TUPLE_RESULT);
			Resource bndNode = vf.createBNode();
			model.add(resultNode, BTVocabulary.HAS_RESULT, bndNode);
			setQueryBindingModel(bindings, bndNode, model);
		}
	}
}
