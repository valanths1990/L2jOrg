package org.l2j.gameserver.data.xml;

import org.l2j.commons.threading.ThreadPool;
import org.l2j.gameserver.data.xml.impl.LevelData;
import org.l2j.gameserver.model.holders.ItemChanceHolder;
import org.l2j.gameserver.model.holders.ItemHolder;
import org.l2j.gameserver.model.purge.Purge;
import org.l2j.gameserver.settings.ServerSettings;
import org.l2j.gameserver.util.GameXmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PurgeData extends GameXmlReader
{
	private static final Logger              LOGGER  = LoggerFactory.getLogger(PurgeData.class);
	private final        Map<Integer, Purge> _purges = new ConcurrentHashMap<>();

	protected PurgeData()
	{
		load();
	}

	@Override
	protected Path getSchemaFilePath()
	{
		return ServerSettings.dataPackDirectory().resolve("data/xsd/PurgeData.xsd");
	}

	@Override
	public void load()
	{
		_purges.clear();
		parseDatapackFile("data/PurgeData.xml");
		if (!_purges.isEmpty())
		{
			LOGGER.info("Loaded {} purges.", _purges.size());
		}
		else
		{
			LOGGER.info("System is disabled.");
		}
		ThreadPool.scheduleAtFixedRate(() ->
		{
			for (Purge purge : _purges.values())
			{
				purge.updateRanking();
			}
		}, 1000, 5 * 60 * 1000);
		scheduleMondayReset();
	}

	@Override
	public void parseDocument(Document doc, File f)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("purge".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						final int id = parseInt(attrs, "id");
						int minLevel = 0;
						int maxLevel = LevelData.getInstance().getMaxLevel();
						int maxSubjugationPoint = 0;
						int maxGachaPoint = 0;
						int maxPeriodicGachaPoint = 0;
						ItemHolder gachaCost = null;
						int maxUsePoint = 0;
						Map<Integer, ItemHolder> rankingRewards = new HashMap<>();
						List<ItemChanceHolder> rewards = new ArrayList<>();
						String monsterIds = null;
						for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
						{
							attrs = b.getAttributes();
							if ("level".equalsIgnoreCase(b.getNodeName()))
							{
								minLevel = parseInt(attrs, "min");
								maxLevel = parseInt(attrs, "max");
							}
							else if ("maxSubjugationPoint".equalsIgnoreCase(b.getNodeName()))
							{
								maxSubjugationPoint = Integer.parseInt(b.getTextContent());
							}
							else if ("maxGachaPoint".equalsIgnoreCase(b.getNodeName()))
							{
								maxGachaPoint = Integer.parseInt(b.getTextContent());
							}
							else if ("maxPeriodicGachaPoint".equalsIgnoreCase(b.getNodeName()))
							{
								maxPeriodicGachaPoint = Integer.parseInt(b.getTextContent());
							}
							else if ("gachaCost".equalsIgnoreCase(b.getNodeName()))
							{
								int costId = parseInt(attrs, "id");
								long costCount = parseLong(attrs, "count");
								gachaCost = new ItemHolder(costId, costCount);
							}
							else if ("maxUsePoint".equalsIgnoreCase(b.getNodeName()))
							{
								maxUsePoint = Integer.parseInt(b.getTextContent());
							}
							else if ("rankingRewards".equalsIgnoreCase(b.getNodeName()))
							{
								for (Node t = b.getFirstChild(); t != null; t = t.getNextSibling())
								{
									if ("reward".equals(t.getNodeName()))
									{
										final int rewardRank = parseInt(t.getAttributes(), "rank");
										final int rewardId = parseInt(t.getAttributes(), "id");
										final long rewardCount = parseInt(t.getAttributes(), "count");
										rankingRewards.put(rewardRank, new ItemHolder(rewardId, rewardCount));
									}
								}
							}
							else if ("rewards".equalsIgnoreCase(b.getNodeName()))
							{
								for (Node t = b.getFirstChild(); t != null; t = t.getNextSibling())
								{
									if ("reward".equals(t.getNodeName()))
									{
										final int rewardId = parseInt(t.getAttributes(), "id");
										final long rewardCount = parseInt(t.getAttributes(), "count");
										final float rewardChance = parseFloat(t.getAttributes(), "chance");
										rewards.add(new ItemChanceHolder(rewardId, rewardChance, rewardCount));
									}
								}
							}
							else if ("monsterIds".equalsIgnoreCase(b.getNodeName()))
							{
								monsterIds = b.getTextContent();
							}
						}
						_purges.put(id, new Purge(id, minLevel, maxLevel, maxSubjugationPoint, maxGachaPoint, maxPeriodicGachaPoint, gachaCost, maxUsePoint, rankingRewards, rewards, monsterIds));
					}
				}
			}
		}
	}

	public boolean isHotTime()
	{
		int from = (12 * 100) + 0;
		int to = (14 * 100) + 0;
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		int t = (c.get(Calendar.HOUR_OF_DAY) * 100) + c.get(Calendar.MINUTE);
		boolean isHotTime = ((to > from) && (t >= from) && (t <= to)) || ((to < from) && ((t >= from) || (t <= to)));
		if (isHotTime)
		{
			return true;
		}
		from = (19 * 100) + 0;
		to = (23 * 100) + 0;
		return ((to > from) && (t >= from) && (t <= to)) || ((to < from) && ((t >= from) || (t <= to)));
	}

	public Purge getPurge(int id)
	{
		return _purges.get(id);
	}

	public Collection<Purge> getPurges()
	{
		return _purges.values();
	}

	private void scheduleMondayReset()
	{
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		cal.set(Calendar.HOUR_OF_DAY, 00);
		cal.set(Calendar.MINUTE, 00);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		if (cal.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			cal.add(Calendar.WEEK_OF_MONTH, 1);
		}
		ThreadPool.schedule(new MondayResetTask(), cal.getTimeInMillis() - System.currentTimeMillis());
	}

	protected class MondayResetTask implements Runnable
	{
		public MondayResetTask()
		{
		}

		@Override
		public void run()
		{
			for (Purge purge : getPurges())
			{
				purge.onMonday();
			}
			LOGGER.info("Purges reset.");
			scheduleMondayReset();
		}
	}

	public static PurgeData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final PurgeData INSTANCE = new PurgeData();
	}
}