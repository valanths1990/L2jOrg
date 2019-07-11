package org.l2j.gameserver.util;

import org.l2j.commons.database.DatabaseFactory;
import org.l2j.gameserver.Config;
import org.l2j.commons.threading.ThreadPoolManager;
import org.l2j.gameserver.data.xml.impl.NpcData;
import org.l2j.gameserver.data.xml.impl.PetDataTable;
import org.l2j.gameserver.model.PetData;
import org.l2j.gameserver.model.actor.Npc;
import org.l2j.gameserver.model.actor.Summon;
import org.l2j.gameserver.model.actor.instance.Pet;
import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.model.actor.templates.NpcTemplate;
import org.l2j.gameserver.model.items.instance.Item;
import org.l2j.gameserver.network.SystemMessageId;
import org.l2j.gameserver.network.serverpackets.InventoryUpdate;
import org.l2j.gameserver.network.serverpackets.MagicSkillLaunched;
import org.l2j.gameserver.network.serverpackets.MagicSkillUse;
import org.l2j.gameserver.network.serverpackets.SystemMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;


/**
 * UnAfraid: TODO: MOVE IT TO DP AI
 */
public final class Evolve {
    protected static final Logger LOGGER = LoggerFactory.getLogger(Evolve.class);

    public static boolean doEvolve(Player player, Npc npc, int itemIdtake, int itemIdgive, int petminlvl) {
        if ((itemIdtake == 0) || (itemIdgive == 0) || (petminlvl == 0)) {
            return false;
        }

        final Summon pet = player.getPet();
        if (pet == null) {
            return false;
        }

        final Pet currentPet = (Pet) pet;
        if (currentPet.isAlikeDead()) {
            GameUtils.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to use death pet exploit!", Config.DEFAULT_PUNISH);
            return false;
        }

        Item item = null;
        long petexp = currentPet.getStat().getExp();
        final String oldname = currentPet.getName();
        final int oldX = currentPet.getX();
        final int oldY = currentPet.getY();
        final int oldZ = currentPet.getZ();

        final PetData oldData = PetDataTable.getInstance().getPetDataByItemId(itemIdtake);

        if (oldData == null) {
            return false;
        }

        final int oldnpcID = oldData.getNpcId();

        if ((currentPet.getStat().getLevel() < petminlvl) || (currentPet.getId() != oldnpcID)) {
            return false;
        }

        final PetData petData = PetDataTable.getInstance().getPetDataByItemId(itemIdgive);

        if (petData == null) {
            return false;
        }

        final int npcID = petData.getNpcId();

        if (npcID == 0) {
            return false;
        }

        final NpcTemplate npcTemplate = NpcData.getInstance().getTemplate(npcID);

        currentPet.unSummon(player);

        // deleting old pet item
        currentPet.destroyControlItem(player, true);

        item = player.getInventory().addItem("Evolve", itemIdgive, 1, player, npc);

        // Summoning new pet
        final Pet petSummon = Pet.spawnPet(npcTemplate, player, item);

        if (petSummon == null) {
            return false;
        }

        // Fix for non-linear baby pet exp
        final long _minimumexp = petSummon.getStat().getExpForLevel(petminlvl);
        if (petexp < _minimumexp) {
            petexp = _minimumexp;
        }

        petSummon.getStat().addExp(petexp);
        petSummon.setCurrentHp(petSummon.getMaxHp());
        petSummon.setCurrentMp(petSummon.getMaxMp());
        petSummon.setCurrentFed(petSummon.getMaxFed());
        petSummon.setTitle(player.getName());
        petSummon.setName(oldname);
        petSummon.setRunning();
        petSummon.storeMe();

        player.setPet(petSummon);

        player.sendPacket(new MagicSkillUse(npc, 2046, 1, 1000, 600000));
        player.sendPacket(SystemMessageId.SUMMONING_YOUR_PET);
        petSummon.spawnMe(oldX, oldY, oldZ);
        petSummon.startFeed();
        item.setEnchantLevel(petSummon.getLevel());

        ThreadPoolManager.getInstance().schedule(new EvolveFinalizer(player, petSummon), 900);

        if (petSummon.getCurrentFed() <= 0) {
            ThreadPoolManager.getInstance().schedule(new EvolveFeedWait(player, petSummon), 60000);
        } else {
            petSummon.startFeed();
        }

        return true;
    }

