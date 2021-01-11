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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@RDFBean("bt:SelectQueryTemplate")
public class SelectQueryTemplate {

	@RDFSubject
	private String id;

	@RDF("bt:query")
	private BehaviorSelectQuery btQuery;

	@RDF("bt:stringTemplate")
	private String template;

	protected static final Logger LOG = LoggerFactory.getLogger(SelectQueryTemplate.class);
	private final ValueFactory vf = SimpleValueFactory.getInstance();

	/*
	$[start]$|$groupName$|
		$[start]$|$id$|$name$|$[end]$
		$[start]$|$room$|$[end]$
	$[end]$
	*/

	public String getResult(final Repository repo) {
		List<BindingSet> result = btQuery.getResult(repo);
		String tmpString = template;
		StringPart part = new StringPart();
		setTmpPart(template, part);
		LOG.info(part.getStartContent());
		Iterator<BindingSet> setItr = result.iterator();
		while (setItr.hasNext()) {
			BindingSet set = setItr.next();
			Iterator<Binding> itr = set.iterator();
			while (itr.hasNext()) {
				Binding bdg = itr.next();
				tmpString = tmpString.replace("$" + bdg.getName() + "$", bdg.getValue().stringValue());
			}
		}
		return tmpString;
	}

	private void setTmpPart(final String template, final StringPart part) {
		int strId = template.indexOf("$[start]$");
		int endId = template.indexOf("$[end]$");
		if (template.contains("$[start]$") && strId < endId) {
			String[] starts = template.split("(\\$\\[start\\]\\$)", 2);
			part.setStartContent(part.getStartContent() + starts[0]);
			StringPart child = new StringPart();
			setTmpPart(starts[1], child);
			part.getPartChilds().add(child);
			setTmpPart(child.getRestContent(), part);
		}
		if (template.contains("$[end]$") && (strId > endId) || strId == -1) {
			String[] ends = template.split("(\\$\\[end\\]\\$)", 2);
			part.setEndContent(part.getEndContent() + ends[0]);
			if (ends.length > 1) {
				part.setRestContent(part.getRestContent() + ends[1]);
			} else {
				part.setRestContent("");
			}
		}
	}
}
