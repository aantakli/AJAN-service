package de.dfki.asr.ajan.pluginsystem.mlplugin.test;

/**
 *
 * @author anan02-admin
 */


import org.apache.commons.csv.CSVFormat;
import smile.data.CategoricalEncoder;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.data.type.DataTypes;
import smile.data.type.StructField;
import smile.data.type.StructType;
import smile.io.CSV;

import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 *
 * @author Haifeng
 */
public class USPS {

    public static DataFrame train;
    public static DataFrame test;
    public static Formula formula = Formula.lhs("class");

    public static double[][] x;
    public static int[] y;
    public static double[][] testx;
    public static int[] testy;

    static {
        ArrayList<StructField> fields = new ArrayList<>();
        fields.add(new StructField("class", DataTypes.ByteType));
        IntStream.range(1, 257).forEach(i -> fields.add(new StructField("V"+i, DataTypes.DoubleType)));
        StructType schema = DataTypes.struct(fields);

        CSVFormat format = CSVFormat.newFormat(' ');
        CSV csv = new CSV(format);
        csv.schema(schema);

        try {
            train = csv.read("C:\\Users\\anan02-admin\\Documents\\Playground\\AJAN\\github\\AJAN-service\\pluginsystem\\plugins\\MLPlugin\\src\\main\\resources\\zip.train");
            test = csv.read("C:\\Users\\anan02-admin\\Documents\\Playground\\AJAN\\github\\AJAN-service\\pluginsystem\\plugins\\MLPlugin\\src\\main\\resources\\zip.test");

            x = formula.x(train).toArray(false, CategoricalEncoder.DUMMY);
            y = formula.y(train).toIntArray();
            testx = formula.x(test).toArray(false, CategoricalEncoder.DUMMY);
            testy = formula.y(test).toIntArray();
        } catch (Exception ex) {
            System.err.println("Failed to load 'USPS': " + ex);
            System.exit(-1);
        }
    }
}
