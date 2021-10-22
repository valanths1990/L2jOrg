package org.l2j.gameserver.network.serverpackets.subjugation;

import io.github.joealisson.mmocore.WritableBuffer;
import org.l2j.gameserver.data.xml.PurgeData;
import org.l2j.gameserver.model.StatsSet;
import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.model.purge.Purge;
import org.l2j.gameserver.network.GameClient;
import org.l2j.gameserver.network.ServerExPacketId;
import org.l2j.gameserver.network.serverpackets.ServerPacket;

public class ExSubjugationRank extends ServerPacket
{
	private final Player _player;
	private final int    _id;

	public ExSubjugationRank(Player player, int id)
	{
		_player = player;
		_id = id;
	}

	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		writeId(ServerExPacketId.EX_SUBJUGATION_RANKING, buffer);
		Purge purge = PurgeData.getInstance().getPurge(_id);
		int size = Math.min(5, purge.getTop5().size());
		buffer.writeInt(size);
		int i = 0;
		for (StatsSet player : purge.getTop5().values())
		{
			i++;
			buffer.writeString(player.getString("name", "-----"));
			buffer.writeInt(player.getInt("points", 0));
			buffer.writeInt(i);
		}
		buffer.writeInt(_id);
		buffer.writeInt(purge.getPlayerPoints(_player.getObjectId()));
		buffer.writeInt(purge.getPlayerRank(_player.getObjectId()));
	}
}
