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
package de.dfki.asr.ajan.functions.math.test;

import de.dfki.asr.ajan.functions.math.utils.Vector3dUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.Function;


/**
 *
 * @author Andre Antakli (German Research Center for Artificial Intelligence,
 * DFKI)
 */
public class Get_TestSeekVector implements Function {
	public static final String NAMESPACE = "http://www.ajan.de/ajan/functions/math/test-ns#";

	String origin;
	String velocity;
	String target;
	float distance;
	float steeringDot;
	float mass;
	float maxForce;
	float maxVelocity;
	float maxSpeed;
	float impactRadius;
	
	@Override
	public String getURI() {
		return NAMESPACE + "seek";
	}

	@Override
	public Value evaluate(final ValueFactory valueFactory, final Value... args) throws ValueExprEvaluationException {
		if (args.length != 9) {
			throw new ValueExprEvaluationException(
					"seek function requires" + "exactly 1 argument, got "
							+ args.length);
		}
		
		origin = ((Literal) args[0]).stringValue();
		velocity = ((Literal) args[1]).stringValue();
		target = ((Literal) args[2]).stringValue();
		steeringDot = ((Literal) args[3]).floatValue();
		mass = ((Literal) args[4]).floatValue();
		maxForce = ((Literal) args[5]).floatValue();
		maxVelocity = ((Literal) args[6]).floatValue();
		maxSpeed = ((Literal) args[7]).floatValue();
		impactRadius = ((Literal) args[8]).floatValue();

		return valueFactory.createLiteral(calculateSteering().toString());
	}

	protected Vector3D calculateSteering() {
		Vector3D originVec = Vector3dUtil.getVector3D(origin);
		System.out.println("originVec: " + originVec);
		Vector3D velocityVec = Vector3dUtil.getVector3D(velocity);
		System.out.println("velocityVec: " + velocityVec);
		Vector3D targetVec = Vector3dUtil.getVector3D(target);
		System.out.println("targetVec: " + targetVec);
		distance = ((float)targetVec.distance(originVec));
		System.out.println("distance: " + distance);
		Vector3D desVelocityVec = getDesiredVelocity(new Vector3D(originVec.getX(),0,originVec.getZ()), new Vector3D(targetVec.getX(),0,targetVec.getZ()));
		System.out.println("desVelocityVec: " + desVelocityVec);
		desVelocityVec = rotateVelocity(velocityVec, desVelocityVec);
		System.out.println("rotated desVelocityVec: " + desVelocityVec);
		Vector3D steeringVec = desVelocityVec.subtract(velocityVec);
		steeringVec = truncate(steeringVec, optimizeForce());
		System.out.println("steeringVec: " + steeringVec);
		return setNewSteering(velocityVec, steeringVec.scalarMultiply(1/mass));
	}

	protected Vector3D getDesiredVelocity(final Vector3D origin, final Vector3D target) {
		Vector3D velo = calculateTargetVector(origin, target);
		if (!velo.equals(new Vector3D(0,0,0))) {
			velo = velo.normalize().scalarMultiply(maxVelocity);
		}
		return velo;
	}

	protected Vector3D calculateTargetVector(final Vector3D origin, final Vector3D target) {
		return target.subtract(origin);
	}

	protected Vector3D rotateVelocity(final Vector3D curSteer, final Vector3D desSteer) {
		Vector3D rotate = desSteer;
		if (!curSteer.equals(new Vector3D(0,0,0)) && !desSteer.equals(new Vector3D(0,0,0))) {
			double dot = Vector3D.dotProduct(curSteer.normalize(), desSteer.normalize());
			System.out.println("dot product: " + dot);
			if(dot < steeringDot) {
				Vector3D rotate1 = rotateVector(1.57, desSteer);
				double dot1 = Vector3D.dotProduct(curSteer.normalize(), rotate1.normalize());
				Vector3D rotate2 = rotateVector(-1.57, desSteer);
				double dot2 = Vector3D.dotProduct(curSteer.normalize(), rotate2.normalize());
				System.out.println("rotated Steering: " + getPositive(dot1) + ";" + getPositive(dot2));
				if (getPositive(dot1) < getPositive(dot2)) {
					rotate = rotate1;
				} else {
					rotate = rotate2;
				}
			}
		}
		return rotate;
	}

	protected float optimizeForce() {
		System.out.println("optForce: " + (maxForce + impactRadius * ( 1 / distance)));
		return maxForce + impactRadius * ( 1 / distance);
	}

	private Vector3D truncate(final Vector3D original, float max) {
		Vector3D steer = original;
		if (steer.getNormSq() > max) {
			steer = steer.normalize().scalarMultiply(max);
		}
		return steer;
	}

	protected Vector3D rotateVector(double radians, final Vector3D vector) {
		double x = vector.getX() * Math.cos(radians) - vector.getZ() * Math.sin(radians);
		double z = vector.getX() * Math.sin(radians) + vector.getZ() * Math.cos(radians);
		return new Vector3D(x,vector.getY(),z);
	}

	protected double getPositive(double input) {
		if (input < 0) {
			return -1 * input;
		} else {
			return input;
		}
	}

	private Vector3D setNewSteering(Vector3D velocity, final Vector3D steering) {
		System.out.println("addSteering: " + steering);
		return truncate(velocity.add(steering), maxSpeed);
	}
}
