USE fit_hub;

DROP TABLE IF EXISTS `friends`;

CREATE TABLE `friends` (
  `username1` varchar(45) DEFAULT NULL,
  `username2` varchar(45) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

LOCK TABLES `friends` WRITE;

UNLOCK TABLES;
