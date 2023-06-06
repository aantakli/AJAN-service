/*
 * Copyright (c) 2023. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package de.dfki.asr.ajan.pluginsystem.mdpplugin.extensions;

import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.NodeStatus;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.MDPUtil;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.POMDP.AJANPlanner;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.POMDP.ValuedAction;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.POMDP.implementation.AJAN_Agent_State;
import de.dfki.asr.ajan.pluginsystem.mdpplugin.vocabularies.POMDPVocabulary;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Resource;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/** Represents the problem in the POMDP Framework
 * <States,Actions,TransitionFunction,Rewards,ObservationProbability> </>
 * */
@Extension
@RDFBean("bt-mdp:CreatePOMDPProblem")
public class Problem extends AbstractTDBLeafTask implements NodeExtension {
    private static final Logger LOG = LoggerFactory.getLogger(Problem.class);
    private String description;

    private ArrayList<String> statesList;
    private ArrayList<String> actionsList;


    @RDF("bt-mdp:states")
    @Getter @Setter
    private List<AJAN_Agent_State> states;

    @RDF("bt-mdp:actions")
    @Getter @Setter
    private List<ValuedAction> actions;

    @RDF("bt-mdp:transitionFunction")
    @Getter @Setter
    private BehaviorSelectQuery transitionFunction;


    @RDF("bt-mdp:observationFunction")
    @Getter @Setter
    private BehaviorSelectQuery observationFunction;
    // Give a template that takes in obs value and gives back double value corresponding to the observation probability
    //private BehaviorSelectQuery observationFunction;

//    @RDF("bt-mdp:rewardFunction")
//    @Getter @Setter
//    private BehaviorSelectQuery rewardFunction;

    @RDF("bt-mdp:solver")
    @Getter @Setter
    private String solverName;

    @RDFSubject
    @Getter @Setter
    private String url;

    @RDF("rdfs:label")
    @Getter @Setter
    private String label;

    @Override
    public NodeStatus executeLeaf() {
        // TODO:Get the states,actions and create state and action nodes
        String report;
        Status stat;

        try {
            double observationProbability = MDPUtil.getObservationProbability(observationFunction,this.getObject());
            LOG.info("Got Observation Probability:"+observationProbability);
            AJANPlanner planner = new AJANPlanner();
            planner.InitializeModel();
            planner.InitializePlannerInDespot();
            planner.RunPlanner(planner.ajanAgent);
            report = this + "SUCCEEDED";
            stat = Status.SUCCEEDED;
        } catch (URISyntaxException e) {
            report = this + "FAILED";
            stat = Status.FAILED;
//            throw new RuntimeException(e);
        }
        return new NodeStatus(stat,this.getObject().getLogger(),this.getClass(),report);
    }

    @Override
    public void end() {
        this.getObject().getLogger().info(this.getClass(), "Status (" + getStatus() + ")");
    }

    @Override
    public EvaluationResult.Result simulateNodeLogic(EvaluationResult result, Resource root) {
        return EvaluationResult.Result.SUCCESS;
    }

    @Override
    public Resource getType() {
        return POMDPVocabulary.POMDP;
    }
}
