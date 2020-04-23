package com.example.storit;

import java.util.ArrayList;
import java.util.Date;

public class Plan {
    int planId;
    int planStorage;
    ArrayList<String> planRegions;
    int planCopies;
    Date renewalDate;
    int planCost;

    public Plan(int planId) {
        this.planId = planId;
        planRegions = new ArrayList<String>();
        if(planId == 1) {
            planStorage = 1000;
            planCopies = 2;
            planCost = 5;
            planRegions.add("EU");
            planRegions.add("NA");
        } else if(planId == 2) {
            planStorage = 5000;
            planCopies = 2;
            planCost = 10;
            planRegions.add("EU");
            planRegions.add("NA");
        } else if(planId == 3) {
            planStorage = 5000;
            planCopies = 3;
            planCost = 15;
            planRegions.add("EU");
            planRegions.add("EU");
            planRegions.add("NA");
        }
        this.renewalDate = new Date();
    }

    public Plan(int planId, ArrayList<String> planRegions, Date planRenewalDate) {
        this.planId = planId;
        if(planId == 1) {
            planStorage = 1000;
            planCopies = 2;
            planCost = 5;
        } else if(planId == 2) {
            planStorage = 5000;
            planCopies = 2;
            planCost = 10;
        } else if(planId == 3) {
            planStorage = 5000;
            planCopies = 3;
            planCost = 15;
        }
        this.planRegions = planRegions;
        this.renewalDate = planRenewalDate;
    }

    public int getPlanCost() {
        return planCost;
    }

    public void setPlanCost(int planCost) {
        this.planCost = planCost;
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

