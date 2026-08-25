package com.viscript_team.util;

import com.viscript_lib.annotation.ViScriptRegisterAccessors;
import com.viscript_lib.event.RegisterAccessorEvent;
import com.viscript_team.ViScriptTeam;
import com.viscript_team.data.faction.EntityFactionEntry;
import com.viscript_team.data.faction.Faction;
import com.viscript_team.data.faction.PlayerFactionStandings;
import com.viscript_team.data.faction.StandingEntry;
import com.viscript_team.data.party.Party;

public final class ViScriptTeamAccessors {
    private ViScriptTeamAccessors() {
    }

    @ViScriptRegisterAccessors(modId = ViScriptTeam.MOD_ID)
    public static void register(RegisterAccessorEvent event) {
        event.register(Faction.class, Faction::new);
        event.register(EntityFactionEntry.class, EntityFactionEntry::new);
        event.register(PlayerFactionStandings.class, PlayerFactionStandings::new);
        event.register(StandingEntry.class, StandingEntry::new);
        event.register(Party.class, Party::new);
    }
}
