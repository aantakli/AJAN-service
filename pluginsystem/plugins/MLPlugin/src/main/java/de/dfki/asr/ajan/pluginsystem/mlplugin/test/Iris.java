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
package de.dfki.asr.ajan.pluginsystem.mlplugin.test;

import smile.data.CategoricalEncoder;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.io.Read;

/**
 *
 * @author Haifeng
 */
public class Iris {

    public static DataFrame data;
    public static Formula formula = Formula.lhs("class");

    public static double[][] x;
    public static int[] y;

    static {
        try {
            data = Read.arff("D:\\Projects\\AJAN\\github\\AJAN-service\\pluginsystem\\plugins\\MLPlugin\\src\\main\\resources\\iris.arff");

            x = formula.x(data).toArray(false, CategoricalEncoder.DUMMY);
            y = formula.y(data).toIntArray();
        } catch (Exception ex) {
            System.err.println("Failed to load 'iris': " + ex);
            System.exit(-1);
        }
    }
}
