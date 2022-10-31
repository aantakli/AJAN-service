package de.dfki.asr.ajan.pluginsystem.mdpplugin.utils;

import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.Repository;

import java.net.URISyntaxException;
import java.util.List;

public final class MDPUtil {
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

    public static String getPortInfo(final BehaviorSelectQuery query, final AgentTaskInformation info) throws URISyntaxException {
        return getStringMap(query, info, "port");
    }

    public static String getFilesPath(final BehaviorSelectQuery query, final AgentTaskInformation info) throws URISyntaxException {
        return getStringMap(query, info, "filesPath");
    }

    public static String getRDDLString(final BehaviorSelectQuery query, final AgentTaskInformation info) throws URISyntaxException {
        return getStringMap(query, info, "rddlString");
    }
}
