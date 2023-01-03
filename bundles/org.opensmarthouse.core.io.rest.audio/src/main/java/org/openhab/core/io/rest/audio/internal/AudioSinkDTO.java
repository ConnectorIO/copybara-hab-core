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
package org.openhab.core.io.rest.audio.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.audio.AudioSink;

/**
 * A DTO that is used on the REST API to provide infos about {@link AudioSink} to UIs.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class AudioSinkDTO {
    public String id;
    public @Nullable String label;

    public AudioSinkDTO(String id, @Nullable String label) {
        this.id = id;
        this.label = label;
    }
}
