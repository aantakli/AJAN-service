package de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions.datamodels;

import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import lombok.Data;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.pf4j.Extension;

@Extension
@RDFBean("bt-mdp:belief")
@Data
public class Belief implements NodeExtension {

    @RDF("bt-mdp:state-id")
    private int stateId;

    @RDF("bt-mdp:state-name")
    private String stateName;

    @RDF("bt-mdp:state-type")
    private String stateType;

    @RDF("bt-mdp:state-probability")
    private float stateProbability;


}
