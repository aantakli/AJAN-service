package de.dfki.asr.ajan.pluginsystem.mdpplugin.utils;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.xerces.parsers.DOMParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import rddl.RDDL;
import rddl.State;
import rddl.competition.*;
import rddl.policy.Policy;
import rddl.viz.NullScreenDisplay;
import rddl.viz.StateViz;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;

import static rddl.competition.SOGBOFA.*;
import static rddl.competition.Server.*;

import rddl.RDDL.PVAR_INST_DEF;



public class PlannerUnified {
    static Logger LOG = LoggerFactory.getLogger(PlannerUnified.class);
    public static String SERVER_FILES_DIR = "";

    private RDDL serverRddl;
    private String requestedInstance;
    private String clientName;
    private String inputLanguage;
    private boolean NO_XML_HEADER;
    private State state;
    private State clientState;
    private RDDL.INSTANCE instance;
    private RDDL.NONFLUENTS nonFluents;
    private RDDL.NONFLUENTS clientNonFluents;
    private boolean executePolicy = true;
    private RDDL.DOMAIN domain;
    private RDDL.DOMAIN clientDomain;
    private RandomDataGenerator rand;
    private StateViz stateViz;
    private int clientNumOfRounds;
    private double clientTimeAllowed;
    private RDDL clientRddl;
    private long randomSeed = 0;
    private RDDL.INSTANCE clientInstance;
    private boolean USE_TIMEOUT = true;
    private boolean MONITOR_EXECUTION = false;


    String serverTask;
    private int clientRound;
    private int clientNumRounds;
    private double clientTimeLimit;
    private String clientRequestedInstance;
    private double clientReward;
    private int clientTurnsUsed;
    private long clientTimeLeft;
    private double clientImmediateReward;
    private double clientTotalReward;
    private int clientRoundsUsed;
    private boolean clientExecutePolicy;


    String client_msg;
    Policy clientPolicy;
    Class client_c;
    String clientInstanceName;

    public void serverInitialize(String filespath) {
        rand = new RandomDataGenerator();
//        timeAllowed = DEFAULT_TIME_ALLOWED;
        numRounds = DEFAULT_NUM_ROUNDS;
        StateViz state_viz = new NullScreenDisplay(false);

        try{
            SERVER_FILES_DIR = filespath;
            File[] subdirs = new File(SERVER_FILES_DIR).listFiles(File::isDirectory);
            serverRddl = new RDDL(SERVER_FILES_DIR);
        } catch (Exception e) {
            LOG.error("Error in server initialization"+e.getMessage());
        }
    }
    public void clientInitialize(String instanceName, String clientName) {
        State      clientState;
        RDDL.INSTANCE clientInstance;
        RDDL.NONFLUENTS clientNonFluents = null;
        RDDL.DOMAIN clientDomain;
        StringBuffer instr = new StringBuffer();

        try{
            try {
                if (clientName.equals("Original")){
                    clientName = "SOGBOFA_Original";
                } else {
                    clientName = "L_C_SOGBOFA";
                }

                String msg = createXMLSessionRequest(instanceName);
                // sendOneMessage(msg); not required
                DOMParser p = new DOMParser();
//                processXMLSessionInit(p, parseString, instanceName);
//                processXMLSessionInit();
                LOG.info("Client number of rounds:"+clientNumOfRounds);
                LOG.info("Client time allowed:"+clientTimeAllowed);
                VisCounter vc = new VisCounter();

                Class c = Class.forName("rddl.policy." + clientName);
                clientState = new State();
                Policy policy = (Policy) c.getConstructor(String.class).newInstance(instanceName);
                policy.setRandSeed(randomSeed);
                policy.setVisCounter(vc);

                clientInstance = clientRddl._tmInstanceNodes.get(instanceName);

                if (clientInstance._sNonFluents != null) {
                    clientNonFluents = clientRddl._tmNonFluentNodes.get(clientInstance._sNonFluents);
                }

                clientDomain = clientRddl._tmDomainNodes.get(clientInstance._sDomain);
                if(clientNonFluents !=null && !clientInstance._sDomain.equals(clientNonFluents._sDomain)) {
                    LOG.error("Domain name in instance file does not match domain name in non-fluents file: " + clientInstance._sDomain + " vs. " + clientNonFluents._sDomain);
                    return;
                }

                clientState.init(clientDomain._hmObjects, clientNonFluents!=null?clientNonFluents._hmObjects:null, clientInstance._hmObjects,
                       clientDomain._hmTypes, clientDomain._hmPVariables, clientDomain._hmCPF, clientInstance._alInitState,
                       clientNonFluents==null?new ArrayList<RDDL.PVAR_INST_DEF>():clientNonFluents._alNonFluents, clientInstance._alNonFluents,
                       clientDomain._alStateConstraints, clientDomain._alActionPreconditions, clientDomain._alStateInvariants,
                       clientDomain._exprReward, clientInstance._nNonDefActions);
                if((clientDomain._bPartiallyObserved && clientState._alObservNames.size() == 0)
                        || (!clientDomain._bPartiallyObserved && clientState._alObservNames.size() > 0)) {
                    boolean observations_present = clientState._alObservNames.size() > 0;
                    LOG.warn("Domain '" + clientDomain._sDomainName
                            + "' partially observed (PO) flag and presence of observations mismatched." +
                            "\nSetting PO flag = " + observations_present + ".");
                    clientDomain._bPartiallyObserved = observations_present;
                }

                policy.horizon = clientInstance._nHorizon;
                policy.setInstance(instanceName);
                policy.setVisCounter(vc);
                Policy.ifFirstStep = true;
                Policy.timeUsedForCal = 0;
                Policy.updatesIntotal = 0;
                Policy.numberNodesUpdates = 0;
                Policy.randomAction = new ArrayList<>();
                Policy.ifConstructConstraints = true;
                //clientStart(clientState, clientInstance, clientNonFluents, clientDomain, msg, policy, c, p, instanceName, clientName, parseString);
                initializeClient(clientState, clientInstance, clientNonFluents, clientDomain, policy, c, p, instanceName);
            } catch ( InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                LOG.error("Error in client initialization"+e.getMessage());
            } catch (ClassNotFoundException e) {
                LOG.error("Class not found:"+e.getMessage());
            }
        } catch (Exception e) {
            LOG.error("Error in client initialization"+e.getMessage());
        }
    }

