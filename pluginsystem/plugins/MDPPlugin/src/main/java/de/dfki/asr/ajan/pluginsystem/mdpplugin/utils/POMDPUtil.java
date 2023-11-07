package de.dfki.asr.ajan.pluginsystem.mdpplugin.utils;

import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.AJANLogger;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorConstructQuery;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.common.AJANVocabulary;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

public final class POMDPUtil {
    protected final static ValueFactory vf = SimpleValueFactory.getInstance();

    private POMDPUtil() {

    }

    public static void writeInput(final Model model, final String repository, final AgentTaskInformation info){
        if(repository.equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())){
            info.getExecutionBeliefs().update(model);
        } else if (repository.equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())){
            info.getAgentBeliefs().update(model);
        }
    }

    public static void removeInput(final Model model, final String repository, final AgentTaskInformation info) {
        if (repository.equals(AJANVocabulary.EXECUTION_KNOWLEDGE.toString())) {
            info.getExecutionBeliefs().update(new LinkedHashModel(), model, false);
        } else if (repository.equals(AJANVocabulary.AGENT_KNOWLEDGE.toString())) {
            info.getAgentBeliefs().update(new LinkedHashModel(), model, false);
        }
    }

    public static JSONObject getProbabilisticModelParams(int pomdpId,String type, String data, BehaviorSelectQuery probability, BehaviorSelectQuery sample, BehaviorSelectQuery argmax) {
        JSONObject params = new JSONObject();
        params.put("data", data);
        if(type !=null){
            params.put("type", type);
        }
        params.put("pomdp_id", pomdpId);
        params.put("probability_query", probability.toString());
        params.put("sample_query", sample.toString());
        params.put("argmax_query", argmax.toString());
        return params;
    }

    public static NodeStatus sendProbabilisticDataToEndpoint(final AgentTaskInformation metadata, final URI originBase,
                                                      int pomdpId,
                                                      String type,
                                                      String endpointUrl,
                                                      RDFFormat format,
                                                      BehaviorConstructQuery data,
                                                      BehaviorSelectQuery probability,
                                                      BehaviorSelectQuery sample,
                                                      BehaviorSelectQuery argmax,
                                                      AJANLogger logger,
                                                      Class<?> thisClass,
                                                      String objectString) {
        String dataString = getDataString(metadata, originBase, format, data);
        JSONObject params = POMDPUtil.getProbabilisticModelParams(pomdpId,type, dataString, probability, sample, argmax);
        int responseCode = HTTPHelper.sendPostRequest(endpointUrl, params, logger, thisClass);
        if(responseCode >= 300 || dataString == null){
            return new NodeStatus(Task.Status.FAILED, logger, thisClass, objectString+"FAILED");
        }
        return new NodeStatus(Task.Status.SUCCEEDED, logger, thisClass, objectString +" SUCCEEDED");
    }

    public static String getDataString(AgentTaskInformation metadata, URI originBase, RDFFormat format, BehaviorConstructQuery data) {
        Repository repository = null;
        String dataString = null;
        try {
            repository = BTUtil.getInitializedRepository(metadata, originBase);
            Model model = data.getResult(repository);
            dataString = KnowledgeBaseHelper.getString(model, format);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return dataString;
    }

}
