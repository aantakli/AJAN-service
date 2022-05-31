package de.dfki.asr.ajan.pluginsystem.mqttplugin.utils;

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.Repository;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MQTTUtil {

    public static int getPortInfos(final BehaviorSelectQuery query, final AgentTaskInformation info) throws URISyntaxException {
        Repository repo = BTUtil.getInitializedRepository(info, query.getOriginBase());
        List<BindingSet> result = query.getResult(repo);
        if (!result.isEmpty()) {
            BindingSet bindings = result.get(0);
            return Integer.parseInt(bindings.getValue("port").stringValue());
        }
        return 0;
    }

    public static Map<String,String> getHostInfos(final BehaviorSelectQuery query, final AgentTaskInformation info) throws URISyntaxException {
        return getStringStringMap(query, info, "host", "port");
    }

    private static Map<String, String> getStringStringMap(BehaviorSelectQuery query, AgentTaskInformation info, String hostString, String port) throws URISyntaxException {
        Map<String,String> host = new HashMap();
        if (query != null) {
            Repository repo = BTUtil.getInitializedRepository(info, query.getOriginBase());
            List<BindingSet> result = query.getResult(repo);
            if (!result.isEmpty()) {
                BindingSet bindings = result.get(0);
                host.put(bindings.getValue(hostString).stringValue(), bindings.getValue(port).stringValue());
            }
        }
        return host;
    }

    public static  Map<String, String> getPublishInfo(final BehaviorSelectQuery query, final AgentTaskInformation info) throws URISyntaxException {
        return getStringStringMap(query, info, "topic", "message");
    }

    public static String getServerUrlInfo(final BehaviorSelectQuery query, final AgentTaskInformation info) throws URISyntaxException {
        return getStringMap(query, info, "serverUrl");
    }

    public static String getTopic(final BehaviorSelectQuery query, final AgentTaskInformation info) throws URISyntaxException {
        return getStringMap(query, info, "topic");
    }

	public static String getId(final BehaviorSelectQuery query, final AgentTaskInformation info) throws URISyntaxException {
        return getStringMap(query, info, "id");
    }

    public static String getStringMap(BehaviorSelectQuery query, AgentTaskInformation info, String stringNameToFetch) throws URISyntaxException {
        if(query != null){
            Repository repo = BTUtil.getInitializedRepository(info, query.getOriginBase());
            List<BindingSet> result = query.getResult(repo);
            if(!result.isEmpty()){
                BindingSet bindings = result.get(0);
                return bindings.getValue(stringNameToFetch).stringValue();
            }
        }
        return null;
    }
}
