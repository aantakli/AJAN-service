/*
 * Copyright (C) 2020 Andre Antakli (German Research Center for Artificial Intelligence, DFKI).
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

package de.dfki.asr.ajan.pluginsystem.standardbtnodes.extensions;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBLeafTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.common.TreeNode;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.behaviour.nodes.common.EvaluationResult;
import de.dfki.asr.ajan.behaviour.nodes.common.LeafStatus;
import de.dfki.asr.ajan.pluginsystem.standardbtnodes.vocabularies.StandardBTVocabulary;
import lombok.Getter;
import lombok.Setter;
import org.cyberborean.rdfbeans.annotations.*;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;

@Extension
@RDFBean("bt:Fail")
public class Fail extends AbstractTDBLeafTask implements NodeExtension, TreeNode {

    @Getter
    @Setter
    @RDFSubject
    private String url;

    private static final Logger LOG = LoggerFactory.getLogger(Fail.class);
    private Resource instance;

    @RDF("rdfs:label")
    @Getter
    @Setter
    private String label;

    @Override
    public void end() {
        LOG.info("Status (" + getStatus() + ")");
    }

    @Override
    public EvaluationResult.Result simulateNodeLogic(final EvaluationResult result, final Resource root) {
        return EvaluationResult.Result.FAIL;
    }

    @Override
    public Resource getType() {
        return StandardBTVocabulary.FAIL;
    }

    @Override
    public Resource getInstance(final Resource btInstance) {
        if (instance == null) {
            instance = BTUtil.getInstanceResource(url, btInstance);
        }
        return instance;
    }

    @Override
    public Resource getDefinition(final Resource btDefinition) {
        if (url == null) {
            return btDefinition;
        }
        return vf.createIRI(url);
    }

    @Override
    public Model getModel(final Model model, final BTRoot root, final BTUtil.ModelMode mode) {
        BTUtil.setGeneralNodeModel(model, root, mode, this);
        return model;
    }

    @Override
    public String toString() {
        return "Fail (" + label + ")";
    }

    @Override
    public LeafStatus executeLeaf() {
        String report = toString() + " FAILED";
		LOG.info(report);
		return new LeafStatus(Status.FAILED, report);
    }
}
