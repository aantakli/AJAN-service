package de.dfki.asr.ajan.pluginsystem.visionnlpplugin.extensions;

import com.taxonic.carml.model.TriplesMap;
import com.taxonic.carml.util.RmlMappingLoader;
import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.knowledge.AbstractBeliefBase;
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
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriterFactory;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import de.dfki.asr.ajan.pluginsystem.mappingplugin.utils.MappingUtil;
import de.dfki.asr.ajan.pluginsystem.visionnlpplugin.utils.Prompts;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static de.dfki.asr.ajan.behaviour.service.impl.IConnection.BASE_URI;

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
            perceiveImage(ollamaChatModel, visionModel);
        } catch (Exception ex){
            report += "FAILED";
            return new NodeStatus(Status.FAILED, this.getObject().getLogger(), this.getClass(), report, ex);
        }
        report += "SUCCEEDED";
        return new NodeStatus(Status.SUCCEEDED, this.getObject().getLogger(), this.getClass(), report);
    }

    private void perceiveImage(ChatLanguageModel languageModel, ChatLanguageModel visionModel){

        // Get Mapping
        String mappingString = getMappingString();
        LOG.info("Fetched Mapping:`{}`", mappingString);


        // Get the image message
        UserMessage imageMessage = UserMessage.from(
                TextContent.from(this.imagePrompt!=null && !this.imagePrompt.isEmpty() ?this.imagePrompt:Prompts.IMAGE_PROMPT),
                ImageContent.from(getImage64String(), "image/png")
        );
        // get available namespaces from the model
        this.getObject().getAgentBeliefs().asModel().getNamespaces();
        long start = System.currentTimeMillis();
        // Generate the response
        Response<AiMessage> imageResponse = visionModel.generate(imageMessage);
        LOG.info("Image Inference Time:{} ms", System.currentTimeMillis()-start);
        LOG.info("Image Response:`{}`", imageResponse.content().text());

        // Generate the response with the language model prompt
        String imageResponse_with_prompt = String.format(Prompts.RDF_PROMPT, imageResponse.content().text());

        start = System.currentTimeMillis();

        // Generate the language model response
        String languageRDFResponse = languageModel.generate(imageResponse_with_prompt);
        LOG.info("Language Inference Time:{} ms", System.currentTimeMillis()-start);
        // validate the response as RDF

        // Print the response
        LOG.info("RDF from Image:`{}`", languageRDFResponse);

        // Update the agent beliefs
        try {
            storeInKnowledgeBase(languageRDFResponse, this.getObject().getAgentBeliefs());
        } catch (IOException | URISyntaxException | TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    private String getMappingString() {
        Repository repo = this.getObject().getDomainTDB().getInitializedRepository();
        String mappingString = null;
        Model mapping = null;
        Set<TriplesMap> mappingInput;
        StringWriter writer = new StringWriter();
        try {
            mapping = MappingUtil.getTriplesMaps(repo, this.mapping);
            mappingInput = RmlMappingLoader.build().load(mapping);

            TurtleWriterFactory turtleWriterFactory = new TurtleWriterFactory();
            TurtleWriter turtleWriter = (TurtleWriter) turtleWriterFactory.getWriter(writer);
            turtleWriter.getWriterConfig().set(BasicWriterSettings.PRETTY_PRINT, true);
            turtleWriter.getWriterConfig().set(BasicWriterSettings.INLINE_BLANK_NODES, true);

            Rio.write(mapping, turtleWriter);
            mappingString = writer.toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return mappingString;
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

    private void storeInKnowledgeBase(String s, AbstractBeliefBase beliefs) throws IOException, URISyntaxException, TransformerException {
        Model model = parseMessageAndGetModel(s);
        beliefs.update(model);
    }

    private Model parseMessageAndGetModel(String s) throws IOException {
        // check if the message is CSV, JSON or XML
        InputStream parsedMessage = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
        Model model = getModel(parsedMessage);
        return model;
    }

    private Model getModel(InputStream parsedMessage) throws IOException{
        if (parsedMessage != null) {
                return Rio.parse(parsedMessage, BASE_URI, RDFFormat.TURTLE);
            }
        return new LinkedHashModel();
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
