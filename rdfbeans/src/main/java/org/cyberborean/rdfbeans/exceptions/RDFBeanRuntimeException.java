package org.cyberborean.rdfbeans.exceptions;

/**
 * Unchecked carrier for an {@link RDFBeanException}. Needed where the checked
 * exception cannot cross an API boundary, e.g. {@link java.util.Iterator#next()}
 * of the iteration returned by {@code RDFBeanManager.getAll()}.
 */
public class RDFBeanRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RDFBeanRuntimeException(RDFBeanException cause) {
        super(cause.getMessage(), cause);
    }
}
