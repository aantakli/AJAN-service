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

package de.dfki.asr.ajan.pluginsystem.mappingplugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;
import ro.fortsoft.pf4j.RuntimeMode;

public class MappingPlugin extends Plugin {
	
	private static final Logger LOG = LoggerFactory.getLogger(MappingPlugin.class);

	public MappingPlugin(PluginWrapper wrapper) {
		super(wrapper);
	}

	@Override
    public void start() {
        System.out.println("MappingPlugin.start()");
        if (RuntimeMode.DEVELOPMENT.equals(wrapper.getRuntimeMode())) {
			LOG.debug("MappingPlugin");
        }
    }

    @Override
    public void stop() {
        System.out.println("MappingPlugin.stop()");
    }
}
