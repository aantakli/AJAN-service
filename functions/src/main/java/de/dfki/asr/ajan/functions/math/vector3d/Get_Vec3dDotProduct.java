/*
 * Copyright (C) 2022 Andre Antakli (German Research Center for Artificial Intelligence, DFKI).
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
package de.dfki.asr.ajan.functions.math.vector3d;

import de.dfki.asr.ajan.functions.math.utils.Vector3dUtil;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.Function;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 *
 * @author Andre Antakli (German Research Center for Artificial Intelligence,
 * DFKI)
 */
public class Get_Vec3dDotProduct implements Function {
	public static final String NAMESPACE = "http://www.ajan.de/ajan/functions/math/vector3d-ns#";

	@Override
	public String getURI() {
		return NAMESPACE + "dot";
	}

	@Override
	public Value evaluate(final ValueFactory valueFactory, final Value... args) throws ValueExprEvaluationException {
		if (args.length != 2) {
			throw new ValueExprEvaluationException(
					"dot function requires" + "exactly 2 argument, got "
							+ args.length);
		}

		Value arg1 = args[0];
		Value arg2 = args[1];

		if (!(arg1 instanceof Literal) || !(arg2 instanceof Literal)) {
			throw new ValueExprEvaluationException(
					"invalid argument (literal expected)");
		}

		Vector3D vec1 = Vector3dUtil.getVector3D(((Literal) arg1).stringValue());
		Vector3D vec2 = Vector3dUtil.getVector3D(((Literal) arg2).stringValue());

		return valueFactory.createLiteral(Vector3D.dotProduct(vec1, vec2));
	}
}
