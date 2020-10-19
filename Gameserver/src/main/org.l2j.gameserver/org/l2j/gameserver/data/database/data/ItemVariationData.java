/*
 * Copyright © 2019-2020 L2JOrg
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
 * @author JoeAlisson
 */
@Table("item_variations")
public class ItemVariationData {

    private int itemId;
    private int mineralId;
    private int option1;
    private int option2;

    public int getMineralId() {
        return mineralId;
    }

    public int getOption1() {
        return option1;
    }

    public int getOption2() {
        return option1;
    }

    public static ItemVariationData of(int itemObjectId, int mineralId, int option1Id, int option2Id) {
        var data = new ItemVariationData();
        data.itemId = itemObjectId;
        data.mineralId = mineralId;
        data.option1 = option1Id;
        data.option2 = option2Id;
        return data;
    }
}