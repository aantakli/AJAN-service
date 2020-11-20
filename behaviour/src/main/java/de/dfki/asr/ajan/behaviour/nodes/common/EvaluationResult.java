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

package de.dfki.asr.ajan.behaviour.nodes.common;

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import lombok.Data;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

/**
 *
 * @author andredfki
 */
@Data
public class EvaluationResult {
	private final ResultCollection collection;
	private final EvaluationBase repo;
	private final AgentTaskInformation object;
	private Result childResult;
	private Direction direction;

	public enum Result {
		SUCCESS,
		UNCLEAR,
		FAIL
	}

	public enum Direction {
		Up,
		Down
	}

	public EvaluationResult(final Model model, final AgentTaskInformation object) {
		this.collection = new ResultCollection();
		this.repo = new EvaluationBase(model);
		this.object = object;
	}

	public void addResult(final Resource resource, final Model statements) {
		this.collection.getSteps().add(resource);
		statements.forEach((stmt) -> {
			this.collection.getModel().add(stmt);
		});
	}

	public Model getResultModel() {
		return collection.getModel();
	}

	public Model getEvaluationModel() {
		return collection.getCollectionModel();
	}

	public EvaluationResult setDirection(final Direction direction) {
		this.direction = direction;
		return this;
	}
}
