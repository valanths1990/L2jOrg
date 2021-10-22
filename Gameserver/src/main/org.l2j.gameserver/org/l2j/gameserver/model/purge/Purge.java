package org.l2j.gameserver.model.purge;

import org.l2j.commons.util.Rnd;
import org.l2j.gameserver.data.database.dao.PurgeDAO;
import org.l2j.gameserver.data.database.data.MailData;
import org.l2j.gameserver.data.sql.impl.PlayerNameTable;
import org.l2j.gameserver.data.xml.PurgeData;
import org.l2j.gameserver.engine.mail.Attachment;
import org.l2j.gameserver.engine.mail.MailEngine;
import org.l2j.gameserver.enums.MailType;
import org.l2j.gameserver.model.StatsSet;
import org.l2j.gameserver.model.actor.Attackable;
import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.model.events.EventType;
import org.l2j.gameserver.model.events.Listeners;
import org.l2j.gameserver.model.events.ListenersContainer;
import org.l2j.gameserver.model.events.impl.character.npc.OnAttackableKill;
import org.l2j.gameserver.model.events.listeners.ConsumerEventListener;
import org.l2j.gameserver.model.holders.ItemChanceHolder;
import org.l2j.gameserver.model.holders.ItemHolder;
import org.l2j.gameserver.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.l2j.commons.database.DatabaseAccess.getDAO;

public class Purge extends ListenersContainer
{
	private static final Logger                   LOGGER            = LoggerFactory.getLogger(Purge.class);
	private final        int                      _purgeId;
	private final        int                      _minLevel;
	private final        int                      _maxLevel;
	private final        int                      _maxSubjugationPoint;
	private final        int                      _maxGachaPoint;
	private final        int                      _maxPeriodicGachaPoint;
	private final        ItemHolder               _gachaCost;
	private final        int                      _maxUsePoint;
	private final        Map<Integer, ItemHolder> _rankingRewards;
	private final        List<ItemChanceHolder>   _rewards;
	private final        int[]                    _monsterIds;
	private final        Map<Integer, StatsSet>   _top5             = new ConcurrentHashMap<>();
	private final        Map<Integer, StatsSet>   _rankingList      = new ConcurrentHashMap<>();
	private final        Map<Integer, Integer>    _rankingListRanks = new ConcurrentHashMap<>();

	public Purge(int purgeId, int minLevel, int maxLevel, int maxSubjugationPoint, int maxGachaPoint, int maxPeriodicGachaPoint, ItemHolder gachaCost, int maxUsePoint, Map<Integer, ItemHolder> rankingRewards, List<ItemChanceHolder> rewards, String monsterIds)
	{
		_purgeId = purgeId;
		_minLevel = minLevel;
		_maxLevel = maxLevel;
		_maxSubjugationPoint = maxSubjugationPoint;
		_maxGachaPoint = maxGachaPoint;
		_maxPeriodicGachaPoint = maxPeriodicGachaPoint;
		_gachaCost = gachaCost;
		_maxUsePoint = maxUsePoint;
		_rankingRewards = rankingRewards;
		_rewards = rewards;
		if (!monsterIds.isEmpty())
		{
			String idsStr[] = monsterIds.split(",");
			_monsterIds = new int[idsStr.length];
			for (int i = 0; i < idsStr.length; i++)
			{
				_monsterIds[i] = Integer.parseInt(idsStr[i]);
			}
			Arrays.sort(_monsterIds);
		}
		else
		{
			_monsterIds = null;
		}
		Listeners.Global().addListener(new ConsumerEventListener(this, EventType.ON_ATTACKABLE_KILL, (Consumer<OnAttackableKill>) this::onMobKilled, this));
	}

