/*
 * Copyright © 2019-2021 L2JOrg
 *
 * This file is part of the L2JOrg project.
 *
 * L2JOrg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2JOrg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2j.gameserver.engine.autoplay;

import org.l2j.gameserver.model.WorldObject;
import org.l2j.gameserver.model.actor.Creature;
import org.l2j.gameserver.model.actor.instance.Monster;
import org.l2j.gameserver.model.actor.instance.Player;

/**
 * @author JoeAlisson
 */
class MonsterFinder extends AbstractAutoPlayTargetFinder {

    @Override
    public boolean canBeTarget(Player player, WorldObject target, int range) {
        return  target instanceof Monster monster && !monster.isDead() && super.canBeTarget(player, monster, range);
    }

    @Override
    public Creature findNextTarget(Player player, int range) {
        return findNextTarget(player, Monster.class, range);
    }
}
