CREATE DATABASE  IF NOT EXISTS `shoppingappdb` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `shoppingappdb`;
-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: shoppingappdb
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `category_id` int NOT NULL AUTO_INCREMENT,
  `category_name` varchar(255) NOT NULL,
  `category_image_url` varchar(255) NOT NULL,
  `category_color` varchar(7) NOT NULL,
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (2,'Dairy Products','dairy_products','#FFEBEE'),(3,'Bread and Pastry','bread_and_pastry','#FFF3E0'),(4,'Fruits and Vegetables','fruits_and_vegetables','#E8F5E9'),(5,'Meat and Seafood','meat_and_seafood','#FFE0E0'),(6,'Beverages','beverages','#E1F5FE'),(7,'Snacks and Confectionery','snacks_and_confectionery','#FFF8E1'),(8,'Canned and Jarred Goods','canned_and_jarred_goods','#F5F5F5'),(9,'Condiments and Sauces','condiments_and_sauces','#FFF4E1'),(10,'Spices and Herbs','spices_and_herbs','#E0F7FA'),(11,'Baking Supplies','baking_supplies','#F3E5F5'),(12,'Personal Care','personal_care','#E0F2F1'),(13,'Household Supplies','household_supplies','#FFFDE7'),(14,'Cleaning Supplies','cleaning_supplies','#EDE7F6'),(15,'Health and Wellness','health_and_wellness','#EFEBE9'),(16,'Baby Products','baby_products','#FFE5D6'),(17,'Pet Supplies','pet_supplies','#FFEBEE'),(18,'Electronics','electronics','#F1F8E9'),(19,'Clothing and Accessories','clothing_and_accessories','#FFF8E1'),(20,'Beauty and Cosmetics','beauty_and_cosmetics','#FCE4EC'),(21,'Uncategorized','uncategorized','#FAFAFA');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `currencies`
--

DROP TABLE IF EXISTS `currencies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `currencies` (
  `currency_id` int NOT NULL AUTO_INCREMENT,
  `currency_name` varchar(50) NOT NULL,
  `currency_symbol` varchar(5) NOT NULL,
  `currency_conversion_factor` decimal(10,4) NOT NULL,
  PRIMARY KEY (`currency_id`),
  UNIQUE KEY `currency_name` (`currency_name`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `currencies`
--

LOCK TABLES `currencies` WRITE;
/*!40000 ALTER TABLE `currencies` DISABLE KEYS */;
INSERT INTO `currencies` VALUES (1,'USD','$',1.0000),(2,'EUR','€',0.9200),(3,'GBP','£',0.8100),(4,'JPY','¥',145.0000),(5,'AUD','A$',1.5000),(6,'CAD','C$',1.3500),(7,'CHF','CHF',0.9100),(8,'CNY','¥',7.3000),(9,'INR','₹',83.0000),(10,'BRL','R$',5.2000),(11,'ZAR','R',19.0000),(12,'NZD','NZ$',1.6000),(13,'MXN','$',17.2000),(14,'KRW','₩',1300.0000),(15,'SGD','S$',1.3600);
/*!40000 ALTER TABLE `currencies` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory`
--

