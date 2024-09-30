package model;

import java.util.ArrayList;
public class Group {
    private final String name;
    private final ArrayList<String> members;

    public Group(String name, ArrayList<String> members) {
        this.name = name;
        this.members = new ArrayList<>();
    }


    public String getName() {
        return name;
    }

    public ArrayList<String> getMembers() {
        return members;
    }


}