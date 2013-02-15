package edu.ncsu.soc.esb;

import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;

import org.apache.log4j.Logger;

public class ResultPrinter extends AbstractActionLifecycle {
	
	protected ConfigTree config;
	private Logger logger = Logger.getLogger(ResultPrinter.class);
	
    public ResultPrinter(ConfigTree config) { this.config = config; } 
	
	public Message process(Message message) throws Exception {
		
        printPayload("buyTickets", message.getBody().get("buyTickets"));
        printPayload("returnTickets", message.getBody().get("returnTickets"));
        printPayload("movieName", message.getBody().get("movieName"));
        printPayload("reserveDinner", message.getBody().get("reserveDinner"));
        printPayload("cancelDinner", message.getBody().get("cancelDinner"));
        printPayload("customerName", message.getBody().get("customerName"));
        
        
		return message;         	
	}
	
	private void printPayload(String displayName, Object payload) {
		
        if (payload instanceof String) {
			logger.info(displayName + ": " + payload);
		} else {
			logger.info(displayName + " is NULL");
		}		
	}
}	
