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
package org.eclipse.smarthome.core.thing.internal.profiles;

import static org.eclipse.smarthome.core.thing.profiles.SystemProfiles.RAWBUTTON_TOGGLE_ROLLERSHUTTER;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.TriggerProfile;
import org.eclipse.smarthome.core.types.State;

/**
 * This profile allows a channel of the "system:rawbutton" type to be bound to an item.
 *
 * It reads the triggered events and uses the item's current state and toggles it once it detects that the
 * button was pressed.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class RawButtonToggleRollershutterProfile implements TriggerProfile {

    private final ProfileCallback callback;

    @Nullable
    private State previousState;

    public RawButtonToggleRollershutterProfile(ProfileCallback callback) {
        this.callback = callback;
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return RAWBUTTON_TOGGLE_ROLLERSHUTTER;
    }

    @Override
    public void onTriggerFromHandler(String event) {
        if (CommonTriggerEvents.PRESSED.equals(event)) {
            UpDownType newState = UpDownType.UP.equals(previousState) ? UpDownType.DOWN : UpDownType.UP;
            callback.sendCommand(newState);
            previousState = newState;
        }
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        previousState = state.as(UpDownType.class);
    }

}
