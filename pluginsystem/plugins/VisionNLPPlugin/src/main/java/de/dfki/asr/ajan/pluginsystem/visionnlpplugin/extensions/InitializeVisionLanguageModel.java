package de.dfki.asr.ajan.pluginsystem.visionnlpplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.visionnlpplugin.utils.VisionLanguageModel;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.pf4j.Extension;
import org.springframework.stereotype.Component;

import static de.dfki.asr.ajan.pluginsystem.visionnlpplugin.utils.Prompts.IMAGE_PROMPT;

@Extension
@Component
@RDFBean("vision-nlp:InitializeVisionLanguageModel")
public class InitializeVisionLanguageModel extends AbstractTDBLeafTask implements NodeExtension {


    @RDFSubject
    @Getter
    @Setter
    private String url;

    @RDF("rdfs:label")
    @Getter @Setter
    private String label;

    @RDF("vision-nlp:modelName")
    @Getter @Setter
    private String modelName;

    @Override
    public NodeStatus executeLeaf() {

        try {
            VisionLanguageModel.initChatModel(
                    this.getObject().getAgentBeliefs().getId(),
                    this.modelName,
                    "http://localhost:11434"
                    );
        } catch (Exception ex){
            return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), "FAILED", ex);
        }
        return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), "SUCCEEDED");
    }

    @Override
    public void end() {
        this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
    }

    @Override
    public EvaluationResult.Result simulateNodeLogic(EvaluationResult result, Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }

    @Override
    public String toString() {
        return "InitializeVisionLanguageModel (" + getLabel() + ")";
    }

    @Override
    public Resource getType() {
        return vf.createIRI("http://www.ajan.de/behavior/vision-nlp-ns#InitializeVisionLanguageModel");
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
        return super.getModel(model, root, mode);
    }
}