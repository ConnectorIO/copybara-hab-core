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
package org.openhab.core.config.core.internal.net;

import java.net.Inet4Address;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.net.CidrAddress;
import org.openhab.core.net.NetUtil;
import org.osgi.service.component.annotations.Component;

/**
 * Provides a list of IPv4 addresses of the local machine and shows the user which interface belongs to which IP address
 *
 * @author Stefan Triller - Initial contribution
 */
@Component
public class NetworkConfigOptionProvider implements ConfigOptionProvider {
    static final URI CONFIG_URI = URI.create("system:network");
    static final String PARAM_PRIMARY_ADDRESS = "primaryAddress";
    static final String PARAM_BROADCAST_ADDRESS = "broadcastAddress";

    @Override
    public Collection<ParameterOption> getParameterOptions(URI uri, String param, Locale locale) {
        if (!uri.equals(CONFIG_URI)) {
            return null;
        }

        if (param.equals(PARAM_PRIMARY_ADDRESS)) {
            Stream<CidrAddress> ipv4Addresses = NetUtil.getAllInterfaceAddresses().stream()
                    .filter(a -> a.getAddress() instanceof Inet4Address);
            return ipv4Addresses.map(a -> new ParameterOption(a.toString(), a.toString())).collect(Collectors.toList());
        }

        if (param.equals(PARAM_BROADCAST_ADDRESS)) {
            List<String> broadcastAddrList = new ArrayList<>(NetUtil.getAllBroadcastAddresses());
            broadcastAddrList.add("255.255.255.255");
            return broadcastAddrList.stream().map(a -> new ParameterOption(a, a)).collect(Collectors.toList());
        }
        return null;
    }
}
