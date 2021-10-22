package org.l2j.gameserver.network.serverpackets.subjugation;

import io.github.joealisson.mmocore.WritableBuffer;
import org.l2j.gameserver.network.GameClient;
import org.l2j.gameserver.network.ServerExPacketId;
import org.l2j.gameserver.network.serverpackets.ServerPacket;

import java.util.Map;

public class ExSubjugationGacha extends ServerPacket
{
	private final Map<Integer, Integer> _obtainedItems;

	public ExSubjugationGacha(Map<Integer, Integer> obtainedItems)
	{
		_obtainedItems = obtainedItems;
	}

	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		writeId(ServerExPacketId.EX_SUBJUGATION_GACHA, buffer);
		buffer.writeInt(_obtainedItems.size());
		for (Map.Entry<Integer, Integer> reward : _obtainedItems.entrySet())
		{
			buffer.writeInt(reward.getKey());
			buffer.writeInt(reward.getValue());
		}
		//buffer.writeInt(gachaItem.size); // TODO gachasize
		//  for (var gachaItem : gachas) {
		// buffer.writeInt(); // TODO classId
		//  buffer.writeInt(); // TODO Amount
		// }
	}
}
