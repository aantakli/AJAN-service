package de.dfki.asr.ajan.pluginsystem.visionnlpplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.visionnlpplugin.utils.LanguageModel;
import de.dfki.asr.ajan.pluginsystem.visionnlpplugin.utils.VisionLanguageModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.Repository;
import org.pf4j.Extension;
import org.springframework.stereotype.Component;

import de.dfki.asr.ajan.pluginsystem.visionnlpplugin.utils.Prompts;

import java.net.URISyntaxException;
import java.util.List;

@Extension
@Component
@RDFBean("vision-nlp:PerceiveImage")
public class PerceiveImage extends AbstractTDBLeafTask implements NodeExtension{

    @RDFSubject
    @Getter @Setter
    private String url;

    @RDF("rdfs:label")
    @Getter @Setter
    private String label;

    @RDF("vision-nlp:imageData")
    @Getter @Setter
    private BehaviorSelectQuery imageData;

    @RDF("vision-nlp:imagePrompt")
    @Getter @Setter
    private String imagePrompt;


    @Override
    public Resource getType() {
        return vf.createIRI("http://www.ajan.de/behavior/vision-nlp-ns#PerceiveImage");
    }

    @Override
    public NodeStatus executeLeaf() {
        String report = toString();
        try {
            ChatLanguageModel ollamaChatModel = LanguageModel.getChatLLMModel(this.getObject().getAgentBeliefs().getId());
            ChatLanguageModel visionModel = VisionLanguageModel.getChatLLMModel(this.getObject().getAgentBeliefs().getId());
            perceiveImage(ollamaChatModel, visionModel);
        } catch (Exception ex){
            report += "FAILED";
            return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), report, ex);
        }
        report += "SUCCEEDED";
        return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), report);
    }

    private void perceiveImage(ChatLanguageModel languageModel, ChatLanguageModel visionModel){
        // Get the image message
        UserMessage imageMessage = UserMessage.from(
                TextContent.from(this.imagePrompt!=null && !this.imagePrompt.isEmpty() ?this.imagePrompt:Prompts.IMAGE_PROMPT),
                ImageContent.from(getImage64String(), "image/png")
        );
        // get available namespaces from the model
        this.getObject().getAgentBeliefs().asModel().getNamespaces();
        // Generate the response
        Response<AiMessage> imageResponse = visionModel.generate(imageMessage);

        // Generate the response with the language model prompt
        String imageResponse_with_prompt = Prompts.RDF_PROMPT
                                + imageResponse.content().text();

        // Generate the language model response
        String languageRDFResponse = languageModel.generate(imageResponse_with_prompt);

        // validate the response as RDF

        // Print the response
        System.out.println("PerceiveImage: "+languageRDFResponse);
    }

    private String getImage64String() {
        try {
            Repository repo = BTUtil.getInitializedRepository(this.getObject(), imageData.getOriginBase());
            List<BindingSet> result = getImageData().getResult(repo);
            if(!result.isEmpty()){
                return result.get(0).getValue("imageData").stringValue();
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void end() {
        this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
    }

    @Override
    public String toString() {
        return "PerceiveImage (" + getLabel() + ")";
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
