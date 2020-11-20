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

package de.dfki.asr.ajan.behaviour.nodes.action.common;

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.Action;
import de.dfki.asr.ajan.behaviour.service.impl.SelectQueryTemplate;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.AbstractActionDefinition;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.ActionVariable;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.InputModel;
import de.dfki.asr.ajan.behaviour.nodes.action.impl.LoadInputModel;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorQuery;
import de.dfki.asr.ajan.behaviour.service.impl.HttpHeader;
import static de.dfki.asr.ajan.common.AgentUtil.formatForMimeType;
import de.dfki.asr.ajan.common.SPARQLUtil;
import static de.dfki.asr.ajan.common.SPARQLUtil.getTupleExpr;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author andredfki
 */
@SuppressWarnings("PMD.ExcessiveImports")
public final class ACTNUtil {

	private static final Logger LOG = LoggerFactory.getLogger(LoadInputModel.class);

	private ACTNUtil() {

	}

	public static InputModel getInputModel(final Action action) {
		return getInputModel(action.getInputs(), action.getObject());
	}

	public static InputModel getInputModel(final List<BehaviorQuery> inputs, final AgentTaskInformation info) {
		InputModel inputModel = new InputModel();
		for (BehaviorQuery input: inputs) {
			Repository repo;
			try {
				repo = BTUtil.getInitializedRepository(info, input.getOriginBase());
			} catch (URISyntaxException | QueryEvaluationException ex) {
				LOG.error("Failed to get repository for input {}", input, ex);
				return null;
			}
			inputModel = getInputModel(inputs, repo);
		}
		return inputModel;
	}

	public static InputModel getInputModel(final List<BehaviorQuery> inputs, final Repository repo) {
		InputModel inputModel = new InputModel();
		for (BehaviorQuery input: inputs) {
			inputModel = ACTNUtil.addInputToModel(repo, input, inputModel);
		}
		return inputModel;
	}

	public static InputModel addInputToModel(final Repository repo, final BehaviorQuery input, final InputModel inputModel) {
		Model result = (Model) input.getResult(repo);
		Iterator<Statement> itr = result.iterator();
		while (itr.hasNext()) {
			inputModel.add(itr.next());
		}
		return inputModel;
	}

	public static Model getAddModel(final AbstractActionDefinition service, final InputModel inputModel) {
		String producibleQuery = service.getProducible().getSparql();
		ParsedTupleQuery selectQuery = ACTNUtil.createSelectQuery(service.getVariables(), service.getConsumable().getSparql());
		return ACTNUtil.getExistsModel(producibleQuery, selectQuery, inputModel);
	}

	public static Model getRemoveModel(final AbstractActionDefinition service, final InputModel inputModel) {
		String producibleQuery = service.getProducible().getSparql();
		ParsedTupleQuery selectQuery = ACTNUtil.createSelectQuery(service.getVariables(), service.getConsumable().getSparql());
		return ACTNUtil.getNotExistsModel(producibleQuery, selectQuery, inputModel);
	}

	public static ParsedTupleQuery createSelectQuery(final List<ActionVariable> vars, final String askQuery) {
		List<String> varNames = getVariableNames(vars);
		return SPARQLUtil.getSelectQuery(askQuery, varNames);
	}

	public static List<String> getVariableNames(final List<ActionVariable> vars) {
		List<String> varNames = new ArrayList();
		vars.forEach((var) -> {
			varNames.add(var.getVarName());
		});
		return varNames;
	}

	public static Model getExistsModel(final String askQuery, final ParsedTupleQuery selectQuery, final Model importModel) {
		TupleExpr tupleExpr = getTupleExpr(askQuery);
		BoundModel visitor = new BoundModel(tupleExpr);
		return visitor.getModel(selectQuery, importModel, true);
	}

	public static Model getNotExistsModel(final String askQuery, final ParsedTupleQuery selectQuery, final Model importModel) {
		TupleExpr tupleExpr = getTupleExpr(askQuery);
		BoundModel visitor = new BoundModel(tupleExpr);
		return visitor.getModel(selectQuery, importModel, false);
	}

	public static boolean dummyStatementsInModel(final Model model) {
		return model.contains(ACTNVocabulary.DUMMY, null, null, (Resource) null)
				|| model.contains(null, ACTNVocabulary.DUMMY, null, (Resource) null)
				|| model.contains(null, null, ACTNVocabulary.DUMMY, (Resource) null);
	}

	public static String getMimeTypeFromHeaders(final List<HttpHeader> headers) {
		String mimeType = "text/turtle";
		Iterator<HttpHeader> itr = headers.iterator();
		while (itr.hasNext()) {
			HttpHeader header = itr.next();
			String headerName = header.getHeaderName().getFragment();
			if ("Content-Type".equalsIgnoreCase(headerName)) {
				mimeType = header.getHeaderValue();
			}
		}
		return mimeType;
	}

	public static String getModelPayload(final Model model, final String mimeType) throws UnsupportedEncodingException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Rio.write(model, out, formatForMimeType(mimeType));
		return out.toString(StandardCharsets.UTF_8.displayName());
	}

	public static String getTemplatePayload(final Model model, final SelectQueryTemplate tmpl) {
		SailRepository repo = SPARQLUtil.createRepository(model);
		String result = tmpl.getResult(repo);
		repo.shutDown();
		return result;
	}
}
