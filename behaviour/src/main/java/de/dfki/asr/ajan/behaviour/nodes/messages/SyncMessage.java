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

package de.dfki.asr.ajan.behaviour.nodes.messages;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.BTVocabulary;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.behaviour.service.impl.HttpBinding;
import de.dfki.asr.ajan.common.AgentUtil;
import java.net.URI;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;

@RDFBean("bt:SyncMessage")
public class SyncMessage extends Message {

	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("bt:validate")
	@Getter @Setter
	private BehaviorConstructQuery validate;

	@RDF("bt:queryUri")
	@Getter @Setter
	private BehaviorSelectQuery queryURI;

	@RDF("bt:binding")
	@Setter @Getter
	private HttpBinding binding;

	@RDF("bt:context")
	@Getter @Setter
	private URI context;

	@Override
	public Status execute() {
		super.setUrl(url);
		super.setBinding(binding);
		super.setQueryURI(queryURI);
		return super.execute();
	}

	@Override
	protected boolean checkResponse(final Object response) {
		if (response instanceof Model) {
			Model model = (Model) response;
			if (model.isEmpty()) {
				return false;
			}
			return updateBeliefs(modifyResponse(model), validate.getTargetBase());
		}
		return false;
	}

	protected Model modifyResponse(final Model response) {
		Model model = validate.getResult(response);
		if (context != null) {
			model = AgentUtil.setNamedGraph(model, context);
		}
		return model;
	}

	@Override
	public String toString() {
		return "SyncMessage (" + super.getUrl() + ")";
	}

	@Override
	public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
		if (mode.equals(BTUtil.ModelMode.DETAIL)) {
			queryURI.setResultModel(getInstance(root.getInstance()), BTVocabulary.QUERY_URI_RESULT, model);
			validate.setResultModel(getInstance(root.getInstance()), BTVocabulary.VALIDATE_RESULT, model);
		}
		return super.getModel(model, root, mode);
	}
}
