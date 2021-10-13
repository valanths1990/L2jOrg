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
package org.l2j.gameserver.network.serverpackets;

import io.github.joealisson.mmocore.WritableBuffer;
import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.model.stats.Stat;
import org.l2j.gameserver.network.GameClient;
import org.l2j.gameserver.network.ServerExPacketId;

/**
 * @author JoeAlisson
 */
public class ExUserBoostStat extends ServerPacket
{
    private final Player        _player;
    private final BoostStatType type;
    private final short         percent;

    public ExUserBoostStat(Player player, BoostStatType type, short percent)
    {
        _player = player;
        this.type = type;
        this.percent = percent;
    }

    @Override
    protected void writeImpl(GameClient client, WritableBuffer buffer)
    {
        writeId(ServerExPacketId.EX_USER_BOOST_STAT, buffer);
        buffer.writeByte(type.ordinal() + 1); // type (Server bonus), 2 - (stats bonus) or 3 (Vitality) ?
        if (type == BoostStatType.SAYHA)
        {
            double sayhaBonus = _player.getStats().getSayhaGraceExpBonus() * 100;
            if (sayhaBonus == 100)
            {
                sayhaBonus = 0;
            }
            buffer.writeByte(sayhaBonus > 0 ? 0x01 : 0x00); // Count
            buffer.writeShort((int) sayhaBonus); // Boost
        }
        else if (type == BoostStatType.BUFFS)
        {
            buffer.writeByte(_player.getStats().getValue(Stat.BONUS_EXP, 0) > 0 ? 1 : 0); // Count
            buffer.writeShort((int) _player.getStats().getValue(Stat.BONUS_EXP, 0)); // Boost
        }
        else if (type == BoostStatType.PASSIVE)
        {
            buffer.writeByte(1); // count
            buffer.writeShort(percent);
        }
    }

    public enum BoostStatType
    {
        SAYHA,
        BUFFS,
        PASSIVE,
    }
}
