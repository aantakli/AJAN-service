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
import java.net.URI;
import lombok.Data;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;

@RDFBean("bt:ConstructQuery")
@Data
public class BehaviorConstructQuery implements BehaviorQuery {

	private String id;
	private URI originBase;
	@RDF("bt:targetBase")
	private URI targetBase;
	private String sparql;
	private Model result;
	private IRI context;

	private Boolean reset = false;
	private final ValueFactory vf = SimpleValueFactory.getInstance();

	@Override
	public Model getResult(final Repository repo) {
		reset = false;
		result = SPARQLUtil.getNamedGraph(SPARQLUtil.queryRepository(repo, sparql));
		return result;
	}

	public Model getResult(final Model model) {
		SailRepository repo = SPARQLUtil.createRepository(model);
		Model resultModel = getResult(repo);
		repo.shutDown();
		return resultModel;
	}

	@Override
	public void setResultModel(final Resource resource, final Resource type, final Model model) {
		if (!reset && result != null && !result.isEmpty()) {
			Resource resultNode = BTUtil.setQueryResultModel(resource, model);
			model.add(resultNode, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, type);
			model.add(resultNode, org.eclipse.rdf4j.model.vocabulary.RDF.TYPE, BTVocabulary.GRAPH_RESULT);
			Resource resultGraph = BTUtil.setGraphResultModel(model, result);
			model.add(resultNode, BTVocabulary.HAS_RESULT, resultGraph);
		}
	}
}
