package de.dfki.asr.ajan.pluginsystem.mdpplugin.utils;

import org.apache.xerces.impl.dv.util.Base64;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import rddl.EvalException;
import rddl.RDDL;
import rddl.State;
import rddl.competition.*;
import rddl.policy.Policy;
import rddl.viz.StateViz;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static rddl.competition.SOGBOFA.*;

public class SOGBOFAClient implements Runnable{
    private static final Logger LOG = LoggerFactory.getLogger(SOGBOFAClient.class);

    public static final boolean SHOW_XML = false;
    public static final boolean SHOW_MSG = false;
    public static final boolean SHOW_MEMORY_USAGE = true;
    public static final Runtime RUNTIME = Runtime.getRuntime();
    public static final int DEFAULT_RANDOM_SEED = 0;
    private static DecimalFormat _df = new DecimalFormat("0.##");

    public SOGBOFAClient() {
        numRounds = 0;
        timeAllowed = 0;
        curRound = 0;
        reward = 0;
        id = 0;
    }

    enum XMLType {
        ROUND,TURN,ROUND_END,END_TEST,NONAME
    }

    private static RDDL rddl = null;

    static int numRounds;
    static double timeAllowed;
    static int curRound;
    double reward;
    int id;

    private String ClientName;
    private String filePath;
    private String portNumber;


