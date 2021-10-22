package org.l2j.gameserver.network.serverpackets.subjugation;

import io.github.joealisson.mmocore.WritableBuffer;
import org.l2j.gameserver.data.xml.PurgeData;
import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.model.purge.Purge;
import org.l2j.gameserver.network.GameClient;
import org.l2j.gameserver.network.ServerExPacketId;
import org.l2j.gameserver.network.serverpackets.ServerPacket;

import java.util.Collection;

public class ExSubjugationList extends ServerPacket
{
	private final Player _player;

	public ExSubjugationList(Player player)
	{
		_player = player;
	}

	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		writeId(ServerExPacketId.EX_SUBJUGATION_LIST, buffer);
		Collection<Purge> purges = PurgeData.getInstance().getPurges();
		buffer.writeInt(purges.size()); // size
		for (Purge purge : purges)
		{
			buffer.writeInt(purge.getPurgeId()); // id
			buffer.writeInt(_player.getPurgeDataPoints(purge.getPurgeId()) % purge.getMaxSubjugationPoint()); // current points
			buffer.writeInt(_player.getPurgeDataCurrentKeys(purge.getPurgeId())); // Gachapoint (keys)
			buffer.writeInt(purge.getMaxPeriodicGachaPoint() - _player.getPurgeDataTotalKeys(purge.getPurgeId())); // RemainPeriodicGachaPoint (remaining keys)
		}
	}
}
