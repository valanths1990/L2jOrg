module org.l2j.gameserver {
    requires org.l2j.commons;

    requires java.sql;
    requires java.desktop;
    requires org.slf4j;
    requires dom4j;
    requires io.github.joealisson.mmocore;
    requires primitive;
    requires cache.api;
    requires jdk.unsupported;

    exports org.l2j.gameserver;
    exports org.l2j.gameserver.model.base;
    exports org.l2j.gameserver.model.instances;
    exports org.l2j.gameserver.model.quest;
    exports org.l2j.gameserver.model;
    exports org.l2j.gameserver.model.items;
    exports org.l2j.gameserver.utils;
    exports org.l2j.gameserver.network.l2.s2c;
    exports org.l2j.gameserver.model.actor.listener;
    exports org.l2j.gameserver.listener.actor.player;
    exports org.l2j.gameserver.network.l2.components;
    exports org.l2j.gameserver.ai;
    exports org.l2j.gameserver.data.xml.holder;
    exports org.l2j.gameserver.data;
    exports org.l2j.gameserver.instancemanager;
    exports org.l2j.gameserver.model.entity.residence;
    exports org.l2j.gameserver.handler.usercommands;
    exports org.l2j.gameserver.listener.script;
    exports org.l2j.gameserver.handler.petition;
    exports org.l2j.gameserver.data.htm;
    exports org.l2j.gameserver.handler.bbs;
    exports org.l2j.gameserver.model.entity.events.impl;
    exports org.l2j.gameserver.model.entity.olympiad;
    exports org.l2j.gameserver.templates.item;
    exports org.l2j.gameserver.handler.voicecommands;
    exports org.l2j.gameserver.model.pledge;
    exports org.l2j.gameserver.network.authcomm;
    exports org.l2j.gameserver.tables;
    exports org.l2j.gameserver.templates.item.data;
    exports org.l2j.gameserver.templates.premiumaccount;
    exports org.l2j.gameserver.model.actor.instances.player;
    exports org.l2j.gameserver.model.entity;
    exports org.l2j.gameserver.model.actor.instances.creature;
    exports org.l2j.gameserver.skills;
    exports org.l2j.gameserver.handler.onshiftaction;
    exports org.l2j.gameserver.handler.bypass;
    exports org.l2j.gameserver.model.entity.events;
    exports org.l2j.gameserver.model.reward;
    exports org.l2j.gameserver.stats;
    exports org.l2j.gameserver.templates.dailymissions;
    exports org.l2j.gameserver.listener;
    exports org.l2j.gameserver.listener.actor;
    exports org.l2j.gameserver.skills.effects;
    exports org.l2j.gameserver.templates.skill;
    exports org.l2j.gameserver.network.authcomm.gs2as;
    exports org.l2j.gameserver.network.l2;
    exports org.l2j.gameserver.handler.admincommands;
    exports org.l2j.gameserver.model.entity.events.objects;
    exports org.l2j.gameserver.handler.admincommands.impl;
    exports org.l2j.gameserver.handler.items;
    exports org.l2j.gameserver.handler.items.impl;
    exports org.l2j.gameserver.handler.dailymissions.impl;
    exports org.l2j.gameserver.handler.dailymissions;
    exports org.l2j.gameserver.data.string;
    exports org.l2j.gameserver.templates.npc;
    exports org.l2j.gameserver.templates.residence;
    exports org.l2j.gameserver.model.entity.residence.clanhall;
    exports org.l2j.gameserver.templates;
    exports org.l2j.gameserver.listener.hooks;
    exports org.l2j.gameserver.geodata;
    exports org.l2j.gameserver.model.instances.residences;
    exports org.l2j.gameserver.skills.skillclasses;
    exports org.l2j.gameserver.network.l2.c2s;
    exports org.l2j.gameserver.model.entity.events.actions;
    exports org.l2j.gameserver.model.actor.basestats;
    exports org.l2j.gameserver.model.actor.flags;
    exports org.l2j.gameserver.templates.player;
    exports org.l2j.gameserver.model.actor.flags.flag;


    exports org.l2j.gameserver.settings;
    exports org.l2j.gameserver.data.dao;
    exports org.l2j.gameserver.data.database;

    opens org.l2j.gameserver.data.dao to org.l2j.commons;
    opens org.l2j.gameserver.settings to org.l2j.commons;
}