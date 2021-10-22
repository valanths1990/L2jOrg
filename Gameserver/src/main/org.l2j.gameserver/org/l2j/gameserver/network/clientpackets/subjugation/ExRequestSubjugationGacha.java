package org.l2j.gameserver.network.clientpackets.subjugation;

import org.l2j.commons.util.Rnd;
import org.l2j.gameserver.data.xml.PurgeData;
import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.model.holders.ItemChanceHolder;
import org.l2j.gameserver.model.purge.Purge;
import org.l2j.gameserver.network.clientpackets.ClientPacket;
import org.l2j.gameserver.network.serverpackets.subjugation.ExSubjugationGacha;

import java.util.HashMap;
import java.util.Map;

public class ExRequestSubjugationGacha extends ClientPacket
{
	private int _id;
	private int _count;

	@Override
	protected void readImpl() throws Exception
	{
		_id = readInt();
		_count = readInt();
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
		final Purge purge = PurgeData.getInstance().getPurge(_id);
		if (purge == null)
		{
			return;
		}
		if (_count > purge.getMaxUsePoint())
		{
			return;
		}
		int currentKeys = player.getPurgeDataCurrentKeys(_id);
		if ((currentKeys >= _count) && player.destroyItemByItemId("Subjugation Gacha", purge.getGachaCost().getId(), purge.getGachaCost().getCount() * _count, player, true))
		{
			player.setPurgeDataCurrentKeys(_id, currentKeys - _count);
			Map<Integer, Integer> obtainedItems = new HashMap<>();
			for (int i = 0; i < _count; i++)
			{
				final double rand = Rnd.nextDouble() * 100;
				final ItemChanceHolder reward = purge.getRewards().get(i);
				if (rand < reward.getChance())
				{
					final long currentCount = obtainedItems.getOrDefault(reward.getId(), (int) 0L);
					obtainedItems.put(reward.getId(), (int) (currentCount + reward.getCount()));
				}
			}
			for (Map.Entry<Integer, Integer> reward : obtainedItems.entrySet())
			{
				player.addItem("Subjugation Gacha", reward.getKey(), reward.getValue(), player, true);
			}
			client.sendPacket(new ExSubjugationGacha(obtainedItems));
		}
	}
}