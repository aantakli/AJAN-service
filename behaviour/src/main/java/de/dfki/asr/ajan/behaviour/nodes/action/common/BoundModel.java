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

import de.dfki.asr.ajan.common.SPARQLUtil;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.algebra.Exists;
import org.eclipse.rdf4j.query.algebra.Join;
import org.eclipse.rdf4j.query.algebra.LeftJoin;
import org.eclipse.rdf4j.query.algebra.Not;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;

public class BoundModel {

	private final TupleExpr tupleExpr;

	public BoundModel(final TupleExpr tupleExpr) {
		this.tupleExpr = tupleExpr;
	}

	public Model getModel(final ParsedTupleQuery selectQuery, final Model importModel, final boolean exists) {
		List<BindingSet> bindings = SPARQLUtil.queryModel(importModel,selectQuery);
		Model result = new LinkedHashModel();
		if (exists) {
			tupleExpr.visit(new ExistsVisitor(bindings, result));
		} else {
			tupleExpr.visit(new NotExistsVisitor(bindings, result));
		}
		return result;
	}

	protected static class ExistsVisitor extends AbstractQueryModelVisitor<RuntimeException> {

		protected List<BindingSet> bindings;
		private final Model model;

		public ExistsVisitor(final List<BindingSet> bindings, final Model model) {
			this.bindings = bindings;
			this.model = model;
		}

		@Override
		public void meet(final LeftJoin node) {
			node.getLeftArg().visit(this);
		}

		@Override
		public void meet(final Join node) {
			node.visitChildren(this);
		}

		@Override
		public void meet(final StatementPattern node) {
			node.visit(new StatementVisitor(bindings, model));
		}

		@Override
		public void meet(final Not node) {
			// Skip Not because we only want the existing
		}
	}

	protected static class NotExistsVisitor extends AbstractQueryModelVisitor<RuntimeException> {

		protected List<BindingSet> bindings;
		private final Model model;

		public NotExistsVisitor(final List<BindingSet> bindings, final Model model) {
			this.bindings = bindings;
			this.model = model;
		}

		@Override
		public void meet(final Exists node) {
			if (node.getParentNode().getClass() == Not.class) {
				node.visitChildren(new StatementVisitor(bindings, model));
			}
		}
	}

	protected static class StatementVisitor extends AbstractQueryModelVisitor<RuntimeException> {

		protected final List<BindingSet> bindings;
		private final Model model;

		public StatementVisitor(final List<BindingSet> bindings, final Model model) {
			this.bindings = bindings;
			this.model = model;
		}

		@Override
		public void meet(final StatementPattern node) {
			Var subVar = node.getSubjectVar();
			Var predVar = node.getPredicateVar();
			Var objVar = node.getObjectVar();
			bindings.stream().forEach((binding) -> {
				addToModel(getValue(binding, subVar), getValue(binding, predVar), getValue(binding, objVar));
			});
		}

		private void addToModel(final Value subj, final Value pred, final Value obj) {
			if (subj != null && pred != null && obj != null) {
				ValueFactory vf = SimpleValueFactory.getInstance();
				Resource subject = vf.createIRI(subj.stringValue());
				IRI predicate = vf.createIRI(pred.stringValue());
				model.add(subject,predicate,obj);
			}
		}

		private Value getValue(final BindingSet binding, final Var var) {
			if (var.hasValue()) {
				return var.getValue();
			} else if (binding.hasBinding(var.getName())) {
				return binding.getValue(var.getName());
			} else {
				return ACTNVocabulary.DUMMY;
			}
		}
	}
}
