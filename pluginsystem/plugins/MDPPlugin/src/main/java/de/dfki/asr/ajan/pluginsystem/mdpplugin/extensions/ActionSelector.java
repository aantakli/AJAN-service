package de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions;

import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.exception.SelectEvaluationException;
import de.dfki.asr.ajan.behaviour.nodes.Action;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBBranchTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.common.AJANVocabulary;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.vocabularies.POMDPVocabulary;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Extension
@RDFBean("bt-mdp:ActionSelector")
public class ActionSelector extends AbstractTDBBranchTask implements NodeExtension {
    private static final Logger LOG = LoggerFactory.getLogger(ActionSelector.class);

    @Setter
    @RDFSubject
    private String url;

    @RDF("rdfs:label")
    @Setter
    private String label;

    @RDF("bt:hasChildren")
    public List<Task<AgentTaskInformation>> getChildren() {
        return Arrays.asList(children.items);
    }

    public void setChildren(final List<Task<AgentTaskInformation>> newChildren) {
        children.clear();
        newChildren.forEach((task) -> children.add(task));
    }

    @Override
    public Resource getType() {
        return POMDPVocabulary.ACTION_SELECTOR;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(26);
        sb.append("ActionSelector (");
        sb.append(url);
        sb.append(" { ");
        for (Task<AgentTaskInformation> task : children) {
            sb.append(task.toString());
            sb.append(", ");
        }
        sb.append(" })");
        return sb.toString();
    }

    @Override
    public void childSuccess (final Task<AgentTaskInformation> runningTask) {
        super.childSuccess(runningTask);
        resetSkippedChildren(currentChildIndex);
        success();
    }

    @Override
    public void run() {
        try {
            // Get the information from the execution knowledge
            int childIndex = getIndexOfMatchedAction();
            if(childIndex==-1){
                throw new SelectEvaluationException("Attached Actions were not matching the planned action");
            }
            this.runningChild = this.children.get(childIndex);
            this.runningChild.setControl(this);
            this.runningChild.start();
            if(!this.runningChild.checkGuard(this)) {
                this.runningChild.fail();
            } else {
                this.runningChild.run();
            }
        } catch (Exception ex) {
            LOG.error(ex.toString());
            fail();
        }
    }

    int getIndexOfMatchedAction() throws URISyntaxException {
        // Get the information from the execution knowledge
        String query = "PREFIX pomdp-ns:<http://www.dfki.de/pomdp-ns#>\n" +
                "\n" +
                "SELECT DISTINCT ?plannedAction\n" +
                "WHERE {\n" +
                "\t?s ?p ?o .\n" +
                "\tpomdp-ns:POMDP pomdp-ns:current ?current .\n" +
                "\t?current pomdp-ns:plannedAction ?plannedAction\n" +
                "}\n";
        String plannedActionName = getBindings(query, this.getObject());
        for (int i =0 ; i < children.size; i++) {
            assert plannedActionName != null;
            String childName = ((Action)children.get(i)).getLabel();
            if(plannedActionName.equals(childName)){
                return i;
            }
       }
        return -1;
    }

    private String getBindings(String sparqlQuery, AgentTaskInformation info) throws URISyntaxException {
        URI originBase = new URI(AJANVocabulary.EXECUTION_KNOWLEDGE.toString());
        if(sparqlQuery!= null) {
            Repository repo = BTUtil.getInitializedRepository(info, originBase);
            try(RepositoryConnection connection = repo.getConnection()){
                TupleQuery query = connection.prepareTupleQuery(sparqlQuery);
                TupleQueryResult result = query.evaluate();
                List<BindingSet> bindings = new ArrayList<>();
                while(result.hasNext()) {
                    bindings.add(result.next());
                }
            return bindings.get(0).getValue("plannedAction").stringValue().replace(
                    POMDPVocabulary.ACTION.stringValue().replace("#","/"),"").substring(1);
            }
        }

        return null;
    }

    @Override
    public void childFail(final Task<AgentTaskInformation> runningTask) {
        super.childFail(runningTask);
        resetSkippedChildren(currentChildIndex);
        fail();
    }

    private void resetSkippedChildren(final int startIndex) {
        for (int i = startIndex; i < children.size; i++) {
            children.get(i).reset();
        }
    }
}