    public void dummyServerStart(int numRounds, long timeLimit, String problemName,String clientName,String inputLanguage,boolean executePolicy){

        try{
            long start_time = System.currentTimeMillis();
            DOMParser p = new DOMParser();
            processXMLSessionRequest(problemName,clientName,inputLanguage);
            LOG.info("Instance requested: " + this.requestedInstance);
            if(this.serverRddl._tmInstanceNodes.containsKey(this.requestedInstance)) {
                LOG.info("Instance found");
                createXMLSessionInit(numRounds, (double) timeLimit);
                processXMLSessionInit(problemName); // client side processing
                clientInitialize(problemName,clientName);
                // region client variables
                int client_r = 0;
                long clientTotalRounds = (long)numRounds;
                long clientTotalStepLeft = clientTotalRounds * (long)clientInstance._nHorizon;
                long clientTotalStepUsed = 0L;
                long clientTotalTimeUsage = 0;
                double clientTotalTimeForInit = 0;
                double clientTotalTimeAllowed = clientTimeAllowed;
                double clientEmergentTime = 3.0 * clientTotalStepLeft/(80 * 100);
                //endregion
                boolean OUT_OF_TIME = false;
                this.initializeState(this.serverRddl, this.requestedInstance);
                double accum_total_reward = 0.0;
                ArrayList<Double> rewards = new ArrayList<>();
                int r = 0;
                double[] rewardArray = new double[numRounds];
                for (rewardArray = new double[numRounds]; r < numRounds && !OUT_OF_TIME; r++){
                    double t0 = System.currentTimeMillis();
                    clientState.init(clientDomain._hmObjects, clientNonFluents!=null?clientNonFluents._hmObjects:null,clientInstance._hmObjects,
                            clientDomain._hmTypes,clientDomain._hmPVariables,clientDomain._hmCPF,clientInstance._alInitState,
                            clientNonFluents==null?new ArrayList<>():clientNonFluents._alNonFluents,clientInstance._alNonFluents,
                            clientDomain._alStateConstraints,clientDomain._alActionPreconditions,clientDomain._alStateInvariants,
                            clientDomain._exprReward,clientInstance._nNonDefActions); //client side processing
                    if (!this.executePolicy) {
                        --r;
                    }
                    // region client side processing
                    createXMLRoundRequest(true); // client side processing
                    clientTotalTimeUsage += System.currentTimeMillis() - t0;
                    clientTotalTimeForInit += System.currentTimeMillis() - t0;
                    double clientTimePerInit = clientTotalTimeForInit / (client_r + 1);
                    //endregion
                    // region server side processing
                    if(!processXMLRoundRequest(executePolicy)){
                        break;
                    }
                    resetState();
                    createAndProcessXMLRoundInit(r+1, numRounds, timeLimit - System.currentTimeMillis() + start_time);
                    //endregion
                    // region client side processing
                    t0 = System.currentTimeMillis();

                    // region client side variables
                    int client_h = 0;
                    boolean client_round_ended_early=false;
                    //endregion
                    //region server side processing
                    if (this.executePolicy) {
                        LOG.info("Round " + (r + 1) + " / " + numRounds + ", time remaining: " + (timeLimit - System.currentTimeMillis() + start_time));
                        // Memory usage
                    }
                    double immediate_reward = 0.0;
                    double accum_reward = 0.0;
                    double cur_discount = 1.0;
                    int h = 0;
                    HashMap<RDDL.PVAR_NAME, HashMap<ArrayList<RDDL.LCONST>, Object>> observStore = null;
                    //endregion
                    do {
                        // region server side processing
                        String msg = createXMLTurn(this.state, h+1, this.domain, observStore, (double)(timeLimit - System.currentTimeMillis() + start_time), immediate_reward);
                        //endregion
                        // region client side processing
//                        for(;client_h< clientInstance._nHorizon;++client_h){

                        if(client_h>clientInstance._nHorizon-1){
                            break;
                        }
                        clientTotalTimeUsage += System.currentTimeMillis() - t0;
                        t0 = System.currentTimeMillis();
                        Element e = parseMessage(p,msg);
                        client_round_ended_early = e.getNodeName().equals(Server.ROUND_END);
                        if(client_round_ended_early){
                            break;
                        }
                        ArrayList<RDDL.PVAR_INST_DEF> client_obs = processXMLTurn(e,clientState);
                        LOG.info("\n******************************************");
                        LOG.info("New Turn Started. Starting initialization...");
                        LOG.info("******************************************");
                        LOG.info("Time left: " + clientTimeAllowed);
                        boolean clientIfEmergency = false;
                        if(client_r!=0 || client_h!=0){
                            LOG.info("Total time used: " + clientTotalTimeUsage);
                            LOG.info("Time per Init: " + clientTimePerInit);
                            LOG.info("Runs left: " + (clientTotalRounds - client_r - 1));
                            LOG.info("time allowed: " + clientTimeAllowed);
                            clientTimeAllowed = clientTimeAllowed - clientTimePerInit * (clientTotalRounds - client_r - 1);
                            if(timeLimit < 1000 * 60 * clientEmergentTime) {
                                System.out.println("Emergent!! With time = " + clientTimeAllowed);
                                clientIfEmergency = true;
                            }
                        }
                        LOG.info("We use " + clientTimeAllowed + " to calculate");
                        double clientTimeForStep = clientTimeAllowed/(double)clientTotalStepLeft;

                        if(client_r == 0 && client_h == 0){
                            Policy.ifFirstStep = true;
                            clientTimeForStep = clientTimeAllowed*0.02;
                        }
                        else {
                            Policy.ifFirstStep = false;
                        }
                        clientTotalStepUsed++;
                        if(client_obs == null){
                            LOG.info("No state/observations received.");
                        }
                        else if(clientDomain._bPartiallyObserved){
                            clientState.clearPVariables(clientState._observ);
                            clientState.setPVariables(clientState._observ,client_obs);
                        } else {
                            clientState.clearPVariables(clientState._state);
                            clientState.setPVariables(clientState._state,client_obs);
                        }

                        clientPolicy.setCurrentRound(client_h);
                        clientPolicy.setRddlDomain(clientRddl);
                        clientPolicy.setTimeAllowed((new Double(clientTimeForStep)).longValue());
                        clientPolicy.timeAllowed = clientTimeAllowed;
                        clientPolicy.instanceName = clientInstanceName;
                        clientPolicy.algoName = clientName;
                        LOG.info("Steps left: "+clientTotalStepLeft);
                        LOG.info("Time allowed for this step: "+clientTimeForStep);

                        ArrayList<RDDL.PVAR_INST_DEF> client_actions = null;
                        if(clientIfEmergency){
                           LOG.info("Time is not sufficient. Require: " + (clientTotalTimeUsage / clientTotalStepUsed)
                                   + ", but only have " + clientTimeAllowed);
                           try {
                               client_actions = clientPolicy.EmergencyReturn(clientState);
                           } catch (Exception e1) {
                               e1.printStackTrace();
                           }
                        }
                        else {
                            client_actions = clientPolicy.getActions(client_obs ==null?null:clientState);
                        }
                        client_msg = createXMLAction(client_actions);
                        clientTotalTimeUsage += System.currentTimeMillis() - t0;
                        clientTotalStepLeft--;
                        ++client_h;
//                        }
                        //endregion
                        //region server side processing
                        ArrayList<RDDL.PVAR_INST_DEF> ds = processXMLAction(p,client_msg);
                        if(ds == null){
                            break;
                        }
                        if(this.executePolicy){
                            boolean suppress_object_cast_temp = RDDL.SUPPRESS_OBJECT_CAST;
                            RDDL.SUPPRESS_OBJECT_CAST = true;
                            LOG.info("** Actions received:  " + ds);
                            RDDL.SUPPRESS_OBJECT_CAST = suppress_object_cast_temp;
                        }
                        try{
                            state.checkStateActionConstraints(ds);
                        } catch (Exception e1){
                            LOG.error("TRIAL ERROR -- ACTION NOT APPLICABLE:\n"+e1.getMessage());
                            return;
                            //break;
                        }
                        try{
                            state.computeNextState(ds, rand);
                        } catch(Exception e1){
                            LOG.error("FATAL SERVER EXCEPTION (in computing the next state):\n"+e1.getMessage());
//                            return;
                        }

                        if(domain._bPartiallyObserved){
                            observStore = copyObserv(state._observ);
                        }

                        immediate_reward = ((Number)domain._exprReward.sample(new HashMap<>(), state, rand)).doubleValue();
                        rewards.add(immediate_reward);
                        accum_reward += cur_discount * immediate_reward;
                        cur_discount *= this.instance._dDiscount;
//                        this.stateViz.display(this.state, h);
                        state.advanceNextState();
                        OUT_OF_TIME = (System.currentTimeMillis() - start_time > clientTimeAllowed) && USE_TIMEOUT;
                        ++h;
                        if (OUT_OF_TIME || ((instance._termCond == null) && (h == instance._nHorizon)) ||
                                ((instance._termCond != null) && state.checkTerminationCondition(instance._termCond))) {
                            break;
                        }
                        //endregion
                    } while(!OUT_OF_TIME && (this.instance._termCond !=null || h != this.instance._nHorizon) && (this.instance._termCond == null || !this.state.checkTerminationCondition(this.instance._termCond)));
                    //region serverside processing
                    if(this.executePolicy){
                        rewardArray[r] = accum_reward;
                        accum_total_reward += accum_reward;
                        LOG.info("** Round reward: "+accum_reward);
                    }

                    createAndProcessXMLRoundEnd(this.requestedInstance, r, accum_reward, h, timeLimit-System.currentTimeMillis()+start_time,immediate_reward);
                    //endregion
                }
                createAndProcessXMLSessionEnd(this.requestedInstance, accum_total_reward, r, timeLimit - System.currentTimeMillis() + start_time);
                // region client side processing
                Records visRec = new Records();
                double res = 0;
                if(clientPolicy._visCounter.randomTime == 0)
                    res = 0.0;
                else
                    res = clientPolicy._visCounter.randomInTotal / clientPolicy._visCounter.randomTime;
                double res2 = 0;
                double res3 = 0;
                double res5 = 0.0;
                if(clientPolicy._visCounter.updateTime == 0)
                    res2 = 0.0;
                else
                    res2 = clientPolicy._visCounter.updatesInTotal / clientPolicy._visCounter.updateTime;
                if(clientPolicy._visCounter.SeenTime == 0)
                    res3 = 0.0;
                else
                    res3 = clientPolicy._visCounter.SeenInTotal / clientPolicy._visCounter.SeenTime;
                double res4 = 0;
                if(clientPolicy._visCounter.depthTime == 0)
                    res4 = 0.0;
                else
                    res4 = clientPolicy._visCounter.depthInTotal / clientPolicy._visCounter.depthTime;

                res5 = clientPolicy._visCounter.sizeInTotal / clientPolicy._visCounter.depthTime;
                visRec.fileAppend(clientName + "_" + (int)Math.round(clientTimeAllowed / 1000) + "_" + clientInstanceName + "_" + "rrCounter", String.valueOf(res));
                visRec.fileAppend(clientName + "_" + (int)Math.round(clientTimeAllowed / 1000) + "_" + clientInstanceName + "_" + "updatesCounter", String.valueOf(res2));
                visRec.fileAppend(clientName + "_" + (int)Math.round(clientTimeAllowed / 1000) + "_" + clientInstanceName + "_" + "seenCounter", String.valueOf(res3));
                visRec.fileAppend(clientName + "_" + (int)Math.round(clientTimeAllowed / 1000) + "_" + clientInstanceName + "_" + "depthCounter", String.valueOf(res4));
                visRec.fileAppend(clientName + "_" + (int)Math.round(clientTimeAllowed / 1000) + "_" + clientInstanceName + "_" + "sizeCounter", String.valueOf(res5));
                clientPolicy.sessionEnd(clientTotalReward);
                //endregion
                Records record = new Records();
                double aveReward = accum_total_reward / (double) numRounds;
                double sumOfErr = 0.0;

                for (r = 0; r < numRounds; ++r) {
                    sumOfErr += Math.pow(rewardArray[r] - aveReward, 2.0);
                }

                double sd = Math.sqrt(sumOfErr / (double) numRounds);
                if (record != null) {
                    LOG.info("Records object successful!");
                }
                String domainName = this.domain._sDomainName;
                String instanceRecord = this.requestedInstance.substring(this.requestedInstance.lastIndexOf("_") + 1);
                if (!record.fileAppend(this.clientName + "_" + timeLimit / 1000L + "_" + this.requestedInstance + "_Score", instanceRecord + " " + aveReward + " " + sd)) {
                    LOG.error("Recording data failed!");
                }
                return;
            }
            LOG.info("Instance name '" + this.requestedInstance + "' not found");
        } catch (Exception e) {
            LOG.info("TERMINATING TRIAL." + e);
            return;
        }
    }





