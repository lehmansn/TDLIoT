package de.uni.stuttgart.ipvs.tdl.rest;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.google.common.collect.Lists;
import com.mongodb.util.JSON;
import de.uni.stuttgart.ipvs.tdl.database.MongoDBConnector;
import de.uni.stuttgart.ipvs.tdl.rest.validation.TopicDescriptionValidator;
import net.minidev.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@CrossOrigin
@RestController
@RequestMapping(value = "catalogue")
public class TDLRestController {
    /**
     * Database connector.
     */
    private MongoDBConnector dbConnector = new MongoDBConnector();

    /**
     * Validator
     */
    private static final TopicDescriptionValidator VALIDATOR = new TopicDescriptionValidator();

    /**
     * return value json properties
     */
    private final String success = "success";
    private final String msg = "msg";

    /**
     * Inserts a new topic to the database.
     *
     * @return the id of the topic, which is provided by the database
     */
    @RequestMapping(method = POST, value = "/add")
    @ResponseBody
    public ResponseEntity addNewTopic(@RequestBody String topicDescription) {
        StringBuilder badRequestMsg = new StringBuilder();
        try {
            ProcessingReport proRep = VALIDATOR.validateTopicOnJsonScheme(topicDescription);
            if (proRep.isSuccess()) {
                JSONObject topic = new JSONObject(topicDescription);
                JSONObject validationJSON = new JSONObject();
                if (topic.has("policy")) {
                    validationJSON = VALIDATOR.validateAllPolicies(topic.getJSONObject("policy"));
                } else {
                    validationJSON.put(success, true);
                }
                if (validationJSON.getBoolean(success)) {
                    return ResponseEntity.ok().body(dbConnector.storeTopicDescription(topicDescription));
                } else {
                    for (int i = 0; i < validationJSON.getJSONArray(msg).length(); i++) {
                        badRequestMsg.append(validationJSON.getJSONArray(msg).getString(i)).append("\n");
                    }
                }
            } else {
                List messages = Lists.newArrayList(proRep);
                for (Object message : messages) {
                    badRequestMsg.append(message.toString());
                }
            }
        } catch (IOException | ProcessingException e) {
            e.printStackTrace();
            badRequestMsg.append(e.getMessage());
        }
        return ResponseEntity
                .badRequest()
                .body(badRequestMsg.toString());

    }

    /**
     * Deletes a topic based on the given topic id.
     *
     * @param id topic description id
     * @return Status code 200, if successful, 500 else
     */
    @RequestMapping(method = DELETE, value = "/delete/{id}")
    @ResponseBody
    public ResponseEntity<HttpStatus> deleteTopic(@PathVariable String id) {
        if (dbConnector.deleteTopicDescription(id)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Updates the attributes of a given topic.
     *
     * @param id            topic description id
     * @param updateTopic new topic which replaces the old one
     * @return status code 200, if successful, 400 and 500 else
     */
    @RequestMapping(method = PUT, value = "/update/{id}")
    @ResponseBody
    public ResponseEntity updateTopic(@PathVariable String id, @RequestBody String updateTopic) {
        StringBuilder badRequestMsg = new StringBuilder();
        try {
            ProcessingReport proRep = VALIDATOR.validateTopicOnJsonScheme(updateTopic);
            if (proRep.isSuccess()) {
                JSONObject topic = new JSONObject(updateTopic);
                JSONObject validationJSON = new JSONObject();
                if (topic.has("policy")) {
                    validationJSON = VALIDATOR.validateAllPolicies(topic.getJSONObject("policy"));
                } else {
                    validationJSON.put(success, true);
                }
                if (validationJSON.getBoolean(success)) {
                    if (dbConnector.updateTopicDescription(id, updateTopic)) {
                        return new ResponseEntity<>(HttpStatus.ACCEPTED);
                    } else {
                        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                } else {
                    for (int i = 0; i < validationJSON.getJSONArray(msg).length(); i++) {
                        badRequestMsg.append(validationJSON.getJSONArray(msg).getString(i)).append("\n");
                    }
                }
            } else {
                List messages = Lists.newArrayList(proRep);
                for (Object message : messages) {
                    badRequestMsg.append(message.toString());
                }
            }
        } catch (IOException | ProcessingException e) {
            e.printStackTrace();
            badRequestMsg.append(e.getMessage());
        }
        return ResponseEntity
                .badRequest()
                .body(badRequestMsg.toString());
    }

    /**
     * Searches for topics based on attributes.
     *
     * @param filters tag map of attributes, which have to match with all topics of
     *                the result list
     * @return list of matching topic descriptions
     * <p>
     * We directly throw the JSON exception to the user :)
     */
    @RequestMapping(method = POST, value = "/search", produces = "application/json; charset=utf-8")
    @ResponseBody
    public ResponseEntity searchTopics(@RequestBody String filters) {
        JSONObject filterJson;
        JSONObject modifiedFilterJson = new JSONObject();
        try {
            filterJson = new JSONObject(filters);
            if (filterJson.keySet().size() > 0) {
                modifiedFilterJson.put("$and", new org.json.JSONArray());
                for (String key : filterJson.keySet()) {
                    JSONObject originJsonObject = new JSONObject();
                    JSONObject topicJsonObject = new JSONObject();
                    JSONObject messageJsonObject = new JSONObject();
                    JSONObject locationJsonObject = new JSONObject();
                    JSONObject messageStructureJsonObject = new JSONObject();

                    originJsonObject.put(key, filterJson.get(key));
                    topicJsonObject.put("policy.topic." + key, filterJson.get(key));
                    messageJsonObject.put("policy.message." + key, filterJson.get(key));
                    locationJsonObject.put("location." + key, filterJson.get(key));
                    messageStructureJsonObject.put("message_structure." + key, filterJson.get(key));

                    JSONObject orQueryJson = new JSONObject();
                    orQueryJson.put("$or", new org.json.JSONArray());
                    orQueryJson.getJSONArray("$or").put(originJsonObject);
                    orQueryJson.getJSONArray("$or").put(topicJsonObject);
                    orQueryJson.getJSONArray("$or").put(messageJsonObject);
                    orQueryJson.getJSONArray("$or").put(locationJsonObject);
                    orQueryJson.getJSONArray("$or").put(messageStructureJsonObject);

                    modifiedFilterJson.getJSONArray("$and").put(orQueryJson);
                }
            }
            List<String> descriptionList = dbConnector.getMatchedTopicDescriptions(modifiedFilterJson);
            JSONArray topicDescriptionJsonArray = new JSONArray();
            topicDescriptionJsonArray.addAll(descriptionList);

            return new ResponseEntity<>(topicDescriptionJsonArray, HttpStatus.OK);
        } catch (JSONException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * Returns a topic with the provided id.
     *
     * @param id topic description id
     * @return topic description
     */
    @RequestMapping(method = GET, value = "/get/{id}", produces = "application/json; charset=utf-8")
    @ResponseBody
    public ResponseEntity<String> getTopic(@PathVariable String id) {
        String topicDescription = dbConnector.getMatchedTopicDescription(id);
        if (null != topicDescription) {
            return new ResponseEntity<>(topicDescription, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("{}", HttpStatus.OK);
        }
    }

    /**
     * Header information for swagger requests.
     *
     * @param response Response information
     */
    @ModelAttribute
    public void setVaryResponseHeader(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    }

}
