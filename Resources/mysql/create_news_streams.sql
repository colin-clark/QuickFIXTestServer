--
-- Create DJHeadlines stream and schema
--
INSERT INTO `QUERY_ENTITY` VALUES 
('DJHeadlines', 'DJHeadlines', 'DJHeadlines', 'DJHeadlines', 'MAP');

INSERT INTO `QUERY_ENTITY_FIELDS` VALUES 
('headline', 'text', 50, 'String', 'DJHeadlines', 'Story headline'),
('source', 'text', 50, 'String', 'DJHeadlines', 'Source of the story'),
('guid', 'text', 50, 'String', 'DJHeadlines', 'row key of the story');

--
-- Create CNNHeadlines stream and schema
--
INSERT INTO `QUERY_ENTITY` VALUES 
('CNNHeadlines', 'CNNHeadlines', 'CNNHeadlines', 'CNNHeadlines', 'MAP');

INSERT INTO `QUERY_ENTITY_FIELDS` VALUES 
('headline', 'text', 50, 'String', 'CNNHeadlines', 'Story headline'),
('source', 'text', 50, 'String', 'CNNHeadlines', 'Source of the story'),
('guid', 'text', 50, 'String', 'CNNHeadlines', 'row key of the story');

--
-- Create named_entities stream and schema
--
INSERT INTO `QUERY_ENTITY` VALUES 
('named_entities', 'Named Entities', 'named_entities', 'named_entities', 'MAP');

INSERT INTO `QUERY_ENTITY_FIELDS` VALUES 
('type', 'text', 50, 'String', 'named_entities', 'type of field'),
('value', 'text', 50, 'String', 'named_entities', 'value of field'),
('guid', 'text', 50, 'String', 'named_entities', 'guid of story');



