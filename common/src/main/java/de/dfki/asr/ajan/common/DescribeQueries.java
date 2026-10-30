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

package de.dfki.asr.ajan.common;

import java.util.Iterator;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.algebra.Filter;
import org.eclipse.rdf4j.query.algebra.MultiProjection;
import org.eclipse.rdf4j.query.algebra.Or;
import org.eclipse.rdf4j.query.algebra.ProjectionElem;
import org.eclipse.rdf4j.query.algebra.ProjectionElemList;
import org.eclipse.rdf4j.query.algebra.SameTerm;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.ValueConstant;
import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;

/**
 * Builds DESCRIBE-like CONSTRUCT algebra; backs {@link SPARQLUtil#getDescribeQuery(Iterator)}.
 */
final class DescribeQueries {

	private DescribeQueries() {

	}

	public static ParsedGraphQuery getDescribeQuery(final Iterator<Resource> resourceIterator) {
		String subjVar = "descr_subj";
		String predVar = "descr_pred";
		String objVar = "descr_obj";
		StatementPattern pattern = new StatementPattern(new Var(subjVar), new Var(predVar), new Var(objVar));
		Filter filter = new Filter(pattern, getDescribeFilter(resourceIterator, subjVar, objVar));
		ProjectionElemList projectionElems = new ProjectionElemList();
		projectionElems.addElement(new ProjectionElem(subjVar, "subject"));
		projectionElems.addElement(new ProjectionElem(predVar, "predicate"));
		projectionElems.addElement(new ProjectionElem(objVar, "object"));
		MultiProjection projection = new MultiProjection();
		projection.addProjection(projectionElems);
		projection.setArg(filter);
		return new ParsedGraphQuery(projection);
	}

	private static ValueExpr getDescribeFilter(final Iterator<Resource> resourceIterator, final String subjVar, final String objVar) {
		Resource resource = resourceIterator.next();
		if (resourceIterator.hasNext()) {
			return new Or(getSameTerm(resource, subjVar),
					new Or(getSameTerm(resource, objVar), getDescribeFilter(resourceIterator, subjVar, objVar)));
		} else {
			return new Or(getSameTerm(resource, subjVar), getSameTerm(resource, objVar));
		}
	}

	private static ValueExpr getSameTerm(final Resource resource, final String var) {
		return new SameTerm(new ValueConstant(resource), new Var(var));
	}
}
