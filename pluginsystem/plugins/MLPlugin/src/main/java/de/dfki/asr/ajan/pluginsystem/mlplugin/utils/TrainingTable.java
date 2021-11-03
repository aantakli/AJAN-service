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
package de.dfki.asr.ajan.pluginsystem.mlplugin.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;

/**
 *
 * @author Andre Antakli (German Research Center for Artificial Intelligence,
 * DFKI)
 */
public class TrainingTable {
	@Getter
	private final double[][] data;
	
	@Getter
	private final int[] objectives;
	
	private final String objective;
	private final Set<String> bindingNames;
	private final Map<String,Set<String>> options = new HashMap();

	public TrainingTable (final List<BindingSet> input, final String objective) {
		bindingNames = input.get(0).getBindingNames();
		objectives = new int[input.size()];
		data = new double[input.size()][bindingNames.size() - 1];
		this.objective = objective;
		readInput(input);
	}

	private void readInput(List<BindingSet> input) {
		setOptions(input);
	}

	public Map<String,Set<String>> getOptions() {
		return options;
	}

	private void setOptions(final List<BindingSet> input) {
		Iterator<String> nameItr = bindingNames.iterator();
		
		while(nameItr.hasNext()) {
			options.put(nameItr.next(), new LinkedHashSet());
		}
		
		int i = 0;
		Iterator<BindingSet> sets = input.iterator();
		while (sets.hasNext()) {
			BindingSet set = sets.next();
			Iterator<String> iter = options.keySet().iterator();
			int j = 0;
			while(iter.hasNext()) {
				String attr = iter.next();
				Binding binding = set.getBinding(attr);
				Set<String> values = options.get(attr);
				String value = binding.getValue().stringValue();
				values.add(value);
				List<String> stringsList = new ArrayList<>(values);
				
				if (binding.getName().equals(objective)) {
					for (int k = 0; k < values.size(); k++) {
						if(stringsList.get(k).equals(value)) {
							objectives[i] = k;
						}
					}
				} else {
					for (int k = 0; k < values.size(); k++) {
						if(stringsList.get(k).equals(value)) {
							data[i][j] = k;
						}
					}
					j++;
				}
			}
			i++;
		}
	}
}
