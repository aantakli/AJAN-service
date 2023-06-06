/*
 * Copyright (c) 2023. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.POMDP.implementation;

import de.dfki.asr.ajan.pluginsystem.mdpplugin.utils.POMDP.State;

public class AJAN_Agent_State extends State {

//    String description;
    int agent_position; // TODO: Can be replaced with ArrayList or Vector<Integer>/Vector<std::any>
                        // denoting different states (i.e) LEFT = 1, RIGHT = 2 etc ...
    public AJAN_Agent_State(int _state_id, double weight) {
        super(_state_id, weight);
    }

    @Override

    public String text() {
        return agent_position==0?"LEFT":"RIGHT";
    }
}
