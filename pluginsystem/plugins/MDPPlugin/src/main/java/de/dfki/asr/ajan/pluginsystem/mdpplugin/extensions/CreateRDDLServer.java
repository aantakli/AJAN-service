package de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.knowledge.AbstractBeliefBase;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.endpoint.RDDLPluginServer;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.KnowledgeBaseHelper;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.MDPUtil;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.PlannerUnified;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.Repository;
import org.pf4j.Extension;
import org.springframework.stereotype.Component;

import java.net.URI;
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

    @RDF("bt:mapping")
    @Getter @Setter
    private URI mapping;

    @RDF("bt:targetBase")
    @Getter @Setter
    private URI targetBase;

    @RDF("bt-mdp:rddlDomainName")
    @Getter @Setter
    private BehaviorSelectQuery rddlDomainName;

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

    @RDF("bt-mdp:domainContent")
    @Getter @Setter
    private BehaviorSelectQuery domainContent;

    @RDF("bt-mdp:instanceContent")
    @Getter @Setter
    private BehaviorSelectQuery instanceContent;


    @Override
    public NodeStatus executeLeaf() {
        String report = toString();
        Status stat;
        try {
            String domainName = MDPUtil.getStringMap(rddlDomainName, this.getObject(), "rddlDomainName");
            String instanceName = MDPUtil.getStringMap(rddlInstanceName, this.getObject(), "rddlInstanceName");
            String clientName = MDPUtil.getStringMap(rddlClientName, this.getObject(), "rddlClientName");
            String domainContent = MDPUtil.getStringMap(this.domainContent, this.getObject(), "domainContent");
            String instanceContent = MDPUtil.getStringMap(this.instanceContent, this.getObject(), "instanceContent");
            boolean executePolicy = Boolean.parseBoolean(MDPUtil.getStringMap(rddlExecutePolicy, this.getObject(), "rddlExecutePolicy"));
            int numRounds = Integer.parseInt(MDPUtil.getStringMap(numOfRounds, this.getObject(), "numOfRounds"));
            long timeLimit = Long.parseLong(MDPUtil.getStringMap(this.timeLimit, this.getObject(), "timeLimit"));
//            startServer(filesPath,instanceName,clientName,executePolicy,numRounds,1080000L);
            AbstractBeliefBase beliefBase = KnowledgeBaseHelper.getBeliefs(this.getObject(), targetBase);
            Repository repo = this.getObject().getDomainTDB().getInitializedRepository();
            startServer(domainName,instanceName,clientName,executePolicy,numRounds,timeLimit,beliefBase, repo, domainContent, instanceContent);

            report = toString()+ " SUCCEEDED";
            stat = Status.SUCCEEDED;
        } catch (URISyntaxException e) {
            report = toString()+ "FAILED";
            stat = Status.FAILED;
        }
        return new NodeStatus(stat, this.getObject().getLogger(), this.getClass(), report);
    }

    private void startServer(String domainName, String instanceName, String clientName, boolean executePolicy, int numRounds, long timeLimit, AbstractBeliefBase base,Repository repo, String domainContent, String instanceContent) throws URISyntaxException{
//        RDDLPluginServer.initServer(filesPath,portNumber);
        try {
            PlannerUnified planner = new PlannerUnified();
            planner.serverInitialize(instanceName, instanceName, base, repo, domainContent, instanceContent);
            // TODO: parseString should be assigned
            planner.dummyServerStart(numRounds,timeLimit,instanceName,clientName,"RDDL",executePolicy, mapping,domainContent, instanceContent);
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