    private void initializeClient(State state, RDDL.INSTANCE instance, RDDL.NONFLUENTS nonFluents, RDDL.DOMAIN domain, Policy policy, Class c, DOMParser p, String instanceName){
        this.clientInstance = instance;
        this.clientNonFluents = nonFluents;
        this.clientDomain = domain;
        this.clientState = state;
//        this.client_msg = msg;
        this.clientPolicy = policy;
        this.client_c = c;
        this.clientInstanceName = instanceName;

    }

    // region Helper functions for Server

    private void createAndProcessXMLRoundEnd(String requested_instance, int round, double reward, int turnsUsed, long timeLeft, double immediateReward){
        this.clientRequestedInstance = requested_instance;
        this.clientRound = round;
        this.clientReward = reward;
        this.clientTurnsUsed = turnsUsed;
        this.clientTimeLeft = timeLeft;
        this.clientImmediateReward = immediateReward;
    }

    private double processXMLRoundEnd() {
        return clientReward;
    }
    private double processXMLSessionEnd(){
        return clientTotalReward;
    }


    private void createAndProcessXMLSessionEnd(String requested_instance, double reward, int roundsUsed, long timeLeft){
        this.clientRequestedInstance = requested_instance;
        this.clientTotalReward = reward;
        this.clientRoundsUsed = roundsUsed;
        this.clientTimeLeft = timeLeft;
    }

