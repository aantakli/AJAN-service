/*
 * Copyright (C) 2020 Andre Antakli (German Research Center for Artificial Intelligence, DFKI).
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

package de.dfki.asr.ajan.pluginsystem.stripsplugin.utils;

import de.dfki.asr.ajan.pluginsystem.stripsplugin.exception.NoActionAvailableException;
import graphplan.domain.Operator;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.slf4j.Logger;

public final class PlannerUtil {

	private PlannerUtil() {

	}

	public static int getRandomNumber(boolean mode, int size) {
		int planNumber = 0;
		if(mode) {
			planNumber = (int)(Math.random() * (size));
		}
		return planNumber;
	}

	public static void printPlan(List<Operator> plan, Logger logger, URIManager uriManager) {
		logger.info("Plan: ");
		plan.stream().map((operator) -> {
			StringBuilder builder = new StringBuilder();
			builder.append(uriManager.getURIFromHash(operator.getFunctor()));
			builder.append("(");
			List terms = operator.getTerms();
			int size = terms.size();
			int i = 0;
			for(Object term: terms) {
				builder.append(uriManager.getURIFromHash(term.toString()));
				if(i < size) {
					builder.append(",");
					i++;
				}
			}
			return builder;
		}).map((builder) -> {
			builder.append(")");
			return builder;
		}).forEach((builder) -> {
			logger.info(builder.toString());
		});
	}

	public static void checkActionsAvailability(List<URI> actions) throws URISyntaxException, NoActionAvailableException {
		URI nil = new URI(org.eclipse.rdf4j.model.vocabulary.RDF.NIL.toString());
		if(actions.get(0).equals(nil)){
			throw new NoActionAvailableException("No Actions defined for planning!");
		} 
	}

}
