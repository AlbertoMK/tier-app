USE fit_hub;

DROP TABLE IF EXISTS `friend_request`;

CREATE TABLE `friend_request` (
  `username_request` varchar(45) DEFAULT NULL,
  `username_requested` varchar(45) DEFAULT NULL,
  `request_date` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


LOCK TABLES `friend_request` WRITE;

UNLOCK TABLES;

