/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.core.io.rest.sse.internal.listeners;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.io.rest.sse.SseResource;
import org.openhab.core.io.rest.sse.internal.ItemStatesSseBroadcaster;
import org.openhab.core.items.events.ItemStateChangedEvent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link SseEventSubscriber} is responsible for broadcasting openHAB events
 * to currently listening SSE clients.
 *
 * @author Stefan Bußweiler - Initial contribution
 * @author Yannick Schaus - Broadcast state events to the specialized {@link ItemStatesSseBroadcaster}
 */
@Component
@NonNullByDefault
public class SseEventSubscriber implements EventSubscriber {

    private final Set<String> subscribedEventTypes = Collections.singleton(EventSubscriber.ALL_EVENT_TYPES);

    private final SseResource sseResource;

    @Activate
    public SseEventSubscriber(final @Reference SseResource sseResource) {
        this.sseResource = sseResource;
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return subscribedEventTypes;
    }

    @Override
    public @Nullable EventFilter getEventFilter() {
        return null;
    }

    @Override
    public void receive(Event event) {
        sseResource.broadcastEvent(event);
        if (event instanceof ItemStateChangedEvent) {
            sseResource.broadcastStateEvent((ItemStateChangedEvent) event);
        }
    }
}