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

import java.util.List;
import org.eclipse.rdf4j.query.algebra.Modify;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.UpdateExpr;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;

public class TupleExprRenderer {

	private final List<UpdateExpr> updateExprs;

	public TupleExprRenderer(final List<UpdateExpr> updateExprs) {
		this.updateExprs = updateExprs;
	}

	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public TupleExpr render() {
		TupleExprVisitor visitor = new TupleExprVisitor();
		for (UpdateExpr expr: updateExprs) {
			expr.visit(visitor);
		}
		return visitor.getExpr();
	}

	protected static class TupleExprVisitor extends AbstractQueryModelVisitor<RuntimeException> {

		private TupleExpr expr;

		public TupleExpr getExpr() {
			return expr;
		}

		@Override
		public void meet(final Modify node) {
			expr = node.getWhereExpr();
		}
	}
}
