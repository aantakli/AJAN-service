package de.dfki.asr.ajan.pluginsystem.visionnlpplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.visionnlpplugin.utils.LanguageModel;
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

@Extension
@Component
@RDFBean("vision-nlp:InitializeVisionLanguageModels")
public class InitializeVisionLanguageModels extends AbstractTDBLeafTask implements NodeExtension {

    @RDFSubject
    @Getter @Setter
    private String url;

    @RDF("rdfs:label")
    @Getter @Setter
    private String label;

    @RDF("vision-nlp:languageModelName")
    @Getter @Setter
    private String languageModelName;

    @RDF("vision-nlp:visionModelName")
    @Getter @Setter
    private String visionModelName;

    @RDF("vision-nlp:modelBaseUrl")
    @Getter @Setter
    private String modelBaseUrl;



    @Override
    public NodeStatus executeLeaf() {
        String report = toString();
        try {
            LanguageModel.initChatModel(
                    this.getObject().getAgentBeliefs().getId(),
                    this.languageModelName,
                    modelBaseUrl);
            VisionLanguageModel.initChatModel(
                    this.getObject().getAgentBeliefs().getId(),
                    this.visionModelName,
                    modelBaseUrl);
        } catch (Exception ex){
            report += "FAILED";
            return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), report, ex);
        }
        report += "SUCCEEDED";
        return  new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), report);
    }

    @Override
    public void end() {
        this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
    }

    @Override
    public Resource getType() {
        return vf.createIRI("http://www.ajan.de/behavior/vision-nlp-ns#InitializeVisionLanguageModels");
    }

    @Override
    public String toString() {
        return "InitializeVisionLanguageModels (" + getLabel() + ")";
    }

    @Override
    public EvaluationResult.Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
        return EvaluationResult.Result.UNCLEAR;
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
        return super.getModel(model, root, mode);
    }
}
