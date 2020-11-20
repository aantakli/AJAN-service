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

package de.dfki.asr.ajan.behaviour.nodes.action.impl;

import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.nodes.Action;
import de.dfki.asr.ajan.behaviour.nodes.action.AbstractChainStep;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.TaskContext;
import de.dfki.asr.ajan.behaviour.nodes.action.definition.InputModel;
import de.dfki.asr.ajan.behaviour.nodes.action.TaskStep;
import de.dfki.asr.ajan.behaviour.nodes.action.common.ACTNUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.BTVocabulary;
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

public class LoadInputModel extends AbstractChainStep {

	private final ValueFactory vf = SimpleValueFactory.getInstance();

	public LoadInputModel(final TaskStep next) {
		super(next);
	}

	@Override
	public Task.Status execute(final TaskContext context) {
		Action action = context.get(Action.class);
		InputModel inputModel = ACTNUtil.getInputModel(action);
		if (inputModel == null) {
			return Task.Status.FAILED;
		}
		Resource node = vf.createBNode();
		if (validateURI(action.getUrl(), "http")) {
			inputModel.add(node, BTVocabulary.BT_NODE, vf.createIRI(action.getUrl()));
		} else {
			inputModel.add(node, BTVocabulary.BT_NODE, vf.createBNode());
		}
		inputModel.add(node, RDFS.LABEL, vf.createLiteral(action.getLabel()));
		context.put(inputModel);
		return executeNext(context);
	}

	private static boolean validateURI(final String uri, final String schema) {
		URL url;
		try {
			url = new URL(uri);
		} catch (MalformedURLException ex) {
			return false;
		}
		return schema.equals(url.getProtocol());
	}
}
