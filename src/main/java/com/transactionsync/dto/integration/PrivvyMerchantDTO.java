package com.transactionsync.dto.integration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivvyMerchantDTO {
    @JsonProperty("MID")
    private String MID;
    
    @JsonProperty("MerchName")
    private String MerchName;
    
    @JsonProperty("legalName")
    private String legalName;
    
    @JsonProperty("customerid")
    private String customerid;
    
    @JsonProperty("agentId")
    private String agentId;
    
    @JsonProperty("agent")
    private String agent;
    
    @JsonProperty("agent_email")
    private String agent_email;
    
    @JsonProperty("customer_status")
    private String customer_status;
    
    @JsonProperty("submitted_at")
    private String submitted_at;
    
    @JsonProperty("approved_at")
    private String approved_at;
    
    @JsonProperty("date_boarded")
    private String date_boarded;
    
    @JsonProperty("offercode")
    private String offercode;
    
    @JsonProperty("offerid")
    private String offerid;
    
    @JsonProperty("fee_schedule")
    private String fee_schedule;
    
    @JsonProperty("CorpPhone")
    private String CorpPhone;
    
    @JsonProperty("CorpEmail")
    private String CorpEmail;
}

