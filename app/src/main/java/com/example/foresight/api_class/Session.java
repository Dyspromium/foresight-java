package com.example.foresight.api_class;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Session implements Parcelable {

    public String name;
    public int difficulty;
    public ArrayList<Exercice> exercices;
    public int fk_session;

    public Session(JSONObject data) throws JSONException {
        this.name = data.getString("name");
        this.difficulty = data.getInt("difficulty");
        this.fk_session = data.getInt("id");
        this.exercices = new ArrayList<>();
    }

    public void addExercice(Exercice exercice){
        exercices.add(exercice);
    }

    protected Session(Parcel in) {
        name = in.readString();
        difficulty = Integer.parseInt(in.readString());
        if (in.readByte() == 0x01) {
            exercices = new ArrayList<Exercice>();
            in.readList(exercices, Exercice.class.getClassLoader());
        } else {
            exercices = null;
        }
        fk_session = Integer.parseInt(in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(String.valueOf(difficulty));
        if (exercices == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(exercices);
        }
        dest.writeString(String.valueOf(fk_session));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Session> CREATOR = new Parcelable.Creator<Session>() {
        @Override
        public Session createFromParcel(Parcel in) {
            return new Session(in);
        }

        @Override
        public Session[] newArray(int size) {
            return new Session[size];
        }
    };
}
