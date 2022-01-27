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
package de.dfki.asr.ajan.pluginsystem.mlplugin.extensions.ongoing;

import com.badlogic.gdx.ai.btree.Task;
import de.dfki.asr.ajan.behaviour.AgentTaskInformation;
import de.dfki.asr.ajan.behaviour.nodes.common.AbstractTDBBranchTask;
import de.dfki.asr.ajan.behaviour.nodes.common.BTUtil;
import de.dfki.asr.ajan.behaviour.nodes.query.BehaviorSelectQuery;
import de.dfki.asr.ajan.pluginsystem.extensionpoints.NodeExtension;
import de.dfki.asr.ajan.pluginsystem.mlplugin.test.Iris;
import de.dfki.asr.ajan.pluginsystem.mlplugin.test.WeatherNominal;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.csv.CSVFormat;
import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.eclipse.rdf4j.model.Resource;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
import org.eclipse.rdf4j.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;
import smile.base.cart.DecisionNode;
import smile.base.cart.OrdinalNode;
import smile.base.cart.SplitRule;
import smile.classification.DecisionTree;
import smile.data.DataFrame;
import smile.data.Tuple;
import smile.data.formula.Formula;
import smile.data.type.StructField;
import smile.data.type.StructType;
import smile.io.CSV;
import smile.validation.ClassificationMetrics;
import smile.validation.LOOCV;

/**
 *
 * @author Andre Antakli (German Research Center for Artificial Intelligence,
 * DFKI)
 */

@Extension
@RDFBean("ml:DecisionTree")
public class DT_Node extends AbstractTDBBranchTask implements NodeExtension {
	@RDFSubject
	@Getter @Setter
	private String url;

	@RDF("rdfs:label")
	@Getter @Setter
	private String label;

	@RDF("bt:mapping")
    @Getter @Setter
    private URI mapping;

	@RDF("ml:trainingTbl")
	@Setter @Getter
	private BehaviorSelectQuery trainingTbl;

	@RDF("bt:hasChildren")
	public List<Task<AgentTaskInformation>> getChildren() {
		return Arrays.asList(children.items);
	}

	public void setChildren(final List<Task<AgentTaskInformation>> newChildren) {
		children.clear();
		newChildren.forEach((task) -> {
			children.add(task);
		});
	}

	protected static final Logger LOG = LoggerFactory.getLogger(DT_Node.class);

	@Override
	public Resource getType() {
		return vf.createIRI("http://www.ajan.de/behavior/ml-ns#DecisionTree");
	}

	@Override
	public void run () {
		try {
			if (runningChild == null) {
				testDT();
			} else {
				runningChild.run();
			}
		} catch (IOException | URISyntaxException ex) {
			LOG.info(ex.toString());
			fail();
		}
	}

	private void testDT() throws IOException, URISyntaxException {
		Repository repo = BTUtil.getInitializedRepository(getObject(), trainingTbl.getOriginBase());
		String result = trainingTbl.getResult(repo, TupleQueryResultFormat.CSV);
		
		CSV csvObj = new CSV(CSVFormat.DEFAULT.withHeader());
        StructType schema = csvObj.inferSchema(new StringReader(result), Math.min(1000, 100));
		
		DataFrame data = read(result, schema);
		data = data.factorize("outlook")
				.factorize("temperature")
				.factorize("windy")
				.factorize("class");
		DataFrame weather = WeatherNominal.data;
		Formula formula = Formula.lhs("class");
        DecisionTree model = DecisionTree.fit(Iris.formula, Iris.data);
        System.out.println(model);

		OrdinalNode root = (OrdinalNode)model.root();
		DecisionNode fChild = (DecisionNode)root.falseChild();
		double impurity = fChild.impurity(SplitRule.GINI);
		double deviance = fChild.deviance();
		int output = fChild.output();
		
        double[] importance = model.importance();
        for (int i = 0; i < importance.length; i++) {
            System.out.format("%-15s %.4f%n", model.schema().fieldName(i), importance[i]);
        }

        ClassificationMetrics metrics = LOOCV.classification(formula, data, DecisionTree::fit);

        System.out.println(metrics);
	}

	private DataFrame read(String result, StructType schema) throws IOException, URISyntaxException {
		if (schema == null) {
            // infer the schema from top 1000 rows.
            throw new IllegalStateException("The schema is not set or inferred.");
        }

        StructField[] fields = schema.fields();
        List<Function<String, Object>> parser = schema.parser();

        try (CSVParser csv = CSVParser.parse(new StringReader(result), CSVFormat.DEFAULT.withHeader())) {
            List<Tuple> rows = new ArrayList<>();
            for (CSVRecord record : csv) {
                Object[] row = new Object[fields.length];
                for (int i = 0; i < fields.length; i++) {
                    String s = record.get(i).trim();
                    if (!s.isEmpty()) {
                        row[i] = parser.get(i).apply(s);
                    }
                }
                rows.add(Tuple.of(row, schema));
                if (rows.size() >= 100) break;
            }

            schema = schema.boxed(rows);
            return DataFrame.of(rows, schema);
        }
    }

	@Override
	public void childSuccess (final Task<AgentTaskInformation> runningTask) {
		super.childSuccess(runningTask);
		success();
	}

	@Override
	public void childFail (final Task<AgentTaskInformation> runningTask) {
		super.childFail(runningTask);
		fail();
	}


	@Override
	public void end() {
		LOG.info("Status (" + getStatus() + ")");
	}
}
