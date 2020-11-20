package org.cyberborean.rdfbeans.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied to: Method declaration<br>
 * Value: String (required)
 * 
 * &#64;RDF annotation declares a RDFBean data property. The annotations must be
 * applied to getter methods of RDFBean class or interface.
 * 
 * The mandatory String value defines a qualified name or absolute URI of
 * an RDF property (predicate) mapped to this property.
 * 
 * Example:
 *
 * ```
 * {@literal @}RDF("foaf:name")
 * public String getName() { ...
 * ```
 * 
 * @version $Id: RDF.java 30 2011-09-28 06:46:32Z alexeya $
 * @author Alex Alishevskikh, alexeya(at)gmail.com
 * 
 */

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RDF {
	/**
	 * A qualified name or absolute URI of an RDF property
	 */
	String value() default "";
	
	String inverseOf() default "";
}
