/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.library.items;

import java.util.ArrayList;
import java.util.List;

import org.openhab.core.items.GenericItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.openhab.core.types.UnDefType;

/**
 * A StringItem can be used for any kind of string to either send or receive
 * from a device.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class StringItem extends GenericItem {

    private static List<Class<? extends State>> acceptedDataTypes = new ArrayList<>();
    private static List<Class<? extends Command>> acceptedCommandTypes = new ArrayList<>();

    static {
        acceptedDataTypes.add(StringType.class);
        acceptedDataTypes.add((DateTimeType.class));
        acceptedDataTypes.add(UnDefType.class);

        acceptedCommandTypes.add(StringType.class);
    }

    public StringItem(String name) {
        super(name);
    }

    @Override
    public List<Class<? extends State>> getAcceptedDataTypes() {
        return acceptedDataTypes;
    }

    @Override
    public List<Class<? extends Command>> getAcceptedCommandTypes() {
        return acceptedCommandTypes;
    }

    @Override
    public State getStateAs(Class<? extends State> typeClass) {
        List<Class<? extends State>> list = new ArrayList<>();
        list.add(typeClass);
        State convertedState = TypeParser.parseState(list, state.toString());
        if (convertedState != null) {
            return convertedState;
        } else {
            return super.getStateAs(typeClass);
        }
    }
}
