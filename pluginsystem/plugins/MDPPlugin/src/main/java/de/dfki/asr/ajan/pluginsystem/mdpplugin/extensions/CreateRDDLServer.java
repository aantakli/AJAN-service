package de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.endpoint.RDDLPluginServer;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.pf4j.Extension;
import org.springframework.stereotype.Component;

@Extension
@Component
@RDFBean("bt-mdp:CreateRDDLServer")
public class CreateRDDLServer extends AbstractTDBLeafTask implements NodeExtension {

    @RDFSubject
    @Getter @Setter
    private String url;

    @RDF("rdfs:label")
    @Getter @Setter
    private String label;


    @RDF("bt-mdp:port")
    @Getter @Setter
    private String port;

    @RDF("bt-mdp:rddlFilesPath")
    @Getter @Setter
    private String rddlFilesPath;

    @RDF("bt-mdp:rddlString")
    @Getter @Setter
    private String rddlString;


    @Override
    public NodeStatus executeLeaf() {
        String report = toString();

        RDDLPluginServer.initServer();

        report += "SUCCEEDED";
        return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), report);
    }

    @Override
    public void end() {
        this.getObject().getLogger().info(this.getClass(),"Status (" + getStatus() + ")");
    }

    @Override
    public EvaluationResult.Result simulateNodeLogic(EvaluationResult result, Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }


    @Override
    public Resource getType() {
        return vf.createIRI("http://ajan.de/behavior/mdp-ns#CreateRDDLServer");
    }


    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
        return super.getModel(model, root, mode);
    }
}
