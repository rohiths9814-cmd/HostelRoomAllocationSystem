package com.hostel.model;

/** One hostel block - Block A, Block B, Block C. */
public class HostelBlock implements Displayable {

    private int blockId;
    private String blockName;
    private String gender;      // MALE / FEMALE - which students may live here
    private int floors;

    public HostelBlock() {
        this.blockId = 0;
        this.blockName = "";
        this.gender = "MALE";
        this.floors = 1;
    }

    public HostelBlock(String blockName, String gender, int floors) {
        this.blockName = blockName;
        this.gender = gender;
        this.floors = floors;
    }

    public HostelBlock(int blockId, String blockName, String gender, int floors) {
        this(blockName, gender, floors);
        this.blockId = blockId;
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getFloors() {
        return floors;
    }

    public void setFloors(int floors) {
        this.floors = floors;
    }

    @Override
    public String header() {
        return String.format("%-5s %-20s %-10s %-8s", "ID", "BLOCK NAME", "GENDER", "FLOORS");
    }

    @Override
    public void display() {
        System.out.println(String.format("%-5d %-20s %-10s %-8d",
                blockId, blockName, gender, floors));
    }

    @Override
    public String toString() {
        return "HostelBlock{id=" + blockId + ", name=" + blockName
                + ", gender=" + gender + ", floors=" + floors + "}";
    }
}
