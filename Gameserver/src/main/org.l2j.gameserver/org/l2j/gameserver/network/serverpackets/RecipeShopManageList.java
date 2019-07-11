package org.l2j.gameserver.network.serverpackets;

import org.l2j.gameserver.model.ManufactureItem;
import org.l2j.gameserver.model.RecipeList;
import org.l2j.gameserver.model.actor.instance.Player;
import org.l2j.gameserver.network.GameClient;
import org.l2j.gameserver.network.ServerPacketId;

import java.util.Iterator;

public class RecipeShopManageList extends ServerPacket {
    private final Player _seller;
    private final boolean _isDwarven;
    private RecipeList[] _recipes;

    public RecipeShopManageList(Player seller, boolean isDwarven) {
        _seller = seller;
        _isDwarven = isDwarven;

        if (_isDwarven && _seller.hasDwarvenCraft()) {
            _recipes = _seller.getDwarvenRecipeBook();
        } else {
            _recipes = _seller.getCommonRecipeBook();
        }

        if (_seller.hasManufactureShop()) {
            final Iterator<ManufactureItem> it = _seller.getManufactureItems().values().iterator();
            ManufactureItem item;
            while (it.hasNext()) {
                item = it.next();
                if ((item.isDwarven() != _isDwarven) || !seller.hasRecipeList(item.getRecipeId())) {
                    it.remove();
                }
            }
        }
    }

    @Override
    public void writeImpl(GameClient client) {
        writeId(ServerPacketId.RECIPE_SHOP_MANAGE_LIST);

        writeInt(_seller.getObjectId());
        writeInt((int) _seller.getAdena());
        writeInt(_isDwarven ? 0x00 : 0x01);

        if (_recipes == null) {
            writeInt(0);
        } else {
            writeInt(_recipes.length); // number of items in recipe book

            for (int i = 0; i < _recipes.length; i++) {
                final RecipeList temp = _recipes[i];
                writeInt(temp.getId());
                writeInt(i + 1);
            }
        }

        if (!_seller.hasManufactureShop()) {
            writeInt(0x00);
        } else {
            writeInt(_seller.getManufactureItems().size());
            for (ManufactureItem item : _seller.getManufactureItems().values()) {
                writeInt(item.getRecipeId());
                writeInt(0x00);
                writeLong(item.getCost());
            }
        }
    }

}
