package com.example.storit;

import java.util.ArrayList;
import java.util.Date;

public class Plan {
    int planId;
    int planStorage;
    ArrayList<String> planRegions;
    int planCopies;
    Date renewalDate;

    public Plan(int planId) {
        this.planId = planId;
        if(planId == 1) {
            planStorage = 1000;
            planCopies = 2;
            planRegions.add("EU");
            planRegions.add("NA");
        } else if(planId == 2) {
            planStorage = 5000;
            planCopies = 2;
            planRegions.add("EU");
            planRegions.add("NA");
        } else if(planId == 3) {
            planStorage = 5000;
            planCopies = 3;
            planRegions.add("EU");
            planRegions.add("EU");
            planRegions.add("NA");
        }
        this.renewalDate = new Date();
    }

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public int getPlanStorage() {
        return planStorage;
    }

    public void setPlanStorage(int planStorage) {
        this.planStorage = planStorage;
    }

    public ArrayList<String> getPlanRegions() {
        return planRegions;
    }

    public void setPlanRegions(ArrayList<String> planRegions) {
        this.planRegions = planRegions;
    }

    public int getPlanCopies() {
        return planCopies;
    }

    public void setPlanCopies(int planCopies) {
        this.planCopies = planCopies;
    }

    public Date getRenewalDate() {
        return renewalDate;
    }

    public void setRenewalDate(Date renewalDate) {
        this.renewalDate = renewalDate;
    }
}

