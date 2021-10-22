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
package org.l2j.gameserver.data.database.data;

import org.l2j.commons.database.annotation.Table;

/**
 * @author Thoss
 */
@Table("purges")
public class PurgeData
{
	private int purgeId;
	private int playerId;
	private int purgeDataPoints;
	private int purgeDataCurrentKeys;
	private int purgeDataTotalKeys;

	public int getPurgeId()
	{
		return purgeId;
	}

	public void setPurgeId(int purgeId)
	{
		this.purgeId = purgeId;
	}

	public int getPlayerId()
	{
		return playerId;
	}

	public void setPlayerId(int playerId)
	{
		this.playerId = playerId;
	}

	public int getPurgeDataPoints()
	{
		return purgeDataPoints;
	}

	public void setPurgeDataPoints(int purgeDataPoints)
	{
		this.purgeDataPoints = purgeDataPoints;
	}

	public int getPurgeDataCurrentKeys()
	{
		return purgeDataCurrentKeys;
	}

	public void setPurgeDataCurrentKeys(int purgeDataCurrentKeys)
	{
		this.purgeDataCurrentKeys = purgeDataCurrentKeys;
	}

	public int getPurgeDataTotalKeys()
	{
		return purgeDataTotalKeys;
	}

	public void setPurgeDataTotalKeys(int purgeDataTotalKeys)
	{
		this.purgeDataTotalKeys = purgeDataTotalKeys;
	}
}