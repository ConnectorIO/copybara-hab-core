/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.core.thing.internal.update;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.internal.update.dto.AddChannel;
import org.openhab.core.thing.internal.update.dto.UpdateChannel;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link UpdateChannelInstructionImpl} implements a {@link ThingUpdateInstruction} that updates a channel of a
 * thing.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class UpdateChannelInstructionImpl implements ThingUpdateInstruction {
    private boolean removeOldChannel;
    private final int thingTypeVersion;
    private final boolean preserveConfig;
    private final String channelId;
    private final String channelTypeUid;
    private final @Nullable String label;
    private final @Nullable String description;
    private final @Nullable List<String> tags;

    UpdateChannelInstructionImpl(int thingTypeVersion, UpdateChannel updateChannel) {
        this.removeOldChannel = true;
        this.thingTypeVersion = thingTypeVersion;
        this.channelId = updateChannel.getId();
        this.channelTypeUid = updateChannel.getType();
        this.label = updateChannel.getLabel();
        this.description = updateChannel.getDescription();
        this.tags = updateChannel.getTags();
        this.preserveConfig = updateChannel.isPreserveConfiguration();
    }

    UpdateChannelInstructionImpl(int thingTypeVersion, AddChannel addChannel) {
        this.removeOldChannel = false;
        this.thingTypeVersion = thingTypeVersion;
        this.channelId = addChannel.getId();
        this.channelTypeUid = addChannel.getType();
        this.label = addChannel.getLabel();
        this.description = addChannel.getDescription();
        this.tags = addChannel.getTags();
        this.preserveConfig = false;
    }

    @Override
    public int getThingTypeVersion() {
        return thingTypeVersion;
    }

    @Override
    public void perform(Thing thing, ThingBuilder thingBuilder) {
        ChannelUID affectedChannelUid = new ChannelUID(thing.getUID(), channelId);

        if (removeOldChannel) {
            thingBuilder.withoutChannel(affectedChannelUid);
        }

        ChannelBuilder channelBuilder = ChannelBuilder.create(affectedChannelUid)
                .withType(new ChannelTypeUID(channelTypeUid));

        if (preserveConfig) {
            Channel oldChannel = thing.getChannel(affectedChannelUid);
            if (oldChannel != null) {
                channelBuilder.withConfiguration(oldChannel.getConfiguration());
                channelBuilder.withDefaultTags(oldChannel.getDefaultTags());
            }
        }

        if (label != null) {
            channelBuilder.withLabel(Objects.requireNonNull(label));
        }
        if (description != null) {
            channelBuilder.withDescription(Objects.requireNonNull(description));
        }
        if (tags != null) {
            channelBuilder.withDefaultTags(Set.copyOf(Objects.requireNonNull(tags)));
        }

        thingBuilder.withChannel(channelBuilder.build());
    }
}
