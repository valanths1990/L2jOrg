/*
 * This file is part of the L2J Mobius project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2j.gameserver.world.zone.type;

import org.l2j.gameserver.model.actor.Creature;
import org.l2j.gameserver.world.zone.Zone;
import org.l2j.gameserver.world.zone.ZoneType;

import static org.l2j.gameserver.util.GameUtils.isPlayer;

/**
 * Zone where store is not allowed.
 *
 * @author fordfrog
 */
public class NoStoreZone extends Zone {
    public NoStoreZone(int id) {
        super(id);
    }

    @Override
    protected void onEnter(Creature character) {
        if (isPlayer(character)) {
            character.setInsideZone(ZoneType.NO_STORE, true);
        }
    }

    @Override
    protected void onExit(Creature character) {
        if (isPlayer(character)) {
            character.setInsideZone(ZoneType.NO_STORE, false);
        }
    }
}