DROP TABLE IF EXISTS `inventory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventory` (
  `inventory_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `inventory_name` varchar(255) NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`inventory_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `inventory_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory`
--

LOCK TABLES `inventory` WRITE;
/*!40000 ALTER TABLE `inventory` DISABLE KEYS */;
INSERT INTO `inventory` VALUES (1,1,'Kitchen Supplies','2024-11-09 13:30:55');
/*!40000 ALTER TABLE `inventory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory_item_history`
--

DROP TABLE IF EXISTS `inventory_item_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventory_item_history` (
  `history_id` int NOT NULL AUTO_INCREMENT,
  `item_id` int NOT NULL,
  `restock_history` datetime DEFAULT NULL,
  `price_history` double DEFAULT NULL,
  `quantity_history` double DEFAULT NULL,
  `priority_history` int DEFAULT NULL,
  PRIMARY KEY (`history_id`),
  KEY `item_id` (`item_id`),
  CONSTRAINT `inventory_item_history_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `inventoryitems` (`item_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory_item_history`
--

LOCK TABLES `inventory_item_history` WRITE;
/*!40000 ALTER TABLE `inventory_item_history` DISABLE KEYS */;
INSERT INTO `inventory_item_history` VALUES (38,364,'2024-12-10 10:34:36',1.2,1,8),(39,364,'2024-12-10 10:35:56',1.2,1,8),(40,367,'2024-12-10 19:00:09',6,1,3),(41,369,'2024-12-11 16:41:04',5,1,5),(42,364,'2024-12-11 16:45:39',1.2,1,8),(43,364,'2024-12-15 09:11:10',1.2,1,8),(49,364,'2024-12-18 17:46:12',1.2,1,8),(51,364,'2024-12-22 11:37:10',1.2,1,8),(52,364,'2024-12-27 10:27:28',1.2,1,8),(53,364,'2024-12-27 15:36:44',1.2,1,8),(54,369,'2024-12-27 16:38:44',5,1,5),(55,424,'2024-12-29 10:21:31',2,1,7),(56,364,'2024-12-31 10:48:35',1.2,1,8),(57,364,'2024-12-31 10:55:49',1.2,1,8),(58,364,'2024-12-31 10:55:49',1.2,1,8),(59,430,'2024-12-31 11:10:05',2,0.5,7),(60,364,'2024-12-31 10:44:45',1.2,1,8);
/*!40000 ALTER TABLE `inventory_item_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventoryitems`
--

DROP TABLE IF EXISTS `inventoryitems`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventoryitems` (
  `item_id` int NOT NULL AUTO_INCREMENT,
  `inventory_id` int NOT NULL,
  `item_name` varchar(255) NOT NULL,
  `quantity_stocked` double DEFAULT NULL,
  `quantity_unit` varchar(50) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `image_url` varchar(255) NOT NULL,
  `priority` int NOT NULL,
  `category_id` int NOT NULL,
  `stocked_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `restock_date` datetime DEFAULT NULL,
  `added_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `expiry_date` datetime DEFAULT NULL,
  `tags` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`item_id`),
  UNIQUE KEY `unique_item_name` (`item_name`),
  KEY `inventory_id` (`inventory_id`),
  KEY `category_id` (`category_id`),
  CONSTRAINT `inventoryitems_ibfk_1` FOREIGN KEY (`inventory_id`) REFERENCES `inventory` (`inventory_id`),
  CONSTRAINT `inventoryitems_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=431 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventoryitems`
--

LOCK TABLES `inventoryitems` WRITE;
/*!40000 ALTER TABLE `inventoryitems` DISABLE KEYS */;
INSERT INTO `inventoryitems` VALUES (364,1,'Milk',1,'lt',1.2,'milk',8,2,'2024-12-31 10:44:45','2025-01-03 22:44:45',NULL,NULL,''),(367,1,'eggs',1,'dozen',6,'eggs',3,5,'2024-12-10 19:00:09','2025-01-09 19:00:09','2024-12-10 20:59:58',NULL,NULL),(369,1,'apples',1,'kg',5,'apples',5,4,'2024-12-27 16:38:44','2025-01-11 16:38:44','2024-12-11 20:40:34',NULL,NULL),(424,1,'Orange Juice',1,'Lt',2,'Orange Juice',7,6,'2024-12-29 10:21:31',NULL,'2024-12-27 12:17:49',NULL,NULL),(430,1,'Ketchup',0.5,'Kg',2,'Ketchup',7,9,'2024-12-31 11:10:05',NULL,'2024-12-31 13:07:45',NULL,NULL);
/*!40000 ALTER TABLE `inventoryitems` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `predefined_items`
--

DROP TABLE IF EXISTS `predefined_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `predefined_items` (
  `item_id` int NOT NULL AUTO_INCREMENT,
  `category_id` int NOT NULL,
  `item_name` varchar(255) NOT NULL,
  `average_quantity` double DEFAULT NULL,
  `default_quantity_unit` varchar(50) NOT NULL,
  `average_price` double DEFAULT NULL,
  `default_currency_id` int DEFAULT NULL,
  `image_url` text NOT NULL,
  `average_priority` int NOT NULL,
  `average_consumption_rate` double DEFAULT NULL,
  `default_consumption_unit` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`item_id`),
  KEY `fk_default_currency` (`default_currency_id`),
  CONSTRAINT `fk_default_currency` FOREIGN KEY (`default_currency_id`) REFERENCES `currencies` (`currency_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `predefined_items`
--

LOCK TABLES `predefined_items` WRITE;
/*!40000 ALTER TABLE `predefined_items` DISABLE KEYS */;
INSERT INTO `predefined_items` VALUES (6,2,'Milk',1,'Lt',1.2,2,'Milk',10,2.12,'lt per week'),(7,2,'Cheese',0.5,'Kg',5,2,'Cheese',8,0.8,'kg per week'),(8,3,'White Bread',1,'Loaf',1.5,2,'White Bread',9,1,'loaf per week'),(9,3,'Croissant',4,'Piece',0.8,2,'Croissant',7,4,'pieces per week'),(10,4,'Bananas',1,'Kg',1,2,'Bananas',6,2,'kg per week'),(11,4,'Tomatoes',1.5,'Kg',2.5,2,'Tomatoes',5,1.5,'kg per week'),(12,5,'Chicken Breast',1,'Kg',4,2,'Chicken Breast',10,1,'kg per week'),(13,5,'Salmon Fillet',0.5,'Kg',7.5,2,'Salmon Fillet',9,0.5,'kg per week'),(14,6,'Orange Juice',1,'Lt',2,2,'Orange Juice',7,1,'lt per week'),(15,6,'Cola',1.5,'Lt',1.5,2,'Cola',6,1.5,'lt per week'),(16,7,'Potato Chips',0.2,'Kg',2.5,2,'Potato Chips',8,0.5,'kg per week'),(17,7,'Chocolate Bar',1,'Piece',1.2,2,'Chocolate Bar',7,4,'pieces per week'),(18,8,'Canned Beans',0.5,'Kg',1.8,2,'Canned Beans',5,1,'kg per week'),(19,8,'Peanut Butter',0.5,'Kg',3,2,'Peanut Butter',6,0.5,'kg per week'),(20,9,'Ketchup',0.5,'Kg',2,2,'Ketchup',7,0.5,'kg per week'),(21,9,'Mayonnaise',0.5,'Kg',2.5,2,'Mayonnaise',6,0.5,'kg per week'),(22,10,'Salt',1,'Kg',1,2,'Salt',8,1,'kg per month'),(23,10,'Black Pepper',0.1,'Kg',3,2,'Black Pepper',8,0.1,'kg per month'),(24,11,'Flour',2,'Kg',1.2,2,'Flour',9,2,'kg per week'),(25,11,'Baking Powder',0.2,'Kg',0.8,2,'Baking Powder',7,0.2,'kg per month');
/*!40000 ALTER TABLE `predefined_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `recommendation_items`
--

DROP TABLE IF EXISTS `recommendation_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recommendation_items` (
  `item_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `item_name` varchar(255) NOT NULL,
  `quantity` double DEFAULT NULL,
  `quantity_unit` varchar(255) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `image_url` varchar(255) NOT NULL,
  `priority` int NOT NULL,
  `frequency_value` int DEFAULT NULL,
  `frequency_unit` varchar(255) DEFAULT NULL,
  `category_id` int NOT NULL,
  `recommendation_msg` varchar(255) NOT NULL,
  `currency_id` int DEFAULT NULL,
  PRIMARY KEY (`item_id`),
  UNIQUE KEY `item_name` (`item_name`),
  KEY `fk_currency` (`currency_id`),
  CONSTRAINT `fk_currency` FOREIGN KEY (`currency_id`) REFERENCES `currencies` (`currency_id`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recommendation_items`
--

LOCK TABLES `recommendation_items` WRITE;
/*!40000 ALTER TABLE `recommendation_items` DISABLE KEYS */;
INSERT INTO `recommendation_items` VALUES (44,1,'Cheese',0.5,'Kg',5,'Cheese',8,NULL,NULL,2,'Based on your purchase history',NULL),(48,1,'Potato Chips',0.2,'Kg',2.5,'Potato Chips',8,NULL,NULL,7,'Based on your purchase history',NULL);
/*!40000 ALTER TABLE `recommendation_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shoppingitems`
--

DROP TABLE IF EXISTS `shoppingitems`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shoppingitems` (
  `item_id` int NOT NULL AUTO_INCREMENT,
  `list_id` int DEFAULT NULL,
  `item_name` varchar(255) NOT NULL,
  `quantity` double DEFAULT NULL,
  `quantity_unit` varchar(50) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `image_url` varchar(255) NOT NULL,
  `priority` int NOT NULL,
  `frequency_value` int DEFAULT NULL,
  `frequency_unit` varchar(50) DEFAULT NULL,
  `category_id` int NOT NULL,
  `laststocked_at` datetime DEFAULT NULL,
  `added_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `purchased_at` datetime DEFAULT NULL,
  `tags` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`item_id`),
  UNIQUE KEY `unique_item_name` (`item_name`),
  KEY `list_id` (`list_id`),
  KEY `category_id` (`category_id`),
  CONSTRAINT `shoppingitems_ibfk_1` FOREIGN KEY (`list_id`) REFERENCES `shoppinglists` (`list_id`),
  CONSTRAINT `shoppingitems_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=438 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shoppingitems`
--

LOCK TABLES `shoppingitems` WRITE;
/*!40000 ALTER TABLE `shoppingitems` DISABLE KEYS */;
INSERT INTO `shoppingitems` VALUES (437,1,'Tomatoes',1.5,'kg',1,'tomatoes',1,4,'Month',4,NULL,'2025-01-02 11:05:54',NULL,NULL);
/*!40000 ALTER TABLE `shoppingitems` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shoppinglists`
--

DROP TABLE IF EXISTS `shoppinglists`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shoppinglists` (
  `list_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `list_name` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`list_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `shoppinglists_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shoppinglists`
--

LOCK TABLES `shoppinglists` WRITE;
/*!40000 ALTER TABLE `shoppinglists` DISABLE KEYS */;
INSERT INTO `shoppinglists` VALUES (1,1,'Groceries','2024-11-08 11:39:31');
/*!40000 ALTER TABLE `shoppinglists` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `user_name` varchar(255) NOT NULL,
  `user_email` varchar(255) NOT NULL,
  `user_password` varchar(255) NOT NULL,
  `currency_id` int DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `user_email` (`user_email`),
  KEY `fk_currency_id` (`currency_id`),
  CONSTRAINT `fk_currency_id` FOREIGN KEY (`currency_id`) REFERENCES `currencies` (`currency_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Osama','oduwairi@gmail.com','sample',1);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-01-02 11:10:49