    private HashMap<RDDL.PVAR_NAME, HashMap<ArrayList<RDDL.LCONST>, Object>> copyObserv(HashMap<RDDL.PVAR_NAME, HashMap<ArrayList<RDDL.LCONST>, Object>> observ) {
        HashMap<RDDL.PVAR_NAME, HashMap<ArrayList<RDDL.LCONST>, Object>> r = new HashMap();
        Iterator var3 = observ.keySet().iterator();

        while(var3.hasNext()) {
            RDDL.PVAR_NAME pn = (RDDL.PVAR_NAME)var3.next();
            HashMap<ArrayList<RDDL.LCONST>, Object> v = new HashMap();
            Iterator var6 = ((HashMap)observ.get(pn)).keySet().iterator();

            while(var6.hasNext()) {
                ArrayList<RDDL.LCONST> aa = (ArrayList)var6.next();
                ArrayList<RDDL.LCONST> raa = new ArrayList();
                Iterator var9 = aa.iterator();

                while(var9.hasNext()) {
                    RDDL.LCONST lc = (RDDL.LCONST)var9.next();
                    raa.add(lc);
                }

                v.put(raa, ((HashMap)observ.get(pn)).get(aa));
            }

            r.put(pn, v);
        }

        return r;
    }

    private ArrayList<RDDL.PVAR_INST_DEF> processXMLAction(DOMParser p, String parseString) {
        try {
            p.parse(new InputSource(new StringReader(parseString)));
            Element root = p.getDocument().getDocumentElement();
            if(!root.getNodeName().equals(Server.ACTIONS)){
                LOG.error("ERROR: NO ACTIONS NODE");
                LOG.error("Received action msg:");
                // printXML(root); not needed
                LOG.error("Expected action tag, got " + root.getNodeName());
            } else {
                NodeList nl = root.getElementsByTagName(Server.ACTION);
                if(nl ==null){
                    return new ArrayList<>();
                } else {
                    ArrayList<RDDL.PVAR_INST_DEF> ds = new ArrayList<>();

                    for(int i=0; i < nl.getLength(); ++i){
                        Element el = (Element)nl.item(i);
                        String name = (String) getTextValue(el,"action-name").get(0);
                        ArrayList<String> args = getTextValue(el,"action-arg");
                        ArrayList<RDDL.LCONST> params = new ArrayList<>();
                        Iterator var11 = args.iterator();

                        while(var11.hasNext()){
                            String arg = (String)var11.next();
                            if(arg.startsWith("@")){
                                params.add(new RDDL.ENUM_VAL(arg));
                            } else {
                                params.add(new RDDL.OBJECT_VAL(arg));
                            }
                        }

                        String pvalue = (String)Server.getTextValue(el,Server.ACTION_VALUE).get(0);
                        Object value = Server.getValue(name, pvalue, state);
                        RDDL.PVAR_INST_DEF d = new RDDL.PVAR_INST_DEF(name, value, params);
                        ds.add(d);
                    }
                    return ds;
                }
            }
        } catch (IOException | SAXException e) {
           LOG.error("Error in processing XML Action"+e.getMessage());
        }
        return null;
    }

