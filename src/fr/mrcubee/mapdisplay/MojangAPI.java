package fr.mrcubee.mapdisplay;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

public class MojangAPI {

    public static JSONObject getProfileJsonFromUUID(UUID uuid) {
        String trimedUUID;
        InputStreamReader inputStreamReader;
        JSONParser jsonParser;
        Object object;

        if (uuid == null)
            return null;
        trimedUUID = uuid.toString().replaceAll("-", "");
        try {
            inputStreamReader = new InputStreamReader(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + trimedUUID).openStream());
        } catch (IOException ignored) {
            ignored.printStackTrace();
            return null;
        }
        jsonParser = new JSONParser();
        try {
            object = jsonParser.parse(inputStreamReader);
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return null;
        }
        try {
            inputStreamReader.close();
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        if (!(object instanceof JSONObject))
            return null;
        return ((JSONObject) object);
    }

    public static JSONObject getTexturesBase64Json(UUID uuid) {
        JSONObject json = getProfileJsonFromUUID(uuid);
        Object propertiesArrayObj;
        JSONArray propertiesArray;
        String[] value = {null};
        JSONParser jsonParser;
        Object result;

        if (json == null || !json.containsKey("properties"))
            return null;
        propertiesArrayObj = json.get("properties");
        if (!(propertiesArrayObj instanceof JSONArray))
            return null;
        propertiesArray = (JSONArray) propertiesArrayObj;
        propertiesArray.stream().filter(o -> (o instanceof JSONObject)).forEach(object -> {
            JSONObject jsonObject = (JSONObject) object;
            Object obj;

            if (!jsonObject.containsKey("value") || !jsonObject.containsKey("name")
                    || !((obj = jsonObject.get("name")) instanceof String) || !((String) obj).equals("textures"))
                return;
            else if (!((obj = jsonObject.get("value")) instanceof String))
                return;
            value[0] = new String(Base64.getDecoder().decode((String) obj));
        });
        if (value[0] == null)
            return null;
        jsonParser = new JSONParser();
        try {
            result = jsonParser.parse(value[0]);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        if (!(result instanceof JSONObject))
            return null;
        return ((JSONObject) result);
    }

    public static BufferedImage getPlayerSkinImage(UUID uuid) {
        JSONObject json = getTexturesBase64Json(uuid);
        JSONObject textures;
        JSONObject skin;
        Object obj;
        BufferedImage result;

        if (json == null || !json.containsKey("textures") || !((obj = json.get("textures")) instanceof JSONObject))
            return null;
        textures = (JSONObject) obj;
        if (!textures.containsKey("SKIN") || !((obj = textures.get("SKIN")) instanceof JSONObject))
            return null;
        skin = (JSONObject) obj;
        if (!skin.containsKey("url") || !((obj = skin.get("url")) instanceof String))
            return null;
        try {
            result = ImageIO.read(new URL((String) obj));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }
}