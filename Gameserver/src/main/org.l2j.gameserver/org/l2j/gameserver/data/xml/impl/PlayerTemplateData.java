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
package org.l2j.gameserver.data.xml.impl;

import org.l2j.gameserver.model.Location;
import org.l2j.gameserver.model.StatsSet;
import org.l2j.gameserver.model.actor.templates.PlayerTemplate;
import org.l2j.gameserver.model.base.ClassId;
import org.l2j.gameserver.settings.ServerSettings;
import org.l2j.gameserver.util.GameXmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Loads player's base stats.
 *
 * @author Forsaiken, Zoey76, GKR
 */
public final class PlayerTemplateData extends GameXmlReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerTemplateData.class);
    public static final int MAX_LEVEL = 85;

    private final Map<ClassId, PlayerTemplate> _playerTemplates = new EnumMap<>(ClassId.class);

    private int _dataCount = 0;
    private int _autoGeneratedCount = 0;

    private PlayerTemplateData() {
        load();
    }

    @Override
    protected Path getSchemaFilePath() {
        return ServerSettings.dataPackDirectory().resolve("data/xsd/charTemplate.xsd");
    }

    @Override
    public void load() {
        _playerTemplates.clear();
        parseDatapackDirectory("data/stats/chars/baseStats", false);
        LOGGER.info("Loaded {} character templates.", _playerTemplates.size());
        LOGGER.info("Loaded {} level up gain records.", _dataCount );
        if (_autoGeneratedCount > 0)
        {
            LOGGER.info("Generated {} level up gain records.", _autoGeneratedCount);
        }
        releaseResources();
    }

    @Override
    public void parseDocument(Document doc, File f) {
        NamedNodeMap attrs;
        int classId = 0;

        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("list".equalsIgnoreCase(n.getNodeName())) {
                for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                    if ("classId".equalsIgnoreCase(d.getNodeName())) {
                        classId = Integer.parseInt(d.getTextContent());
                    } else if ("staticData".equalsIgnoreCase(d.getNodeName())) {
                        final StatsSet set = new StatsSet();
                        set.set("classId", classId);
                        final List<Location> creationPoints = new ArrayList<>();

                        for (Node nd = d.getFirstChild(); nd != null; nd = nd.getNextSibling()) {
                            // Skip odd nodes
                            if (nd.getNodeName().equals("#text")) {
                                continue;
                            }

                            if (nd.getChildNodes().getLength() > 1) {
                                for (Node cnd = nd.getFirstChild(); cnd != null; cnd = cnd.getNextSibling()) {
                                    // use CreatureTemplate(superclass) fields for male collision height and collision radius
                                    if (nd.getNodeName().equalsIgnoreCase("collisionMale")) {
                                        if (cnd.getNodeName().equalsIgnoreCase("radius")) {
                                            set.set("collision_radius", cnd.getTextContent());
                                        } else if (cnd.getNodeName().equalsIgnoreCase("height")) {
                                            set.set("collision_height", cnd.getTextContent());
                                        }
                                    }
                                    if ("node".equalsIgnoreCase(cnd.getNodeName())) {
                                        attrs = cnd.getAttributes();
                                        creationPoints.add(new Location(parseInt(attrs, "x"), parseInt(attrs, "y"), parseInt(attrs, "z")));
                                    } else if ("walk".equalsIgnoreCase(cnd.getNodeName())) {
                                        set.set("baseWalkSpd", cnd.getTextContent());
                                    } else if ("run".equalsIgnoreCase(cnd.getNodeName())) {
                                        set.set("baseRunSpd", cnd.getTextContent());
                                    } else if ("slowSwim".equals(cnd.getNodeName())) {
                                        set.set("baseSwimWalkSpd", cnd.getTextContent());
                                    } else if ("fastSwim".equals(cnd.getNodeName())) {
                                        set.set("baseSwimRunSpd", cnd.getTextContent());
                                    } else if (!cnd.getNodeName().equals("#text")) {
                                        set.set((nd.getNodeName() + cnd.getNodeName()), cnd.getTextContent());
                                    }
                                }
                            } else {
                                set.set(nd.getNodeName(), nd.getTextContent());
                            }
                        }
                        // calculate total pdef and mdef from parts
                        set.set("basePDef", (set.getInt("basePDefchest", 0) + set.getInt("basePDeflegs", 0) + set.getInt("basePDefhead", 0) + set.getInt("basePDeffeet", 0) + set.getInt("basePDefgloves", 0) + set.getInt("basePDefunderwear", 0) + set.getInt("basePDefcloak", 0)));
                        set.set("baseMDef", (set.getInt("baseMDefrear", 0) + set.getInt("baseMDeflear", 0) + set.getInt("baseMDefrfinger", 0) + set.getInt("baseMDefrfinger", 0) + set.getInt("baseMDefneck", 0)));

                        _playerTemplates.put(ClassId.getClassId(classId), new PlayerTemplate(set, creationPoints));
                    } else if ("lvlUpgainData".equalsIgnoreCase(d.getNodeName())) {
                        int level = 0;
                        for (Node lvlNode = d.getFirstChild(); lvlNode != null; lvlNode = lvlNode.getNextSibling()) {

                            if ("level".equalsIgnoreCase(lvlNode.getNodeName())) {
                                attrs = lvlNode.getAttributes();
                                level = parseInt(attrs, "val");

                                var maxLevel = LevelData.getInstance().getMaxLevel();
                                for (Node valNode = lvlNode.getFirstChild(); valNode != null; valNode = valNode.getNextSibling()) {
                                    final String nodeName = valNode.getNodeName();

                                    if (level <= maxLevel && (nodeName.startsWith("hp") || nodeName.startsWith("mp") || nodeName.startsWith("cp")) && _playerTemplates.containsKey(ClassId.getClassId(classId))) {
                                        _playerTemplates.get(ClassId.getClassId(classId)).setUpgainValue(nodeName, level, Double.parseDouble(valNode.getTextContent()));
                                        _dataCount++;
                                    }
                                }
                            }
                        }
                        // Generate missing stats automatically.
                        while (level < MAX_LEVEL)
                        {
                            level++;
                            _autoGeneratedCount++;
                            final double hpM1 = _playerTemplates.get(ClassId.getClassId(classId)).getBaseHpMax(level - 1);
                            _playerTemplates.get(ClassId.getClassId(classId)).setUpgainValue("hp", level, (((hpM1 * level) / (level - 1)) + ((hpM1 * (level + 1)) / (level - 1))) / 2);
                            final double mpM1 = _playerTemplates.get(ClassId.getClassId(classId)).getBaseMpMax(level - 1);
                            _playerTemplates.get(ClassId.getClassId(classId)).setUpgainValue("mp", level, (((mpM1 * level) / (level - 1)) + ((mpM1 * (level + 1)) / (level - 1))) / 2);
                            final double cpM1 = _playerTemplates.get(ClassId.getClassId(classId)).getBaseCpMax(level - 1);
                            _playerTemplates.get(ClassId.getClassId(classId)).setUpgainValue("cp", level, (((cpM1 * level) / (level - 1)) + ((cpM1 * (level + 1)) / (level - 1))) / 2);
                            final double hpRegM1 = _playerTemplates.get(ClassId.getClassId(classId)).getBaseHpRegen(level - 1);
                            final double hpRegM2 = _playerTemplates.get(ClassId.getClassId(classId)).getBaseHpRegen(level - 2);
                            _playerTemplates.get(ClassId.getClassId(classId)).setUpgainValue("hpRegen", level, (hpRegM1 * 2) - hpRegM2);
                            final double mpRegM1 = _playerTemplates.get(ClassId.getClassId(classId)).getBaseMpRegen(level - 1);
                            final double mpRegM2 = _playerTemplates.get(ClassId.getClassId(classId)).getBaseMpRegen(level - 2);
                            _playerTemplates.get(ClassId.getClassId(classId)).setUpgainValue("mpRegen", level, (mpRegM1 * 2) - mpRegM2);
                            final double cpRegM1 = _playerTemplates.get(ClassId.getClassId(classId)).getBaseCpRegen(level - 1);
                            final double cpRegM2 = _playerTemplates.get(ClassId.getClassId(classId)).getBaseCpRegen(level - 2);
                            _playerTemplates.get(ClassId.getClassId(classId)).setUpgainValue("cpRegen", level, (cpRegM1 * 2) - cpRegM2);
                        }
                    }
                }
            }
        }
    }

    public PlayerTemplate getTemplate(int classId) {
        return _playerTemplates.get(ClassId.getClassId(classId));
    }

    public static PlayerTemplateData getInstance() {
        return Singleton.INSTANCE;
    }

    private static class Singleton {
        private static final PlayerTemplateData INSTANCE = new PlayerTemplateData();
    }
}
