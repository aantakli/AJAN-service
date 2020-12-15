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

package de.dfki.asr.ajan.behaviour.service.impl;

import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import java.util.Iterator;
import java.util.List;
import lombok.Data;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.Repository;

@RDFBean("bt:SelectQueryTemplate")
@Data
public class SelectQueryTemplate {
	@RDFSubject
	private String id;

	@RDF("bt:query")
	private BehaviorSelectQuery btQuery;

	@RDF("bt:stringTemplate")
	private String template;

	private final ValueFactory vf = SimpleValueFactory.getInstance();

	public String getResult(final Repository repo) {
		List<BindingSet> result = btQuery.getResult(repo);
		String tmpString = "";
		Iterator<BindingSet> setItr = result.iterator();
		while (setItr.hasNext()) {
			BindingSet set = setItr.next();
			Iterator<Binding> itr = set.iterator();
			while (itr.hasNext()) {
				Binding bdg = itr.next();
				tmpString = template.replace("$" + bdg.getName() + "$", bdg.getValue().stringValue());
			}
		}
		return tmpString;
	}
}
