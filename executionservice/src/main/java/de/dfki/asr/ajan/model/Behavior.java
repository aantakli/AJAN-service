/*
 * Copyright (C) 2020 see AJAN-service/AUTHORS.txt (German Research Center for Artificial Intelligence, DFKI).
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

package de.dfki.asr.ajan.model;

import de.dfki.asr.ajan.behaviour.nodes.BTRoot;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil.DebugMode;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil.ModelMode;
import de.dfki.asr.ajan.behaviour.nodes.common.Debug;
import java.util.List;
import lombok.Data;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

@Data
public class Behavior {
	private final String name;
	private final Resource resource;
	private final BTRoot behaviorTree;
        private final boolean clearEKB;
	private final List<Resource> events;

	public Model getStatus(final String btURL, final ModelMode mode) {
		Model model = new LinkedHashModel();
		return behaviorTree.getModel(model, behaviorTree.getInstance(), mode);
	}

        public void setDebug(final String btURL, final DebugMode mode) {
            Debug debug = behaviorTree.getObject().getDebug();
            if (debug.isDebugging()) {
                switch (mode) {
                    case RESUME:
                        debug.setDebugging(false);
                        debug.setMode(DebugMode.NONE);
                        break;
                    case STEP:
                        debug.setMode(mode);
                        break;
                    default:
                        break;
                }
            } else {
                if (mode.equals(DebugMode.PAUSE)) {
                    debug.setDebugging(true);
                    debug.setMode(DebugMode.NONE);
                }
            }
            behaviorTree.run();
	}
}
