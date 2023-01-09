package com.example.foresight.api_class;

import org.json.JSONException;
import org.json.JSONObject;

public class Exercice {

    public String name;
    public String description;
    public int set;
    public int rep;
    public int weight;
    public String fk_session;
    public String fk_exercice;

    public Exercice(JSONObject data) throws JSONException {
        this.name = data.getString("name");
        this.description = data.getString("description");
        this.set = data.getInt("set");
        this.rep = data.getInt("rep");
        this.weight = data.getInt("weight");
        this.fk_session = data.getString("fk_session");
        this.fk_exercice = data.getString("fk_exercice");
    }



}
