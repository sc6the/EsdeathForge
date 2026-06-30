package net.labymod.labyplay;

import net.labymod.labyplay.party.PartySystem;

/**
 * Phase 1 stub of LabyPlay (party/queue features). Only the party hook used by
 * LabyConnect's SingleChat is exposed.
 */
public class LabyPlay {

    private final PartySystem partySystem = new PartySystem();

    public PartySystem getPartySystem() {
        return partySystem;
    }
}
