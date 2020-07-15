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
package org.openhab.core.thing.internal.console;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * {@link LinkConsoleCommandExtension} provides console commands for listing,
 * adding and removing links.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Alex Tugarev - Added support for links between items and things
 * @author Kai Kreuzer - Removed Thing link commands
 * @author Jan N. Klug - Add orphan link handling
 */
@Component(immediate = true, service = ConsoleCommandExtension.class)
public class LinkConsoleCommandExtension extends AbstractConsoleCommandExtension {

    private static final String SUBCMD_LIST = "list";
    private static final String SUBCMD_CL_ADD = "addChannelLink";
    private static final String SUBCMD_CL_REMOVE = "removeChannelLink";
    private static final String SUBCMD_CLEAR = "clear";
    private static final String SUBCMD_ORPHAN = "orphan";

    private final ThingRegistry thingRegistry;
    private final ItemRegistry itemRegistry;
    private final ItemChannelLinkRegistry itemChannelLinkRegistry;

    @Activate
    public LinkConsoleCommandExtension(@Reference ThingRegistry thingRegistry, @Reference ItemRegistry itemRegistry,
            @Reference ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super("links", "Manage your links.");

        this.thingRegistry = thingRegistry;
        this.itemRegistry = itemRegistry;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length > 0) {
            String subCommand = args[0];
            switch (subCommand) {
                case SUBCMD_LIST:
                    list(console, itemChannelLinkRegistry.getAll());
                    return;
                case SUBCMD_ORPHAN:
                    if (args.length == 2 && (args[1].equals("list") || args[1].equals("purge"))) {
                        orphan(console, args[1], itemChannelLinkRegistry.getAll(), thingRegistry.getAll(),
                                itemRegistry.getAll());
                    } else {
                        console.println("Specify action 'list' or 'purge' to be executed: orphan <list|purge>");
                    }
                    return;
                case SUBCMD_CL_ADD:
                    if (args.length > 2) {
                        String itemName = args[1];
                        ChannelUID channelUID = new ChannelUID(args[2]);
                        addChannelLink(console, itemName, channelUID);
                    } else {
                        console.println("Specify item name and channel UID to link: link <itemName> <channelUID>");
                    }
                    return;
                case SUBCMD_CL_REMOVE:
                    if (args.length > 2) {
                        String itemName = args[1];
                        ChannelUID channelUID = new ChannelUID(args[2]);
                        removeChannelLink(console, itemName, channelUID);
                    } else {
                        console.println("Specify item name and channel UID to unlink: link <itemName> <channelUID>");
                    }
                    return;
                case SUBCMD_CLEAR:
                    clear(console);
                    return;
                default:
                    console.println("Unknown command '" + subCommand + "'");
                    printUsage(console);
                    break;
            }
        } else {
            printUsage(console);
        }
    }

    private void orphan(Console console, String action, Collection<ItemChannelLink> itemChannelLinks,
            Collection<Thing> things, Collection<Item> items) {
        Collection<ChannelUID> channelUIDS = things.stream().map(Thing::getChannels).flatMap(List::stream)
                .map(Channel::getUID).collect(Collectors.toCollection(HashSet::new));
        Collection<String> itemNames = items.stream().map(Item::getName).collect(Collectors.toCollection(HashSet::new));

        itemChannelLinks.forEach(itemChannelLink -> {
            if (!channelUIDS.contains(itemChannelLink.getLinkedUID())) {
                console.println("Thing channel missing: " + itemChannelLink.toString());
                if (action.equals("purge")) {
                    removeChannelLink(console, itemChannelLink.getItemName(), itemChannelLink.getLinkedUID());
                }
            } else if (!itemNames.contains(itemChannelLink.getItemName())) {
                console.println("Item missing: " + itemChannelLink.toString());
                if (action.equals("purge")) {
                    removeChannelLink(console, itemChannelLink.getItemName(), itemChannelLink.getLinkedUID());
                }
            }
        });
    }

    @Override
    public List<String> getUsages() {
        return Arrays.asList(new String[] { buildCommandUsage(SUBCMD_LIST, "lists all links"),
                buildCommandUsage(SUBCMD_CL_ADD + " <itemName> <channelUID>", "links an item with a channel"),
                buildCommandUsage(SUBCMD_CL_REMOVE + " <itemName> <thingUID>", "unlinks an item with a channel"),
                buildCommandUsage(SUBCMD_CLEAR, "removes all managed links"),
                buildCommandUsage(SUBCMD_ORPHAN, "<list|purge> lists/purges all links with one missing element") });
    }

    private void clear(Console console) {
        Collection<ItemChannelLink> itemChannelLinks = itemChannelLinkRegistry.getAll();
        for (ItemChannelLink itemChannelLink : itemChannelLinks) {
            itemChannelLinkRegistry.remove(itemChannelLink.getUID());
        }
        console.println(itemChannelLinks.size() + " links successfully removed.");
    }

    private void addChannelLink(Console console, String itemName, ChannelUID channelUID) {
        ItemChannelLink itemChannelLink = new ItemChannelLink(itemName, channelUID);
        itemChannelLinkRegistry.add(itemChannelLink);
        console.println("Link " + itemChannelLink.toString() + " successfully added.");
    }

    private void list(Console console, Collection<ItemChannelLink> itemChannelLinks) {
        for (ItemChannelLink itemChannelLink : itemChannelLinks) {
            console.println(itemChannelLink.toString());
        }
    }

    private void removeChannelLink(Console console, String itemName, ChannelUID channelUID) {
        ItemChannelLink itemChannelLink = new ItemChannelLink(itemName, channelUID);
        ItemChannelLink removedItemChannelLink = itemChannelLinkRegistry.remove(itemChannelLink.getUID());
        if (removedItemChannelLink != null) {
            console.println("Link " + itemChannelLink.toString() + "successfully removed.");
        } else {
            console.println("Could not remove link " + itemChannelLink.toString() + ".");
        }
    }
}
