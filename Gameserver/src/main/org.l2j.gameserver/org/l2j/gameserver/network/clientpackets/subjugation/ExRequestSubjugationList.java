package org.l2j.gameserver.network.clientpackets.subjugation;

import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.network.clientpackets.ClientPacket;
import org.l2j.gameserver.network.serverpackets.subjugation.ExSubjugationList;

public class ExRequestSubjugationList extends ClientPacket
{
	@Override
	protected void readImpl() throws Exception
	{
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
		client.sendPacket(new ExSubjugationList(player));
	}
}
