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
package org.openhab.core.persistence.config;

/**
 * This class represents the configuration that is used for group items.
 *
 * @author Markus Rathgeb - Initial contribution
 */
public class SimpleGroupConfig extends SimpleConfig {

    private final String group;

    public SimpleGroupConfig(final String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return String.format("%s [group=%s]", getClass().getSimpleName(), group);
    }

}
