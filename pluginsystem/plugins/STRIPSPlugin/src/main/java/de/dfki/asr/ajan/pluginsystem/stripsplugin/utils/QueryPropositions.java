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

import de.dfki.asr.ajan.pluginsystem.stripsplugin.exception.TermEvaluationException;
import graphplan.domain.Proposition;
import graphplan.domain.jason.PropositionImpl;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.rdf4j.query.algebra.Exists;
import org.eclipse.rdf4j.query.algebra.Not;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;

public class QueryPropositions {

	public void setPropositions(final List<Proposition> props, final TupleExpr tupleExpr, final URIManager uriManager, final Map<String, String> variables) {
		tupleExpr.visit(new StatementVisitor(props, uriManager, variables));
	}

	protected static class StatementVisitor extends AbstractQueryModelVisitor<RuntimeException> {

		protected List<Proposition> props;
		private boolean pos = true;
		private final URIManager uriManager;
		private final Map<String, String> variables;

		public StatementVisitor(final List<Proposition> props, final URIManager uriManager, final Map<String, String> variables) {
			this.props = props;
			this.uriManager = uriManager;
			this.variables = variables;
		}

		@Override
		public void meet(final Exists node) {
			pos = node.getParentNode().getClass() != Not.class;
			node.getSubQuery().visit(this);
			pos = true;
		}

		@Override
		public void meet(final StatementPattern node) {
			Var subject = node.getSubjectVar();
			Var predicate = node.getPredicateVar();
			Var object = node.getObjectVar();
			try {
				checkPredicate(predicate);
				PropositionImpl proposition = new PropositionImpl(pos,uriManager.setObjTermHash(predicate.getValue()));
				proposition.addTerm(getTerm(subject));
				proposition.addTerm(getTerm(object));
				props.add(proposition);
			} catch (TermEvaluationException ex) {
				Logger.getLogger(QueryPropositions.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		private void checkPredicate(final Var var) throws TermEvaluationException {
			if(!(var.hasValue())) {
				throw new TermEvaluationException("Predicate Term: " + var.getName() + " not supportet, no URI defined.");
			}
		}

		private Term getTerm(final Var var) throws TermEvaluationException {
			Term term;
			if (var.hasValue()) {
				term = new LiteralImpl(uriManager.setObjTermHash(var.getValue()));
			} else {
				String varHash = uriManager.getVarTermHash(var.getName());
				variables.put(varHash, var.getName());
				term = new VarTerm(varHash);
			}
			return term;
		}
	}
}