	private void onMobKilled(OnAttackableKill event)
	{
		if (_monsterIds == null)
		{
			return;
		}
		final Attackable monster = event.getTarget();
		if (Arrays.binarySearch(_monsterIds, monster.getId()) < 0)
		{
			return;
		}
		final Player player = event.getAttacker();
		final int playerLevel = player.getLevel();
		if ((playerLevel < _minLevel) || (playerLevel > _maxLevel) || ((player.getLevel() - playerLevel) > 14))
		{
			return;
		}
		int points = player.getPurgeDataPoints(getPurgeId()); //player.getVariables().getInt(Variables.PURGE_DATA_POINTS + "_" + getPurgeId(), 0);
		points = points + (Rnd.get(40, 60) * (PurgeData.getInstance().isHotTime() ? 2 : 1));
		int totalKeys = player.getPurgeDataTotalKeys(getPurgeId());//player.getVariables().getInt(PlayerVariables.PURGE_DATA_TOTAL_KEYS + "_" + getPurgeId(), 0);
		if ((points / getMaxSubjugationPoint()) > totalKeys)
		{
			if (totalKeys < getMaxPeriodicGachaPoint())
			{
				int currentKeys = player.getPurgeDataCurrentKeys(getPurgeId());//player.getVariables().getInt(PlayerVariables.PURGE_DATA_CURRENT_KEYS + "_" + getPurgeId(), 0);
				player.setPurgeDataCurrentKeys(getPurgeId(), currentKeys + 1);//player.getVariables().set(PlayerVariables.PURGE_DATA_CURRENT_KEYS + "_" + getPurgeId(), currentKeys + 1);
				player.setPurgeDataTotalKeys(getPurgeId(), totalKeys + 1);//player.getVariables().set(PlayerVariables.PURGE_DATA_TOTAL_KEYS + "_" + getPurgeId(), totalKeys + 1);
			}
		}
		player.setPurgeDataPoints(getPurgeId(), points); //player.getVariables().set(PlayerVariables.PURGE_DATA_POINTS + "_" + getPurgeId(), points);
	}

	public int getPlayerPoints(int charId)
	{
		int rank = _rankingListRanks.getOrDefault(charId, -1);
		if (rank != -1)
		{
			StatsSet player = _rankingList.get(rank);
			if (player != null)
			{
				return player.getInt("points");
			}
		}
		return 0;
	}

	public int getPlayerRank(int charId)
	{
		return _rankingListRanks.getOrDefault(charId, 0);
	}

	public void updateRanking()
	{
		_top5.clear();
		_rankingList.clear();
		_rankingListRanks.clear();
		List<org.l2j.gameserver.data.database.data.PurgeData> _purges = getDAO(PurgeDAO.class).findByPurgeId(getPurgeId());
		for (int i = 0; i < _purges.size(); i++)
		{
			StatsSet player = new StatsSet();
			int charId = _purges.get(i).getPlayerId();
			player.set("charId", charId);
			player.set("name", PlayerNameTable.getInstance().getNameById(charId));
			player.set("points", _purges.get(i).getPurgeDataPoints());
			_rankingList.put(i, player);
			_rankingListRanks.put(charId, i);
			if (i <= 5)
			{
				_top5.put(i, player);
			}
		}
	}

	public void onMonday()
	{
		for (int i = 1; i <= 5; i++)
		{
			StatsSet player = _rankingList.get(i);
			final MailData mail = MailData.of(player.getInt("charId"), "Purge Reward", "", MailType.NPC);
			final Attachment attachement = new Attachment(mail.getSender(), mail.getId());
			attachement.addItem("Purge Reward", getRankingRewards().get(i).getId(), getRankingRewards().get(i).getCount(), null, this);
			mail.attach(attachement);
			MailEngine.getInstance().sendMail(mail);
		}
		getDAO(PurgeDAO.class).resetPurgeData();
		World.getInstance().getPlayers().stream().forEach(player -> player.setPurges(null));
		LOGGER.info("Purge " + getPurgeId() + " reset.");
	}

	public Map<Integer, StatsSet> getTop5()
	{
		return _top5;
	}

	public int getPurgeId()
	{
		return _purgeId;
	}

	public int getMinLevel()
	{
		return _minLevel;
	}

	public int getMaxLevel()
	{
		return _maxLevel;
	}

	public int getMaxSubjugationPoint()
	{
		return _maxSubjugationPoint;
	}

	public int getMaxGachaPoint()
	{
		return _maxGachaPoint;
	}

	public int getMaxPeriodicGachaPoint()
	{
		return _maxPeriodicGachaPoint;
	}

	public ItemHolder getGachaCost()
	{
		return _gachaCost;
	}

	public int getMaxUsePoint()
	{
		return _maxUsePoint;
	}

	public Map<Integer, ItemHolder> getRankingRewards()
	{
		return _rankingRewards;
	}

	public List<ItemChanceHolder> getRewards()
	{
		return _rewards;
	}
}