    public SOGBOFAClient(String filesPath,String portNumber, String ClientName){
        numRounds = 0;
        timeAllowed = 0;
        curRound = 0;
        reward = 0;
        id = 0;
        this.ClientName = ClientName;
        this.portNumber = portNumber;
        this.filePath = filesPath;
    }
    @Override
    public void run() {
        String host = Server.HOST_NAME;
        int port = Server.PORT_NUMBER;
        String instanceName = null;
        int randomSeed = DEFAULT_RANDOM_SEED;

        State state;
        RDDL.INSTANCE instance;
        RDDL.NONFLUENTS nonFluents = null;
        RDDL.DOMAIN domain;
        StateViz stateViz;

        StringBuffer instr = new StringBuffer();
        String TimeStamp;

        double timeLeft = 0;
        Socket connection = null;
        try {

            if(ClientName.equals("Original")) {
                ClientName = "SOGBOFA_Original";
            }else {
                ClientName = "L_C_SOGBOFA";
            }

            /** Obtain an address object of the server */
            InetAddress address = InetAddress.getByName(host);
            /** Establish a socket connetion */
            connection = new Socket(address, port);
            LOG.info("RDDL client initialized");

            /** Instantiate a BufferedOutputStream object */
            BufferedOutputStream bos = new BufferedOutputStream(connection.
                    getOutputStream());
            /** Instantiate an OutputStreamWriter object with the optional character
             * encoding.
             */
            OutputStreamWriter osw = new OutputStreamWriter(bos, "US-ASCII");
            /** Write across the socket connection and flush the buffer */
            String msg = createXMLSessionRequest(instanceName, ClientName);
            Server.sendOneMessage(osw, msg);
            BufferedInputStream isr = new BufferedInputStream(connection.getInputStream());
            /**Instantiate an InputStreamReader with the optional
             * character encoding.
             */
            //InputStreamReader isr = new InputStreamReader(bis, "US-ASCII");
            DOMParser p = new DOMParser();

            /**Read the socket's InputStream and append to a StringBuffer */
            InputSource isrc = Server.readOneMessage(isr);
            SOGBOFAClient client = processXMLSessionInit(p, isrc, instanceName);

            LOG.info(client.id + ":" + client.numRounds);
            LOG.info("Total time allowed: " + client.timeAllowed);

//            VisCounter visCounter = new VisCounter();

            Class c = Class.forName("rddl.policy"+ClientName);
            rddl = new RDDL(filePath);
            port = Integer.parseInt(portNumber);
            if(!rddl._tmInstanceNodes.containsKey(instanceName)){
                LOG.error("Instance name '" + instanceName + "' not found in " + filePath + "\nPossible choices: " + rddl._tmInstanceNodes.keySet());
                return;
            }

            state = new State();
            // Note: following constructor approach suggested by Alan Olsen
            Policy policy = (Policy)c.getConstructor(
                    new Class[]{String.class}).newInstance(new Object[]{instanceName});
//            policy.setRDDL(rddl);
            policy.setRandSeed(randomSeed);

            instance = rddl._tmInstanceNodes.get(instanceName);
            if (instance._sNonFluents != null) {
                nonFluents = rddl._tmNonFluentNodes.get(instance._sNonFluents);
            }
            domain = rddl._tmDomainNodes.get(instance._sDomain);
            if (nonFluents != null && !instance._sDomain.equals(nonFluents._sDomain)) {
                LOG.error("Domain name of instance and fluents do not match: " +
                        instance._sDomain + " vs. " + nonFluents._sDomain);
            }

            state.init(domain._hmObjects, nonFluents != null ? nonFluents._hmObjects : null, instance._hmObjects,
                    domain._hmTypes, domain._hmPVariables, domain._hmCPF,
                    instance._alInitState, nonFluents == null ? new ArrayList<RDDL.PVAR_INST_DEF>() : nonFluents._alNonFluents, instance._alNonFluents,
                    domain._alStateConstraints, domain._alActionPreconditions, domain._alStateInvariants,
                    domain._exprReward, instance._nNonDefActions);

            // If necessary, correct the partially observed flag since this flag determines what content will be seen by the Client
            if ((domain._bPartiallyObserved && state._alObservNames.size() == 0)
                    || (!domain._bPartiallyObserved && state._alObservNames.size() > 0)) {
                boolean observations_present = (state._alObservNames.size() > 0);
                LOG.warn("WARNING: Domain '" + domain._sDomainName
                        + "' partially observed (PO) flag and presence of observations mismatched.\nSetting PO flag = " + observations_present + ".");
                domain._bPartiallyObserved = observations_present;
            }

            policy.horizon = instance._nHorizon;
            policy.setInstance(instanceName);
//            policy.setVisCounter(visCounter);
            Policy.ifFirstStep = true;
            Policy.timeUsedForCal = 0;
            Policy.updatesIntotal = 0;
            Policy.numberNodesUpdates = 0;
            Policy.randomAction = new ArrayList<>();
            Policy.ifConstructConstraints = true;
            Run(state, instance, nonFluents, domain, isrc,client, osw, msg, policy, c, isr, p, instanceName, client.ClientName);

        } catch (ClassNotFoundException | IOException | InvocationTargetException | InstantiationException |
                 IllegalAccessException | NoSuchMethodException | RDDLXMLException | EvalException e) {
            LOG.error(e.getMessage());
        }

    }
    public static void Run(State state, RDDL.INSTANCE instance, RDDL.NONFLUENTS nonFluents, RDDL.DOMAIN domain,
                           InputSource isrc, SOGBOFAClient client, OutputStreamWriter osw, String msg, Policy policy, Class c, BufferedInputStream isr,
                           DOMParser p, String instanceName, String clientName) throws IOException, RDDLXMLException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, EvalException {
        int r = 0;
        //number of rounds as sum of original rounds and testing rounds
        long totalRounds = SOGBOFAClient.numRounds;
        long totalStepLeft = totalRounds * instance._nHorizon;
        long totalStepUsed = 0;
        double totalTimeUsage = 0;

        double totalTimeForInit = 0;

        double TotalTimeAllowed = client.timeAllowed;//120 * totalStepLeft * 1000;
        double emergentTime = 3.0 * totalStepLeft / (80 * 100);


        for( ; r < totalRounds; r++ ) {
            double t0 = System.currentTimeMillis();
            if (SHOW_MEMORY_USAGE)
                System.out.print("[ Memory usage: " +
                        _df.format((RUNTIME.totalMemory() - RUNTIME.freeMemory())/1e6d) + "Mb / " +
                        _df.format(RUNTIME.totalMemory()/1e6d) + "Mb" +
                        " = " + _df.format(((double) (RUNTIME.totalMemory() - RUNTIME.freeMemory()) /
                        (double) RUNTIME.totalMemory())) + " ]\n");

            state.init(domain._hmObjects, nonFluents != null ? nonFluents._hmObjects : null, instance._hmObjects,
                    domain._hmTypes, domain._hmPVariables, domain._hmCPF,
                    instance._alInitState, nonFluents == null ? new ArrayList<RDDL.PVAR_INST_DEF>() : nonFluents._alNonFluents, instance._alNonFluents,
                    domain._alStateConstraints, domain._alActionPreconditions, domain._alStateInvariants,
                    domain._exprReward, instance._nNonDefActions);

            msg = createXMLRoundRequest(true);
            totalTimeUsage += System.currentTimeMillis() - t0;
            totalTimeForInit += System.currentTimeMillis() - t0;
            double timePerInit = totalTimeForInit / (r + 1);

            Server.sendOneMessage(osw, msg);
            isrc = Server.readOneMessage(isr);

            t0 = System.currentTimeMillis();
            processXMLRoundInit(p, isrc, r+1);

            int h =0;
            //System.out.println(instance._nHorizon);
            boolean round_ended_early = false;
            for(; h < instance._nHorizon; h++ ) {
                //first estimate the avg updating time per node
                if (SHOW_MSG) LOG.info("Reading turn message");
                totalTimeUsage += System.currentTimeMillis() - t0;
                isrc = Server.readOneMessage(isr);

                t0 = System.currentTimeMillis();
                Element e = parseMessage(p, isrc);
                round_ended_early = e.getNodeName().equals(Server.ROUND_END);
                if (round_ended_early)
                    break;
                if (SHOW_MSG) LOG.info("Done reading turn message");
                //if (SHOW_XML)
                //	Server.printXMLNode(e); // DEBUG
                ArrayList<RDDL.PVAR_INST_DEF> obs = processXMLTurn(e,state);

                //time allowed is deducted by 3 seconds to avoid time issue
                LOG.info("\n******************************************");
                LOG.info("New Turn Started. Starting initialization...");
                LOG.info("******************************************");
                LOG.info("Time left: " + timeAllowed);

                boolean ifEmeergency = false;
                if(r != 0 || h != 0) {

                    //directly go to emergency return mode


                    LOG.info("Total time used: " + totalTimeUsage);
                    LOG.info("Time per Init: " + timePerInit);
                    LOG.info("Runs left: " + (totalRounds - r - 1));
                    LOG.info("time allowed: " + timeAllowed);
                    timeAllowed = timeAllowed - timePerInit * (totalRounds - r - 1);
                    if(timeAllowed < 1000 * 60 * emergentTime) {
                        LOG.info("Emergent!! With time = " + timeAllowed);
                        ifEmeergency = true;
                    }

                }

                LOG.info("We use " + timeAllowed + " to calculate");

                //calculate total time for this step
                double timeForStep = timeAllowed / totalStepLeft;
                if(r == 0 && h == 0) {
                    Policy.ifFirstStep = true;
                    timeForStep = timeAllowed * 0.02;
                }
                else {
                    Policy.ifFirstStep = false;
                }

                //calculate avg time usage
                //if the avg time usage is too long to complete the step
                //simply do random

                totalStepUsed ++;

                if (SHOW_MSG) LOG.info("Done parsing turn message");
                if ( obs == null ) {
                    if (SHOW_MSG) LOG.info("No state/observations received.");
                    if (SHOW_XML)
                        Server.printXMLNode(p.getDocument()); // DEBUG
                } else if (domain._bPartiallyObserved) {
                    state.clearPVariables(state._observ);
                    state.setPVariables(state._observ, obs);
                } else {
                    state.clearPVariables(state._state);
                    state.setPVariables(state._state, obs);
                }
                policy.setCurrentRound(h);
                policy.setRddlDomain(rddl);
                policy.setTimeAllowed(new Double(timeForStep).longValue());
                policy.timeAllowed = client.timeAllowed;
                policy.instanceName = instanceName;
                policy.algoName = clientName;

                LOG.info("Steps left: " + totalStepLeft);
                LOG.info("Time allowed for this step: " + timeForStep);

                ArrayList<RDDL.PVAR_INST_DEF> actions = null;
                if(ifEmeergency){
                    LOG.info("Time is not sufficient. Require: " + (totalTimeUsage / totalStepUsed)
                            + ", but only have " + timeAllowed);
                    try {
                        actions = policy.EmergencyReturn(state);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
                else {
                    actions = policy.getActions(obs == null ? null : state);
                }

                msg = createXMLAction(actions);
                if (SHOW_MSG)
                    LOG.info("Sending: " + msg);
                totalTimeUsage += System.currentTimeMillis() - t0;
                Server.sendOneMessage(osw, msg);
                totalStepLeft --;
            }
            if ( h < instance._nHorizon ) {
                break;
            }
            if (!round_ended_early) // otherwise isrc is the round-end message
                isrc = Server.readOneMessage(isr);
            Element round_end_msg = parseMessage(p, isrc);
            double reward = processXMLRoundEnd(round_end_msg);

            policy.roundEnd(reward);


            //System.out.println("Round reward: " + reward);
            if (getTimeLeft(round_end_msg) <= 0l)
                break;
        }
        Records visRec = new Records();
        double res = 0;
        if(policy._visCounter.randomTime == 0)
            res = 0.0;
        else
            res = policy._visCounter.randomInTotal / policy._visCounter.randomTime;
        double res2 = 0;
        double res3 = 0;
        double res5 = 0.0;
        if(policy._visCounter.updateTime == 0)
            res2 = 0.0;
        else
            res2 = policy._visCounter.updatesInTotal / policy._visCounter.updateTime;
        if(policy._visCounter.SeenTime == 0)
            res3 = 0.0;
        else
            res3 = policy._visCounter.SeenInTotal / policy._visCounter.SeenTime;
        double res4 = 0;
        if(policy._visCounter.depthTime == 0)
            res4 = 0.0;
        else
            res4 = policy._visCounter.depthInTotal / policy._visCounter.depthTime;

        res5 = policy._visCounter.sizeInTotal / policy._visCounter.depthTime;
        visRec.fileAppend(clientName + "_" + (int)Math.round(client.timeAllowed / 1000) + "_" + instanceName + "_" + "rrCounter", String.valueOf(res));
        visRec.fileAppend(clientName + "_" + (int)Math.round(client.timeAllowed / 1000) + "_" + instanceName + "_" + "updatesCounter", String.valueOf(res2));
        visRec.fileAppend(clientName + "_" + (int)Math.round(client.timeAllowed / 1000) + "_" + instanceName + "_" + "seenCounter", String.valueOf(res3));
        visRec.fileAppend(clientName + "_" + (int)Math.round(client.timeAllowed / 1000) + "_" + instanceName + "_" + "depthCounter", String.valueOf(res4));
        visRec.fileAppend(clientName + "_" + (int)Math.round(client.timeAllowed / 1000) + "_" + instanceName + "_" + "sizeCounter", String.valueOf(res5));
        isrc = Server.readOneMessage(isr);
        double total_reward = processXMLSessionEnd(p, isrc);
        policy.sessionEnd(total_reward);
    }
//
//
//
//


    static SOGBOFAClient processXMLSessionInit(DOMParser p, InputSource isrc, String instanceName) throws RDDLXMLException {
        try {
            p.parse(isrc);
        } catch (SAXException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            throw new RDDLXMLException("sax exception");
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            throw new RDDLXMLException("io exception");
        }
        SOGBOFAClient c = new SOGBOFAClient();
        Element e = p.getDocument().getDocumentElement();
        byte[] rddlDesc =  null;

        if ( !e.getNodeName().equals(Server.SESSION_INIT) ) {
            throw new RDDLXMLException("not session init");
        }
        ArrayList<String> r = Server.getTextValue(e, Server.SESSION_ID);
        if ( r != null ) {
            c.id = Integer.valueOf(r.get(0));
        }
        r = Server.getTextValue(e, Server.NUM_ROUNDS);
        if ( r != null ) {
            c.numRounds = Integer.valueOf(r.get(0));
        }
        r = Server.getTextValue(e, Server.TIME_ALLOWED);
        if ( r!= null ) {
            c.timeAllowed = Double.valueOf(r.get(0));
        }
        r = Server.getTextValue(e, Server.TASK_DESC);
        if ( r!= null ) {
            rddlDesc = Base64.decode(r.get(0));
        }

        Records rec = new Records();

        rec.fileAppend("tmp.rddl", new String(rddlDesc), true);

        //read the file
        MyPath myPath = new MyPath();
        String absPath = System.getProperty("user.home") + System.getProperties().getProperty("file.separator") + "tmp.rddl";
        try {
            rddl = new RDDL(absPath);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return c;
    }
}
