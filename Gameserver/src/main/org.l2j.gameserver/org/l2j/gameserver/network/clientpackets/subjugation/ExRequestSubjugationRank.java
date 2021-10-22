package org.l2j.gameserver.network.clientpackets.subjugation;

import org.l2j.gameserver.data.xml.PurgeData;
import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.network.clientpackets.ClientPacket;
import org.l2j.gameserver.network.serverpackets.subjugation.ExSubjugationRank;

public class ExRequestSubjugationRank extends ClientPacket
{
	private int _id;

	@Override
	protected void readImpl() throws Exception
	{
		_id = readInt();
		// dummy byte
	}

	@Override
	protected void runImpl()
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		if (PurgeData.getInstance().getPurge(_id) != null)
		{
			client.sendPacket(new ExSubjugationRank(player, _id));
		}
	}
}
