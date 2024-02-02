package de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.endpoint.SOGBOFAPluginClient;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.MDPUtil;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.pf4j.Extension;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;


@Extension
@Component
@RDFBean("bt-mdp:CreateSOGBOFAClient")
public class CreateSOGBOFAClient extends AbstractTDBLeafTask implements NodeExtension {

    @RDFSubject
    @Getter
    @Setter
    private String url;

    @RDF("rdfs:label")
    @Getter @Setter
    private String label;


    @RDF("bt-mdp:port")
    @Getter @Setter
    private BehaviorSelectQuery port;

    @RDF("bt-mdp:rddlFilesPath")
    @Getter @Setter
    private BehaviorSelectQuery rddlFilesPath;

    @RDF("bt-mdp:rddlString")
    @Getter @Setter
    private BehaviorSelectQuery rddlString;

    @RDF("bt-mdp:instanceName")
    @Getter @Setter
    private BehaviorSelectQuery instanceName;


    @Override
    public NodeStatus executeLeaf() {
        String report = toString();
        Status stat;
        try {
            String portNumber = MDPUtil.getPortInfo(port,this.getObject());
            String filesPath = MDPUtil.getFilesPath(rddlFilesPath, this.getObject());
            String rddlDataString = MDPUtil.getRDDLString(rddlString, this.getObject());
            String instanceName = MDPUtil.getInstanceName(this.instanceName, this.getObject());
            SOGBOFAPluginClient.initClient(filesPath,portNumber, "AJAN_SOGBOFA_Client", instanceName);
            report = toString() + " SUCCEEDED";
            stat = Status.SUCCEEDED;
        } catch (URISyntaxException e) {
            report = toString()+ "FAILED";
            stat = Status.FAILED;
        }
        return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), report);
    }

    @Override
    public void end() {
        this.getObject().getLogger().info(this.getClass(),"Status (" + getStatus() + ")");
    }

    @Override
    public String toString() {
        return "CreateSOGBOFAClient (" + getLabel() + ")";
    }

    @Override
    public EvaluationResult.Result simulateNodeLogic(EvaluationResult result, Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }


    @Override
    public Resource getType() {
        return vf.createIRI("http://ajan.de/behavior/mdp-ns#CreateSOGBOFAClient");
    }


    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
        return super.getModel(model, root, mode);
    }
}