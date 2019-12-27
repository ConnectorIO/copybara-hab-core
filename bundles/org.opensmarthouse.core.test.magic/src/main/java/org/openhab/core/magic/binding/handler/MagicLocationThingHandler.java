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
package org.openhab.core.magic.binding.handler;

import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;

/**
 * The {@link MagicLocationThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Henning Treu - Initial contribution
 */
public class MagicLocationThingHandler extends BaseThingHandler {

    public MagicLocationThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // process the command for the location channel here
        // if (channelUID.getId().equals(MagicBindingConstants.CHANNEL_LOCATION)) {
        // }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }
}
