package de.dfki.asr.ajan.pluginsystem.visionnlpplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.knowledge.AbstractBeliefBase;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.utils.MappingUtil;
import de.dfki.asr.ajan.pluginsystem.visionnlpplugin.utils.LanguageModel;
import de.dfki.asr.ajan.pluginsystem.visionnlpplugin.utils.MappingHelper;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import de.dfki.asr.ajan.pluginsystem.visionnlpplugin.utils.Prompts;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

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
    private String userQuestion;

    @RDF("bt:mapping")
    @Getter @Setter
    private URI mapping;

    private static final Logger LOG = LoggerFactory.getLogger(PerceiveImage.class);

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

            // Get Mapping
            String mappingString = MappingHelper.getMappingString(this.getObject(), this.mapping);
            LOG.info("Fetched Mapping:`{}`", mappingString);

            String imageResponse = perceiveImage(visionModel, Objects.requireNonNull(getImage64String()));
            String jsonResponse = convertToJSONLD(ollamaChatModel, imageResponse, mappingString);
            storeInKnowledgeBase(jsonResponse, this.mapping, this.getObject().getDomainTDB().getInitializedRepository(), this.getObject().getAgentBeliefs());
        } catch (Exception ex){
            report += "FAILED";
            return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), report, ex);
        }
        report += "SUCCEEDED";
        return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), report);
    }

    private String perceiveImage(ChatLanguageModel visionModel, String image64String){
        // Get the image message
        UserMessage imageMessage = UserMessage.from(
                TextContent.from(this.userQuestion !=null && !this.userQuestion.isEmpty()
                        ? String.format(Prompts.IMAGE_PROMPT, this.userQuestion)
                        : String.format(Prompts.IMAGE_PROMPT, Prompts.QUESTION)),
                ImageContent.from(image64String, "image/png")
        );

        long start = System.currentTimeMillis();

        // Generate the response
        Response<AiMessage> imageResponse = visionModel.generate(imageMessage);
        LOG.info("Image Inference Time:{} ms", System.currentTimeMillis()-start);
        LOG.info("Image Response:`{}`", imageResponse.content().text());
        return imageResponse.content().text();
    }

    private String convertToJSONLD(ChatLanguageModel languageModel, String imageResponse, String mappingString) {
        // Generate the response with the language model prompt
        String imageResponse_with_prompt = String.format(Prompts.JSON_PROMPT, mappingString, imageResponse);
        long start = System.currentTimeMillis();

        // Generate the language model response
        String languageRDFResponse = languageModel.generate(imageResponse_with_prompt);
        LOG.info("Language Inference Time:{} ms", System.currentTimeMillis()-start);
        // validate the response as JSON-LD

        // Print the response
        LOG.info("JSON-LD from Image:`{}`", languageRDFResponse);
        return languageRDFResponse;
    }

    private Model mapToModel(String jsonLdResponse, Repository repo, URI mapping) throws URISyntaxException {
        InputStream parsedMessage = new ByteArrayInputStream(jsonLdResponse.getBytes(StandardCharsets.UTF_8));
//        return Rio.parse(parsedMessage, BASE_URI, RDFFormat.JSONLD);
        return MappingUtil.getMappedModel(MappingUtil.getTriplesMaps(repo, mapping), parsedMessage);
    }

    private void storeInKnowledgeBase(String s, URI mapping, Repository repo, AbstractBeliefBase beliefs) throws URISyntaxException {
        // Measure the RML Mapper Inference Time
        long start = System.currentTimeMillis();
        // RML Mapping
        Model model = mapToModel(s, repo, mapping);
        LOG.info("RML Mapping Time:{} ms", System.currentTimeMillis()-start);
        beliefs.update(model);
    }


//    private static void fetchNamespacesAndAddToWriter(Repository repo, TurtleWriter turtleWriter) {
//        try(RepositoryConnection conn = repo.getConnection()){
//            Iterable<Namespace> namespaces = conn.getNamespaces();
//            for (Namespace namespace : namespaces) {
//                turtleWriter.handleNamespace(namespace.getPrefix(), namespace.getName());
//            }
//        }
//    }

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
