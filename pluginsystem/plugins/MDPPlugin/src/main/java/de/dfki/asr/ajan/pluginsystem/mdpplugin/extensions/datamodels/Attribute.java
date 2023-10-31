package de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions.datamodels;

import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import lombok.Data;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.pf4j.Extension;

@Extension
@RDFBean("bt-mdp:attributes")
@Data
public class Attribute implements NodeExtension {

    @RDF("bt-mdp:attribute-name")
    private String name;

    @RDF("bt-mdp:attribute-value")
    private String value;

}