    private void resetState() {
        this.state.init(this.domain._hmObjects, this.nonFluents != null ? this.nonFluents._hmObjects : null, this.instance._hmObjects, this.domain._hmTypes, this.domain._hmPVariables, this.domain._hmCPF, this.instance._alInitState, this.nonFluents == null ? new ArrayList() : this.nonFluents._alNonFluents, this.instance._alNonFluents, this.domain._alStateConstraints, this.domain._alActionPreconditions, this.domain._alStateInvariants, this.domain._exprReward, this.instance._nNonDefActions);
        if (this.domain._bPartiallyObserved && this.state._alObservNames.size() == 0 || !this.domain._bPartiallyObserved && this.state._alObservNames.size() > 0) {
            boolean observations_present = this.state._alObservNames.size() > 0;
            LOG.error("WARNING: Domain '" + this.domain._sDomainName + "' partially observed (PO) flag and presence of observations mismatched.\nSetting PO flag = " + observations_present + ".");
            this.domain._bPartiallyObserved = observations_present;
        }
    }

    private boolean processXMLRoundRequest(boolean executePolicy) {
        this.executePolicy = executePolicy;
        return this.executePolicy;
    }

    private void initializeState(RDDL rddl, String requestedInstance) {
        this.state = new State();
        this.instance = (RDDL.INSTANCE) rddl._tmInstanceNodes.get(requestedInstance);
        this.nonFluents = null;
        if (this.instance._sNonFluents != null) {
            this.nonFluents = (RDDL.NONFLUENTS)rddl._tmNonFluentNodes.get(this.instance._sNonFluents);
        }
        this.domain = (RDDL.DOMAIN)rddl._tmDomainNodes.get(this.instance._sDomain);
        if (this.nonFluents != null && !this.instance._sDomain.equals(this.nonFluents._sDomain)) {
            try {
                throw new Exception("Domain name of instance and fluents do not match: " + this.instance._sDomain + " vs. " + this.nonFluents._sDomain);
            } catch (Exception var6) {
                var6.printStackTrace();
            }
        }

    }
    private void createAndProcessXMLRoundInit(int round, int numRounds, double timeLimit){
        this.clientRound = round;
        this.clientNumRounds = numRounds;
        this.clientTimeLimit = timeLimit;
    }

