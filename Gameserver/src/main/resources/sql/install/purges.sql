DROP TABLE IF EXISTS purges;

CREATE TABLE IF NOT EXISTS purges
(
    `purgeId`               INT  UNSIGNED    NOT NULL,
    `playerId`              INT  UNSIGNED    NOT NULL,
    `purgeDataPoints`       INT  UNSIGNED    NOT NULL    DEFAULT '0',
    `purgeDataCurrentKeys`  INT  UNSIGNED    NOT NULL    DEFAULT '0',
    `purgeDataTotalKeys`    INT  UNSIGNED    NOT NULL    DEFAULT '0',

    PRIMARY KEY (`purgeId`, `playerId`),
    CONSTRAINT purges_purgeId_playerId_uindex
    UNIQUE KEY (purgeId, playerId)
)
ENGINE=InnoDB DEFAULT CHARSET=UTF8MB4;