    public static boolean doRestore(Player player, Npc npc, int itemIdtake, int itemIdgive, int petminlvl) {
        if ((itemIdtake == 0) || (itemIdgive == 0) || (petminlvl == 0)) {
            return false;
        }

        final Item item = player.getInventory().getItemByItemId(itemIdtake);
        if (item == null) {
            return false;
        }

        int oldpetlvl = item.getEnchantLevel();
        if (oldpetlvl < petminlvl) {
            oldpetlvl = petminlvl;
        }

        final PetData oldData = PetDataTable.getInstance().getPetDataByItemId(itemIdtake);
        if (oldData == null) {
            return false;
        }

        final PetData petData = PetDataTable.getInstance().getPetDataByItemId(itemIdgive);
        if (petData == null) {
            return false;
        }

        final int npcId = petData.getNpcId();
        if (npcId == 0) {
            return false;
        }

        final NpcTemplate npcTemplate = NpcData.getInstance().getTemplate(npcId);

        // deleting old pet item
        final Item removedItem = player.getInventory().destroyItem("PetRestore", item, player, npc);
        final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
        sm.addItemName(removedItem);
        player.sendPacket(sm);

        // Give new pet item
        final Item addedItem = player.getInventory().addItem("PetRestore", itemIdgive, 1, player, npc);

        // Summoning new pet
        final Pet petSummon = Pet.spawnPet(npcTemplate, player, addedItem);
        if (petSummon == null) {
            return false;
        }

        final long _maxexp = petSummon.getStat().getExpForLevel(oldpetlvl);

        petSummon.getStat().addExp(_maxexp);
        petSummon.setCurrentHp(petSummon.getMaxHp());
        petSummon.setCurrentMp(petSummon.getMaxMp());
        petSummon.setCurrentFed(petSummon.getMaxFed());
        petSummon.setTitle(player.getName());
        petSummon.setRunning();
        petSummon.storeMe();

        player.setPet(petSummon);

        player.sendPacket(new MagicSkillUse(npc, 2046, 1, 1000, 600000));
        player.sendPacket(SystemMessageId.SUMMONING_YOUR_PET);
        petSummon.spawnMe(player.getX(), player.getY(), player.getZ());
        petSummon.startFeed();
        addedItem.setEnchantLevel(petSummon.getLevel());

        // Inventory update
        final InventoryUpdate iu = new InventoryUpdate();
        iu.addRemovedItem(removedItem);
        player.sendInventoryUpdate(iu);

        player.broadcastUserInfo();

        ThreadPoolManager.getInstance().schedule(new EvolveFinalizer(player, petSummon), 900);

        if (petSummon.getCurrentFed() <= 0) {
            ThreadPoolManager.getInstance().schedule(new EvolveFeedWait(player, petSummon), 60000);
        } else {
            petSummon.startFeed();
        }

        // pet control item no longer exists, delete the pet from the db
        try (Connection con = DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?")) {
            ps.setInt(1, removedItem.getObjectId());
            ps.execute();
        } catch (Exception e) {
        }
        return true;
    }

    static final class EvolveFeedWait implements Runnable {
        private final Player _activeChar;
        private final Pet _petSummon;

        EvolveFeedWait(Player activeChar, Pet petSummon) {
            _activeChar = activeChar;
            _petSummon = petSummon;
        }

        @Override
        public void run() {
            try {
                if (_petSummon.getCurrentFed() <= 0) {
                    _petSummon.unSummon(_activeChar);
                } else {
                    _petSummon.startFeed();
                }
            } catch (Exception e) {
                LOGGER.warn("", e);
            }
        }
    }

    static final class EvolveFinalizer implements Runnable {
        private final Player _activeChar;
        private final Pet _petSummon;

        EvolveFinalizer(Player activeChar, Pet petSummon) {
            _activeChar = activeChar;
            _petSummon = petSummon;
        }

        @Override
        public void run() {
            try {
                _activeChar.sendPacket(new MagicSkillLaunched(_activeChar, 2046, 1));
                _petSummon.setFollowStatus(true);
                _petSummon.setShowSummonAnimation(false);
            } catch (Throwable e) {
                LOGGER.warn("", e);
            }
        }
    }
}
