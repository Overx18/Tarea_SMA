import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class AgentePago extends Agent {

    @Override
    protected void setup() {
        System.out.println("AgentePago " + getLocalName() + " iniciado.");

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("pago");
        sd.setName("ServicioPago");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null && msg.getPerformative() == ACLMessage.REQUEST) {
                    System.out.println("AgentePago " + getLocalName() + " validando el yapeo");

                    ACLMessage resp = new ACLMessage(ACLMessage.CONFIRM);
                    resp.addReceiver(msg.getSender());
                    resp.setContent("Pago validado por " + getLocalName());
                    send(resp);
                } else {
                    block();
                }
            }
        });
    }
}



