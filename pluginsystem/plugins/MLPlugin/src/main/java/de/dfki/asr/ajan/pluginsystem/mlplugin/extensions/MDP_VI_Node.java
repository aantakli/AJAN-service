/*
 * Copyright (C) 2021 Andre Antakli (German Research Center for Artificial Intelligence, DFKI).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package de.dfki.asr.ajan.pluginsystem.mlplugin.extensions;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.domain.singleagent.gridworld.state.GridAgent;
import burlap.domain.singleagent.gridworld.state.GridLocation;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import com.badlogic.gdx.ai.btree.Task.Status;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mlplugin.test.GridWorldDomain;
import de.dfki.asr.ajan.pluginsystem.mlplugin.utils.AJANStateModel;
import de.dfki.asr.ajan.pluginsystem.mlplugin.utils.AJANPropositionalFunction;
import de.dfki.asr.ajan.pluginsystem.mlplugin.utils.AgentInstance;
import de.dfki.asr.ajan.pluginsystem.mlplugin.utils.OOInstance;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;

/**
 *
 * @author Andre Antakli (German Research Center for Artificial Intelligence,
 * DFKI)
 */

@Extension
@RDFBean("ml:MDP_VI")
public class MDP_VI_Node extends AbstractTDBLeafTask implements NodeExtension {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	protected static final Logger LOG = LoggerFactory.getLogger(MDP_VI_Node.class);
	
	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/ml-ns#MDP_VI");
	}
	
	@Override
	public LeafStatus executeLeaf() {
		valueIterationExample();
		String report = toString() + " SUCCEEDED";
		return new LeafStatus(Status.SUCCEEDED, report);
	}

	private void valueIterationExample(){
		GridWorldDomain gw = new GridWorldDomain(11,11);
		gw.setMapToFourRooms();
		gw.setProbSucceedTransitionDynamics(1.0);
		OOSADomain domain = gw.generateDomain();
		
		AgentInstance agent = createAgent();
		OOInstance instance = createInstance();

		State initialState = new GridWorldState(new GridAgent(0, 0), new GridLocation(10, 10, "location0"));
		
		SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory();
		
		Planner planner = new ValueIteration(domain, 0.99, hashingFactory, 0.001, 100);
		Policy p = planner.planFromState(initialState);
		Action a = p.action(initialState);
	}

	private OOSADomain createDomain() {
		OOSADomain domain = new OOSADomain();
		int[][] cmap = {
			{0, 0, 0},
			{0, 0, 0},
			{0, 0, 0}
		};

		Map<String,double[][]> actions = new HashMap<>();
		
		actions.put("ACTION_WAIT", new double[][]{{0.1, 0.9, 0.},{0.1, 0., 0.9},{0.1, 0., 0.9}});
		actions.put("ACTION_CUT", new double[][]{{1., 0., 0.},{1., 0., 0.},{1., 0., 0.}});

		domain.addStateClass("AGENT_CLASS", AgentInstance.class)
				.addStateClass("INSTANCE_CLASS", OOInstance.class);
		
		FullStateModel smodel = new AJANStateModel(cmap, actions);
	
		FactoredModel model = new FactoredModel(smodel, new UniformCostRF(), new NullTermination());
		domain.setModel(model);
		
		domain.addActionTypes(
				new UniversalActionType("ACTION_WAIT"),
				new UniversalActionType("ACTION_CUT"));
		
		OODomain.Helper.addPfsToDomain(domain, this.generatePfs());
		return domain;
	}

	private AgentInstance createAgent() {
		HashMap<String,Object> variabels = new HashMap();
		variabels.put("VAR_X", 0);
		variabels.put("VAR_Y", 0);
		return new AgentInstance(variabels);
	}

	private OOInstance createInstance() {
		HashMap<String,Object> variabels = new HashMap();
		variabels.put("VAR_X", 0);
		variabels.put("VAR_Y", 1);
		return new OOInstance("location1", "CLASS_LOCATION", variabels);
	}

	public List<PropositionalFunction> generatePfs(){
		List<PropositionalFunction> pfs = Arrays.asList(
				new AJANPropositionalFunction("atLocation", new String[]{"CLASS_AGENT", "CLASS_LOCATION"})
		);

		return pfs;
	}

	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}

	@Override
	public EvaluationResult.Result simulateNodeLogic(EvaluationResult result, Resource root) {
		return EvaluationResult.Result.UNCLEAR;
	}
}
