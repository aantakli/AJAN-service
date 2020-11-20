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

package de.dfki.asr.rdfbeans;

import java.math.BigInteger;
import org.cyberborean.rdfbeans.datatype.DefaultDatatypeMapper;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

public class XSDDatatypeMapper extends DefaultDatatypeMapper {

	@Override
	public Object getJavaObject(final Literal literal) {
		if (literal.getDatatype().equals(XMLSchema.INTEGER)) {
			return new BigInteger(literal.getLabel());
		}
		return super.getJavaObject(literal);
	}

	@Override
	public Literal getRDFValue(final Object value, final ValueFactory vf) {
		if (value instanceof BigInteger) {
			vf.createLiteral((BigInteger)value);
		}
		return super.getRDFValue(value, vf);
	}
}
