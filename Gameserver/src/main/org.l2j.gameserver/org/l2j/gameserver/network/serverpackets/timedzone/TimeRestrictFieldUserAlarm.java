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
package org.l2j.gameserver.network.serverpackets.timedzone;

import io.github.joealisson.mmocore.WritableBuffer;
import org.l2j.gameserver.network.GameClient;
import org.l2j.gameserver.network.serverpackets.ServerPacket;

import static org.l2j.gameserver.network.ServerExPacketId.EX_TIME_RESTRICT_FIELD_USER_ALARM;

/**
 * @author JoeAlisson
 */
public class TimeRestrictFieldUserAlarm extends ServerPacket {

    private final int zoneId;
    private final int remainingTime;

    public TimeRestrictFieldUserAlarm(int zoneId, int remainingTime) {
        this.zoneId = zoneId;
        this.remainingTime = remainingTime;
    }

    @Override
    protected void writeImpl(GameClient client, WritableBuffer buffer) throws Exception {
        writeId(EX_TIME_RESTRICT_FIELD_USER_ALARM, buffer);
        buffer.writeInt(zoneId);
        buffer.writeInt(remainingTime);
    }
}