    private void createXMLSessionInit(int numRounds, double timeLimit) {
        this.clientNumRounds = numRounds;
        this.clientTimeLimit = timeLimit;
        RDDL.INSTANCE instance = (RDDL.INSTANCE) this.serverRddl._tmInstanceNodes.get(this.requestedInstance);
        RDDL.DOMAIN domain = (RDDL.DOMAIN) this.serverRddl._tmDomainNodes.get(instance._sDomain);
        String domainFile = SERVER_FILES_DIR + "/" + domain._sFileName + "." + this.inputLanguage;
        String instanceFile = SERVER_FILES_DIR + "/" + instance._sFileName + "." + this.inputLanguage;
        StringBuilder task = null;
        try {
            task = new StringBuilder(new String(Files.readAllBytes(Paths.get(domainFile))));
            task.append(System.getProperty("line.separator"));
            task.append(System.getProperty("line.separator"));
            task.append(new String(Files.readAllBytes(Paths.get(instanceFile))));
            task.append(System.getProperty("line.separator"));
        } catch (IOException e) {
            LOG.error("Error in creating XML Session Init"+e.getMessage());
        }
        byte[] encodedBytes = Base64.getEncoder().encode(task.toString().getBytes());
        serverTask = new String(encodedBytes);
    }

    private static String createXMLTurn(State state, int turn,RDDL.DOMAIN domain, HashMap<RDDL.PVAR_NAME, HashMap<ArrayList<RDDL.LCONST>, Object>> observStore, double timeLeft, double immediateReward) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.newDocument();
            Element rootEle = dom.createElement("turn");
            dom.appendChild(rootEle);
            Element turnNum = dom.createElement("turn-num");
            Text textTurnNum = dom.createTextNode("" + turn);
            turnNum.appendChild(textTurnNum);
            rootEle.appendChild(turnNum);
            Element timeElem = dom.createElement("time-left");
            Text textTimeElem = dom.createTextNode("" + timeLeft);
            timeElem.appendChild(textTimeElem);
            rootEle.appendChild(timeElem);
            Element immediateRewardElem = dom.createElement("immediate-reward");
            Text textImmediateRewardElem = dom.createTextNode("" + immediateReward);
            immediateRewardElem.appendChild(textImmediateRewardElem);
            rootEle.appendChild(immediateRewardElem);
            if (domain._bPartiallyObserved && observStore == null) {
                Element ofEle = dom.createElement("no-observed-fluents");
                rootEle.appendChild(ofEle);
            } else {
                Iterator var18 = (domain._bPartiallyObserved ? observStore.keySet() : state._state.keySet()).iterator();
                while(var18.hasNext()) {
                    RDDL.PVAR_NAME pn = (RDDL.PVAR_NAME)var18.next();
                    if (domain._bPartiallyObserved && observStore != null) {
                        state._observ.put(pn, (HashMap)observStore.get(pn));
                    }
                    ArrayList<ArrayList<RDDL.LCONST>> gfluents = state.generateAtoms(pn);
                    Iterator var21 = gfluents.iterator();

                    while(var21.hasNext()) {
                        ArrayList<RDDL.LCONST> gfluent = (ArrayList)var21.next();
                        Element ofEle = dom.createElement("observed-fluent");
                        rootEle.appendChild(ofEle);
                        Element pName = dom.createElement("fluent-name");
                        Text pTextName = dom.createTextNode(pn.toString());
                        pName.appendChild(pTextName);
                        ofEle.appendChild(pName);
                        Iterator var26 = gfluent.iterator();

                        while(var26.hasNext()) {
                            RDDL.LCONST lc = (RDDL.LCONST)var26.next();
                            Element pArg = dom.createElement("fluent-arg");
                            Text pTextArg = dom.createTextNode(lc.toSuppString());
                            pArg.appendChild(pTextArg);
                            ofEle.appendChild(pArg);
                        }
                        Element pValue = dom.createElement("fluent-value");
                        Object value = state.getPVariableAssign(pn, gfluent);
                        if (value == null) {
                            System.out.println("STATE:\n" + state);
                            throw new Exception("ERROR: Could not retrieve value for " + pn + gfluent.toString());
                        }
                        Text pTextValue = value instanceof RDDL.LCONST ? dom.createTextNode(((RDDL.LCONST)value).toSuppString()) : dom.createTextNode(value.toString());
                        pValue.appendChild(pTextValue);
                        ofEle.appendChild(pValue);
                    }
                }
            }
            return serialize(dom);
            } catch (Exception e) {
            LOG.error("FATAL SERVER EXCEPTION: "+e.getMessage());
            throw e;
        }
    }

    private void processXMLSessionRequest(String problemName,String clientName, String inputLanguage) {
        this.requestedInstance = problemName;
        this.clientName = clientName;
        this.inputLanguage = inputLanguage;
    }

    private ArrayList<String> getTextValue(Element e, String tagName) {
        ArrayList<String> returnVal = new ArrayList<String>();
//		NodeList nll = ele.getElementsByTagName("*");

        NodeList nl = e.getElementsByTagName(tagName);
        if(nl.getLength() > 0) {
            for ( int i= 0; i < nl.getLength(); i++ ) {
                Element el = (Element)nl.item(i);
                returnVal.add(el.getFirstChild().getNodeValue());
            }
        }
        return returnVal;
    }
    // endregion


