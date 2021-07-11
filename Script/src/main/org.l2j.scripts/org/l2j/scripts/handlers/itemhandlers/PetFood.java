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
package org.l2j.scripts.handlers.itemhandlers;

import org.l2j.gameserver.data.xml.impl.PetDataTable;
import org.l2j.gameserver.engine.item.Item;
import org.l2j.gameserver.engine.skill.api.Skill;
import org.l2j.gameserver.engine.skill.api.SkillEngine;
import org.l2j.gameserver.enums.ItemSkillType;
import org.l2j.gameserver.handler.IItemHandler;
import org.l2j.gameserver.model.actor.Playable;
import org.l2j.gameserver.model.actor.instance.Pet;
import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.network.SystemMessageId;
import org.l2j.gameserver.network.serverpackets.MagicSkillUse;
import org.l2j.gameserver.network.serverpackets.SystemMessage;

import static org.l2j.gameserver.util.GameUtils.isPet;

/**
 * @author Kerberos, Zoey76
 */
public class PetFood implements IItemHandler
{
	@Override
	public boolean useItem(Playable playable, Item item, boolean forceUse)
	{
		if (isPet(playable) && !((Pet) playable).canEatFoodId(item.getId()))
		{
			playable.sendPacket(SystemMessageId.THIS_PET_CANNOT_USE_THIS_ITEM);
			return false;
		}

		item.forEachSkill(ItemSkillType.NORMAL, s -> useFood(playable, s.getSkillId(), s.getLevel(), item));
		return true;
	}
	
	private void useFood(Playable playable, int skillId, int skillLevel, Item item) {
		final Skill skill = SkillEngine.getInstance().getSkill(skillId, skillLevel);
		if (skill != null) {
			if (playable instanceof Pet pet) {
				consumeItem(skillId, skillLevel, item, skill, pet);
			} else if (playable instanceof Player player) {
				feedPet(skillId, skillLevel, item, skill, player);
			}
		}
	}

	private void feedPet(int skillId, int skillLevel, Item item, Skill skill, Player player) {
		if (player.isMounted()) {
			var foodIds = PetDataTable.getInstance().getPetTemplate(player.getMountNpcId()).getFood();
			if (foodIds.contains(item.getId()) && player.destroyItem("Consume", item.getObjectId(), 1, null, false)) {
				player.broadcastPacket(new MagicSkillUse(player, player, skillId, skillLevel, 0, 0));
				skill.applyEffects(player, player);
			}
		} else {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS));
		}
	}

	private void consumeItem(int skillId, int skillLevel, Item item, Skill skill, Pet pet) {
		if (pet.destroyItem("Consume", item.getObjectId(), 1, null, false)) {
			pet.broadcastPacket(new MagicSkillUse(pet, pet, skillId, skillLevel, 0, 0));
			skill.applyEffects(pet, pet);
			pet.broadcastStatusUpdate();
			if (pet.isHungry()) {
				pet.sendPacket(SystemMessageId.YOUR_PET_ATE_A_LITTLE_BUT_IS_STILL_HUNGRY);
			}
		}
	}
}