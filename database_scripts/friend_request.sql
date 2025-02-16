USE fit_hub;

DROP TABLE IF EXISTS `friend_request`;

CREATE TABLE `friend_request` (
  `requester` varchar(45) NOT NULL,
  `requested` varchar(45) NOT NULL,
  `date` timestamp NOT NULL,
  PRIMARY KEY (`requester`, `requested`)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


LOCK TABLES `friend_request` WRITE;

UNLOCK TABLES;