// region Client Helper Methods
    private ArrayList<RDDL.PVAR_INST_DEF> processXMLTurn(Element e, State state) throws RDDLXMLException {
        if ( e.getNodeName().equals(Server.TURN) ) {
            clientTimeAllowed = Double.valueOf(Server.getTextValue(e, TIME_LEFT).get(0));
            if (e.getElementsByTagName(Server.NULL_OBSERVATIONS).getLength() > 0) {
                return null;
            }
            NodeList nl = e.getElementsByTagName(Server.OBSERVED_FLUENT);
            if(nl != null && nl.getLength() > 0){
                ArrayList<RDDL.PVAR_INST_DEF> ds = new ArrayList<RDDL.PVAR_INST_DEF>();
                for(int i = 0 ; i < nl.getLength();i++) {
                    Element el = (Element)nl.item(i);
                    String name = Server.getTextValue(el, Server.FLUENT_NAME).get(0);
                    ArrayList<String> args = Server.getTextValue(el, Server.FLUENT_ARG);
                    ArrayList<RDDL.LCONST> lcArgs = new ArrayList<RDDL.LCONST>();
                    for( String arg : args ) {
                        if (arg.startsWith("@"))
                            lcArgs.add(new RDDL.ENUM_VAL(arg));
                        else
                            lcArgs.add(new RDDL.OBJECT_VAL(arg));
                    }
                    String value = Server.getTextValue(el, Server.FLUENT_VALUE).get(0);
                    Object r = Server.getValue(name, value, state);
                    PVAR_INST_DEF d = new RDDL.PVAR_INST_DEF(name, r, lcArgs);
                    ds.add(d);
                }
                return ds;
            } else {
                return new ArrayList<PVAR_INST_DEF>();
            }
        }
        throw new RDDLXMLException("Error in processing XML Turn Request");
    }

    private Element parseMessage(DOMParser p, String parseString) throws RDDLXMLException {
        try {
            p.parse(new InputSource(new StringReader(parseString)));
            return p.getDocument().getDocumentElement();
        } catch (Exception e) {
            LOG.error("Error in processing XML Session Request"+e.getMessage());
            throw new RDDLXMLException(""+e.getMessage());
        }
    }


    public void createXMLRoundRequest(boolean ifExecutePolicy){
        this.clientExecutePolicy = ifExecutePolicy;
    }
    private void processXMLSessionInit(DOMParser p, String parseString, String instanceName) throws RDDLXMLException {
        try {
            p.parse(new InputSource(new StringReader(parseString)));
        } catch (IOException | SAXException e) {
            LOG.error("Error in processXMLSessionInit"+e.getMessage());
        }
        Element e = p.getDocument().getDocumentElement();
        byte[] rddlDesc = null;
        if(!e.getNodeName().equals("session-init")) {
            throw new RDDLXMLException("Expected session-init element, got " + e.getNodeName());
        } else {
            ArrayList<String> r = getTextValue(e,"session-id"); // session-id not implemented
            if(r !=null){
                // assign client id here but not needed
            }
            r= getTextValue(e,"num-rounds");
            if(r !=null){
                // assign num rounds here but not needed
                clientNumOfRounds = Integer.parseInt(r.get(0));
            }
            r = getTextValue(e, "time-allowed");
            if (r != null) {
                clientTimeAllowed = Double.valueOf((String)r.get(0));
            }

            r = getTextValue(e, "task");
            if (r != null) {
                rddlDesc = org.apache.xerces.impl.dv.util.Base64.decode((String)r.get(0));
            }

            Records rec = new Records();
            rec.fileAppend(instanceName + ".rddl", new String(rddlDesc), true);
            String userHome = System.getProperty("user.home");
            String absPath = userHome + System.getProperties().getProperty("file.separator") + "rddl" + System.getProperties().getProperty("file.separator") + instanceName + ".rddl";

            try{
                clientRddl = new RDDL(absPath);
            } catch (Exception ex) {
                LOG.error("Error in processXMLSessionInit"+ex.getMessage());
            }
        }
    }
    private void processXMLSessionInit(String instanceName){
        clientNumOfRounds = 30;
        clientTimeAllowed = 10.0;
        byte[] rddlDesc = null;
        rddlDesc = org.apache.xerces.impl.dv.util.Base64.decode(serverTask);
        Records rec = new Records();
        rec.fileAppend(instanceName + ".rddl", new String(rddlDesc), true);
        String userHome = System.getProperty("user.home");
        String absPath = userHome + System.getProperties().getProperty("file.separator") + instanceName + ".rddl";

        try{
            clientRddl = new RDDL(absPath);
        } catch (Exception ex) {
            LOG.error("Error in processXMLSessionInit"+ex.getMessage());
        }

    }
    private String createXMLSessionRequest(String instanceName) {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            Element root = doc.createElement("session-request");
            doc.appendChild(root);
            addOneText(doc, root, "problem-name", instanceName);
            addOneText(doc, root, "input-language", "rddl");
            return serialize(doc);
        } catch (Exception e) {
            LOG.error("Error in creating XML Session Request"+e.getMessage());
            return null;
        }
    }
    // endregion

}
