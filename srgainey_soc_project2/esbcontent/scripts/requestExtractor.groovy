import org.jboss.soa.esb.message.*
import groovy.util.*

//Put the message in a local variable 
Message myMessage = message;

println "*********** Begin Service ***********"

def incomingXML = myMessage.getBody().get()

println "Extracting Variables"
def conciergeRequest = new XmlParser().parseText(incomingXML)
def buyTickets = conciergeRequest.buyTickets.text()
def returnTickets = conciergeRequest.returnTickets.text()
def movieName = conciergeRequest.movieName.text()

def reserveDinner = conciergeRequest.reserveDinner.text()
def cancelDinner = conciergeRequest.cancelDinner.text()
def customerName = conciergeRequest.customerName.text()

println "Returning Variables to Requester"
myMessage.getBody().add("buyTickets", buyTickets)
myMessage.getBody().add("returnTickets", returnTickets)
myMessage.getBody().add("movieName", movieName)
myMessage.getBody().add("reserveDinner", reserveDinner)
myMessage.getBody().add("cancelDinner", cancelDinner)
myMessage.getBody().add("customerName", customerName)

println "************ End Service ************"