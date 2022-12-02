package de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.endpoint.RDDLPluginServer;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.MDPUtil;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.PlannerUnified;
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
    private BehaviorSelectQuery port;

    @RDF("bt-mdp:rddlFilesPath")
    @Getter @Setter
    private BehaviorSelectQuery rddlFilesPath;

    @RDF("bt-mdp:rddlString")
    @Getter @Setter
    private BehaviorSelectQuery rddlString;

    @RDF("bt-mdp:rddlInstanceName")
    @Getter @Setter
    private BehaviorSelectQuery rddlInstanceName;

    @RDF("bt-mdp:rddlClientName")
    @Getter @Setter
    private BehaviorSelectQuery rddlClientName;

    @RDF("bt-mdp:rddlExecutePolicy")
    @Getter @Setter
    private BehaviorSelectQuery rddlExecutePolicy;

    @RDF("bt-mdp:numOfRounds")
    @Getter @Setter
    private BehaviorSelectQuery numOfRounds;

    @RDF("bt-mdp:timeLimit")
    @Getter @Setter
    private BehaviorSelectQuery timeLimit;



    @Override
    public NodeStatus executeLeaf() {
        String report = toString();
        Status stat;
        try {
            String portNumber = MDPUtil.getPortInfo(port,this.getObject());
            String filesPath = MDPUtil.getFilesPath(rddlFilesPath, this.getObject());
            String rddlDataString = MDPUtil.getRDDLString(rddlString, this.getObject());
            String instanceName = MDPUtil.getStringMap(rddlInstanceName, this.getObject(), "rddlInstanceName");
            String clientName = MDPUtil.getStringMap(rddlClientName, this.getObject(), "rddlClientName");
            boolean executePolicy = Boolean.parseBoolean(MDPUtil.getStringMap(rddlExecutePolicy, this.getObject(), "rddlExecutePolicy"));
            int numRounds = Integer.parseInt(MDPUtil.getStringMap(numOfRounds, this.getObject(), "numOfRounds"));
            long timeLimit = Long.parseLong(MDPUtil.getStringMap(this.timeLimit, this.getObject(), "timeLimit"));
//            startServer(filesPath,instanceName,clientName,executePolicy,numRounds,1080000L);
            startServer(filesPath,instanceName,clientName,executePolicy,numRounds,timeLimit);
            report = toString()+ " SUCCEEDED";
            stat = Status.SUCCEEDED;
        } catch (URISyntaxException e) {
            report = toString()+ "FAILED";
            stat = Status.FAILED;
        }
        return new NodeStatus(stat, this.getObject().getLogger(), this.getClass(), report);
    }

    private void startServer(String filesPath, String instanceName, String clientName, boolean executePolicy, int numRounds, long timeLimit) throws URISyntaxException{
//        RDDLPluginServer.initServer(filesPath,portNumber);
        try {
            PlannerUnified planner = new PlannerUnified();
            planner.serverInitialize(filesPath);
            // TODO: parseString should be assigned
            planner.dummyServerStart(numRounds,timeLimit,instanceName,clientName,"RDDL",executePolicy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void end() {
        this.getObject().getLogger().info(this.getClass(),"Status (" + getStatus() + ")");
    }

    @Override
    public String toString() {
        return "CreateRDDLServer (" + getLabel() + ")";
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
