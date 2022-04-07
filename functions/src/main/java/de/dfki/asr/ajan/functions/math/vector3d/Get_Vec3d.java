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
public class Get_Vec3d implements Function {
	public static final String NAMESPACE = "http://www.ajan.de/ajan/functions/math/vector3d-ns#";

	@Override
	public String getURI() {
		return NAMESPACE + "vector3D";
	}

	@Override
	public Value evaluate(final ValueFactory valueFactory, final Value... args) throws ValueExprEvaluationException {
		if (args.length != 3) {
			throw new ValueExprEvaluationException(
					"square root function requires" + "exactly 1 argument, got "
							+ args.length);
		}

		Value argX = args[0];
		Value argY = args[1];
		Value argZ = args[2];

		if (!(argX instanceof Literal) || !(argY instanceof Literal) || !(argZ instanceof Literal)) {
			throw new ValueExprEvaluationException(
					"invalid argument (literal expected)");
		}

		float x = ((Literal) argX).floatValue();
		float y = ((Literal) argY).floatValue();
		float z = ((Literal) argZ).floatValue();

		return valueFactory.createLiteral(new Vector3D(x,y,z).toString());
	}
}
